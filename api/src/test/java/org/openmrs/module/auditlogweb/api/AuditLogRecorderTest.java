/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.auditlogweb.ReadAuditLog;
import org.openmrs.module.auditlogweb.api.dao.ReadAuditDAO;
import org.openmrs.module.auditlogweb.api.impl.AuditLogRecorderImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public class AuditLogRecorderTest {
	
	@Mock
	private ReadAuditDAO readAuditDAO;
	
	@InjectMocks
	private AuditLogRecorderImpl auditLogRecorder;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void shouldDelegateSaveReadAuditLog() {
		ReadAuditLog mockLog = mock(ReadAuditLog.class);
		auditLogRecorder.logReadAudit(mockLog);
		verify(readAuditDAO).saveReadAuditLog(mockLog);
	}
	
	@Test
	void shouldDelegateSaveReadAuditLogs() {
		ReadAuditLog mockLog1 = mock(ReadAuditLog.class);
		ReadAuditLog mockLog2 = mock(ReadAuditLog.class);
		List<ReadAuditLog> logs = Arrays.asList(mockLog1, mockLog2);
		
		auditLogRecorder.logReadAudits(logs);
		
		verify(readAuditDAO).saveReadAuditLog(mockLog1);
		verify(readAuditDAO).saveReadAuditLog(mockLog2);
	}
	
	@Test
	void shouldDoNothingWhenLogReadAuditsIsNull() {
		auditLogRecorder.logReadAudits(null);
	}
	
	@Test
	void shouldNotAbleToAccessThisObjectOutsideThisModule() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(() -> Context.getService(AuditLogRecorder.class))
			        .thenThrow(new APIException("Service not found"));
			
			assertThrows(APIException.class, () -> Context.getService(AuditLogRecorder.class));
		}
	}
}
