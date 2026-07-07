/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.auditlogweb.AppCacheManager;
import org.openmrs.module.auditlogweb.ReadAuditLog;
import org.openmrs.module.auditlogweb.ReadAuditWorker;
import org.openmrs.module.auditlogweb.api.AuditLogContext;
import org.openmrs.module.auditlogweb.api.ReadAuditWriteService;

import java.lang.reflect.Method;

import org.mockito.MockedStatic;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

class ReadAuditHelperTest {
	
	@Mock
	private AppCacheManager appCacheManager;
	
	@Mock
	private ReadAuditWorker readAuditWorker;
	
	@Mock
	private ReadAuditWriteService readAuditService;
	
	@InjectMocks
	private ReadAuditHelper readAuditHelper;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		org.springframework.test.util.ReflectionTestUtils.setField(readAuditHelper, "appCacheManager", appCacheManager);
		org.springframework.test.util.ReflectionTestUtils.setField(readAuditHelper, "readAuditWorker", readAuditWorker);
		org.springframework.test.util.ReflectionTestUtils.setField(readAuditHelper, "readAuditService", readAuditService);
		when(appCacheManager.get(any())).thenReturn(null);
		when(readAuditWorker.submitTask(any(ReadAuditLog.class))).thenReturn(true);
	}
	
	@Test
	void shouldProceedAndLogOnAuditReadRequestSuccess() throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		MethodSignature signature = mock(MethodSignature.class);
		Method method = TestService.class.getMethod("getSomeData");
		
		when(joinPoint.getTarget()).thenReturn(new Object());
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getMethod()).thenReturn(method);
		when(joinPoint.proceed()).thenReturn("some result");
		
		Object result = readAuditHelper.auditReadRequest(joinPoint);
		
		assertEquals("some result", result);
		verify(joinPoint).proceed();
	}
	
	@Test
	void shouldProceedAndThrowOnAuditReadRequestFailure() throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		MethodSignature signature = mock(MethodSignature.class);
		Method method = TestService.class.getMethod("getSomeData");
		RuntimeException expectedException = new RuntimeException("test exception");
		
		when(joinPoint.getTarget()).thenReturn(new Object());
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getMethod()).thenReturn(method);
		when(joinPoint.proceed()).thenThrow(expectedException);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> readAuditHelper.auditReadRequest(joinPoint));
		
		assertEquals(expectedException, thrown);
		verify(joinPoint).proceed();
	}
	
	@Test
	void shouldSaveReadAuditRequestWithFallbackUserWhenContextIsNull() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<Daemon> daemonMock = mockStatic(Daemon.class)) {
			User mockUser = mock(User.class);
			when(mockUser.getUsername()).thenReturn("fallback-user");
			when(mockUser.getUuid()).thenReturn("fallback-uuid");
			
			contextMock.when(Context::isAuthenticated).thenReturn(true);
			contextMock.when(Context::getAuthenticatedUser).thenReturn(mockUser);
			daemonMock.when(() -> Daemon.isDaemonUser(mockUser)).thenReturn(false);
			
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("entity-uuid");
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker).submitTask(any(ReadAuditLog.class));
		}
	}
	
	@Test
	void shouldSkipSaveReadAuditRequestWhenUserIsDaemon() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<Daemon> daemonMock = mockStatic(Daemon.class)) {
			User mockUser = mock(User.class);
			
			contextMock.when(Context::isAuthenticated).thenReturn(true);
			contextMock.when(Context::getAuthenticatedUser).thenReturn(mockUser);
			daemonMock.when(() -> Daemon.isDaemonUser(mockUser)).thenReturn(true);
			
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("entity-uuid");
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker, never()).submitTask(any(ReadAuditLog.class));
		}
	}
	
	@Test
	void shouldSkipSaveReadAuditRequestWhenUserUUIDIsNull() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::isAuthenticated).thenReturn(false);
			
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("entity-uuid");
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker, never()).submitTask(any(ReadAuditLog.class));
		}
	}
	
	@Test
	void shouldSaveReadAuditLogWhenItsNewInCache() {
		buildAuditContext();
		
		try {
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("test-uuid");
			
			String key = "test-user:127.0.0.1:test-uuid";
			when(appCacheManager.get(key)).thenReturn(null);
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker).submitTask(any(ReadAuditLog.class));
			verify(appCacheManager).set(key, true);
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	@Test
	void shouldNotSaveReadAuditLogWhenItsAlreadyInCache() {
		buildAuditContext();
		
		try {
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("test-uuid");
			
			String key = "test-user:127.0.0.1:test-uuid";
			when(appCacheManager.get(key)).thenReturn(true);
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(appCacheManager, never()).set(any(), any());
			verify(readAuditWorker, never()).submitTask(any(ReadAuditLog.class));
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	@Test
	void shouldSaveReadAuditRequestWithFallbackUserWhenContextHasNoUser() {
		AuditLogContext auditContext = new AuditLogContext();
		auditContext.setIpAddress("127.0.0.1");
		auditContext.setUserAgent("user-agent");
		auditContext.setSessionId("session-id");
		
		AuditLogContext.set(auditContext);
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<Daemon> daemonMock = mockStatic(Daemon.class)) {
			User mockUser = mock(User.class);
			when(mockUser.getUsername()).thenReturn("fallback-user");
			when(mockUser.getUuid()).thenReturn("fallback-uuid");
			
			contextMock.when(Context::isAuthenticated).thenReturn(true);
			contextMock.when(Context::getAuthenticatedUser).thenReturn(mockUser);
			daemonMock.when(() -> Daemon.isDaemonUser(mockUser)).thenReturn(false);
			
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("entity-uuid");
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker).submitTask(any(ReadAuditLog.class));
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	@Test
	void shouldNotSaveReadAuditLogToCacheWhenSubmitTaskFails() {
		buildAuditContext();
		
		try {
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("test-uuid");
			
			String key = "test-user:127.0.0.1:test-uuid";
			when(appCacheManager.get(key)).thenReturn(null);
			when(readAuditWorker.submitTask(any(ReadAuditLog.class))).thenReturn(false);
			doThrow(new RuntimeException("Sync save failed")).when(readAuditService).logReadAudit(any(ReadAuditLog.class));
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker).submitTask(any(ReadAuditLog.class));
			verify(appCacheManager, never()).set(any(), any());
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	@Test
	void shouldSaveReadAuditLogSynchronouslyIfQueueIsFull() {
		buildAuditContext();
		
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("test-uuid");
			
			String key = "test-user:127.0.0.1:test-uuid";
			when(appCacheManager.get(key)).thenReturn(null);
			when(readAuditWorker.submitTask(any(ReadAuditLog.class))).thenReturn(false);
			
			readAuditHelper.saveReadAuditRequest("Patient", true, mockObject);
			
			verify(readAuditWorker).submitTask(any(ReadAuditLog.class));
			verify(readAuditService).logReadAudit(any(ReadAuditLog.class));
			verify(appCacheManager).set(key, true);
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	@Test
	void shouldSaveUnAuthenticatedUserAsAnonymousUser() {
		AuditLogContext auditContext = new AuditLogContext();
		auditContext.setLoggedInUsername(null);
		auditContext.setLoggedInUserUUID(null);
		auditContext.setIpAddress("127.0.0.1");
		auditContext.setUserAgent("user-agent");
		auditContext.setSessionId("session-id");
		
		AuditLogContext.set(auditContext);
		
		try {
			OpenmrsObject mockObject = mock(OpenmrsObject.class);
			when(mockObject.getId()).thenReturn(1);
			when(mockObject.getUuid()).thenReturn("test-uuid");
			
			String key = "anonymous:127.0.0.1:test-uuid";
			when(appCacheManager.get(key)).thenReturn(null);
			
			readAuditHelper.saveReadAuditRequest("Patient", false, mockObject);
			
			ArgumentCaptor<ReadAuditLog> logCaptor = ArgumentCaptor.forClass(ReadAuditLog.class);
			verify(readAuditWorker).submitTask(logCaptor.capture());
			
			ReadAuditLog capturedLog = logCaptor.getValue();
			assertEquals("anonymous", capturedLog.getUsername());
			assertEquals("anonymous", capturedLog.getUserUUID());
			
			verify(appCacheManager).set(key, true);
		}
		finally {
			AuditLogContext.clear();
		}
	}
	
	void buildAuditContext() {
		AuditLogContext auditContext = new AuditLogContext();
		auditContext.setLoggedInUsername("test-user");
		auditContext.setLoggedInUserUUID("test-user-uuid");
		auditContext.setIpAddress("127.0.0.1");
		auditContext.setUserAgent("user-agent");
		auditContext.setSessionId("session-id");
		
		AuditLogContext.set(auditContext);
	}
	
	interface TestService {
		
		String getSomeData();
		
	}
}
