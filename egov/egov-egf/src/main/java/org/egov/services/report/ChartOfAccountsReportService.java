package org.egov.services.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.egov.commons.CChartOfAccounts;
import org.egov.commons.dao.ChartOfAccountsHibernateDAO;
import org.egov.egf.utils.AuditReportUtils;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infstr.services.PersistenceService;
import org.egov.model.report.ChartOfAccountsAuditReport;
import org.egov.model.report.ChartOfAccountsReport;
import org.egov.utils.Constants;
import org.egov.utils.FinancialConstants;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChartOfAccountsReportService {

	@Autowired
	private SecurityUtils securityUtils;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@Autowired
	private AppConfigValueService appConfigValueService;

	@Autowired
	private ChartOfAccountsHibernateDAO chartOfAccountsHibernateDAO;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public List<String> getCoaModificationReport(final String glcode) {

		List<ChartOfAccountsAuditReport> resultList = getCoaAuditReport(glcode);
		List<String> modificationsList = new ArrayList<String>();

		if (resultList.size() == 1)
			return modificationsList;

		for (int i = 0; i < resultList.size() - 1; i++) {

			ChartOfAccountsAuditReport previousModifiedRow = resultList.get(i);
			ChartOfAccountsAuditReport currentModifiedRow = resultList.get(i + 1);
			String modifications = "";

			if (!previousModifiedRow.getName().equals(currentModifiedRow.getName())) {
				modifications = modifications + "Name : " + previousModifiedRow.getName() + " --> "
						+ currentModifiedRow.getName() + "<br>";
			}
			if (!previousModifiedRow.getDescription().equals(currentModifiedRow.getDescription())) {
				modifications = modifications + "Description : " + previousModifiedRow.getDescription() + " --> "
						+ currentModifiedRow.getDescription() + "<br>";
			}
			if (!previousModifiedRow.getPurpose().equals(currentModifiedRow.getPurpose())) {
				modifications = modifications + "Purpose : " + previousModifiedRow.getPurpose() + " --> "
						+ currentModifiedRow.getPurpose() + "<br>";
			}
			if (!previousModifiedRow.getFunctionReqd().equals(currentModifiedRow.getFunctionReqd())) {
				modifications = modifications + "Function Required : " + previousModifiedRow.getFunctionReqd() + " --> "
						+ currentModifiedRow.getFunctionReqd() + "<br>";
			}
			if (!previousModifiedRow.getBudgetCheckReq().equals(currentModifiedRow.getBudgetCheckReq())) {
				modifications = modifications + "Budget Required : " + previousModifiedRow.getBudgetCheckReq() + " --> "
						+ currentModifiedRow.getBudgetCheckReq() + "<br>";
			}
			if (!previousModifiedRow.getDetailType().equals(currentModifiedRow.getDetailType())) {
				modifications = modifications + "Account detail type : " + previousModifiedRow.getDetailType() + " --> "
						+ currentModifiedRow.getDetailType() + "<br>";
			}
			if (!previousModifiedRow.getIsActiveForPosting().equals(currentModifiedRow.getIsActiveForPosting())) {
				modifications = modifications + "Active : " + previousModifiedRow.getIsActiveForPosting() + " --> "
						+ currentModifiedRow.getIsActiveForPosting() + "<br>";
			}

			if (modifications.length() > 0 && currentModifiedRow.getNameOfmodifyingUser().length() > 0) {
				modificationsList.add("User : "+currentModifiedRow.getNameOfmodifyingUser() + "<br>"
						+ "Modification date-time : " +currentModifiedRow.getLastModifiedDate() + "<br><br>" + modifications);
			}

		}

		return modificationsList;

	}

	public List<ChartOfAccountsAuditReport> getCoaAuditReport(final String glcode) {

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append(
				"select coa_aud.id, coa_aud.rev, coa_aud.glcode, coa_aud.name, coa_aud.description, coa_aud.isactiveforposting, coa_aud.parentid, coa_aud.purposeid, coa_aud.operation, coa_aud.type, coa_aud.class, coa_aud.classification, ")
				.append("coa_aud.functionreqd, coa_aud.budgetcheckreq, coa_aud.scheduleid, coa_aud.receiptscheduleid, coa_aud.receiptoperation, coa_aud.paymentscheduleid, coa_aud.paymentoperation, coa_aud.fiescheduleid, coa_aud.fieoperation, ")
				.append("coa_aud.createddate, coa_aud.createdby, coa_aud.lastmodifieddate, coa_aud.lastmodifiedby, coa_aud.version, coa_aud.revtype, coadetail_aud.detailtypeid, coa_aud.nameofmodifyinguser ")
				.append(" from chartofaccounts_aud coa_aud LEFT JOIN chartofaccountdetail_aud coadetail_aud ON coa_aud.rev=coadetail_aud.rev where coa_aud.glcode=:glcode order by coa_aud.lastmodifieddate NULLS FIRST");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		queryResult.setString("glcode", glcode);
		final List<Object[]> coaAuditReportList = queryResult.list();

		List<ChartOfAccountsAuditReport> resultList = new ArrayList<ChartOfAccountsAuditReport>();
		Map<String, String> purposeIdNameMap = getPurposeData();
		Map<String, String> accDetailTypeIdNameMap = getAccountDetailTypeData();	

		for (Object[] obj : coaAuditReportList) {
			ChartOfAccountsAuditReport element = new ChartOfAccountsAuditReport();
			element.setId(String.valueOf(obj[0] != null ? obj[0] : ""));
			element.setRev(String.valueOf(obj[1] != null ? obj[1] : ""));
			element.setGlcode(obj[2].toString());
			element.setName(obj[3].toString());
			element.setDescription(String.valueOf(obj[4] != null ? obj[4] : ""));
			element.setIsActiveForPosting((Boolean) obj[5]);
			element.setParentId(String.valueOf(obj[6] != null ? obj[6] : ""));
			String purposeId = String.valueOf(obj[7] != null ? obj[7] : "");
			element.setPurposeId(purposeId);
			String purposeName = (purposeId != null && purposeId.length() > 0) ? purposeIdNameMap.get(purposeId) : "";
			element.setPurpose(purposeName);
			element.setOperation(String.valueOf(obj[8] != null ? obj[8] : ""));
			element.setType(String.valueOf(obj[9] != null ? obj[9] : ""));
			element.setClassCoa(String.valueOf(obj[10] != null ? obj[10] : ""));
			element.setClassification(String.valueOf(obj[11] != null ? obj[11] : ""));
			element.setFunctionReqd((Boolean) obj[12]);
			element.setBudgetCheckReq((Boolean) obj[13]);
			element.setScheduleId(String.valueOf(obj[14] != null ? obj[14] : ""));
			element.setRecieptScheduleId(String.valueOf(obj[15] != null ? obj[15] : ""));
			element.setRecieptOperation(String.valueOf(obj[16] != null ? obj[16] : ""));
			element.setPaymentScheduleId(String.valueOf(obj[17] != null ? obj[17] : ""));
			element.setPaymentOperation(String.valueOf(obj[18] != null ? obj[18] : ""));
			element.setFieScheduleId(String.valueOf(obj[19] != null ? obj[19] : ""));
			element.setFieOperation(String.valueOf(obj[20] != null ? obj[20] : ""));
			element.setCreateDate((Date) obj[21]);
			element.setCreatedBy(String.valueOf(obj[22] != null ? obj[22] : ""));
			String lastModifiedDate = String.valueOf(obj[23] != null ? obj[23] : "");
			element.setLastModifiedDate(AuditReportUtils.getFormattedDateTime(lastModifiedDate));
			element.setLastModifiedBy(String.valueOf(obj[24] != null ? obj[24] : ""));
			element.setVersion(String.valueOf(obj[25] != null ? obj[25] : ""));
			element.setRevType(String.valueOf(obj[26] != null ? obj[26] : ""));
			String accountDetailTypeId = String.valueOf(obj[27] != null ? obj[27] : "");
			element.setDetailType(accDetailTypeIdNameMap.get(accountDetailTypeId) != null
					? accDetailTypeIdNameMap.get(accountDetailTypeId)
					: "");
			element.setNameOfmodifyingUser(String.valueOf(obj[28] != null ? obj[28] : ""));
			resultList.add(element);
			element = null;
		}		

		return resultList;

	}
	
	private Map<String, String> getPurposeData() {

		Map<String, String> purposeIdNameMap = new HashMap<String, String>();

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select id, name from egf_accountcode_purpose");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());

		final List<Object[]> objectArrayList = queryResult.list();

		for (Object[] obj : objectArrayList) {

			purposeIdNameMap.put(obj[0].toString(), obj[1].toString());
		}

		return purposeIdNameMap;

	}

	private Map<String, String> getAccountDetailTypeData() {

		Map<String, String> accDetailTypeIdNameMap = new HashMap<String, String>();

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append("select id, name from accountdetailtype");
		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());

		final List<Object[]> objectArrayList = queryResult.list();

		for (Object[] obj : objectArrayList) {

			accDetailTypeIdNameMap.put(obj[0].toString(), obj[1].toString());
		}

		return accDetailTypeIdNameMap;

	}	

	public List<ChartOfAccountsReport> getCoaReport(final ChartOfAccountsReport coaSearchResultObj) {
		Map<Character, String> typeNameMap = new HashMap();
		typeNameMap.put('I', "Income");
		typeNameMap.put('E', "Expense");
		typeNameMap.put('L', "Liability");
		typeNameMap.put('A', "Asset");
		if (coaSearchResultObj.getAccountCode() != null) {
			final String[] accountCodes = coaSearchResultObj.getAccountCode().split("-");
			coaSearchResultObj.setAccountCode(accountCodes[0].trim());
		}

		final StringBuilder queryStr = new StringBuilder();
		queryStr.append(" select coa.glcode as accountCode,coa.name as accountName,")
				.append("concat(minorcoa.glcode,'-',minorcoa.name) as ")
				.append(" minorCode,concat(majorcoa.glcode,'-',majorcoa.name) as majorCode,acp.name as purpose,")
				.append("string_agg(acdt.name,',') as accountDetailType ")
				.append(" ,coa.type as type,coa.isactiveforposting as isActiveForPosting  ")
				.append(" from chartofaccounts coa ")
				.append(" left join chartofaccountdetail coad on coa.id=coad.glcodeid ")
				.append(" left join accountdetailtype acdt on acdt.id=coad.detailtypeid")
				.append(" left join egf_accountcode_purpose acp on coa.purposeid=acp.id ")
				.append(",chartofaccounts minorcoa,chartofaccounts majorcoa,chartofaccounts parent")
				.append(" where coa.parentid=minorcoa.id and minorcoa.parentid=majorcoa.id and majorcoa.parentid=parent.id ");
		getAppendQuery(coaSearchResultObj, queryStr);
		queryStr.append(" group by coa.glcode,minorcoa.glcode,majorcoa.glcode,parent.glcode,coa.name,minorcoa.name,")
				.append("majorcoa.name,acp.name,coa.type,coa.isactiveforposting order by coa.glcode asc ");

		SQLQuery queryResult = persistenceService.getSession().createSQLQuery(queryStr.toString());
		setParametersToQuery(coaSearchResultObj, queryResult);
		final List<Object[]> coaReportList = queryResult.list();
		List<ChartOfAccountsReport> coaReport = new ArrayList<ChartOfAccountsReport>();
		for (Object[] obj : coaReportList) {
			ChartOfAccountsReport report = new ChartOfAccountsReport();
			report.setAccountCode(obj[0].toString());
			report.setAccountName(obj[1].toString());
			report.setMajorCode(obj[3].toString());
			report.setMinorCode(obj[2].toString());
			report.setPurpose(obj[4] != null ? obj[4].toString() : null);
			report.setAccountDetailType(obj[5] != null ? obj[5].toString() : null);

			report.setType(typeNameMap.get(obj[6].toString().charAt(0)));
			report.setIsActiveForPosting((Boolean) obj[7]);
			coaReport.add(report);
		}

		return coaReport;

	}

	private List<ChartOfAccountsReport> prepareDetailTypeNames(List<ChartOfAccountsReport> coaReportList) {

		Map<String, ChartOfAccountsReport> coaMap = new HashMap<String, ChartOfAccountsReport>();

		for (ChartOfAccountsReport coar : coaReportList) {

			if (coaMap.get(coar.getAccountCode()) != null) {
				coaMap.get(coar.getAccountCode()).setAccountDetailType(
						coaMap.get(coar.getAccountCode()).getAccountDetailType() + "," + coar.getAccountDetailType());
			} else {
				coaMap.put(coar.getAccountCode(), coar);
			}
		}

		return new ArrayList(coaMap.values());
	}

	private void getAppendQuery(final ChartOfAccountsReport coaSearchResultObj, final StringBuilder queryStr) {
		if (StringUtils.isNotBlank(coaSearchResultObj.getAccountCode()))
			queryStr.append(" and coa.glcode = :accountCode");
		if (coaSearchResultObj.getMajorCodeId() != null)
			queryStr.append(" and majorcoa.id =:majorCodeId ");
		if (coaSearchResultObj.getMinorCodeId() != null)
			queryStr.append(" and minorcoa.id =:minorCodeId ");
		if (coaSearchResultObj.getType() != null)
			queryStr.append(" and coa.type =:type ");
		if (coaSearchResultObj.getPurposeId() != null)
			queryStr.append(" and acp.id =:purposeId");
		if (coaSearchResultObj.getDetailTypeId() != null)
			queryStr.append(" and acdt.id =:detailTypeId ");
		if (coaSearchResultObj.getIsActiveForPosting() != null)
			queryStr.append(" and coa.isActiveForPosting =:isActiveForPosting ");
		if (coaSearchResultObj.getBudgetCheckReq() != null)
			queryStr.append(" and coa.budgetCheckReq =:budgetCheckReq ");
		if (coaSearchResultObj.getFunctionReqd() != null)
			queryStr.append(" and coa.functionReqd =:functionReqd ");

	}

	private SQLQuery setParametersToQuery(final ChartOfAccountsReport coaSearchResultObj, final SQLQuery queryResult) {

		if (StringUtils.isNotBlank(coaSearchResultObj.getAccountCode()))
			queryResult.setString("accountCode", coaSearchResultObj.getAccountCode());
		if (coaSearchResultObj.getMajorCodeId() != null)
			queryResult.setLong("majorCodeId", coaSearchResultObj.getMajorCodeId());

		if (coaSearchResultObj.getMinorCodeId() != null)
			queryResult.setLong("minorCodeId", coaSearchResultObj.getMinorCodeId());

		if (coaSearchResultObj.getType() != null)
			queryResult.setString("type", coaSearchResultObj.getType());
		if (coaSearchResultObj.getPurposeId() != null)
			queryResult.setLong("purposeId", coaSearchResultObj.getPurposeId());
		if (coaSearchResultObj.getDetailTypeId() != null)
			queryResult.setLong("detailTypeId", coaSearchResultObj.getDetailTypeId());
		if (coaSearchResultObj.getIsActiveForPosting() != null)
			queryResult.setBoolean("isActiveForPosting", coaSearchResultObj.getIsActiveForPosting());
		if (coaSearchResultObj.getFunctionReqd() != null)
			queryResult.setBoolean("functionReqd", coaSearchResultObj.getFunctionReqd());
		if (coaSearchResultObj.getBudgetCheckReq() != null)
			queryResult.setBoolean("budgetCheckReq", coaSearchResultObj.getBudgetCheckReq());
		return queryResult;
	}

	public List<CChartOfAccounts> getMinCodeListByMajorCodeId(Long parentId) {

		Integer minorCodeLength = 0;
		minorCodeLength = Integer.valueOf(appConfigValueService
				.getConfigValuesByModuleAndKey(Constants.EGF, FinancialConstants.APPCONFIG_COA_MINORCODE_LENGTH).get(0)
				.getValue());

		return chartOfAccountsHibernateDAO.findCOAByLengthAndParentId(minorCodeLength, parentId);
	}

	public int getMajorCodeLength() {
		final List<AppConfigValues> appList = appConfigValueService.getConfigValuesByModuleAndKey(Constants.EGF,
				FinancialConstants.APPCONFIG_COA_MAJORCODE_LENGTH);
		return Integer.valueOf(appList.get(0).getValue());
	}

	public int getMinorCodeLength() {
		final List<AppConfigValues> appList = appConfigValueService.getConfigValuesByModuleAndKey(Constants.EGF,
				FinancialConstants.APPCONFIG_COA_MINORCODE_LENGTH);
		return Integer.valueOf(appList.get(0).getValue());
	}

	public List<CChartOfAccounts> getMajorCodeList() {
		return chartOfAccountsHibernateDAO.findCOAByLength(getMajorCodeLength());
	}

	public List<CChartOfAccounts> getMinorCodeList() {
		return chartOfAccountsHibernateDAO.findCOAByLength(getMinorCodeLength());
	}

}
