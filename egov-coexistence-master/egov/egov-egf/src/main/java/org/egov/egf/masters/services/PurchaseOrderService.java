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
import org.egov.commons.BankAudit;
import org.egov.commons.service.AccountDetailKeyService;
import org.egov.commons.service.AccountdetailtypeService;
import org.egov.commons.service.EntityTypeService;
import org.egov.commons.service.FundService;
import org.egov.egf.masters.repository.PurchaseOrderRepository;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infstr.services.PersistenceService;
import org.egov.model.masters.PurchaseOrder;
import org.egov.model.masters.PurchaseOrderAudit;
import org.egov.model.masters.PurchaseOrderSearchRequest;
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
public class PurchaseOrderService implements EntityTypeService {
	
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@Autowired
	private PurchaseOrderRepository purchaseOrderRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private AccountDetailKeyService accountDetailKeyService;

	@Autowired
	private AccountdetailtypeService accountdetailtypeService;

	@Autowired
	private FundService fundService;

	@Autowired
	private SupplierService supplierService;

	@Autowired
	private SchemeService schemeService;

	@Autowired
	private SubSchemeService subSchemeService;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public PurchaseOrder getById(final Long id) {
		return purchaseOrderRepository.findOne(id);
	}

	public List<PurchaseOrder> getBySupplierId(final Long supplierId) {
		return purchaseOrderRepository.findBySupplier_Id(supplierId);
	}

	public PurchaseOrder getByOrderNumber(final String orderNumber) {
		return purchaseOrderRepository.findByOrderNumber(orderNumber);
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public PurchaseOrder create(PurchaseOrder purchaseOrder) {

		setAuditDetails(purchaseOrder);
		if (purchaseOrder.getFund() != null && purchaseOrder.getFund().getId() != null) {
			purchaseOrder.setFund(fundService.findOne(purchaseOrder.getFund().getId()));
		}
		if (purchaseOrder.getScheme() != null && purchaseOrder.getScheme().getId() != null) {
			purchaseOrder.setScheme(schemeService.findById(purchaseOrder.getScheme().getId(), false));
		} else {
			purchaseOrder.setScheme(null);
		}
		if (purchaseOrder.getSubScheme() != null && purchaseOrder.getSubScheme().getId() != null) {
			purchaseOrder.setSubScheme(subSchemeService.findById(purchaseOrder.getSubScheme().getId(), false));
		} else {
			purchaseOrder.setSubScheme(null);
		}
		if (purchaseOrder.getSupplier() != null && purchaseOrder.getSupplier().getId() != null) {
			purchaseOrder.setSupplier(supplierService.getById(purchaseOrder.getSupplier().getId()));
		}
		purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
		saveAccountDetailKey(purchaseOrder);
		return purchaseOrder;
	}

	@Transactional
	public void saveAccountDetailKey(PurchaseOrder purchaseOrder) {

		Accountdetailkey accountdetailkey = new Accountdetailkey();
		accountdetailkey.setDetailkey(purchaseOrder.getId().intValue());
		accountdetailkey.setDetailname(purchaseOrder.getName());
		accountdetailkey
				.setAccountdetailtype(accountdetailtypeService.findByName(purchaseOrder.getClass().getSimpleName()));
		accountdetailkey.setGroupid(1);
		accountDetailKeyService.create(accountdetailkey);
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public PurchaseOrder update(PurchaseOrder purchaseOrder) {

		if (purchaseOrder.getEditAllFields().booleanValue()) {
			setAuditDetails(purchaseOrder);
			if (purchaseOrder.getFund() != null && purchaseOrder.getFund().getId() != null) {
				purchaseOrder.setFund(fundService.findOne(purchaseOrder.getFund().getId()));
			}
			if (purchaseOrder.getScheme() != null && purchaseOrder.getScheme().getId() != null) {
				purchaseOrder.setScheme(schemeService.findById(purchaseOrder.getScheme().getId(), false));
			} else {
				purchaseOrder.setScheme(null);
			}
			if (purchaseOrder.getSubScheme() != null && purchaseOrder.getSubScheme().getId() != null) {
				purchaseOrder.setSubScheme(subSchemeService.findById(purchaseOrder.getSubScheme().getId(), false));
			} else {
				purchaseOrder.setSubScheme(null);
			}
			if (purchaseOrder.getSupplier() != null && purchaseOrder.getSupplier().getId() != null) {
				purchaseOrder.setSupplier(supplierService.getById(purchaseOrder.getSupplier().getId()));
			}
			purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
		} else {
			PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.findOne(purchaseOrder.getId());
			savedPurchaseOrder.setName(purchaseOrder.getName());
			savedPurchaseOrder.setDescription(purchaseOrder.getDescription());
			savedPurchaseOrder.setActive(purchaseOrder.getActive());
			savedPurchaseOrder.setSanctionNumber(purchaseOrder.getSanctionNumber());
			savedPurchaseOrder.setSanctionDate(purchaseOrder.getSanctionDate());
			setAuditDetails(savedPurchaseOrder);
			purchaseOrder = purchaseOrderRepository.save(savedPurchaseOrder);
		}
		return purchaseOrder;
	}

	private void setAuditDetails(PurchaseOrder purchaseOrder) {
		if (purchaseOrder.getId() == null) {
			purchaseOrder.setCreatedDate(new Date());
			purchaseOrder.setCreatedBy(ApplicationThreadLocals.getUserId());
		}
		purchaseOrder.setLastModifiedDate(new Date());
		purchaseOrder.setLastModifiedBy(ApplicationThreadLocals.getUserId());
	}

	public List<PurchaseOrder> search(final PurchaseOrderSearchRequest purchaseOrderSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<PurchaseOrder> createQuery = cb.createQuery(PurchaseOrder.class);
		final Root<PurchaseOrder> purchaseOrders = createQuery.from(PurchaseOrder.class);
		createQuery.select(purchaseOrders);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<PurchaseOrder> purchaseOrderEntityType = m.entity(PurchaseOrder.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (purchaseOrderSearchRequest.getName() != null) {
			final String name = "%" + purchaseOrderSearchRequest.getName().toLowerCase() + "%";
			predicates.add(cb.isNotNull(purchaseOrders.get("name")));
			predicates
					.add(cb.like(
							cb.lower(purchaseOrders
									.get(purchaseOrderEntityType.getDeclaredSingularAttribute("name", String.class))),
							name));
		}
		if (purchaseOrderSearchRequest.getOrderNumber() != null) {
			final String code = "%" + purchaseOrderSearchRequest.getOrderNumber().toLowerCase() + "%";
			predicates.add(cb.isNotNull(purchaseOrders.get("orderNumber")));
			predicates.add(cb.like(
					cb.lower(purchaseOrders
							.get(purchaseOrderEntityType.getDeclaredSingularAttribute("orderNumber", String.class))),
					code));
		}
		if (purchaseOrderSearchRequest.getSupplierId() != null) {
			predicates.add(
					cb.equal(purchaseOrders.get("supplier").get("id"), purchaseOrderSearchRequest.getSupplierId()));
		}

		if (purchaseOrderSearchRequest.getFundId() != null) {
			predicates.add(cb.equal(purchaseOrders.get("fund").get("id"), purchaseOrderSearchRequest.getFundId()));
		}

		createQuery.where(predicates.toArray(new Predicate[] {}));
		final TypedQuery<PurchaseOrder> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> getAllActiveEntities(Integer accountDetailTypeId) {

		return purchaseOrderRepository.findActiveOrders();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> filterActiveEntities(String filterKey, int maxRecords,
			Integer accountDetailTypeId) {
		return purchaseOrderRepository.findByNameLikeIgnoreCaseOrOrderNumberLikeIgnoreCaseAndActive(filterKey + "%",
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
	
	public List<String> getPurchaseOrderAuditReport(final String purchaseOrderId) {

		List<PurchaseOrderAudit> resultList = getPurchaseOrderAuditList(purchaseOrderId);

		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			PurchaseOrderAudit previousModifiedRow = resultList.get(i);
			PurchaseOrderAudit currentModifiedRow = resultList.get(i + 1);
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
			if (!previousModifiedRow.getSupplier().equals(currentModifiedRow.getSupplier())) {
				modifications = modifications + "Supplier : " + previousModifiedRow.getSupplier()
						+ " --> " + currentModifiedRow.getSupplier() + "<br>";
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

	private List<PurchaseOrderAudit> getPurchaseOrderAuditList(final String purchaseOrderId) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select po_aud.id, po_aud.name, po_aud.ordernumber, po_aud.orderdate, supp.name supplier_name, po_aud.ordervalue, po_aud.advancepayable, po_aud.description, fnd.name fund_name, po_aud.department, ")
		.append("schm.name scheme_name, sub_schm.name sub_scheme_name, po_aud.sanctionnumber, po_aud.sanctiondate, po_aud.active, po_aud.lastmodifiedby, po_aud.lastmodifieddate, po_aud.nameofmodifyinguser ")
		.append("FROM egf_purchaseorder_aud po_aud LEFT JOIN scheme supp ON supp.id=po_aud.supplier ")
		.append("LEFT JOIN scheme schm ON schm.id=po_aud.scheme LEFT JOIN sub_scheme sub_schm ON sub_schm.id=po_aud.subscheme LEFT JOIN fund fnd ON fnd.id=po_aud.fund ")
		.append("WHERE po_aud.id=:purchaseOrderId ORDER BY po_aud.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("purchaseOrderId", Long.valueOf(purchaseOrderId));
		final List<Object[]> purchaseOrderListFromQuery = queryResult.list();

		List<PurchaseOrderAudit> purchaseOrderAuditList = new ArrayList<PurchaseOrderAudit>();

		for (Object[] obj : purchaseOrderListFromQuery) {
			PurchaseOrderAudit element = new PurchaseOrderAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setName(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setOrderNumber(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setOrderDate(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			element.setSupplier(String.valueOf(obj[4] != null ? obj[4] : AuditReportUtils.NO_VALUE));
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
			purchaseOrderAuditList.add(element);
			element = null;
		}

		return purchaseOrderAuditList;
	}

}
