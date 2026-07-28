/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.auditlogweb.AuditSecurityEvent;
import org.openmrs.module.auditlogweb.api.AuditService;
import org.openmrs.module.auditlogweb.api.utils.AuditSecurityEventType;
import org.openmrs.module.auditlogweb.rest.exceptions.RestExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityAuditRestControllerTest {
	
	private MockMvc mockMvc;
	
	@Mock
	private AuditService auditService;
	
	@InjectMocks
	private SecurityAuditRestController securityAuditRestController;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(securityAuditRestController)
		        .setControllerAdvice(new RestExceptionHandler()).build();
	}
	
	@Test
	public void shouldFetchSecurityAuditsSuccessfullyWithoutFilter() throws Exception {
		when(auditService.getSecurityEvents(null, null, null, null, 0, 15)).thenReturn(Collections.emptyList());
		when(auditService.countSecurityEvents(null, null, null, null)).thenReturn(0L);
		
		mockMvc.perform(get("/rest/v1/securityauditlogs")).andExpect(status().isOk());
		
		verify(auditService).getSecurityEvents(null, null, null, null, 0, 15);
		verify(auditService).countSecurityEvents(null, null, null, null);
	}
	
	@Test
	public void shouldFetchSecurityAuditsSuccessfullyWithIdFilter() throws Exception {
		AuditSecurityEvent mockEvent = mock(AuditSecurityEvent.class);
		when(mockEvent.getEventType()).thenReturn(AuditSecurityEventType.LOGIN_SUCCESS);
		when(auditService.getSecurityEventById(1)).thenReturn(mockEvent);
		
		mockMvc.perform(get("/rest/v1/securityauditlogs").param("logId", "1")).andExpect(status().isOk());
		
		verify(auditService).getSecurityEventById(1);
	}
	
	@Test
	public void shouldThrowErrorIfInvalidLogIdPassed() throws Exception {
		mockMvc.perform(get("/rest/v1/securityauditlogs").param("logId", "-1")).andExpect(status().isBadRequest())
		        .andExpect(jsonPath("$.error", is("Bad Request")))
		        .andExpect(jsonPath("$.message", is("Please provide a valid log ID")));
	}
	
	@Test
	public void shouldFetchSecurityAuditsSuccessfullyWithEventTypeFilter() throws Exception {
		AuditSecurityEvent mockEvent = mock(AuditSecurityEvent.class);
		when(mockEvent.getEventType()).thenReturn(AuditSecurityEventType.LOGIN_SUCCESS);
		List<AuditSecurityEvent> eventList = Collections.singletonList(mockEvent);
		
		when(auditService.getSecurityEvents("LOGIN_SUCCESS", null, null, null, 0, 15)).thenReturn(eventList);
		when(auditService.countSecurityEvents("LOGIN_SUCCESS", null, null, null)).thenReturn(1L);
		
		mockMvc.perform(get("/rest/v1/securityauditlogs").param("eventType", "LOGIN_SUCCESS")).andExpect(status().isOk());
		
		verify(auditService).getSecurityEvents("LOGIN_SUCCESS", null, null, null, 0, 15);
		verify(auditService).countSecurityEvents("LOGIN_SUCCESS", null, null, null);
	}
	
	@Test
	public void shouldFetchSecurityAuditsSuccessfullyWithUserNameFilter() throws Exception {
		AuditSecurityEvent mockEvent = mock(AuditSecurityEvent.class);
		when(mockEvent.getEventType()).thenReturn(AuditSecurityEventType.LOGIN_SUCCESS);
		List<AuditSecurityEvent> eventList = Collections.singletonList(mockEvent);
		
		when(auditService.getSecurityEvents(null, "admin", null, null, 0, 15)).thenReturn(eventList);
		when(auditService.countSecurityEvents(null, "admin", null, null)).thenReturn(1L);
		
		mockMvc.perform(get("/rest/v1/securityauditlogs").param("username", "admin")).andExpect(status().isOk());
		
		verify(auditService).getSecurityEvents(null, "admin", null, null, 0, 15);
		verify(auditService).countSecurityEvents(null, "admin", null, null);
	}
	
	@Test
	public void shouldThrowErrorIfInvalidDatePassed() throws Exception {
		mockMvc.perform(get("/rest/v1/securityauditlogs").param("startDate", "31/02/2025"))
		        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is("Bad Request"))).andExpect(jsonPath(
		            "$.message", is("Invalid month date or date format: '31/02/2025'. Expected format: DD/MM/YYYY")));
	}
	
	@Test
	public void shouldFetchRelatedAuditsSuccessfully() throws Exception {
		AuditSecurityEvent mockEvent = mock(AuditSecurityEvent.class);
		when(mockEvent.getEventType()).thenReturn(AuditSecurityEventType.LOGIN_SUCCESS);
		List<AuditSecurityEvent> relatedList = Collections.singletonList(mockEvent);
		
		when(auditService.getRelatedSecurityEvents("session-123", 10, 0)).thenReturn(relatedList);
		
		mockMvc.perform(get("/rest/v1/securityauditlogs/releatedAudits").param("sessionId", "session-123").param("page", "0")
		        .param("size", "10")).andExpect(status().isOk());
		
		verify(auditService).getRelatedSecurityEvents("session-123", 10, 0);
	}
}
