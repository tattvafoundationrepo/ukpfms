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
package org.egov.commons.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.commons.Accountdetailtype;
import org.egov.commons.CChartOfAccountDetail;
import org.egov.commons.CChartOfAccounts;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.exception.ApplicationException;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.microservice.contract.AccountCodeTemplate;
import org.egov.infra.microservice.models.ChartOfAccounts;
import org.egov.infstr.services.PersistenceService;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class ChartOfAccountsService extends PersistenceService<CChartOfAccounts, Long> {

    private static final String PURPOSE_ID = "purposeId";
    private static final String CONTINGENCY_BILL_PURPOSE_IDS = "contingencyBillPurposeIds";
    private static final String GLCODE = "glcode";
    private static final String CONTRACTOR = "Contractor";
    private static final String SUPPLIER = "Supplier";

    @Autowired
    protected AppConfigValueService appConfigValuesService;
    
    @Autowired
    private AccountdetailtypeService accountdetailtypeService;

    public ChartOfAccountsService(final Class<CChartOfAccounts> type) {
        super(type);
    }

    public List<CChartOfAccounts> getActiveCodeList() {

        return findAllBy("select acc from CChartOfAccounts acc where acc.isActiveForPosting=true order by acc.glcode");

    }

    public List<CChartOfAccounts> getAllAccountCodes(final String glcode) {

        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and (a.glcode like :glcode or lower(a.name) like :name) order by a.glcode");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");
        return entitysQuery.list();

    }
    public List<CChartOfAccounts> getAllAccountCodes1(final String glcode) {

        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where where type IN ('A','E') order by a.glcode");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");
        return entitysQuery.list();

    }
    public List<CChartOfAccounts> getGlCodes( final Set<String> glcodes) {

        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and (glcode in (:glcodes)) order by a.id");
        entitysQuery.setParameterList("glcodes", glcodes);
        return entitysQuery.list();

    }

	@Transactional
	public void updateActiveForPostingByMaterializedPath(final String materializedPath) {
		final Query entitysQuery = getSession().createSQLQuery(
				"update chartofaccounts set isactiveforposting = true where isactiveforposting = false and id in (select distinct bg.mincode from egf_budgetgroup bg,egf_budgetdetail bd where bd.budgetgroup = bg.id  and bd.materializedpath like :materializedPath ) ");
		entitysQuery.setString("materializedPath", materializedPath + "%");
		entitysQuery.executeUpdate();
	}

    public List<CChartOfAccounts> getSupplierDebitAccountCodes(final String glcode) {
        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4  and (glcode like :glcode or lower(name) like :name) and  type in ('E','A') order by a.id");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");
        return entitysQuery.list();
    }

    public List<CChartOfAccounts> getSupplierCreditAccountCodes(final String glcode) {
        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and (a.glcode like :glcode or lower(a.name) like :name) and a.type in ('I','L') order by a.id");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");

        List<CChartOfAccounts> netPayableCodes = getSupplierNetPayableAccountCodes();
        Map<String, CChartOfAccounts> netPayableMap = new HashMap<>();
        for (CChartOfAccounts coa : netPayableCodes) {
            netPayableMap.put(coa.getGlcode(), coa);
        }
        List<CChartOfAccounts> tempList = entitysQuery.list();
        List<CChartOfAccounts> finalList = new ArrayList<>();
        for (CChartOfAccounts coa : tempList) {
            if (netPayableMap.get(coa.getGlcode()) == null) {
                finalList.add(coa);
            }
        }
        return finalList;
    }

    public List<CChartOfAccounts> getContractorDebitAccountCodes(final String glcode) {
        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4  and (glcode like :glcode or lower(name) like :name) and  type in ('E','A') order by a.id");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");
        return entitysQuery.list();
    }

    public List<CChartOfAccounts> getContractorCreditAccountCodes(final String glcode) {
        final Query entitysQuery = getSession()
                .createQuery(
                        " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and (a.glcode like :glcode or lower(a.name) like :name) and a.type in ('I','L') order by a.id");
        entitysQuery.setString(GLCODE, glcode + "%");
        entitysQuery.setString("name", glcode.toLowerCase() + "%");

        List<CChartOfAccounts> netPayableCodes = getContractorNetPayableAccountCodes();
        Map<String, CChartOfAccounts> netPayableMap = new HashMap<>();
        for (CChartOfAccounts coa : netPayableCodes) {
            netPayableMap.put(coa.getGlcode(), coa);
        }
        List<CChartOfAccounts> tempList = entitysQuery.list();
        List<CChartOfAccounts> finalList = new ArrayList<>();
        for (CChartOfAccounts coa : tempList) {
            if (netPayableMap.get(coa.getGlcode()) == null) {
                finalList.add(coa);
            }
        }
        return finalList;
    }

    public List<CChartOfAccounts> getSupplierNetPayableAccountCodes() {
        final Query entitysQuery = getSession()
                .createQuery(
                        " select a from CChartOfAccounts a,EgfAccountcodePurpose purpose where a.purposeId = purpose.id and a.isActiveForPosting=true and a.classification=4 and purpose.name = 'Creditors-Supplier Payable'  order by a.id");
        return entitysQuery.list();
    }

    public List<CChartOfAccounts> getContractorNetPayableAccountCodes() {
        final Query entitysQuery = getSession()
                .createQuery(
                        " select a from CChartOfAccounts a,EgfAccountcodePurpose purpose where a.purposeId = purpose.id and a.isActiveForPosting=true and a.classification=4 and purpose.name = 'Creditors-Contractor Payable'  order by a.id");
        return entitysQuery.list();
    }

    public List<CChartOfAccounts> getSubledgerAccountCodesForAccountDetailTypeAndNonSubledgers(
            final Integer accountDetailTypeId, final String glcode) {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService.getConfigValuesByModuleAndKey(
                "EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final List<Long> contingencyBillPurposeIds = new ArrayList<>();
        for (final AppConfigValues av : configValuesByModuleAndKey)
            contingencyBillPurposeIds.add(Long.valueOf(av.getValue()));

        if (accountDetailTypeId == 0 || accountDetailTypeId == -1) {
            final Query entitysQuery = getSession()
                    .createQuery(
                            " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and size(a.chartOfAccountDetails) = 0 and (glcode like :glcode or lower(name) like :name) and (purposeId is null or purposeId not in (:ids)) order by a.id");
            entitysQuery.setString(GLCODE, "%" + glcode + "%");
            entitysQuery.setString("name", "%" + glcode.toLowerCase() + "%");
            entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
            return entitysQuery.list();
        } else {
            final Query entitysQuery = getSession()
                    .createQuery(
                            " from CChartOfAccounts  a LEFT OUTER JOIN  fetch a.chartOfAccountDetails  b where (size(a.chartOfAccountDetails) = 0 or b.detailTypeId.id=:accountDetailTypeId) and a.isActiveForPosting=true and a.classification=4 and (a.glcode like :glcode or lower(a.name) like :name) and (purposeId is null or purposeId not in (:ids)) order by a.id");
            entitysQuery.setInteger("accountDetailTypeId", accountDetailTypeId);
            entitysQuery.setString(GLCODE, "%" + glcode + "%");
            entitysQuery.setString("name", "%" + glcode.toLowerCase() + "%");
            entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
            return entitysQuery.list();
        }
    }
    
    public List<CChartOfAccounts> getAccountCodeByPurpose(final Integer purposeId) throws ApplicationException {
        final List<CChartOfAccounts> accountCodeList = new ArrayList<CChartOfAccounts>();
        try {
            if (purposeId == null || purposeId.intValue() == 0)
                throw new ApplicationException("Purpose Id is null or zero");
            Query query = getSession().createQuery(
                    " from EgfAccountcodePurpose purpose where purpose.id=" + purposeId + "");
            if (query.list().isEmpty())
                throw new ApplicationException("Purpose ID provided is not defined in the system");
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE purposeid=:purposeId))) AND classification=4 AND isActiveForPosting=true ");
            query.setLong(PURPOSE_ID, purposeId);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE purposeid=:purposeId)) AND classification=4 AND isActiveForPosting=true ");
            query.setLong(PURPOSE_ID, purposeId);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE purposeid=:purposeId) AND classification=4 AND isActiveForPosting=true ");
            query.setLong(PURPOSE_ID, purposeId);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE purposeid=:purposeId AND classification=4 AND isActiveForPosting=true ");
            query.setLong(PURPOSE_ID, purposeId);
            accountCodeList.addAll(query.list());
        } catch (final HibernateException e) {
            throw new ApplicationRuntimeException("Error occurred while getting Account Code by purpose", e);
        }
        return accountCodeList;
    }

    public List<CChartOfAccounts> getAccountCodeByPurposeName(final String purposeName) {
        final List<CChartOfAccounts> accountCodeList = new ArrayList<CChartOfAccounts>();
        try {
            if (purposeName == null || purposeName.equalsIgnoreCase(""))
                throw new ApplicationException("Purpose Name is null or empty");
            Query query = getSession().createQuery(
                    " from EgfAccountcodePurpose purpose where purpose.name='" + purposeName + "'");
            if (query.list().size() == 0)
                throw new ApplicationException("Purpose Name provided is not defined in the system");
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT coa.id FROM CChartOfAccounts coa,EgfAccountcodePurpose purpose WHERE coa.purposeId=purpose.id and purpose.name = :purposeName))) AND classification=4 AND isActiveForPosting=true ");
            query.setString("purposeName", purposeName);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT id FROM CChartOfAccounts WHERE parentId IN (SELECT coa.id FROM CChartOfAccounts coa,EgfAccountcodePurpose purpose WHERE coa.purposeId=purpose.id and purpose.name = :purposeName)) AND classification=4 AND isActiveForPosting=true ");
            query.setString("purposeName", purposeName);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            " FROM CChartOfAccounts WHERE parentId IN (SELECT coa.id FROM CChartOfAccounts coa,EgfAccountcodePurpose purpose WHERE coa.purposeId=purpose.id and purpose.name = :purposeName) AND classification=4 AND isActiveForPosting=true ");
            query.setString("purposeName", purposeName);
            accountCodeList.addAll(query.list());
            query = getSession()
                    .createQuery(
                            "SELECT coa FROM CChartOfAccounts coa,EgfAccountcodePurpose purpose WHERE coa.purposeId=purpose.id and purpose.name = :purposeName AND coa.classification=4 AND coa.isActiveForPosting=true ");
            query.setString("purposeName", purposeName);
            accountCodeList.addAll(query.list());
        } catch (final ApplicationException e) {
            throw new ApplicationRuntimeException("Error occurred while getting Account Code by purpose name", e);
        }
        return accountCodeList;
    }

    public List<CChartOfAccounts> getNetPayableCodesByAccountDetailType(final Integer accountDetailType) {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService.getConfigValuesByModuleAndKey(
                "EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final Set<CChartOfAccounts> netPayList = new HashSet<>();
        List<CChartOfAccounts> accountCodeByPurpose = new ArrayList<>();
        for (int i = 0; i < configValuesByModuleAndKey.size(); i++) {
            try {
                accountCodeByPurpose = getAccountCodeByPurpose(Integer
                        .valueOf(configValuesByModuleAndKey.get(i).getValue()));
            } catch (final ApplicationException e) {
                // Ignore
            }

            if (accountDetailType == null || accountDetailType == 0) {
                for (final CChartOfAccounts coa : accountCodeByPurpose)
                    if (coa.getChartOfAccountDetails().isEmpty())
                        netPayList.add(coa);

            } else
                for (final CChartOfAccounts coa : accountCodeByPurpose) {
                    if (!coa.getChartOfAccountDetails().isEmpty())
                        for (final CChartOfAccountDetail coaDtl : coa.getChartOfAccountDetails())
                            if (coaDtl.getDetailTypeId() != null && coaDtl.getDetailTypeId().getId().equals(accountDetailType))
                                netPayList.add(coa);
                    netPayList.add(coa);
                }

        }
        return new ArrayList<>(netPayList);
    }


    public List<CChartOfAccounts> getNetPayableCodes() {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService.getConfigValuesByModuleAndKey(
                "EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final Set<CChartOfAccounts> netPayList = new HashSet<>();
        List<CChartOfAccounts> accountCodeByPurpose = new ArrayList<>();
        for (int i = 0; i < configValuesByModuleAndKey.size(); i++) {
            try {
                accountCodeByPurpose = getAccountCodeByPurpose(Integer
                        .valueOf(configValuesByModuleAndKey.get(i).getValue()));
            } catch (final ApplicationException e) {
                // Ignore
            }

            for (final CChartOfAccounts coa : accountCodeByPurpose)
                netPayList.add(coa);

        }
        return new ArrayList<>(netPayList);
    }

    public List<CChartOfAccounts> getAccountTypes() {

        final Query accountTypesQuery = getSession()
                .createQuery(" from CChartOfAccounts WHERE glcode LIKE '4502%' AND classification=2 ORDER BY glcode");
        return accountTypesQuery.list();
    }

    public CChartOfAccounts getByGlCodeDesc(final String glcode) {

        final Query query = getSession()
                .createQuery(" from CChartOfAccounts a where  a.glcode like :glcode  order by a.glcode desc");
        query.setString(GLCODE, glcode + "%");
        final List<CChartOfAccounts> resultList = query.list();

        return resultList.isEmpty() ? null : resultList.get(0);

    }

    public CChartOfAccounts getByGlCode(final String glcode) {

        final Query query = getSession().createQuery(" from CChartOfAccounts a where  a.glcode =:glcode ");
        query.setString("glcode", glcode);
        final List<CChartOfAccounts> resultList = query.list();

        return resultList.isEmpty() ? null : resultList.get(0);

    }

    public CChartOfAccounts getById(final Long id) {

        return getSession().load(CChartOfAccounts.class, id);

    }

    public List<CChartOfAccounts> getAllAccountCodesByIsactiveAndClassification() {
        final Query query = getSession()
                .createQuery(
                        " from CChartOfAccounts coa where coa.isActiveForPosting=true and coa.classification=4");
        return query.list();
    }
    
    public List<CChartOfAccounts> getSubledgerAccountCodesForAccountDetailTypeAndNonSubledgers(
            final Integer accountDetailTypeId, final Set<String> glcodes) {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService.getConfigValuesByModuleAndKey(
                "EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final List<Long> contingencyBillPurposeIds = new ArrayList<>();
        for (final AppConfigValues av : configValuesByModuleAndKey)
            contingencyBillPurposeIds.add(Long.valueOf(av.getValue()));

        if (accountDetailTypeId == 0 || accountDetailTypeId == -1) {
            final Query entitysQuery = getSession()
                    .createQuery(
                            " from CChartOfAccounts a where a.isActiveForPosting=true and a.classification=4 and size(a.chartOfAccountDetails) = 0 and (glcode in (:glcodes)) and (purposeId is null or purposeId not in (:ids)) order by a.id");
            entitysQuery.setParameterList("glcodes", glcodes);
            entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
            return entitysQuery.list();
        } else {
            final Query entitysQuery = getSession()
                    .createQuery(
                            "  from CChartOfAccounts  a LEFT OUTER JOIN  fetch a.chartOfAccountDetails  b where (size(a.chartOfAccountDetails) = 0 or b.detailTypeId.id=:accountDetailTypeId) and a.isActiveForPosting=true and a.classification=4 and a.glcode in (:glcodes) and (purposeId is null or purposeId not in (:ids)) order by a.id");
            entitysQuery.setInteger("accountDetailTypeId", accountDetailTypeId);
            entitysQuery.setParameterList("glcodes", glcodes);
            entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
            return entitysQuery.list();
        }
    }
    
    public List<CChartOfAccounts> getSubledgerAccountCodesForAccountDetailTypeAndNonSubledgersWithContractors(
            final Set<String> glcodes) {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService
                .getConfigValuesByModuleAndKey("EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final List<Long> contingencyBillPurposeIds = new ArrayList<>();
        for (final AppConfigValues av : configValuesByModuleAndKey)
            contingencyBillPurposeIds.add(Long.valueOf(av.getValue()));
        final List<CChartOfAccounts> glcodesList = getGlCodes(glcodes);
        final List<CChartOfAccounts> nonContarctorList = new ArrayList<CChartOfAccounts>();
        Integer contractorAccountDetailTypeId = null;
        Accountdetailtype contractorAccountdetailtype = accountdetailtypeService.findByName(CONTRACTOR);
        contractorAccountDetailTypeId = contractorAccountdetailtype.getId();
        for (CChartOfAccounts coa : glcodesList) {
            Boolean contractorExist = false;
            Boolean check = false;
            for (CChartOfAccountDetail cad : coa.getChartOfAccountDetails()) {
                if (cad.getDetailTypeId() != null && cad.getDetailTypeId().getName().equalsIgnoreCase(CONTRACTOR)) {
                    contractorExist = true;
                }
                if (!cad.getDetailTypeId().getName().equalsIgnoreCase(CONTRACTOR)) {
                    check = true;
                }
              }
            if (check && !contractorExist) {
                nonContarctorList.add(coa);
                }
            }
        glcodesList.removeAll(nonContarctorList);
        Map<String, CChartOfAccounts> glcodeSet = glcodesList.stream()
                .collect(Collectors.toMap(CChartOfAccounts::getGlcode, Function.identity()));

        final Query entitysQuery = getSession().createQuery(
                "  from CChartOfAccounts  a LEFT OUTER JOIN  fetch a.chartOfAccountDetails  b where (size(a.chartOfAccountDetails) = 0 or b.detailTypeId.id=:accountDetailTypeId) and a.isActiveForPosting=true and a.classification=4 and a.glcode in (:glcodes) and (purposeId is null or purposeId not in (:ids)) order by a.id");
        entitysQuery.setInteger("accountDetailTypeId", contractorAccountDetailTypeId);
        entitysQuery.setParameterList("glcodes", glcodeSet.keySet());
        entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
        return entitysQuery.list();
    }
    
    public List<CChartOfAccounts> getSubledgerAccountCodesForAccountDetailTypeAndNonSubledgersWithSupplier(
            final Set<String> glcodes) {
        final List<AppConfigValues> configValuesByModuleAndKey = appConfigValuesService
                .getConfigValuesByModuleAndKey("EGF", CONTINGENCY_BILL_PURPOSE_IDS);
        final List<Long> contingencyBillPurposeIds = new ArrayList<>();
        for (final AppConfigValues av : configValuesByModuleAndKey)
            contingencyBillPurposeIds.add(Long.valueOf(av.getValue()));
        final List<CChartOfAccounts> glcodesList = getGlCodes(glcodes);
        final List<CChartOfAccounts> nonSupplierList = new ArrayList<CChartOfAccounts>();
        Integer supplierAccountDetailTypeId = null;
        Accountdetailtype supplierAccountdetailtype = accountdetailtypeService.findByName(SUPPLIER);
        supplierAccountDetailTypeId = supplierAccountdetailtype.getId();
        for (CChartOfAccounts coa : glcodesList) {
            Boolean supplierExist = false;
            Boolean check = false;
            for (CChartOfAccountDetail cad : coa.getChartOfAccountDetails()) {
                if (cad.getDetailTypeId() != null && cad.getDetailTypeId().getName().equalsIgnoreCase(SUPPLIER)) {
                    supplierExist = true;
                }
                if (!cad.getDetailTypeId().getName().equalsIgnoreCase(SUPPLIER)) {
                    check = true;
                }
              }
            if (check && !supplierExist) {
                nonSupplierList.add(coa);
                }
            }
        glcodesList.removeAll(nonSupplierList);
        Map<String, CChartOfAccounts> glcodeSet = glcodesList.stream()
                .collect(Collectors.toMap(CChartOfAccounts::getGlcode, Function.identity()));

        final Query entitysQuery = getSession().createQuery(
                "  from CChartOfAccounts  a LEFT OUTER JOIN  fetch a.chartOfAccountDetails  b where (size(a.chartOfAccountDetails) = 0 or b.detailTypeId.id=:accountDetailTypeId) and a.isActiveForPosting=true and a.classification=4 and a.glcode in (:glcodes) and (purposeId is null or purposeId not in (:ids)) order by a.id");
        entitysQuery.setInteger("accountDetailTypeId", supplierAccountDetailTypeId);
        entitysQuery.setParameterList("glcodes", glcodeSet.keySet());
        entitysQuery.setParameterList("ids", contingencyBillPurposeIds);
        return entitysQuery.list();
    }

}