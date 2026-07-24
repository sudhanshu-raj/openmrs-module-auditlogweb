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
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleActionEvent;
import org.openmrs.module.auditlogweb.api.ModuleEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModuleEventListener {
	
	private final Logger log = LoggerFactory.getLogger(ModuleEventListener.class);
	
	private final ModuleEventService moduleEventService;
	
	@EventListener
	public void listenModuleAction(ModuleActionEvent moduleActionEvent) {
		try {
			if (Daemon.isDaemonThread()) {
				return;
			}
			
			String eventType = String.valueOf(moduleActionEvent.getActionType());
			String moduleName = moduleActionEvent.getModuleName();
			boolean isSuccess = moduleActionEvent.isSuccess();
			
			Context.openSessionWithCurrentUser();
			try {
				moduleEventService.saveModuleEvent(eventType, moduleName, isSuccess);
			}
			finally {
				Context.closeSessionWithCurrentUser();
			}
			
			log.info("===Received module action event===");
			log.info("Module action : {}", moduleActionEvent.getActionType());
			log.info("Module name : {}", moduleActionEvent.getModuleName());
			log.info("=======");
		}
		catch (Exception e) {
			log.error("Error occur while listening to module events", e);
		}
	}
}
