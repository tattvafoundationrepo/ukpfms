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

import java.text.SimpleDateFormat;
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
import org.egov.egf.masters.repository.SupplierRepository;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infstr.services.PersistenceService;
import org.egov.model.masters.Supplier;
import org.egov.model.masters.SupplierAudit;
import org.egov.model.masters.SupplierSearchRequest;
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
public class SupplierService implements EntityTypeService {
	
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@Autowired
	private SupplierRepository supplierRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private AccountDetailKeyService accountDetailKeyService;

	@Autowired
	private AccountdetailtypeService accountdetailtypeService;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public Supplier getById(final Long id) {
		return supplierRepository.findOne(id);
	}

	@Transactional
	public Supplier create(Supplier supplier) {
		setAuditDetails(supplier);
		supplier = supplierRepository.save(supplier);
		saveAccountDetailKey(supplier);
		return supplier;
	}

	@Transactional
	public Supplier update(final Supplier supplier) {
		setAuditDetails(supplier);
		return supplierRepository.save(supplier);
	}

	@Transactional
	public void saveAccountDetailKey(Supplier supplier) {

		Accountdetailkey accountdetailkey = new Accountdetailkey();
		accountdetailkey.setDetailkey(supplier.getId().intValue());
		accountdetailkey.setDetailname(supplier.getName());
		accountdetailkey.setAccountdetailtype(accountdetailtypeService.findByName(supplier.getClass().getSimpleName()));
		accountdetailkey.setGroupid(1);
		accountDetailKeyService.create(accountdetailkey);
	}

	private void setAuditDetails(Supplier supplier) {
		if (supplier.getId() == null) {
			supplier.setCreatedDate(new Date());
			supplier.setCreatedBy(ApplicationThreadLocals.getUserId());
		}
		supplier.setLastModifiedDate(new Date());
		supplier.setLastModifiedBy(ApplicationThreadLocals.getUserId());
	}

	public List<Supplier> search(final SupplierSearchRequest supplierSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Supplier> createQuery = cb.createQuery(Supplier.class);
		final Root<Supplier> suppliers = createQuery.from(Supplier.class);
		createQuery.select(suppliers);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<Supplier> supplierEntityType = m.entity(Supplier.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (supplierSearchRequest.getName() != null) {
			final String name = "%" + supplierSearchRequest.getName().toLowerCase() + "%";
			predicates.add(cb.isNotNull(suppliers.get("name")));
			predicates.add(cb.like(
					cb.lower(suppliers.get(supplierEntityType.getDeclaredSingularAttribute("name", String.class))),
					name));
		}
		if (supplierSearchRequest.getCode() != null) {
			final String code = "%" + supplierSearchRequest.getCode().toLowerCase() + "%";
			predicates.add(cb.isNotNull(suppliers.get("code")));
			predicates.add(cb.like(
					cb.lower(suppliers.get(supplierEntityType.getDeclaredSingularAttribute("code", String.class))),
					code));
		}

		createQuery.where(predicates.toArray(new Predicate[] {}));
		createQuery.orderBy(cb.asc(suppliers.get("name")));
		final TypedQuery<Supplier> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}

	public List<Supplier> getAllActiveSuppliers() {
		return supplierRepository.findByStatus();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> getAllActiveEntities(Integer accountDetailTypeId) {
		return supplierRepository.findByStatus();
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> filterActiveEntities(String filterKey, int maxRecords,
			Integer accountDetailTypeId) {
		return supplierRepository.findByNameLikeIgnoreCaseOrCodeLikeIgnoreCase(filterKey + "%", filterKey + "%");
	}

	@Override
	public List<? extends org.egov.commons.utils.EntityType> getAssetCodesForProjectCode(Integer accountdetailkey)
			throws ValidationException {
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
	
	public List<String> getSupplierAuditReport(final String supplierId) {
		
		List<SupplierAudit> resultList = getSupplierAuditList(supplierId);
		
		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;
		
		for (int i = 0; i < resultList.size() - 1; i++) {
			
			SupplierAudit previousModifiedRow = resultList.get(i);
			SupplierAudit currentModifiedRow = resultList.get(i+1);
			String modifications = "";
			
			if (!previousModifiedRow.getName().equals(currentModifiedRow.getName())) {
				modifications = modifications + "Name : " + previousModifiedRow.getName() + " --> "
						+ currentModifiedRow.getName() + "<br>";
			}
			if (!previousModifiedRow.getCode().equals(currentModifiedRow.getCode())) {
				modifications = modifications + "Code : " + previousModifiedRow.getCode() + " --> "
						+ currentModifiedRow.getCode() + "<br>";
			}
			if (!previousModifiedRow.getCorrespondenceAddress().equals(currentModifiedRow.getCorrespondenceAddress())) {
				modifications = modifications + "Correspondence Address : " + previousModifiedRow.getCorrespondenceAddress() + " --> "
						+ currentModifiedRow.getCorrespondenceAddress() + "<br>";
			}
			if (!previousModifiedRow.getPaymentAddress().equals(currentModifiedRow.getPaymentAddress())) {
				modifications = modifications + "Permanent Address : " + previousModifiedRow.getPaymentAddress() + " --> "
						+ currentModifiedRow.getPaymentAddress() + "<br>";
			}
			if (!previousModifiedRow.getContactPerson().equals(currentModifiedRow.getContactPerson())) {
				modifications = modifications + "Contact Person : " + previousModifiedRow.getContactPerson() + " --> "
						+ currentModifiedRow.getContactPerson() + "<br>";
			}
			if (!previousModifiedRow.getEmail().equals(currentModifiedRow.getEmail())) {
				modifications = modifications + "Email : " + previousModifiedRow.getEmail() + " --> "
						+ currentModifiedRow.getEmail() + "<br>";
			}
			if (!previousModifiedRow.getNarration().equals(currentModifiedRow.getNarration())) {
				modifications = modifications + "Narration : " + previousModifiedRow.getNarration() + " --> "
						+ currentModifiedRow.getNarration() + "<br>";
			}
			if (!previousModifiedRow.getPanNumber().equals(currentModifiedRow.getPanNumber())) {
				modifications = modifications + "Pan # : " + previousModifiedRow.getPanNumber() + " --> "
						+ currentModifiedRow.getPanNumber() + "<br>";
			}
			if (!previousModifiedRow.getTinNumber().equals(currentModifiedRow.getTinNumber())) {
				modifications = modifications + "Tin # : " + previousModifiedRow.getTinNumber() + " --> "
						+ currentModifiedRow.getTinNumber() + "<br>";
			}
			if (!previousModifiedRow.getMobileNumber().equals(currentModifiedRow.getMobileNumber())) {
				modifications = modifications + "Mobile # : " + previousModifiedRow.getMobileNumber() + " --> "
						+ currentModifiedRow.getMobileNumber() + "<br>";
			}
			if (!previousModifiedRow.getBank().equals(currentModifiedRow.getBank())) {
				modifications = modifications + "Bank : " + previousModifiedRow.getBank() + " --> "
						+ currentModifiedRow.getBank() + "<br>";
			}
			if (!previousModifiedRow.getIfscCode().equals(currentModifiedRow.getIfscCode())) {
				modifications = modifications + "IFSC code : " + previousModifiedRow.getIfscCode() + " --> "
						+ currentModifiedRow.getIfscCode() + "<br>";
			}
			if (!previousModifiedRow.getBankAccount().equals(currentModifiedRow.getBankAccount())) {
				modifications = modifications + "Bank Account : " + previousModifiedRow.getBankAccount() + " --> "
						+ currentModifiedRow.getBankAccount() + "<br>";
			}
			if (!previousModifiedRow.getRegistrationNumber().equals(currentModifiedRow.getRegistrationNumber())) {
				modifications = modifications + "Registration Number : " + previousModifiedRow.getRegistrationNumber() + " --> "
						+ currentModifiedRow.getRegistrationNumber() + "<br>";
			}
			if (!previousModifiedRow.getStatus().equals(currentModifiedRow.getStatus())) {
				modifications = modifications + "Status : " + previousModifiedRow.getStatus() + " --> "
						+ currentModifiedRow.getStatus() + "<br>";
			}
			if (!previousModifiedRow.getEpfNumber().equals(currentModifiedRow.getEpfNumber())) {
				modifications = modifications + "EPF Number : " + previousModifiedRow.getEpfNumber() + " --> "
						+ currentModifiedRow.getEpfNumber() + "<br>";
			}
			if (!previousModifiedRow.getEsiNumber().equals(currentModifiedRow.getEsiNumber())) {
				modifications = modifications + "ESI Number : " + previousModifiedRow.getEsiNumber() + " --> "
						+ currentModifiedRow.getEsiNumber() + "<br>";
			}
			if (!previousModifiedRow.getGstRegisteredState().equals(currentModifiedRow.getGstRegisteredState())) {
				modifications = modifications + "GST : " + previousModifiedRow.getGstRegisteredState() + " --> "
						+ currentModifiedRow.getGstRegisteredState() + "<br>";
			}
			
			String previousSupplierType = previousModifiedRow.getSupplierType().toLowerCase();
			String currentSupplierType = currentModifiedRow.getSupplierType().toLowerCase();
			
			if (!previousSupplierType.equals(currentSupplierType)) {
				modifications = modifications + "Supplier Type : " + previousModifiedRow.getSupplierType() + " --> "
						+ currentModifiedRow.getSupplierType() + "<br>";
			}
			
			if (modifications.length() > 0) {
				modificationsList.add("User : "+currentModifiedRow.getNameOfmodifyingUser() + "<br>"
						+ "Modification date-time : " +currentModifiedRow.getLastModifiedDate() + "<br><br>" + modifications);
			}
			
		}
		
		return modificationsList;
	}
	
	private List<SupplierAudit> getSupplierAuditList(final String supplierId){
		
		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select efg_sup_aud.id, efg_sup_aud.code, efg_sup_aud.name, efg_sup_aud.correspondenceaddress, efg_sup_aud.paymentaddress, efg_sup_aud.contactperson, ")
				.append(" efg_sup_aud.email, efg_sup_aud.narration, efg_sup_aud.pannumber, efg_sup_aud.tinnumber, efg_sup_aud.mobilenumber, ")
				.append(" efg_sup_aud.ifsccode, efg_sup_aud.bankaccount, efg_sup_aud.registrationnumber, efg_sup_aud.lastmodifiedby, ")
				.append(" efg_sup_aud.lastmodifieddate, efg_sup_aud.epfnumber, efg_sup_aud.esinumber, efg_sup_aud.gstregisteredstate, efg_sup_aud.suppliertype, efg_sup_aud.nameofmodifyinguser, ")
				.append(" bnk.name as bankName, egws.description ")
				.append(" from egf_supplier_aud efg_sup_aud LEFT JOIN bank bnk ON efg_sup_aud.bank=bnk.id LEFT JOIN egw_status egws ON egws.id=efg_sup_aud.status where efg_sup_aud.id=:supplierId order by efg_sup_aud.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("supplierId", Long.valueOf(supplierId));
		final List<Object[]> supplierAuditListFromQuery = queryResult.list();
		
		List<SupplierAudit> supplierAuditList = new ArrayList<SupplierAudit>();
		
		for (Object[] obj : supplierAuditListFromQuery) {
			SupplierAudit element = new SupplierAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setCode(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setName(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setCorrespondenceAddress(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			element.setPaymentAddress(String.valueOf(obj[4] != null ? obj[4] : AuditReportUtils.NO_VALUE));
			element.setContactPerson(String.valueOf(obj[5] != null ? obj[5] : AuditReportUtils.NO_VALUE));
			element.setEmail(String.valueOf(obj[6] != null ? obj[6] : AuditReportUtils.NO_VALUE));
			element.setNarration(String.valueOf(obj[7] != null ? obj[7] : AuditReportUtils.NO_VALUE));
			element.setPanNumber(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			element.setTinNumber(String.valueOf(obj[9] != null ? obj[9] : AuditReportUtils.NO_VALUE));
			element.setMobileNumber(String.valueOf(obj[10] != null ? obj[10] : AuditReportUtils.NO_VALUE));
			element.setIfscCode(String.valueOf(obj[11] != null ? obj[11] : AuditReportUtils.NO_VALUE));
			element.setBankAccount(String.valueOf(obj[12] != null ? obj[12] : AuditReportUtils.NO_VALUE));
			element.setRegistrationNumber(String.valueOf(obj[13] != null ? obj[13] : AuditReportUtils.NO_VALUE));
			element.setLastModifiedBy(String.valueOf(obj[14] != null ? obj[14] : AuditReportUtils.NO_VALUE));
			String lastModifiedDate = String.valueOf(obj[15] != null ? obj[15] : "");	
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));
			element.setEpfNumber(String.valueOf(obj[16] != null ? obj[16] : AuditReportUtils.NO_VALUE));
			element.setEsiNumber(String.valueOf(obj[17] != null ? obj[17] : AuditReportUtils.NO_VALUE));
			element.setGstRegisteredState(String.valueOf(obj[18] != null ? obj[18] : AuditReportUtils.NO_VALUE));
			element.setSupplierType(String.valueOf(obj[19] != null ? obj[19] : AuditReportUtils.NO_VALUE));
			element.setNameOfmodifyingUser(String.valueOf(obj[20] != null ? obj[20] : AuditReportUtils.NO_VALUE));
			element.setBank(String.valueOf(obj[21] != null ? obj[21] : AuditReportUtils.NO_VALUE));
			String isActive = String.valueOf(obj[22] != null ? obj[22] : AuditReportUtils.NO_VALUE);
			element.setStatus(isActive);
			supplierAuditList.add(element);
			element = null;
		}		
		
		return supplierAuditList;
	}	
	
}

















