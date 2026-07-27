<%--
  This Source Code Form is subject to the terms of the Mozilla Public License,
  v. 2.0. If a copy of the MPL was not distributed with this file, You can
  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  graphic logo is a trademark of OpenMRS Inc.
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@300;400;600&display=swap">
<link rel="stylesheet" type="text/css" href="<openmrs:contextPath/>/moduleResources/auditlogweb/css/viewReadAuditLog.css" />
<div class="audit-detail-container">

    <div class="title-section">
        <h1 class="detail-title">Read Audit Event Detail</h1>
        <p class="detail-subtitle">Viewing detailed record for read event</p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="error-box"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${not empty readAudit}">
        <div class="info-section">
            <h2 class="section-title">PRIMARY INFORMATION</h2>
            <table class="detail-table">
                <tbody>
                <tr>
                    <td class="label-cell">Event ID</td>
                    <td><c:out value="${readAudit.id}"/></td>
                </tr>
                <tr>
                    <td class="label-cell">Entity Type</td>
                    <td>
                        <c:choose>
                            <c:when test="${readAudit.readSuccess}">
                                <span class="badge badge-success" style="text-transform: none;"><c:out value="${readAudit.entityName}"/></span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-failure" style="text-transform: none;"><c:out value="${readAudit.entityName}"/></span>
                            </c:otherwise>
                        </c:choose>
                </tr>
                <c:if test="${not empty distinctPatientNames}">
                <tr>
                    <td class="label-cell">Patient Name(s)</td>
                    <td>
                        <c:choose>
                            <c:when test="${distinctPatientNames.size() == 1}">
                                <c:out value="${distinctPatientNames.get(0)}"/>
                            </c:when>
                            <c:otherwise>
                                <span class="patient-toggle-container">
                                    <span class="patient-primary-name"><c:out value="${distinctPatientNames.get(0)}"/></span>
                                    <button type="button" class="btn-toggle-patients" onclick="togglePatientNamesList()">
                                        <span id="patientToggleIcon">+</span> <span id="patientToggleText">${distinctPatientNames.size() - 1} more</span>
                                    </button>
                                </span>
                                <div id="additionalPatientsList" style="display: none; margin-top: 5px; padding-left: 10px; border-left: 2px solid #1bac9a;">
                                    <c:forEach var="name" items="${distinctPatientNames}" varStatus="status">
                                        <c:if test="${status.index > 0}">
                                            <div style="margin-bottom: 3px; font-weight: 600;"><c:out value="${name}"/></div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                </c:if>
                <tr>
                    <td class="label-cell">Username</td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty readAudit.username}"><c:out value="${readAudit.username}"/></c:when>
                            <c:otherwise><span class="null-value">-</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="label-cell">User UUID</td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty readAudit.userUUID}"><c:out value="${readAudit.userUUID}"/></c:when>
                            <c:otherwise><span class="null-value">-</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="label-cell">Read success</td>
                    <td>
                        <c:choose>
                            <c:when test="${readAudit.readSuccess}">
                                <span>Yes</span>
                            </c:when>
                            <c:otherwise>
                                <span>No</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="label-cell">Event Time</td>
                    <td><fmt:formatDate value="${readAudit.eventTime}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                </tr>
                <tr>
                    <td class="label-cell">IP Address</td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty readAudit.ipAddress}"><c:out value="${readAudit.ipAddress}"/></c:when>
                            <c:otherwise><span class="null-value">-</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="label-cell">User Agent</td>
                    <td class="user-agent-cell">
                        <c:choose>
                            <c:when test="${not empty readAudit.userAgent}">
                                <span class="truncate-user-agent"><c:out value="${readAudit.userAgent}"/></span>
                            </c:when>
                            <c:otherwise><span class="null-value">-</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td class="label-cell">Session ID</td>
                    <td class="session-id-cell">
                        <c:choose>
                            <c:when test="${not empty readAudit.sessionId}"><c:out value="${readAudit.sessionId}"/></c:when>
                            <c:otherwise><span class="null-value">-</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="info-section">
            <c:set var="tooltipText" value="UUID of the fetched entity like Patient's UUID" />
            <h2 class="section-title">
                TARGET ENTITY METADATA
                <span class="tooltip-container">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                    stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="info-icon-svg"><circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                    <span class="tooltip-text"><c:out value="${tooltipText}" /></span>
                </span>
            </h2>
            <div class="details-content">
                <c:choose>
                    <c:when test="${not empty readAudit.targets}">
                        <ul class="uuid-list">
                             <c:forEach var="target" items="${readAudit.targets}">
                                 <li><c:out value="${target.entityUuid}"/></li>
                             </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <p class="no-details">No target UUIDs available.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Related Activity Section -->
        <c:if test="${not empty relatedAudits}">
            <div class="info-section">
                <h2 class="section-title">RELATED ACTIVITY (SAME SESSION)</h2>
                <table class="related-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Time</th>
                        <th>Type</th>
                        <th>User</th>
                        <th>Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="relAudit" items="${relatedAudits}">
                        <tr <c:if test="${relAudit.id == readAudit.id}">class="current-event"</c:if>>
                            <td>
                                <c:out value="${relAudit.id}"/>
                            </td>
                            <td><fmt:formatDate value="${relAudit.eventTime}" pattern="HH:mm:ss" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${relAudit.readSuccess}">
                                        <span class="badge badge-success" style="text-transform: none;"><c:out value="${relAudit.entityName}"/></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-failure" style="text-transform: none;"><c:out value="${relAudit.entityName}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty relAudit.username}"><c:out value="${relAudit.username}"/></c:when>
                                    <c:otherwise><span class="null-value">-</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:set var="ipText" value="${empty relAudit.ipAddress ? '-' : relAudit.ipAddress}"/>
                                <c:set var="detailsText" value="IP: ${ipText}"/>
                                <c:choose>
                                    <c:when test="${relAudit.id == readAudit.id}"><strong>This Event</strong></c:when>
                                    <c:otherwise><c:out value="${detailsText}"/></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <div class="footer-actions">
            <a href="<c:out value='${pageContext.request.contextPath}'/>/module/auditlogweb/readauditlogs.form" class="btn btn-secondary">
                Back to Audit Logs
            </a>
        </div>

    </c:if>
</div>

<script type="text/javascript">
    function togglePatientNamesList() {
        var list = document.getElementById("additionalPatientsList");
        var icon = document.getElementById("patientToggleIcon");
        var text = document.getElementById("patientToggleText");
        if (list.style.display === "none") {
            list.style.display = "block";
            icon.innerText = "-";
            text.innerText = "hide";
        } else {
            list.style.display = "none";
            icon.innerText = "+";
            text.innerText = (parseInt("${distinctPatientNames.size()}") - 1) + " more";
        }
    }
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
