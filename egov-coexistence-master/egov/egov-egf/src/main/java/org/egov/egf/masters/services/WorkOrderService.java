/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */
package org.egov.egf.masters.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.egov.commons.Accountdetailkey;
import org.egov.commons.service.AccountDetailKeyService;
import org.egov.commons.service.AccountdetailtypeService;
import org.egov.commons.service.EntityTypeService;
import org.egov.commons.service.FundService;
import org.egov.egf.masters.repository.WorkOrderRepository;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infstr.services.PersistenceService;
import org.egov.model.masters.WorkOrder;
import org.egov.model.masters.WorkOrderAudit;
import org.egov.model.masters.WorkOrderSearchRequest;
import org.egov.services.masters.SchemeService;
import org.egov.services.masters.SubSchemeService;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author venki
 */

@Service
@Transactional(readOnly = true)
public class WorkOrderService implements EntityTypeService {
	
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@Autowired
	private WorkOrderRepository workOrderRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private AccountDetailKeyService accountDetailKeyService;

	@Autowired
	private AccountdetailtypeService accountdetailtypeService;

	@Autowired
	private FundService fundService;

	@Autowired
	private ContractorService contractorService;

	@Autowired
	private SchemeService schemeService;

	@Autowired
	private SubSchemeService subSchemeService;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public WorkOrder getById(final Long id) {
		return workOrderRepository.findOne(id);
	}

	public List<WorkOrder> getByContractorId(final Long contractorId) {
		return workOrderRepository.findByContractor_Id(contractorId);
	}

	public WorkOrder getByOrderNumber(final String orderNumber) {
		return workOrderRepository.findByOrderNumber(orderNumber);
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public WorkOrder create(WorkOrder workOrder) {

		setAuditDetails(workOrder);
		if (workOrder.getFund() != null && workOrder.getFund().getId() != null) {
			workOrder.setFund(fundService.findOne(workOrder.getFund().getId()));
		}
		if (workOrder.getScheme() != null && workOrder.getScheme().getId() != null) {
			workOrder.setScheme(schemeService.findById(workOrder.getScheme().getId(), false));
		} else {
			workOrder.setScheme(null);
		}
		if (workOrder.getSubScheme() != null && workOrder.getSubScheme().getId() != null) {
			workOrder.setSubScheme(subSchemeService.findById(workOrder.getSubScheme().getId(), false));
		} else {
			workOrder.setSubScheme(null);
		}
		if (workOrder.getContractor() != null && workOrder.getContractor().getId() != null) {
			workOrder.setContractor(contractorService.getById(workOrder.getContractor().getId()));
		}
		workOrder = workOrderRepository.save(workOrder);
		saveAccountDetailKey(workOrder);
		return workOrder;
	}

	@Transactional
	public void saveAccountDetailKey(WorkOrder workOrder) {

		Accountdetailkey accountdetailkey = new Accountdetailkey();
		accountdetailkey.setDetailkey(workOrder.getId().intValue());
		accountdetailkey.setDetailname(workOrder.getName());
		accountdetailkey
				.setAccountdetailtype(accountdetailtypeService.findByName(workOrder.getClass().getSimpleName()));
		accountdetailkey.setGroupid(1);
		accountDetailKeyService.create(accountdetailkey);
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public WorkOrder update(WorkOrder workOrder) {
		if (workOrder.getEditAllFields().booleanValue()) {
			setAuditDetails(workOrder);
			if (workOrder.getFund() != null && workOrder.getFund().getId() != null) {
				workOrder.setFund(fundService.findOne(workOrder.getFund().getId()));
			}
			if (workOrder.getScheme() != null && workOrder.getScheme().getId() != null) {
				workOrder.setScheme(schemeService.findById(workOrder.getScheme().getId(), false));
			} else {
				workOrder.setScheme(null);
			}
			if (workOrder.getSubScheme() != null && workOrder.getSubScheme().getId() != null) {
				workOrder.setSubScheme(subSchemeService.findById(workOrder.getSubScheme().getId(), false));
			} else {
				workOrder.setSubScheme(null);
			}
			if (workOrder.getContractor() != null && workOrder.getContractor().getId() != null) {
				workOrder.setContractor(contractorService.getById(workOrder.getContractor().getId()));
			}
			workOrder = workOrderRepository.save(workOrder);
		} else {
			setAuditDetails(workOrder);
			WorkOrder savedWorkOrder = workOrderRepository.findOne(workOrder.getId());
			savedWorkOrder.setName(workOrder.getName());
			savedWorkOrder.setDescription(workOrder.getDescription());
			savedWorkOrder.setActive(workOrder.getActive());
			savedWorkOrder.setSanctionNumber(workOrder.getSanctionNumber());
			savedWorkOrder.setSanctionDate(workOrder.getSanctionDate());

			workOrder = workOrderRepository.save(savedWorkOrder);
		}
		return workOrder;
	}

	private void setAuditDetails(WorkOrder workOrder) {
		if (workOrder.getId() == null) {
			workOrder.setCreatedDate(new Date());
			workOrder.setCreatedBy(ApplicationThreadLocals.getUserId());
		}
		workOrder.setLastModifiedDate(new Date());
		workOrder.setLastModifiedBy(ApplicationThreadLocals.getUserId());
	}

	public List<WorkOrder> search(final WorkOrderSearchRequest workOrderSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<WorkOrder> createQuery = cb.createQuery(WorkOrder.class);
		final Root<WorkOrder> workOrders = createQuery.from(WorkOrder.class);
		createQuery.select(workOrders);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<WorkOrder> workOrderEntityType = m.entity(WorkOrder.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (workOrderSearchRequest.getName() != null) {
			final String name = "%" + workOrderSearchRequest.getName().toLowerCase() + "%";
			predicates.add(cb.isNotNull(workOrders.get("name")));
			predicates.add(cb.like(
					cb.lower(workOrders.get(workOrderEntityType.getDeclaredSingularAttribute("name", String.class))),
					name));
		}
		if (workOrderSearchRequest.getOrderNumber() != null) {
			final String code = "%" + workOrderSearchRequest.getOrderNumber().toLowerCase() + "%";
			predicates.add(cb.isNotNull(workOrders.get("orderNumber")));
			predicates.add(cb.like(
					cb.lower(workOrders
							.get(workOrderEntityType.getDeclaredSingularAttribute("orderNumber", String.class))),
					code));
		}

		if (workOrderSearchRequest.getContractorId() != null) {
			predicates.add(cb.equal(workOrders.get("contractor").get("id"), workOrderSearchRequest.getContractorId()));
		}
		if (workOrderSearchRequest.getFundId() != null) {
			predicates.add(cb.equal(workOrders.get("fund").get("id"), workOrderSearchRequest.getFundId()));
		}

		createQuery.where(predicates.toArray(new Predicate[] {}));
		final TypedQuery<WorkOrder> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> getAllActiveEntities(Integer accountDetailTypeId) {

		return workOrderRepository.findActiveOrders();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> filterActiveEntities(String filterKey, int maxRecords,
			Integer accountDetailTypeId) {
		return workOrderRepository.findByNameLikeIgnoreCaseOrOrderNumberLikeIgnoreCaseAndActive(filterKey + "%",
				filterKey + "%", true);
	}

	@Override
	public List<?> getAssetCodesForProjectCode(Integer accountdetailkey) throws ValidationException {
		return Collections.emptyList();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> validateEntityForRTGS(List<Long> idsList)
			throws ValidationException {
		return Collections.emptyList();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> getEntitiesById(List<Long> idsList)
			throws ValidationException {
		return Collections.emptyList();
	}
	
	public List<String> getWorkOrderAuditReport(final String workOrderId) {

		List<WorkOrderAudit> resultList = getWorkOrderAuditList(workOrderId);

		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			WorkOrderAudit previousModifiedRow = resultList.get(i);
			WorkOrderAudit currentModifiedRow = resultList.get(i + 1);
			String modifications = "";

			if (!previousModifiedRow.getName().equals(currentModifiedRow.getName())) {
				modifications = modifications + "Name : " + previousModifiedRow.getName() + " --> "
						+ currentModifiedRow.getName() + "<br>";
			}
			if (!previousModifiedRow.getOrderNumber().equals(currentModifiedRow.getOrderNumber())) {
				modifications = modifications + "Order Number : " + previousModifiedRow.getOrderNumber() + " --> "
						+ currentModifiedRow.getOrderNumber() + "<br>";
			}			
			if (!previousModifiedRow.getOrderDate().equals(currentModifiedRow.getOrderDate())) {
				modifications = modifications + "Order Date : " + previousModifiedRow.getOrderDate()
						+ " --> " + currentModifiedRow.getOrderDate() + "<br>";
			}
			if (!previousModifiedRow.getOrderValue().equals(currentModifiedRow.getOrderValue())) {
				modifications = modifications + "Order Value : " + previousModifiedRow.getOrderValue()
						+ " --> " + currentModifiedRow.getOrderValue() + "<br>";
			}
			if (!previousModifiedRow.getContractor().equals(currentModifiedRow.getContractor())) {
				modifications = modifications + "Contractor : " + previousModifiedRow.getContractor()
						+ " --> " + currentModifiedRow.getContractor() + "<br>";
			}
			if (!previousModifiedRow.getAdvancePayable().equals(currentModifiedRow.getAdvancePayable())) {
				modifications = modifications + "Advance Payble : " + previousModifiedRow.getAdvancePayable()
						+ " --> " + currentModifiedRow.getAdvancePayable() + "<br>";
			}
			if (!previousModifiedRow.getDescription().equals(currentModifiedRow.getDescription())) {
				modifications = modifications + "Description : " + previousModifiedRow.getDescription()
						+ " --> " + currentModifiedRow.getDescription() + "<br>";
			}
			if (!previousModifiedRow.getFund().equals(currentModifiedRow.getFund())) {
				modifications = modifications + "Fund : " + previousModifiedRow.getFund()
						+ " --> " + currentModifiedRow.getFund() + "<br>";
			}
			if (!previousModifiedRow.getDepartment().equals(currentModifiedRow.getDepartment())) {
				modifications = modifications + "Department : " + previousModifiedRow.getDepartment()
						+ " --> " + currentModifiedRow.getDepartment() + "<br>";
			}
			if (!previousModifiedRow.getScheme().equals(currentModifiedRow.getScheme())) {
				modifications = modifications + "Scheme : " + previousModifiedRow.getScheme()
						+ " --> " + currentModifiedRow.getScheme() + "<br>";
			}
			if (!previousModifiedRow.getSubScheme().equals(currentModifiedRow.getSubScheme())) {
				modifications = modifications + "Sub-Scheme : " + previousModifiedRow.getSubScheme()
						+ " --> " + currentModifiedRow.getSubScheme() + "<br>";
			}
			if (!previousModifiedRow.getSanctionNumber().equals(currentModifiedRow.getSanctionNumber())) {
				modifications = modifications + "Sanction Number : " + previousModifiedRow.getSanctionNumber()
						+ " --> " + currentModifiedRow.getSanctionNumber() + "<br>";
			}
			if (!previousModifiedRow.getSanctionDate().equals(currentModifiedRow.getSanctionDate())) {
				modifications = modifications + "Sanction Date : " + previousModifiedRow.getSanctionDate()
						+ " --> " + currentModifiedRow.getSanctionDate() + "<br>";
			}
			if (!previousModifiedRow.getActive().equals(currentModifiedRow.getActive())) {
				modifications = modifications + "Active : " + previousModifiedRow.getActive() + " --> "
						+ currentModifiedRow.getActive();
			}			

			if (modifications.length() > 0) {
								
				modificationsList.add(
						"User : " + currentModifiedRow.getNameOfmodifyingUser() + "<br>" + "Modified On : "
								+ currentModifiedRow.getLastModifiedDate() + "<br><br>" + modifications);
			}

		}

		return modificationsList;
	}

	private List<WorkOrderAudit> getWorkOrderAuditList(final String workOrderId) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select wo_aud.id, wo_aud.name, wo_aud.ordernumber, wo_aud.orderdate, cntr.name as contractorName, wo_aud.ordervalue, wo_aud.advancepayable, ")
		.append("wo_aud.description, fnd.name fundName, wo_aud.department, schm.name scheme_name, sub_schm.name sub_scheme_name, wo_aud.sanctionnumber, wo_aud.sanctiondate, ")
		.append("wo_aud.active, wo_aud.lastmodifiedby, wo_aud.lastmodifieddate, wo_aud.nameofmodifyinguser ")
		.append("FROM egf_workorder_aud wo_aud LEFT JOIN egf_contractor cntr ON cntr.id=wo_aud.contractor LEFT JOIN fund fnd ON fnd.id=wo_aud.fund LEFT JOIN scheme schm ON schm.id=wo_aud.scheme ")
		.append("LEFT JOIN sub_scheme sub_schm ON sub_schm.id=wo_aud.subscheme where wo_aud.id=:workOrderId order by wo_aud.lastmodifieddate NULLS FIRST");		
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("workOrderId", Long.valueOf(workOrderId));
		final List<Object[]> workOrderListFromQuery = queryResult.list();

		List<WorkOrderAudit> workOrderAuditList = new ArrayList<WorkOrderAudit>();

		for (Object[] obj : workOrderListFromQuery) {
			WorkOrderAudit element = new WorkOrderAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setName(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setOrderNumber(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setOrderDate(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			element.setContractor(String.valueOf(obj[4] != null ? obj[4] : AuditReportUtils.NO_VALUE));
			element.setOrderValue(String.valueOf(obj[5] != null ? obj[5] : AuditReportUtils.NO_VALUE));
			element.setAdvancePayable(String.valueOf(obj[6] != null ? obj[6] : AuditReportUtils.NO_VALUE));
			element.setDescription(String.valueOf(obj[7] != null ? obj[7] : AuditReportUtils.NO_VALUE));
			element.setFund(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			element.setDepartment(String.valueOf(obj[9] != null ? obj[9] : AuditReportUtils.NO_VALUE));
			element.setScheme(String.valueOf(obj[10] != null ? obj[10] : AuditReportUtils.NO_VALUE));
			element.setSubScheme(String.valueOf(obj[11] != null ? obj[11] : AuditReportUtils.NO_VALUE));
			element.setSanctionNumber(String.valueOf(obj[12] != null ? obj[12] : AuditReportUtils.NO_VALUE));
			element.setSanctionDate(String.valueOf(obj[13] != null ? obj[13] : AuditReportUtils.NO_VALUE));
			String isActive = (obj[14] != null && (Boolean)obj[14].equals(true)) ? "Active" : "Inactive";
			element.setActive(String.valueOf(isActive));
			element.setLastModifiedBy(String.valueOf(obj[15] != null ? obj[15] : AuditReportUtils.NO_VALUE));
			String lastModifiedDate = String.valueOf(obj[16] != null ? obj[16] : "");			
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));			
			element.setNameOfmodifyingUser(String.valueOf(obj[17] != null ? obj[17] : AuditReportUtils.NO_VALUE));
			workOrderAuditList.add(element);
			element = null;
		}

		return workOrderAuditList;
	}
	

}





