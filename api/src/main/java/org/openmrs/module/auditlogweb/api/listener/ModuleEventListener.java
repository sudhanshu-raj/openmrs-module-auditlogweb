/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.listener;

import lombok.RequiredArgsConstructor;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleActionEvent;
import org.openmrs.module.ModuleEventType;
import org.openmrs.module.auditlogweb.AuditlogwebActivator;
import org.openmrs.module.auditlogweb.api.AuditLogRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModuleEventListener {
	
	private final Logger log = LoggerFactory.getLogger(ModuleEventListener.class);
	
	private final AuditLogRecorder auditLogRecorder;
	
	/**
	 * We're listening to the module event like MODULE_START, MODULE_STOP, MODULE_LOAD, MODULE_UNLOAD.
	 * which got published from the core.We do not need the module action during the startup and
	 * shutdown of application that's why checking if not isDaemonThread.Then further opening the new
	 * session for the case if dur
	 */
	@EventListener
	public void listenModuleAction(ModuleActionEvent moduleActionEvent) {
		try {
			if (Daemon.isDaemonThread()) {
				return;
			}
			
			if (AuditlogwebActivator.isAppShuttingDown()) {
				return;
			}
			
			ModuleEventType moduleEventType = moduleActionEvent.getEventType();
			String moduleId = moduleActionEvent.getModuleId();
			String moduleName = moduleActionEvent.getModuleName();
			String moduleVersion = moduleActionEvent.getModuleVersion();
			boolean isSuccess = moduleActionEvent.isSuccess();
			
			auditLogRecorder.logModuleEvent(moduleEventType, moduleId, moduleName, moduleVersion, isSuccess);
		}
		catch (Exception e) {
			log.error("Error occur while listening to module events", e);
		}
	}
}
