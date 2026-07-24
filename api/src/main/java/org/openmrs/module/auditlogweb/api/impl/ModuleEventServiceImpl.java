/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.impl;

import lombok.RequiredArgsConstructor;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.auditlogweb.ModuleEvent;
import org.openmrs.module.auditlogweb.api.AuditLogContext;
import org.openmrs.module.auditlogweb.api.ModuleEventService;
import org.openmrs.module.auditlogweb.api.dao.ModuleEventDao;
import org.openmrs.module.auditlogweb.api.utils.ModuleEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ModuleEventServiceImpl extends BaseOpenmrsService implements ModuleEventService {
	
	private final Logger log = LoggerFactory.getLogger(ModuleEventServiceImpl.class);
	
	private final ModuleEventDao moduleEventDao;
	
	@Override
	public void saveModuleEvent(String eventType, String moduleName, boolean isSuccess) {
		String username = null;
		String userUUID = null;
		String ipAddress = null;
		String userAgent = null;
		String sessionId = null;
		
		try {
			AuditLogContext ctx = AuditLogContext.get();
			if (ctx != null) {
				username = ctx.getLoggedInUsername();
				userUUID = ctx.getLoggedInUserUUID();
				ipAddress = ctx.getIpAddress();
				userAgent = ctx.getUserAgent();
				if (userAgent != null && userAgent.length() > 500) {
					userAgent = userAgent.substring(0, 500);
				}
				sessionId = ctx.getSessionId();
			}
			
			if (userUUID == null) {
				if (Context.isAuthenticated()) {
					User user = Context.getAuthenticatedUser();
					if (user != null && Daemon.isDaemonUser(user)) {
						return;
					}
					if (user != null) {
						username = user.getUsername();
						if (username == null) {
							username = user.getSystemId();
						}
						userUUID = user.getUuid();
					}
				}
			}
			
			if (userUUID == null) {
				userUUID = "anonymous";
				username = "anonymous";
			}
			
			ModuleEventType moduleEventType = ModuleEventType.fromName(eventType);
			if (moduleEventType == null || moduleEventType == ModuleEventType.UNKNOWN) {
				log.warn("Unknown module event type: {}", eventType);
			}
			if (moduleName == null || moduleName.isEmpty()) {
				log.warn("Module name can't be null or empty");
			}
			ModuleEvent moduleEvent = ModuleEvent.builder().eventType(moduleEventType).moduleName(moduleName)
			        .eventSuccess(isSuccess).username(username).userUUID(userUUID).eventTime(new Date()).ipAddress(ipAddress)
			        .userAgent(userAgent).sessionId(sessionId).build();
			saveModuleEvent(moduleEvent);
		}
		catch (Exception e) {
			log.error("Error while saving module event", e);
		}
	}
	
	@Override
	public void saveModuleEvent(ModuleEvent moduleEvent) {
		moduleEventDao.saveModuleEvent(moduleEvent);
	}
	
	@Override
	public List<ModuleEvent> getModuleEvents(String eventType, String moduleName, String username, String userUUID,
	        Date startDate, Date endDate, int page, int size) {
		return moduleEventDao.getModuleEvents(eventType, moduleName, username, userUUID, startDate, endDate, page, size);
	}
	
	@Override
	public long countModuleEvents(String eventType, String moduleName, String username, String userUUID, Date startDate,
	        Date endDate) {
		return moduleEventDao.countModuleEvents(eventType, moduleName, username, userUUID, startDate, endDate);
	}
	
	@Override
	public ModuleEvent getModuleEventById(Integer moduleEventId) {
		return moduleEventDao.getModuleEventById(moduleEventId);
	}
	
	@Override
	public List<ModuleEvent> getRelatedModuleEvents(String sessionId, int page, int size) {
		return moduleEventDao.getRelatedModuleEvents(sessionId, page, size);
	}
	
	@Override
	public Integer countRelatedModuleEvents(String sessionId) {
		return moduleEventDao.countRelatedModuleEvents(sessionId);
	}
}
