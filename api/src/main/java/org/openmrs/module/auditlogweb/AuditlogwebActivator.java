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

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.auditlogweb.api.AuditBackfillService;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class AuditlogwebActivator extends BaseModuleActivator {

	private Log log = LogFactory.getLog(this.getClass());

	@Getter
	@Setter
	private static volatile boolean appShuttingDown = false;

	/**
	 * It's a JVM shutdown hook that fires when the server process exits. Registered on module start so that
	 * {@code appShuttingDown} returns {@code true} so that we can know when the server is getting shutdown.
	 */
	private Thread shutdownHook;

	@Override
	public void started() {
		log.info("Started Auditlogweb");

		// Register a JVM shutdown hook so we know when the whole server is stopping.
		shutdownHook = new Thread(() -> {
			AuditlogwebActivator.setAppShuttingDown(true);
		}, "auditlogweb-shutdown-hook");
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		try {
			Context.getRegisteredComponent("auditlogweb.auditBackfillService", AuditBackfillService.class)
			        .backfillExistingDataIfEnabled();
		}
		catch (Exception e) {
			log.error("One-time audit backfill of existing data failed", e);
		}
	}

	@Override
	public void stopped() {
		log.info("Stopped Auditlogweb");
		// Remove the shutdown hook when the module is stopped manually (not due to JVM exit).
		if (shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
			catch (IllegalStateException ignored) {
				// Cause because JVM is already shutting down, ignore because hook either fired or is in flight
			}
			shutdownHook = null;
		}
	}
}
