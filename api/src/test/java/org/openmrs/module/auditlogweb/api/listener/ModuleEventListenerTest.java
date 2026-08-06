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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleActionEvent;
import org.openmrs.module.ModuleEventType;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.auditlogweb.AuditlogwebActivator;
import org.openmrs.module.auditlogweb.api.AuditLogRecorder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ModuleEventListenerTest {
	
	@Mock
	private AuditLogRecorder auditLogRecorder;
	
	@InjectMocks
	private ModuleEventListener moduleEventListener;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void shouldListenModuleActionEventSuccessfully() {
		ModuleActionEvent mockEvent = buildModuleActionEvent();
		
		moduleEventListener.listenModuleAction(mockEvent);
		
		verify(auditLogRecorder).logModuleEvent(ModuleEventType.MODULE_LOAD, "Event1", "Event 1", "1.0.0-Snapshot", true);
	}
	
	@Test
	void shouldIgnoreModuleActionEventIfDoneByDaemonThread() {
		try (MockedStatic<Daemon> mockStatic = Mockito.mockStatic(Daemon.class)) {
			mockStatic.when(Daemon::isDaemonThread).thenReturn(true);
			
			ModuleActionEvent mockEvent = buildModuleActionEvent();
			moduleEventListener.listenModuleAction(mockEvent);
			
			verify(auditLogRecorder, never()).logModuleEvent(any(), anyString(), anyString(), anyString(), anyBoolean());
		}
	}
	
	@Test
	void shouldIgnoreModuleActionEventIfOccurDuringShutdown() {
		try (MockedStatic<AuditlogwebActivator> mockStatic = Mockito.mockStatic(AuditlogwebActivator.class)) {
			mockStatic.when(AuditlogwebActivator::isAppShuttingDown).thenReturn(true);
			
			ModuleActionEvent mockEvent = buildModuleActionEvent();
			moduleEventListener.listenModuleAction(mockEvent);
			
			verify(auditLogRecorder, never()).logModuleEvent(any(), anyString(), anyString(), anyString(), anyBoolean());
		}
	}
	
	private ModuleActionEvent buildModuleActionEvent() {
		return new ModuleActionEvent(ModuleFactory.class, ModuleEventType.MODULE_LOAD, "Event1", "Event 1", "1.0.0-Snapshot",
		        true);
	}
}
