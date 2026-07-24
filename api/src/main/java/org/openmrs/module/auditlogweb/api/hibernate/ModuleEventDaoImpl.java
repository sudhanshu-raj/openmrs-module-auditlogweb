/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.module.auditlogweb.ModuleEvent;
import org.openmrs.module.auditlogweb.api.dao.ModuleEventDao;
import org.openmrs.module.auditlogweb.api.utils.ModuleEventType;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;

@Repository("auditlogweb.ModuleActionEventDao")
@RequiredArgsConstructor
public class ModuleEventDaoImpl implements ModuleEventDao {
	
	private final SessionFactory sessionFactory;
	
	@Override
	public void saveModuleEvent(ModuleEvent moduleEvent) {
		sessionFactory.getCurrentSession().save(moduleEvent);
	}
	
	@Override
	public List<ModuleEvent> getModuleEvents(String eventType, String moduleName, String username, String userUUID,
	        Date startDate, Date endDate, int page, int size) {
		
		String hql = buildModuleEventQuery(eventType, moduleName, username, userUUID, startDate, endDate);
		ModuleEventType moduleEventType = ModuleEventType.fromName(eventType);
		
		Query<ModuleEvent> query = sessionFactory.getCurrentSession().createQuery(hql, ModuleEvent.class);
		bindModuleEventFilters(query, moduleEventType, moduleName, username, userUUID, startDate, endDate);
		return query.setFirstResult(page * size).setMaxResults(size).list();
		
	}
	
	@Override
	public Integer countModuleEvents(String eventType, String moduleName, String username, String userUUID, Date startDate,
	        Date endDate) {
		
		String hql = "select count(e) "
		        + buildModuleEventQuery(eventType, moduleName, username, userUUID, startDate, endDate);
		
		ModuleEventType moduleEventType = ModuleEventType.fromName(eventType);
		Query<Long> query = sessionFactory.getCurrentSession().createQuery(hql, Long.class);
		bindModuleEventFilters(query, moduleEventType, moduleName, username, userUUID, startDate, endDate);
		
		try {
			Long result = query.getSingleResult();
			return result != null ? result.intValue() : 0;
		}
		catch (NoResultException e) {
			return 0;
		}
	}
	
	@Override
	public ModuleEvent getModuleEventById(Integer moduleEventId) {
		return sessionFactory.getCurrentSession().get(ModuleEvent.class, moduleEventId);
	}
	
	@Override
	public List<ModuleEvent> getRelatedModuleEvents(String sessionId, int page, int size) {
		Query<ModuleEvent> query = sessionFactory.getCurrentSession().createQuery(
		    "from ModuleEvent e where e.sessionId = :sessionId order by e.eventTime desc", ModuleEvent.class);
		query.setParameter("sessionId", sessionId);
		query.setFirstResult(page * size);
		query.setMaxResults(size);
		return query.getResultList();
	}
	
	@Override
	public Integer countRelatedModuleEvents(String sessionId) {
		Query<Long> query = sessionFactory.getCurrentSession()
		        .createQuery("select count(e) from ModuleEvent e where e.sessionId = :sessionId", Long.class);
		query.setParameter("sessionId", sessionId);
		
		try {
			Long result = query.getSingleResult();
			return result != null ? result.intValue() : 0;
		}
		catch (NoResultException e) {
			return 0;
		}
	}
	
	public String buildModuleEventQuery(String eventType, String moduleName, String username, String userUUID,
	        Date startDate, Date endDate) {
		StringBuilder hql = new StringBuilder("from ModuleEvent e where 1=1");
		
		ModuleEventType moduleEventType = ModuleEventType.fromName(eventType);
		
		if (moduleEventType != null) {
			hql.append(" and e.eventType = :eventType");
		}
		if (moduleName != null) {
			hql.append(" and e.moduleName = :moduleName");
		}
		if (username != null) {
			hql.append(" and lower(e.username) like :username");
		}
		if (userUUID != null) {
			hql.append(" and e.userUUID = :userUUID");
		}
		if (startDate != null) {
			hql.append(" and e.eventTime >= :startDate");
		}
		if (endDate != null) {
			hql.append(" and e.eventTime <= :endDate");
		}
		hql.append(" order by e.eventTime desc");
		
		return hql.toString();
	}
	
	private void bindModuleEventFilters(Query<?> query, ModuleEventType eventType, String moduleName, String username,
	        String userUUID, Date startDate, Date endDate) {
		if (eventType != null) {
			query.setParameter("eventType", eventType);
		}
		if (moduleName != null) {
			query.setParameter("moduleName", moduleName);
		}
		if (username != null && !username.trim().isEmpty()) {
			query.setParameter("username", "%" + username.trim().toLowerCase() + "%");
		}
		if (userUUID != null && !userUUID.trim().isEmpty()) {
			query.setParameter("userUUID", userUUID);
		}
		if (startDate != null) {
			query.setParameter("startDate", startDate);
		}
		if (endDate != null) {
			query.setParameter("endDate", endDate);
		}
	}
}
