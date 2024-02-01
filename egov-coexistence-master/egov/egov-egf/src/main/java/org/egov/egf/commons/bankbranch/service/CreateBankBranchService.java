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
package org.egov.egf.commons.bankbranch.service;

import java.util.ArrayList;
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

import org.egov.commons.BankAudit;
import org.egov.commons.Bankbranch;
import org.egov.commons.BankbranchAudit;
import org.egov.commons.contracts.BankBranchSearchRequest;
import org.egov.egf.commons.bankbranch.repository.BankBranchRepository;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infstr.services.PersistenceService;
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
public class CreateBankBranchService {
	
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@PersistenceContext
	private EntityManager entityManager;

	private final BankBranchRepository bankBranchRepository;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	@Autowired
	public CreateBankBranchService(final BankBranchRepository bankBranchRepository) {
		this.bankBranchRepository = bankBranchRepository;
	}

	public Bankbranch getById(final Integer id) {
		return bankBranchRepository.findOne(id);
	}

	public List<Bankbranch> getByBankId(final Integer bankId) {
		return bankBranchRepository.findByBank_Id(bankId);
	}

	public List<Bankbranch> getByIsActive(final Boolean isActive) {
		return bankBranchRepository.findByIsactive(isActive);
	}

	public List<Bankbranch> getByIsActiveTrueOrderByBranchname() {
		return bankBranchRepository.findByIsactiveTrueOrderByBranchnameAsc();
	}

	@Transactional
	public Bankbranch create(final Bankbranch bankBranch) {

		bankBranch.setCreatedDate(new Date());
		bankBranch.setCreatedBy(ApplicationThreadLocals.getUserId());

		return bankBranchRepository.save(bankBranch);
	}

	@Transactional
	public Bankbranch update(final Bankbranch bankbranch) {

		bankbranch.setLastModifiedDate(new Date());
		bankbranch.setLastModifiedBy(ApplicationThreadLocals.getUserId());
		return bankBranchRepository.save(bankbranch);
	}

	public List<Bankbranch> search(final BankBranchSearchRequest bankBranchSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Bankbranch> createQuery = cb.createQuery(Bankbranch.class);
		final Root<Bankbranch> bankBranchs = createQuery.from(Bankbranch.class);
		createQuery.select(bankBranchs);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<Bankbranch> tempBankBranch = m.entity(Bankbranch.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (bankBranchSearchRequest.getBranchcode() != null) {
			final String code = "%" + bankBranchSearchRequest.getBranchcode().toLowerCase() + "%";
			predicates.add(cb.isNotNull(bankBranchs.get("branchcode")));
			predicates.add(cb.like(
					cb.lower(bankBranchs.get(tempBankBranch.getDeclaredSingularAttribute("branchcode", String.class))),
					code));
		}

		if (bankBranchSearchRequest.getBranchMICR() != null)
			predicates.add(cb.equal(bankBranchs.get("branchMICR"), bankBranchSearchRequest.getBranchMICR()));

		if (bankBranchSearchRequest.getBranchaddress1() != null)
			predicates.add(cb.equal(bankBranchs.get("branchaddress1"), bankBranchSearchRequest.getBranchaddress1()));

		if (bankBranchSearchRequest.getContactperson() != null)
			predicates.add(cb.equal(bankBranchs.get("contactperson"), bankBranchSearchRequest.getContactperson()));

		if (bankBranchSearchRequest.getBranchphone() != null)
			predicates.add(cb.equal(bankBranchs.get("branchphone"), bankBranchSearchRequest.getBranchphone()));

		if (bankBranchSearchRequest.getNarration() != null)
			predicates.add(cb.equal(bankBranchs.get("narration"), bankBranchSearchRequest.getNarration()));

		if (bankBranchSearchRequest.getIsactive().booleanValue())
			predicates.add(cb.equal(bankBranchs.get("isactive"), true));

		if (bankBranchSearchRequest.getBankId() != null)
			predicates.add(cb.equal(bankBranchs.get("bank").get("id"), bankBranchSearchRequest.getBankId()));

		if (bankBranchSearchRequest.getBankBranchId() != null)
			predicates.add(cb.equal(bankBranchs.get("id"), bankBranchSearchRequest.getBankBranchId()));

		createQuery.where(predicates.toArray(new Predicate[] {}));
		final TypedQuery<Bankbranch> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}
	
	
	public List<String> getBankBranchAuditReport(final String bankBranchId) {

		List<BankbranchAudit> resultList = getBankBranchAuditList(bankBranchId);

		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			BankbranchAudit previousModifiedRow = resultList.get(i);
			BankbranchAudit currentModifiedRow = resultList.get(i + 1);
			String modifications = "";

			if (!previousModifiedRow.getBranchname().equals(currentModifiedRow.getBranchname())) {
				modifications = modifications + "Branch Name : " + previousModifiedRow.getBranchname() + " --> "
						+ currentModifiedRow.getBranchname() + "<br>";
			}
			if (!previousModifiedRow.getBankName().equals(currentModifiedRow.getBankName())) {
				modifications = modifications + "Bank Name : " + previousModifiedRow.getBankName() + " --> "
						+ currentModifiedRow.getBankName() + "<br>";
			}
			if (!previousModifiedRow.getBranchcode().equals(currentModifiedRow.getBranchcode())) {
				modifications = modifications + "Branch Code : " + previousModifiedRow.getBranchcode() + " --> "
						+ currentModifiedRow.getBranchcode() + "<br>";
			}			
			if (!previousModifiedRow.getNarration().equals(currentModifiedRow.getNarration())) {
				modifications = modifications + "Narration : " + previousModifiedRow.getNarration()
						+ " --> " + currentModifiedRow.getNarration() + "<br>";
			}
			if (!previousModifiedRow.getBranchaddress1().equals(currentModifiedRow.getBranchaddress1())) {
				modifications = modifications + "Address 1 : " + previousModifiedRow.getBranchaddress1() + " --> "
						+ currentModifiedRow.getBranchaddress1() + "<br>";
			}
			if (!previousModifiedRow.getBranchaddress2().equals(currentModifiedRow.getBranchaddress2())) {
				modifications = modifications + "Address 2 : " + previousModifiedRow.getBranchaddress2() + " --> "
						+ currentModifiedRow.getBranchaddress2() + "<br>";
			}
			if (!previousModifiedRow.getBranchcity().equals(currentModifiedRow.getBranchcity())) {
				modifications = modifications + "City : " + previousModifiedRow.getBranchcity() + " --> "
						+ currentModifiedRow.getBranchcity() + "<br>";
			}
			if (!previousModifiedRow.getBranchstate().equals(currentModifiedRow.getBranchstate())) {
				modifications = modifications + "State : " + previousModifiedRow.getBranchstate() + " --> "
						+ currentModifiedRow.getBranchstate() + "<br>";
			}
			if (!previousModifiedRow.getBranchpin().equals(currentModifiedRow.getBranchpin())) {
				modifications = modifications + "Pin : " + previousModifiedRow.getBranchpin() + " --> "
						+ currentModifiedRow.getBranchpin() + "<br>";
			}
			if (!previousModifiedRow.getBranchphone().equals(currentModifiedRow.getBranchphone())) {
				modifications = modifications + "Phone : " + previousModifiedRow.getBranchphone() + " --> "
						+ currentModifiedRow.getBranchphone() + "<br>";
			}
			if (!previousModifiedRow.getBranchfax().equals(currentModifiedRow.getBranchfax())) {
				modifications = modifications + "Fax : " + previousModifiedRow.getBranchfax() + " --> "
						+ currentModifiedRow.getBranchfax() + "<br>";
			}
			if (!previousModifiedRow.getContactperson().equals(currentModifiedRow.getContactperson())) {
				modifications = modifications + "Contact Person : " + previousModifiedRow.getContactperson() + " --> "
						+ currentModifiedRow.getContactperson() + "<br>";
			}
			if (!previousModifiedRow.getBranchMICR().equals(currentModifiedRow.getBranchMICR())) {
				modifications = modifications + "MICR : " + previousModifiedRow.getBranchMICR() + " --> "
						+ currentModifiedRow.getBranchMICR() + "<br>";
			}			
			if (!previousModifiedRow.getIsactive().equals(currentModifiedRow.getIsactive())) {
				modifications = modifications + "Active : " + previousModifiedRow.getIsactive() + " --> "
						+ currentModifiedRow.getIsactive();
			}		

			if (modifications.length() > 0) {
								
				modificationsList.add(
						"User : " + currentModifiedRow.getNameOfmodifyingUser() + "<br>" + "Modified On : "
								+ currentModifiedRow.getLastModifiedDate() + "<br><br>" + modifications);
			}

		}

		return modificationsList;
	}

	private List<BankbranchAudit> getBankBranchAuditList(final String bankBranchId) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append(
				"select bb_aud.id, bb_aud.branchcode, bb_aud.branchname, bb_aud.branchaddress1, bb_aud.branchaddress2, bb_aud.branchcity, bb_aud.branchstate, ")
				.append("bb_aud.branchpin, bb_aud.branchphone, bb_aud.branchfax, bb_aud.contactperson, bb_aud.isactive, bb_aud.narration, bb_aud.micr, ")
				.append("bb_aud.lastmodifieddate, bb_aud.lastmodifiedby, bb_aud.nameofmodifyinguser, bank.name ")
				.append("from bankbranch_aud bb_aud LEFT JOIN bank bank ON bb_aud.bankid = bank.id where bb_aud.id=:bankBranchId order by bb_aud.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("bankBranchId", Long.valueOf(bankBranchId));
		final List<Object[]> bankBranchAuditListFromQuery = queryResult.list();

		List<BankbranchAudit> bankBranchAuditList = new ArrayList<BankbranchAudit>();

		for (Object[] obj : bankBranchAuditListFromQuery) {
			BankbranchAudit element = new BankbranchAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setBranchcode(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setBranchname(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setBranchaddress1(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			element.setBranchaddress2(String.valueOf(obj[4] != null ? obj[4] : AuditReportUtils.NO_VALUE));
			element.setBranchcity(String.valueOf(obj[5] != null ? obj[5] : AuditReportUtils.NO_VALUE));
			element.setBranchstate(String.valueOf(obj[6] != null ? obj[6] : AuditReportUtils.NO_VALUE));
			element.setBranchpin(String.valueOf(obj[7] != null ? obj[7] : AuditReportUtils.NO_VALUE));
			element.setBranchphone(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			element.setBranchfax(String.valueOf(obj[9] != null ? obj[9] : AuditReportUtils.NO_VALUE));
			element.setContactperson(String.valueOf(obj[10] != null ? obj[10] : AuditReportUtils.NO_VALUE));
			String isActive = (obj[11] != null && (Boolean)obj[11].equals(true)) ? "Active" : "Inactive"; 
			element.setIsactive(isActive);
			element.setNarration(String.valueOf(obj[12] != null ? obj[12] : AuditReportUtils.NO_VALUE));			
			element.setBranchMICR(String.valueOf(obj[13] != null ? obj[13] : AuditReportUtils.NO_VALUE));
			String lastModifiedDate = String.valueOf(obj[14] != null ? obj[14] : "");			
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));
			element.setLastModifiedBy(String.valueOf(obj[15] != null ? obj[15] : AuditReportUtils.NO_VALUE));
			element.setNameOfmodifyingUser(String.valueOf(obj[16] != null ? obj[16] : AuditReportUtils.NO_VALUE));
			element.setBankName(String.valueOf(obj[17] != null ? obj[17] : AuditReportUtils.NO_VALUE));
			bankBranchAuditList.add(element);
			element = null;
		}

		return bankBranchAuditList;
	}

}
