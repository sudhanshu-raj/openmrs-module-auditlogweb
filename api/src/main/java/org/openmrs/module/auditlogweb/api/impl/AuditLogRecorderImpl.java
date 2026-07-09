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
import org.openmrs.module.auditlogweb.ReadAuditLog;
import org.openmrs.module.auditlogweb.api.AuditLogRecorder;
import org.openmrs.module.auditlogweb.api.dao.ReadAuditDAO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component("auditlogweb.AuditLogRecorder")
@RequiredArgsConstructor
public class AuditLogRecorderImpl implements AuditLogRecorder {
	
	private final ReadAuditDAO readAuditDAO;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void logReadAudit(ReadAuditLog readAuditLog) {
		readAuditDAO.saveReadAuditLog(readAuditLog);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void logReadAudits(List<ReadAuditLog> readAuditLogs) {
		if (readAuditLogs != null) {
			for (ReadAuditLog log : readAuditLogs) {
				readAuditDAO.saveReadAuditLog(log);
			}
		}
	}
}
