/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb;

import lombok.RequiredArgsConstructor;
import org.openmrs.api.context.Context;
import org.openmrs.module.auditlogweb.api.AuditLogRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This has been used for the read audit log request to put the log in the queue and then a
 * background thread will consume this or may be in a batch upto 49 logs and saves the log either
 * once or one by one.
 */
@Component
@RequiredArgsConstructor
public class ReadAuditWorker {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final AuditLogRecorder auditLogRecorder;
	
	private final AppCacheManager appCacheManager;
	
	private final BlockingQueue<ReadAuditLog> queue = new LinkedBlockingQueue<>(10000);
	
	private Thread workerThread;
	
	private volatile boolean running = true;
	
	private static final long TIMEOUT_SECONDS = 10;
	
	@PostConstruct
	public void init() {
		log.info("Starting ReadAuditWorker background thread");
		workerThread = new Thread(() -> run(), "ReadAuditWorkerThread");
		workerThread.setDaemon(true);
		workerThread.start();
	}
	
	@PreDestroy
	public void destroy() {
		log.info("Stopping ReadAuditWorker background thread");
		running = false;
		if (workerThread != null) {
			workerThread.interrupt();
			try {
				workerThread.join(TIMEOUT_SECONDS * 1000);
			}
			catch (InterruptedException e) {
				log.warn("Worker thread got interrupted while waiting to stop", e);
				Thread.currentThread().interrupt();
			}
		}
		flushRemainingQueueLogsToDB();
	}
	
	public boolean submitTask(ReadAuditLog readAuditLog) {
		if (!running) {
			log.warn("ReadAuditWorker is stopped. Cannot accept new read audit task.");
			return false;
		}
		
		boolean isAdded = queue.offer(readAuditLog);
		if (!isAdded) {
			log.error("Queue is full!, can't submit new read audit task ");
		}
		return isAdded;
	}
	
	private void run() {
		while (running) {
			try {
				ReadAuditLog item = queue.take();
				List<ReadAuditLog> batch = new ArrayList<>();
				batch.add(item);
				
				// It will drain any additional queued logs that can go up to 49 more, making it a max batch of 50
				queue.drainTo(batch, 49);
				saveBatch(batch);
			}
			catch (InterruptedException e) {
				log.debug("ReadAuditWorker thread interrupted, shutting down");
				Thread.currentThread().interrupt();
				break;
			}
			catch (Throwable t) {
				log.error("Error in ReadAuditWorker execution loop", t);
				if (t instanceof Error) {
					log.error("Unexpected JVM Error encountered. Stopping ReadAuditWorker.");
					running = false;
					break;
				}
			}
		}
	}
	
	private void saveBatch(List<ReadAuditLog> batch) {
		if (batch.isEmpty()) {
			return;
		}
		
		boolean isBatchLogsSaved = false;
		try {
			Context.openSession();
			auditLogRecorder.logReadAudits(batch);
			isBatchLogsSaved = true;
		}
		catch (Throwable t) {
			if (t instanceof Error) {
				throw (Error) t;
			}
			log.warn("Failed to save read audit logs in batch, falling back to one-by-one save", t);
		}
		finally {
			Context.closeSession();
		}
		
		if (!isBatchLogsSaved) {
			for (ReadAuditLog logEntry : batch) {
				try {
					Context.openSession();
					auditLogRecorder.logReadAudit(logEntry);
				}
				catch (Throwable t) {
					log.error("Failed to save individual read audit log in fallback", t);
					removeCacheKeys(logEntry);
					if (t instanceof Error) {
						throw (Error) t;
					}
				}
				finally {
					Context.closeSession();
				}
			}
		}
	}
	
	private void removeCacheKeys(ReadAuditLog readAuditLog) {
		try {
			String username = readAuditLog.getUsername();
			String userUUID = readAuditLog.getUserUUID();
			String userKey = username != null ? username : userUUID;
			
			String ipAddress = readAuditLog.getIpAddress();
			String safeIp = ipAddress != null ? ipAddress : "unknown";
			
			for (ReadAuditEntityMetadata entityData : readAuditLog.getTargets()) {
				String entityUUID = entityData.getEntityUuid();
				String key = userKey + ":" + safeIp + ":" + entityUUID;
				appCacheManager.invalidate(key);
			}
		}
		catch (Exception e) {
			log.error("Failed to remove read audit keys", e);
		}
	}
	
	ExecutorService getExecutorService() {
		return Executors.newSingleThreadExecutor();
	}
	
	private void flushRemainingQueueLogsToDB() {
		long startTime = System.currentTimeMillis();
		long totalTimeoutMs = TIMEOUT_SECONDS * 1000;
		
		// We will stop draining new batches 2 seconds before the hard timeout totalTimeoutMs
		long softTimeoutMs = Math.max(0, totalTimeoutMs - 2000);
		
		log.info("Draining remaining read audit logs to DB. Max timeout: {} seconds", TIMEOUT_SECONDS);
		
		ExecutorService executor = getExecutorService();
		int savedCount = 0;
		
		try {
			while (!queue.isEmpty()) {
				long elapsed = System.currentTimeMillis() - startTime;
				
				// Checking the soft timeout, if it exceeds,
				// then we will stop accepting new logs so that DB can commit the processed logs.
				if (elapsed > softTimeoutMs) {
					log.warn("Soft shutdown timeout reached. Stopping batch draining. Discarding remaining {} logs.",
					    queue.size());
					break;
				}
				
				List<ReadAuditLog> batch = new ArrayList<>();
				queue.drainTo(batch, 20);
				
				if (!batch.isEmpty()) {
					long remainingMs = totalTimeoutMs - (System.currentTimeMillis() - startTime);
					if (remainingMs <= 0) {
						break;
					}
					
					// Let the saveBatch run in the separate thread, so we can halt its execution if timeout reached.
					Future<?> future = executor.submit(() -> saveBatch(batch));
					try {
						future.get(remainingMs, TimeUnit.MILLISECONDS);
						savedCount += batch.size();
					}
					catch (TimeoutException e) {
						future.cancel(true);
						log.error("Timeout of {}s reached while saving batch! Aborting DB write.", TIMEOUT_SECONDS);
						break;
					}
					catch (Exception e) {
						log.error("Error saving batch during shutdown", e);
						break;
					}
				}
			}
		}
		finally {
			executor.shutdownNow();
		}
		if (savedCount > 0) {
			log.info("Successfully flushed {} logs to DB during shutdown.", savedCount);
		}
	}
	
}
