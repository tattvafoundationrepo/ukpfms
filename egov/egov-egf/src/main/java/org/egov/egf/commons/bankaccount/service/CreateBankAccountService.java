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
package org.egov.egf.commons.bankaccount.service;

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

import org.egov.commons.BankaccountAudit;
import org.egov.commons.Bankaccount;
import org.egov.commons.Bankbranch;
import org.egov.commons.CChartOfAccounts;
import org.egov.commons.contracts.BankAccountSearchRequest;
import org.egov.commons.service.ChartOfAccountsService;
import org.egov.egf.commons.bankaccount.repository.BankAccountRepository;
import org.egov.egf.commons.bankbranch.service.CreateBankBranchService;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infstr.services.PersistenceService;
import org.egov.infstr.utils.EGovConfig;
import org.egov.utils.FinancialConstants;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exilant.GLEngine.CoaCache;

/**
 * @author venki
 */

@SuppressWarnings("deprecation")
@Service
@Transactional(readOnly = true)
public class CreateBankAccountService {

	private final String code = EGovConfig.getProperty("egf_config.xml", "glcodeMaxLength", "", "AccountCode");
	
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@PersistenceContext
	private EntityManager entityManager;

	private final BankAccountRepository bankAccountRepository;

	@Autowired
	private AppConfigValueService appConfigValuesService;

	@Autowired
	@Qualifier(value = "chartOfAccountsService")
	private ChartOfAccountsService chartOfAccountsService;

	@Autowired
	private CreateBankBranchService createBankBranchService;

	@Autowired
	private CoaCache coaCache;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	@Autowired
	public CreateBankAccountService(final BankAccountRepository bankAccountRepository) {
		this.bankAccountRepository = bankAccountRepository;
	}

	public Bankaccount getById(final Long id) {
		return bankAccountRepository.findOne(id);
	}

	public Bankaccount getByGlcode(final String glcode) {
		return bankAccountRepository.findByChartofaccounts_Glcode(glcode);
	}

	public List<Bankaccount> getByBranchId(final Integer branchId) {
		return bankAccountRepository.findByBankbranch_Id(branchId);
	}

	@Transactional
	public Bankaccount create(final Bankaccount bankaccount) {
		String newGLCode;
		if (autoBankAccountGLCodeEnabled().booleanValue()) {
			if (!bankaccount.getAccounttype().isEmpty()) {
				final CChartOfAccounts coa = chartOfAccountsService
						.getByGlCode(bankaccount.getAccounttype().split("-")[0].trim());
				newGLCode = prepareBankAccCode(bankaccount.getAccounttype().split("-")[0].trim(), code);
				final Long coaID = postInChartOfAccounts(newGLCode, coa.getId(), bankaccount.getAccountnumber(),
						bankaccount.getBankbranch().getId());
				if (coaID != null) {
					final CChartOfAccounts chartofaccounts = chartOfAccountsService.getById(coaID);
					bankaccount.setChartofaccounts(chartofaccounts);
				}
			}
		} else if (bankaccount.getChartofaccounts() != null && !bankaccount.getChartofaccounts().getGlcode().isEmpty())
			bankaccount.setChartofaccounts(
					chartOfAccountsService.getByGlCode(bankaccount.getChartofaccounts().getGlcode()));
		bankaccount.setCreatedDate(new Date());
		bankaccount.setCreatedBy(ApplicationThreadLocals.getUserId());
		return bankAccountRepository.save(bankaccount);
	}

	@Transactional
	public Bankaccount update(final Bankaccount bankaccount) {

		bankaccount.setLastModifiedDate(new Date());
		bankaccount.setLastModifiedBy(ApplicationThreadLocals.getUserId());
		return bankAccountRepository.save(bankaccount);
	}

	public List<Bankaccount> search(final BankAccountSearchRequest bankAccountSearchRequest) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Bankaccount> createQuery = cb.createQuery(Bankaccount.class);
		final Root<Bankaccount> bankaccounts = createQuery.from(Bankaccount.class);
		createQuery.select(bankaccounts);
		final Metamodel m = entityManager.getMetamodel();
		final EntityType<Bankaccount> tempBankaccount = m.entity(Bankaccount.class);

		final List<Predicate> predicates = new ArrayList<>();
		if (bankAccountSearchRequest.getAccountnumber() != null) {
			final String accountnumber = "%" + bankAccountSearchRequest.getAccountnumber().toLowerCase() + "%";
			predicates.add(cb.isNotNull(bankaccounts.get("accountnumber")));
			predicates.add(cb.like(
					cb.lower(bankaccounts
							.get(tempBankaccount.getDeclaredSingularAttribute("accountnumber", String.class))),
					accountnumber));
		}

		if (bankAccountSearchRequest.getFundId() != null)
			predicates.add(cb.equal(bankaccounts.get("fund").get("id"), bankAccountSearchRequest.getFundId()));

		if (bankAccountSearchRequest.getBankId() != null)
			predicates.add(cb.equal(bankaccounts.get("bankbranch").get("bank").get("id"),
					bankAccountSearchRequest.getBankId()));

		if (bankAccountSearchRequest.getBankbranchId() != null)
			predicates.add(
					cb.equal(bankaccounts.get("bankbranch").get("id"), bankAccountSearchRequest.getBankbranchId()));

		if (bankAccountSearchRequest.getGlcode() != null)
			predicates.add(
					cb.equal(bankaccounts.get("chartofaccounts").get("glcode"), bankAccountSearchRequest.getGlcode()));

		if (bankAccountSearchRequest.getAccounttype() != null)
			predicates.add(cb.equal(bankaccounts.get("accounttype"), bankAccountSearchRequest.getAccounttype()));

		if (bankAccountSearchRequest.getPayTo() != null)
			predicates.add(cb.equal(bankaccounts.get("payTo"), bankAccountSearchRequest.getPayTo()));

		if (bankAccountSearchRequest.getType() != null)
			predicates.add(cb.equal(bankaccounts.get("type"), bankAccountSearchRequest.getType()));

		if (bankAccountSearchRequest.getNarration() != null)
			predicates.add(cb.equal(bankaccounts.get("narration"), bankAccountSearchRequest.getNarration()));

		if (bankAccountSearchRequest.getIsactive().booleanValue())
			predicates.add(cb.equal(bankaccounts.get("isactive"), true));

		createQuery.where(predicates.toArray(new Predicate[] {}));
		final TypedQuery<Bankaccount> query = entityManager.createQuery(createQuery);
		return query.getResultList();

	}

	public Boolean autoBankAccountGLCodeEnabled() {
		final AppConfigValues appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
				FinancialConstants.MODULE_NAME_APPCONFIG, FinancialConstants.APPCONFIG_AUTO_BANKACCOUNT_GLCODE).get(0);
		return "YES".equalsIgnoreCase(appConfigValue.getValue());
	}

	@SuppressWarnings("deprecation")
	public String prepareBankAccCode(final String glCode, final String code) {
		String newGlCode;
		Long glcode;
		Long tempCode;
		final String subminorvalue = EGovConfig.getProperty("egf_config.xml", "subminorvalue", "", "AccountCode");
		newGlCode = glCode.substring(0, Integer.parseInt(subminorvalue));
		final CChartOfAccounts coa = chartOfAccountsService.getByGlCodeDesc(newGlCode);
		newGlCode = coa.getGlcode();
		final String zero = EGovConfig.getProperty("egf_config.xml", "zerofill", "", "AccountCode");
		if (newGlCode.length() == Integer.parseInt(code)) {
			glcode = Long.parseLong(newGlCode);
			tempCode = glcode + 1;
		} else {
			newGlCode = newGlCode + zero;
			glcode = Long.parseLong(newGlCode);
			tempCode = glcode + 1;
		}
		newGlCode = Long.toString(tempCode);
		return newGlCode;
	}

	public Long postInChartOfAccounts(final String glCode, final Long parentId, final String accNumber,
			final Integer branchId) {
		final Bankbranch bankBranch = createBankBranchService.getById(branchId);
		int majorCodeLength;
		majorCodeLength = Integer
				.valueOf(appConfigValuesService.getConfigValuesByModuleAndKey(FinancialConstants.MODULE_NAME_APPCONFIG,
						FinancialConstants.APPCONFIG_COA_MAJORCODE_LENGTH).get(0).getValue());
		final CChartOfAccounts chart = new CChartOfAccounts();
		chart.setGlcode(glCode);
		chart.setName(bankBranch.getBank().getName() + " " + bankBranch.getBranchname() + " " + accNumber);
		chart.setParentId(parentId);
		chart.setType('A');
		chart.setClassification(Long.parseLong("4"));
		chart.setIsActiveForPosting(true);
		chart.setMajorCode(chart.getGlcode().substring(0, majorCodeLength));
		chartOfAccountsService.persist(chart);
		return chart.getId();
	}

	public void clearCache() {
		coaCache.reLoad();
	}
	
	public List<String> getBankAccountAuditReport(final String bankAccountId) {

		List<BankaccountAudit> resultList = getBankAccountAuditList(bankAccountId);

		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			BankaccountAudit previousModifiedRow = resultList.get(i);
			BankaccountAudit currentModifiedRow = resultList.get(i + 1);
			String modifications = "";

			if (!previousModifiedRow.getBankbranch().equals(currentModifiedRow.getBankbranch())) {
				modifications = modifications + "Bank Branch : " + previousModifiedRow.getBankbranch() + " --> "
						+ currentModifiedRow.getBankbranch() + "<br>";
			}
			if (!previousModifiedRow.getBank().equals(currentModifiedRow.getBank())) {
				modifications = modifications + "Bank : " + previousModifiedRow.getBank() + " --> "
						+ currentModifiedRow.getBank() + "<br>";
			}
			if (!previousModifiedRow.getAccountnumber().equals(currentModifiedRow.getAccountnumber())) {
				modifications = modifications + "Account # : " + previousModifiedRow.getAccountnumber() + " --> "
						+ currentModifiedRow.getAccountnumber() + "<br>";
			}
			if (!previousModifiedRow.getAccounttype().equals(currentModifiedRow.getAccounttype())) {
				modifications = modifications + "Account Type : " + previousModifiedRow.getAccounttype() + " --> "
						+ currentModifiedRow.getAccounttype() + "<br>";
			}
			if (!previousModifiedRow.getFund().equals(currentModifiedRow.getFund())) {
				modifications = modifications + "Fund : " + previousModifiedRow.getFund() + " --> "
						+ currentModifiedRow.getFund() + "<br>";
			}
			if (!previousModifiedRow.getPayTo().equals(currentModifiedRow.getPayTo())) {
				modifications = modifications + "Pay To : " + previousModifiedRow.getPayTo() + " --> "
						+ currentModifiedRow.getPayTo() + "<br>";
			}
			if (!previousModifiedRow.getType().equals(currentModifiedRow.getType())) {
				modifications = modifications + "Usage Type : " + previousModifiedRow.getType() + " --> "
						+ currentModifiedRow.getType() + "<br>";
			}
			if (!previousModifiedRow.getChequeformat().equals(currentModifiedRow.getChequeformat())) {
				modifications = modifications + "Cheque Format : " + previousModifiedRow.getChequeformat() + " --> "
						+ currentModifiedRow.getChequeformat() + "<br>";
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

	private List<BankaccountAudit> getBankAccountAuditList(final String bankAccountId) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select bacc_aud.id, bb.branchname, bnk.name bank_name, bacc_aud.accountnumber, bacc_aud.accounttype, ")
				.append("bacc_aud.narration, bacc_aud.isactive, bacc_aud.glcodeid, fnd.name, bacc_aud.payto, bacc_aud.type, ")
				.append("bacc_aud.lastmodifiedby, bacc_aud.lastmodifieddate, bacc_aud.chequeformatid, bacc_aud.nameofmodifyinguser ")
				.append("FROM bankaccount_aud bacc_aud ")
				.append("LEFT JOIN bankbranch bb ON bacc_aud.branchid=bb.id LEFT JOIN bank bnk ON bnk.id=bb.bankid LEFT JOIN fund fnd ON bacc_aud.fundid=fnd.id ")
				.append("where bacc_aud.id=:bankAccountId order by bacc_aud.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setLong("bankAccountId", Long.valueOf(bankAccountId));
		final List<Object[]> bankAuditListFromQuery = queryResult.list();

		List<BankaccountAudit> bankAuditList = new ArrayList<BankaccountAudit>();

		for (Object[] obj : bankAuditListFromQuery) {
			BankaccountAudit element = new BankaccountAudit();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : AuditReportUtils.NO_VALUE));
			element.setBankbranch(String.valueOf(obj[1] != null ? obj[1] : AuditReportUtils.NO_VALUE));
			element.setBank(String.valueOf(obj[2] != null ? obj[2] : AuditReportUtils.NO_VALUE));
			element.setAccountnumber(String.valueOf(obj[3] != null ? obj[3] : AuditReportUtils.NO_VALUE));
			element.setAccounttype(String.valueOf(obj[4] != null ? obj[4] : AuditReportUtils.NO_VALUE));			
			element.setNarration(String.valueOf(obj[5] != null ? obj[5] : AuditReportUtils.NO_VALUE));
			String isActive = (obj[6] != null && (Boolean)obj[6].equals(true)) ? "Active" : "Inactive"; 
			element.setIsactive(String.valueOf(isActive));
			element.setChartofaccounts(String.valueOf(obj[7] != null ? obj[7] : AuditReportUtils.NO_VALUE));
			element.setFund(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			element.setPayTo(String.valueOf(obj[9] != null ? obj[9] : AuditReportUtils.NO_VALUE));
			element.setType(String.valueOf(obj[10] != null ? obj[10] : AuditReportUtils.NO_VALUE));
			element.setLastModifiedBy(String.valueOf(obj[11] != null ? obj[11] : AuditReportUtils.NO_VALUE));
			String lastModifiedDate = String.valueOf(obj[12] != null ? obj[12] : "");			
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));			
			element.setChequeformat(String.valueOf(obj[13] != null ? obj[13] : AuditReportUtils.NO_VALUE));
			element.setNameOfmodifyingUser(String.valueOf(obj[8] != null ? obj[8] : AuditReportUtils.NO_VALUE));
			bankAuditList.add(element);
			element = null;
		}

		return bankAuditList;
	}
}






































