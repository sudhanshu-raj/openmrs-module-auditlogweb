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

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ReadAuditAOP {
	
	private final ReadAuditHelper readAuditHelper;
	
	@Around("execution(* org.openmrs.api.PatientService.getPatient*(..)) || "
	        + "execution(* org.openmrs.api.PatientService.getAllPatient*(..)) || "
	        + "execution(* org.openmrs.api.PatientService.getDuplicatePatient*(..)) || "
	        + "execution(* org.openmrs.api.PatientService.getAllerg*(..)) ")
	public Object auditPatientDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.EncounterService.getEncounter*(..)) || "
	        + "execution(* org.openmrs.api.EncounterService.getAllEncounter*(..))")
	public Object auditEncounterDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("(execution(* org.openmrs.api.ObsService.getObs*(..)) || "
	        + "execution(* org.openmrs.api.ObsService.getRevisionObs(..)) || "
	        + "execution(* org.openmrs.api.ObsService.getObservations*(..))) && "
	        + "!execution(* org.openmrs.api.ObsService.getObservationCount(..))")
	public Object auditObsDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.CohortService.getCohort*(..)) || "
	        + "execution(* org.openmrs.api.CohortService.getAllCohorts(..))")
	public Object auditCohortDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("(execution(* org.openmrs.api.ConceptService.getConcept*(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getDrug*(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getAllConcept*(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getSetsContainingConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getProposedConcepts(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getPrevConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getNextConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getTrueConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getFalseConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getUnknownConcept(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.findConceptAnswers(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getActiveConceptMapTypes(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getReferenceTermMappingsTo(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getDefaultConceptMapType(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getOrderableConcepts(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getAllConceptAttributeType*(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getConceptAttributeType*(..)) || "
	        + "execution(* org.openmrs.api.ConceptService.getAllDrugs(..))) && "
	        + "!execution(* org.openmrs.api.ConceptService.getConceptStopWords(..)) && "
	        + "!execution(* org.openmrs.api.ConceptService.getConceptIdsByMapping(..))")
	public Object auditConceptDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.ConditionService.getCondition*(..)) || "
	        + "execution(* org.openmrs.api.ConditionService.getActiveConditions(..)) || "
	        + "execution(* org.openmrs.api.ConditionService.getAllConditions(..)) ")
	public Object auditConditionDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.DiagnosisService.getDiagnosis*(..)) || "
	        + "execution(* org.openmrs.api.DiagnosisService.getDiagnoses*(..)) || "
	        + "execution(* org.openmrs.api.DiagnosisService.getAllDiagnosisAttributeTypes(..)) || "
	        + "execution(* org.openmrs.api.DiagnosisService.getDiagnosisAttributeType*(..)) || "
	        + "execution(* org.openmrs.api.DiagnosisService.getDiagnosisAttribute*(..)) || "
	        + "execution(* org.openmrs.api.DiagnosisService.getUniqueDiagnoses(..)) ")
	public Object auditDiagnosisDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("(execution(* org.openmrs.api.FormService.getForm*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getAllForms*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getFieldType*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getAllFieldTypes*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getFormsContainingConcept(..)) || "
	        + "execution(* org.openmrs.api.FormService.getAllFormFields(..)) || "
	        + "execution(* org.openmrs.api.FormService.getField*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getAllFields*(..)) || "
	        + "execution(* org.openmrs.api.FormService.getPublishedForms(..))) && "
	        + "!execution(* org.openmrs.api.FormService.getFormCount(..))")
	public Object auditFormDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.LocationService.getLocation*(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getDefaultLocation(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getAllLocations*(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getRootLocations(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getAllLocationAttributeTypes*(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getLocationAttributeType*(..)) || "
	        + "execution(* org.openmrs.api.LocationService.getLocationAttribute*(..))")
	public Object auditLocationDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.MedicationDispenseService.getMedicationDispense*(..))")
	public Object auditMedicationDispenseDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.OrderService.getOrder*(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getDiscontinuationOrder(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getRevisionOrder(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getAllOrders*(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getActiveOrders(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getCareSetting*(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getSubtypes(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getDrug*(..)) || "
	        + "execution(* org.openmrs.api.OrderService.getTestSpecimenSources(..)) ||"
	        + "execution(* org.openmrs.api.OrderService.getNonCodedDrugConcept(..))")
	public Object auditOrderDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.PersonService.getPeople*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getPerson*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getSimilarPeople*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getAllPersonAttributeTypes*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getPersonAttribute*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getRelationship*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getAllRelationship*(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getAllPersonMergeLogs(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getWinningPersonMergeLogs(..)) || "
	        + "execution(* org.openmrs.api.PersonService.getLosingPersonMergeLog(..)) ")
	public Object auditPersonDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.ProgramWorkflowService.getProgram*(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getAllPrograms(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getPatientStateByUuid(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getPatientProgram*(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getPossibleOutcomes(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getWorkflow*(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getConceptStateConversion*(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getAllConceptStateConversions(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getAllConceptStateConversions(..)) || "
	        + "execution(* org.openmrs.api.ProgramWorkflowService.getState*(..)) ")
	public Object auditProgramDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.ProviderService.getAllProviders(..)) || "
	        + "execution(* org.openmrs.api.ProviderService.getProvider*(..)) || "
	        + "execution(* org.openmrs.api.ProviderService.getAllProviderAttributeTypes*(..)) || "
	        + "execution(* org.openmrs.api.ProviderService.getProviderAttribute*(..)) || "
	        + "execution(* org.openmrs.api.ProviderService.getUnknownProvider(..)) ")
	public Object auditProviderDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.UserService.getUser*(..)) || "
	        + "execution(* org.openmrs.api.UserService.getAllUsers(..))")
	public Object auditUserDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
	
	@Around("execution(* org.openmrs.api.VisitService.getAllVisit*(..)) || "
	        + "execution(* org.openmrs.api.VisitService.getVisit*(..)) || "
	        + "execution(* org.openmrs.api.VisitService.getActiveVisitsByPatient(..)) ")
	public Object auditVisitDataRead(ProceedingJoinPoint joinPoint) throws Throwable {
		return readAuditHelper.auditReadRequest(joinPoint);
	}
}
