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
package org.egov.egf.commons.bank.service;

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

import org.egov.commons.Bank;
import org.egov.commons.BankAudit;
import org.egov.commons.contracts.BankSearchRequest;
import org.egov.egf.commons.bank.repository.BankRepository;
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
public class CreateBankService {

	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@PersistenceContext
	private EntityManager entityManager;

	private final BankRepository bankRepository;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	@Autowired
	public CreateBankService(final BankRepository bankRepository) {
		this.bankRepository = bankRepository;
	}

	public Bank getById(final Integer id) {
		return bankRepository.findOne(id);
	}

	public List<Bank> getByIsActive(final Boolean isActive) {
		return bankRepository.findByIsactive(isActive);
	}

	public List<Bank> getByIsActiveTrueOrderByName() {
		return bankRepository.findByIsactiveTrueOrderByNameAsc();
	}

	@Transactional
	public Bank create(final Bank bank) {

		bank.setCreatedDate(new Date());
		bank.setCreatedBy(ApplicationThreadLocals.getUserId());

		return bankRepository.save(bank);
	}

	@Transactional
	public Bank update(final Bank bank) {

		bank.setLastModifiedDate(new Date());
		bank.setLastModifiedBy(ApplicationThreadLocals.getUserId());
		return bankRepository.save(bank);
	}

	public List<Bank> search(final BankSearchRequest bankSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Bank> createQuery = cb.createQuery(Bank.class);
		final Root<Bank> banks = createQuery.from(Bank.class);
		createQuery.select(banks);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<Bank> tempBank = m.entity(Bank.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (bankSearchRequest.getName() != null) {
			final String name = "%" + bankSearchRequest.getName().toLowerCase() + "%";
			predicates.add(cb.isNotNull(banks.get("name")));
			predicates.add(
					cb.like(cb.lower(banks.get(tempBank.getDeclaredSingularAttribute("name", String.class))), name));
		}
		if (bankSearchRequest.getCode() != null) {
			final String code = "%" + bankSearchRequest.getCode().toLowerCase() + "%";
			predicates.add(cb.isNotNull(banks.get("code")));
			predicates.add(
					cb.like(cb.lower(banks.get(tempBank.getDeclaredSingularAttribute("code", String.class))), code));
		}
		if (bankSearchRequest.getIsactive().booleanValue())
			predicates.add(cb.equal(banks.get("isactive"), true));
		if (bankSearchRequest.getNarration() != null)
			predicates.add(cb.equal(banks.get("narration"), bankSearchRequest.getNarration()));

		createQuery.where(predicates.toArray(new Predicate[] {}));
		final TypedQuery<Bank> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}

	public List<Bank> getAll() {
		return bankRepository.findAll();
	}

	public List<String> getBankAuditReport(final String bankId) {

		List<BankAudit> resultList = getBankAuditList(bankId);

		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			BankAudit previousModifiedRow = resultList.get(i);
			BankAudit currentModifiedRow = resultList.get(i + 1);
			String modifications = "";

			if (!previousModifiedRow.getName().equals(currentModifiedRow.getName())) {
				modifications = modifications + "<strong>" + "Name : " + "</strong>" + previousModifiedRow.getName() + " --> "
						+ currentModifiedRow.getName() + "<br>";
			}
			if (!previousModifiedRow.getCode().equals(currentModifiedRow.getCode())) {
				modifications = modifications + "Code : " + previousModifiedRow.getCode() + " --> "
						+ currentModifiedRow.getCode() + "<br>";
			}			
			if (!previousModifiedRow.getNarration().equals(currentModifiedRow.getNarration())) {
				modifications = modifications + "Narration : " + previousModifiedRow.getNarration()
						+ " --> " + currentModifiedRow.getNarration() + "<br>";
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

	private List<BankAudit> getBankAuditList(final String bankId) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append(
				"select bnk.id, bnk.code, bnk.name, bnk.narration, bnk.isactive, bnk.type, bnk.lastmodifieddate, bnk.lastmodifiedby, bnk.nameofmodifyinguser ")
				.append(" from bank_aud bnk where bnk.id=:bankId order by bnk.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("bankId", Long.valueOf(bankId));
		final List<Object[]> bankAuditListFromQuery = queryResult.list();

		List<BankAudit> bankAuditList = new ArrayList<BankAudit>();

		for (Object[] obj : bankAuditListFromQuery) {
			BankAudit element = new BankAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setCode(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setName(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setNarration(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			String isActive = (obj[4] != null && (Boolean)obj[4].equals(true)) ? "Active" : "Inactive"; 
			element.setIsactive(String.valueOf(isActive));
			element.setType(String.valueOf(obj[5] != null ? obj[5] : AuditReportUtils.NO_VALUE));
			String lastModifiedDate = String.valueOf(obj[6] != null ? obj[6] : "");			
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));
			element.setLastModifiedBy(String.valueOf(obj[7] != null ? obj[7] : AuditReportUtils.NO_VALUE));
			element.setNameOfmodifyingUser(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			bankAuditList.add(element);
			element = null;
		}

		return bankAuditList;
	}

}
