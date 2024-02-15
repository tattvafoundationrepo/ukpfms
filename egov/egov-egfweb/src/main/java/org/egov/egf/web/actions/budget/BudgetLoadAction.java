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
package org.egov.egf.web.actions.budget;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.egov.commons.CChartOfAccounts;
import org.egov.commons.CFinancialYear;
import org.egov.commons.CFunction;
import org.egov.commons.Fund;
import org.egov.commons.dao.FinancialYearDAO;
import org.egov.commons.dao.FunctionDAO;
import org.egov.commons.dao.FundHibernateDAO;
import org.egov.commons.service.ChartOfAccountsService;
import org.egov.infra.admin.master.service.DepartmentService;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.microservice.models.Department;
import org.egov.infra.validation.exception.ValidationError;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infra.web.struts.actions.BaseFormAction;
import org.egov.infra.web.struts.annotation.ValidationErrorPage;
import org.egov.infstr.services.PersistenceService;
import org.egov.infstr.utils.EgovMasterDataCaching;
import org.egov.model.budget.BudgetUpload;
import org.egov.services.budget.BudgetDetailService;
import org.egov.utils.FinancialConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ParentPackage("egov")
@Results({ @Result(name = "upload", location = "budgetLoad-upload.jsp"),
		@Result(name = "result", location = "budgetLoad-result.jsp") })
public class BudgetLoadAction extends BaseFormAction {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(BudgetLoadAction.class);
	private File budgetInXls;
	private String budgetInXlsFileName;
	private String budgetInXlsContentType;
	private static final int RE_YEAR_ROW_INDEX = 1;
	private static final int BE_YEAR_ROW_INDEX = 2;
	private static final int DATA_STARTING_ROW_INDEX = 4;
	private static final int FUNDCODE_CELL_INDEX = 0;
	private static final int DEPARTMENTCODE_CELL_INDEX = 1;
	private static final int FUNCTIONCODE_CELL_INDEX = 2;
	private static final int GLCODE_CELL_INDEX = 3;
	private static final int REAMOUNT_CELL_INDEX = 4;
	private static final int BEAMOUNT_CELL_INDEX = 5;
	private static final int PLANNINGPERCENTAGE_CELL_INDEX = 6;
	private boolean errorInMasterData = false;
	private boolean isBudgetUploadFileEmpty = true;
	private MultipartFile[] originalFile = new MultipartFile[1];
	private MultipartFile[] outPutFile = new MultipartFile[1];
	private String originalFileStoreId, outPutFileStoreId;
	private List<FileStoreMapper> originalFiles = new ArrayList<FileStoreMapper>();
	private List<FileStoreMapper> outPutFiles = new ArrayList<FileStoreMapper>();
	private String budgetOriginalFileName;
	private String budgetOutPutFileName;
	private String timeStamp;
	private String budgetUploadError = "Upload the Budget Data as shown in the Download Template format";
	@Autowired
	private FinancialYearDAO financialYearDAO;

	@Autowired
	private FundHibernateDAO fundDAO;

	@Autowired
	private FunctionDAO functionDAO;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	@Qualifier("chartOfAccountsService")
	private ChartOfAccountsService chartOfAccountsService;
	@Autowired
	@Qualifier("persistenceService")
	private PersistenceService persistenceService;

	@Autowired
	@Qualifier("budgetDetailService")
	private BudgetDetailService budgetDetailService;

	@Autowired
	protected FileStoreService fileStoreService;

	@Autowired
	@Qualifier("masterDataCache")
	private EgovMasterDataCaching masterDataCache;

	@Override
	@SuppressWarnings("unchecked")
	public void prepare() {

	}

	@Override
	public Object getModel() {
		return null;
	}

	@Action(value = "/budget/budget-template-result")
	public void generateBudgetTemplate() {

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=Budget_Data_Template.xls");

		try {
			Workbook workbook = createWorkbook();
			workbook.write(response.getOutputStream());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private Workbook createWorkbook() {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");
		sheet.setColumnWidth(0, 8000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 8000);
		sheet.setColumnWidth(3, 15000);
		sheet.setColumnWidth(4, 8000);
		sheet.setColumnWidth(5, 8000);
		sheet.setColumnWidth(6, 8000);
		sheet.autoSizeColumn(0);

		HSSFCellStyle headerStyle = (HSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font boldFont = workbook.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(boldFont);

		// Add text in three rows
//		for (int i = 0; i <= 2; i++) {
//			Row textRow = sheet.createRow(i);
//			Cell textCell = textRow.createCell(1);
//			textCell.setCellValue("Text Row " + i);
//		}

		Row textRow_0 = sheet.createRow(0);
		Cell textCell_00 = textRow_0.createCell(0);
		Cell textCell_01 = textRow_0.createCell(1);
		textCell_00.setCellValue("Name of Muncipallity");
		textCell_01.setCellValue("Kurnnool Municipal Corporation");
		textCell_00.setCellStyle(headerStyle);
		textCell_01.setCellStyle(headerStyle);

		Row textRow_1 = sheet.createRow(1);
		Cell textCell_10 = textRow_1.createCell(0);
		Cell textCell_11 = textRow_1.createCell(1);
		textCell_10.setCellValue("RE Year");
		textCell_11.setCellValue("2016-17");
		textCell_10.setCellStyle(headerStyle);
		textCell_11.setCellStyle(headerStyle);

		Row textRow_2 = sheet.createRow(2);
		Cell textCell_20 = textRow_2.createCell(0);
		Cell textCell_21 = textRow_2.createCell(1);
		textCell_20.setCellValue("BE Year");
		textCell_21.setCellValue("2017-18");
		textCell_20.setCellStyle(headerStyle);
		textCell_21.setCellStyle(headerStyle);

		// Add headers in the 03 row
		Row headerRow = sheet.createRow(3);
		Cell headerCell0 = headerRow.createCell(0);
		headerCell0.setCellValue("Fund Code");
		headerCell0.setCellStyle(headerStyle);

		Cell headerCell1 = headerRow.createCell(1);
		headerCell1.setCellValue("Department Code");
		headerCell1.setCellStyle(headerStyle);

		Cell headerCell2 = headerRow.createCell(2);
		headerCell2.setCellValue("Function Code");
		headerCell2.setCellStyle(headerStyle);

		Cell headerCell3 = headerRow.createCell(3);
		headerCell3.setCellValue("Chart of Account Code");
		headerCell3.setCellStyle(headerStyle);

		Cell headerCell4 = headerRow.createCell(4);
		headerCell4.setCellValue("RE Amount (Rs)");
		headerCell4.setCellStyle(headerStyle);

		Cell headerCell5 = headerRow.createCell(5);
		headerCell5.setCellValue("BE Amount (Rs)");
		headerCell5.setCellStyle(headerStyle);

		Cell headerCell6 = headerRow.createCell(6);
		headerCell6.setCellValue("Planning Percentage");
		headerCell6.setCellStyle(headerStyle);

		List<Fund> fundList = fundDAO.findAllActiveIsLeafFunds();
		List<CFunction> functionList = functionDAO.getAllActiveFunctions();
		List<Department> departmentList = masterDataCache.get("egi-department");
		List<CChartOfAccounts> coaList = chartOfAccountsService.getActiveCodeListWithFourClassification();

		// Define your dropdown values
		String[] fundArray = fundList.stream().map(fund -> fund.getCode() + "::" + fund.getName())
				.toArray(String[]::new);
		String[] departmentArray = departmentList.stream()
				.map(department -> department.getCode() + "::" + department.getName()).toArray(String[]::new);
		String[] functionArray = functionList.stream().map(function -> function.getCode() + "::" + function.getName())
				.toArray(String[]::new);
		String[] coaArray = coaList.stream().map(coa -> coa.getGlcode() + "::" + coa.getName()).toArray(String[]::new);

		// Create dropdowns in the remaining rows (from 5th to 100th row)
		for (int row = 4; row <= 100; row++) {
			Row dataRow = sheet.createRow(row);
			for (int col = 0; col < 4; col++) {
				Cell dropdownCell = dataRow.createCell(col);
				switch (col) {
				case 0:
					setDropdownValues(workbook, sheet, dropdownCell, "DropdownList1", fundArray);
					break;
				case 1:
					setDropdownValues(workbook, sheet, dropdownCell, "DropdownList2", departmentArray);
					break;
				case 2:
					setDropdownValues(workbook, sheet, dropdownCell, "DropdownList3", functionArray);
					break;
				case 3:
					setDropdownValues(workbook, sheet, dropdownCell, "DropdownList4", coaArray);
					break;
				}

			}
		}

		return workbook;
	}

	private static void setDropdownValues(Workbook workbook, Sheet sheet, Cell cell, String name, String[] values) {
		// Check if the named range already exists
		Name namedRange = workbook.getName(name);
		if (namedRange == null) {
			// Create a new hidden sheet for options
			Sheet hiddenSheet = workbook.createSheet(name);

			// Add a default "Select" option at the first position
			Row selectRow = hiddenSheet.createRow(0);
			Cell selectCell = selectRow.createCell(0);
			selectCell.setCellValue("");

			for (int i = 0; i < values.length; i++) {
				Row row = hiddenSheet.createRow(i + 1);
				Cell hiddenCell = row.createCell(0);
				hiddenCell.setCellValue(values[i]);
			}

			workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

			// Define the named range
			Name range = workbook.createName();
			range.setNameName(name);
			range.setRefersToFormula(name + "!$A$1:$A$" + (values.length + 1));
		}

		// Set up dropdown using named range
		DataValidationHelper validationHelper = sheet.getDataValidationHelper();
		CellRangeAddressList addressList = new CellRangeAddressList(cell.getRowIndex(), cell.getRowIndex(),
				cell.getColumnIndex(), cell.getColumnIndex());
		DataValidationConstraint validationConstraint = validationHelper.createFormulaListConstraint(name);
		DataValidation dataValidation = validationHelper.createValidation(validationConstraint, addressList);
		sheet.addValidationData(dataValidation);
	}

	@Action(value = "/budget/budgetLoad-beforeUpload")
	public String beforeUpload() {
		originalFiles = (List<FileStoreMapper>) persistenceService.getSession()
				.createQuery("from FileStoreMapper where fileName like '%budget_original%' order by id desc ")
				.setMaxResults(5).list();
		outPutFiles = (List<FileStoreMapper>) persistenceService.getSession()
				.createQuery("from FileStoreMapper where fileName like '%budget_output%' order by id desc ")
				.setMaxResults(5).list();
		return "upload";
	}

	@ValidationErrorPage("upload")
	@Action(value = "/budget/budgetLoad-upload")
	public String upload() {
		try {
			FileInputStream fsIP = new FileInputStream(budgetInXls);

			final POIFSFileSystem fs = new POIFSFileSystem(fsIP);
			final HSSFWorkbook wb = new HSSFWorkbook(fs);
			wb.getNumberOfSheets();
			final HSSFSheet sheet = wb.getSheetAt(0);
			final HSSFRow reRow = sheet.getRow(RE_YEAR_ROW_INDEX);
			final HSSFRow beRow = sheet.getRow(BE_YEAR_ROW_INDEX);
			String reFinYearRange = getStrValue(reRow.getCell(1));
			String beFinYearRange = getStrValue(beRow.getCell(1));
			CFinancialYear reFYear = financialYearDAO.getFinancialYearByFinYearRange(reFinYearRange);
			CFinancialYear beFYear = financialYearDAO.getNextFinancialYearByDate(reFYear.getStartingDate());

			if (!validateFinancialYears(reFYear, beFYear, beFinYearRange))
				throw new ValidationException(
						Arrays.asList(new ValidationError(getText("be.year.is.not.immediate.next.fy.year.of.re.year"),
								getText("be.year.is.not.immediate.next.fy.year.of.re.year"))));
			timeStamp = new Timestamp((new Date()).getTime()).toString().replace(".", "_");
			if (budgetInXlsFileName.contains("_budget_original_")) {
				budgetOriginalFileName = budgetInXlsFileName.split("_budget_original_")[0] + "_budget_original_"
						+ timeStamp + "." + budgetInXlsFileName.split("\\.")[1];
			} else if (budgetInXlsFileName.contains("_budget_output_")) {
				budgetOriginalFileName = budgetInXlsFileName.split("_budget_output_")[0] + "_budget_original_"
						+ timeStamp + "." + budgetInXlsFileName.split("\\.")[1];
			} else {
				if (budgetInXlsFileName.length() > 60) {
					throw new ValidationException(
							Arrays.asList(new ValidationError(getText("file.name.should.be.less.then.60.characters"),
									getText("file.name.should.be.less.then.60.characters"))));
				} else
					budgetOriginalFileName = budgetInXlsFileName.split("\\.")[0] + "_budget_original_" + timeStamp + "."
							+ budgetInXlsFileName.split("\\.")[1];
			}

			final FileStoreMapper originalFileStore = fileStoreService.store(budgetInXls, budgetOriginalFileName,
					budgetInXlsContentType, FinancialConstants.MODULE_NAME_APPCONFIG, false);

			persistenceService.persist(originalFileStore);
			originalFileStoreId = originalFileStore.getFileStoreId();

			List<BudgetUpload> budgetUploadList = loadToBudgetUpload(sheet);

			budgetUploadList = sanitizeBudgetUploadList(budgetUploadList);

			if (isBudgetUploadFileEmpty) {
				fsIP.close();
				throw new ValidationException(new ValidationError(getText("error.while.counting.upload.records"),
						"There should be atleast one record in the upload file"));
			}
			budgetUploadList = validateMasterData(budgetUploadList);
			budgetUploadList = !errorInMasterData ? validateDuplicateData(budgetUploadList) : budgetUploadList;

			if (errorInMasterData) {
				fsIP.close();
				prepareOutPutFileWithErrors(budgetUploadList);
				addActionMessage(getText("error.while.validating.masterdata"));
				return "result";
			}

			budgetUploadList = removeEmptyRows(budgetUploadList);

			budgetUploadList = budgetDetailService.loadBudget(budgetUploadList, reFYear, beFYear);

			fsIP.close();
			Boolean isBudgetDataValid = prepareOutPutFileWithFinalStatus(budgetUploadList);

			if (isBudgetDataValid)
				addActionMessage(getText("budget.load.sucessful"));
			else
				addActionMessage(getText("msg.file.data.invalid"));

		} catch (final ValidationException e) {
			originalFiles = (List<FileStoreMapper>) persistenceService.getSession()
					.createQuery("from FileStoreMapper where fileName like '%budget_original%' order by id desc ")
					.setMaxResults(5).list();
			outPutFiles = (List<FileStoreMapper>) persistenceService.getSession()
					.createQuery("from FileStoreMapper where fileName like '%budget_output%' order by id desc ")
					.setMaxResults(5).list();
			throw new ValidationException(Arrays.asList(new ValidationError(budgetUploadError, budgetUploadError)));
		} catch (final IOException e) {
			originalFiles = (List<FileStoreMapper>) persistenceService.getSession()
					.createQuery("from FileStoreMapper where fileName like '%budget_original%' order by id desc ")
					.setMaxResults(5).list();
			outPutFiles = (List<FileStoreMapper>) persistenceService.getSession()
					.createQuery("from FileStoreMapper where fileName like '%budget_output%' order by id desc ")
					.setMaxResults(5).list();
			throw new ValidationException(Arrays.asList(new ValidationError(budgetUploadError, budgetUploadError)));

		}

		return "result";
	}

	private List<BudgetUpload> sanitizeBudgetUploadList(List<BudgetUpload> budgetUploadList) {

		List<BudgetUpload> tempList = new ArrayList<>();
		String error = "";
		try {
			for (BudgetUpload budget : budgetUploadList) {

				budget.setDeptCode(getSanitizedDataOfCell(budget.getDeptCode()));
				budget.setFundCode(getSanitizedDataOfCell(budget.getFundCode()));
				budget.setFunctionCode(getSanitizedDataOfCell(budget.getFunctionCode()));
				budget.setBudgetHead(getSanitizedDataOfCell(budget.getBudgetHead()));

				tempList.add(budget);
			}
		} catch (Exception e) {
			throw new ValidationException(Arrays.asList(new ValidationError(e.getMessage(), e.getMessage())));
		}

		return tempList;
	}

	private String getSanitizedDataOfCell(String originalData) {

		String[] resultArray = null;
		String sanitizedData = null;

		if (originalData.contains("::")) {

			resultArray = originalData.split("::");
			sanitizedData = resultArray[0].trim();
			return sanitizedData;
		}
		return originalData;
	}

	private void prepareOutPutFileWithErrors(List<BudgetUpload> budgetUploadList) {
		FileInputStream fsIP;
		try {
			fsIP = new FileInputStream(budgetInXls);
			Map<String, String> errorsMap = new HashMap<String, String>();
			final POIFSFileSystem fs = new POIFSFileSystem(fsIP);
			final HSSFWorkbook wb = new HSSFWorkbook(fs);
			wb.getNumberOfSheets();
			final HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row = sheet.getRow(3);
			HSSFCell cell = row.createCell(7);
			cell.setCellValue("Error Reason");
			cell.setAsActiveCell();
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFillForegroundColor(HSSFColor.RED.index);
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cell.setCellStyle(cellStyle);
			for (BudgetUpload budget : budgetUploadList)
				errorsMap.put(budget.getFundCode() + "-" + budget.getFunctionCode() + "-" + budget.getDeptCode() + "-"
						+ budget.getBudgetHead(), budget.getErrorReason());

			for (int i = DATA_STARTING_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
				HSSFRow errorRow = sheet.getRow(i);
				if (!isRowEmpty(errorRow)) {
					HSSFCell errorCell = errorRow.createCell(7);
					String fundCode = getStrValue(sheet.getRow(i).getCell(FUNDCODE_CELL_INDEX));
					fundCode = fundCode != null ? fundCode : "";
					String funcCode = getStrValue(sheet.getRow(i).getCell(FUNCTIONCODE_CELL_INDEX));
					funcCode = funcCode != null ? funcCode : "";
					String deptCode = getStrValue(sheet.getRow(i).getCell(DEPARTMENTCODE_CELL_INDEX));
					deptCode = deptCode != null ? deptCode : "";
					String coaCode = getStrValue(sheet.getRow(i).getCell(GLCODE_CELL_INDEX));
					coaCode = coaCode != null ? coaCode : "";
					String errorMsg = fundCode + "-" + funcCode + "-" + deptCode + "-" + coaCode;
					errorCell.setCellValue(errorsMap.get(errorMsg));
				}
			}

			FileOutputStream output_file = new FileOutputStream(budgetInXls);
			wb.write(output_file);
			output_file.close();
			if (budgetInXlsFileName.contains("_budget_original_")) {
				budgetOutPutFileName = budgetInXlsFileName.split("_budget_original_")[0] + "_budget_output_" + timeStamp
						+ "." + budgetInXlsFileName.split("\\.")[1];
			} else if (budgetInXlsFileName.contains("_budget_output_")) {
				budgetOutPutFileName = budgetInXlsFileName.split("_budget_output_")[0] + "_budget_output_" + timeStamp
						+ "." + budgetInXlsFileName.split("\\.")[1];
			} else {
				if (budgetInXlsFileName.length() > 60) {
					throw new ValidationException(
							Arrays.asList(new ValidationError(getText("file.name.should.be.less.then.60.characters"),
									getText("file.name.should.be.less.then.60.characters"))));
				} else
					budgetOutPutFileName = budgetInXlsFileName.split("\\.")[0] + "_budget_output_" + timeStamp + "."
							+ budgetInXlsFileName.split("\\.")[1];
			}
			final FileStoreMapper outPutFileStore = fileStoreService.store(budgetInXls, budgetOutPutFileName,
					budgetInXlsContentType, FinancialConstants.MODULE_NAME_APPCONFIG);

			persistenceService.persist(outPutFileStore);

			outPutFileStoreId = outPutFileStore.getFileStoreId();
		} catch (FileNotFoundException e) {
			throw new ValidationException(Arrays.asList(new ValidationError(e.getMessage(), e.getMessage())));
		} catch (IOException e) {
			throw new ValidationException(Arrays.asList(new ValidationError(e.getMessage(), e.getMessage())));
		}
	}

	private Boolean prepareOutPutFileWithFinalStatus(List<BudgetUpload> budgetUploadList) {
		FileInputStream fsIP;
		try {
			fsIP = new FileInputStream(budgetInXls);

			Map<String, String> errorsMap = new HashMap<String, String>();
			final POIFSFileSystem fs = new POIFSFileSystem(fsIP);
			final HSSFWorkbook wb = new HSSFWorkbook(fs);
			wb.getNumberOfSheets();
			final HSSFSheet sheet = wb.getSheetAt(0);
			Map<String, String> finalStatusMap = new HashMap<String, String>();

			HSSFRow row = sheet.getRow(3);
			HSSFCell cell = row.createCell(7);
			cell.setCellValue("Status");

			for (BudgetUpload budget : budgetUploadList)
				finalStatusMap.put(budget.getFundCode() + "-" + budget.getFunctionCode() + "-" + budget.getDeptCode()
						+ "-" + budget.getBudgetHead(), budget.getFinalStatus());

			int lastRowNumber = getLastFilledRow(sheet);

			for (int i = DATA_STARTING_ROW_INDEX; i <= lastRowNumber; i++) {
				HSSFRow finalStatusRow = sheet.getRow(i);
				HSSFCell finalStatusCell = finalStatusRow.createCell(7);

				if (!isBudgetRowValid(sheet.getRow(i)))
					return false;

				finalStatusCell
						.setCellValue(finalStatusMap.get((getStrValue(sheet.getRow(i).getCell(FUNDCODE_CELL_INDEX))
								+ "-" + getStrValue(sheet.getRow(i).getCell(FUNCTIONCODE_CELL_INDEX)) + "-"
								+ getStrValue(sheet.getRow(i).getCell(DEPARTMENTCODE_CELL_INDEX)) + "-"
								+ getStrValue(sheet.getRow(i).getCell(GLCODE_CELL_INDEX)))));
			}

			FileOutputStream output_file = new FileOutputStream(budgetInXls);
			wb.write(output_file);
			output_file.close();
			if (budgetInXlsFileName.contains("_budget_original_")) {
				budgetOutPutFileName = budgetInXlsFileName.split("_budget_original_")[0] + "_budget_output_" + timeStamp
						+ "." + budgetInXlsFileName.split("\\.")[1];
			} else if (budgetInXlsFileName.contains("_budget_output_")) {
				budgetOutPutFileName = budgetInXlsFileName.split("_budget_output_")[0] + "_budget_output_" + timeStamp
						+ "." + budgetInXlsFileName.split("\\.")[1];
			} else {
				if (budgetInXlsFileName.length() > 60) {
					throw new ValidationException(
							Arrays.asList(new ValidationError(getText("file.name.should.be.less.then.60.characters"),
									getText("file.name.should.be.less.then.60.characters"))));
				} else
					budgetOutPutFileName = budgetInXlsFileName.split("\\.")[0] + "_budget_output_" + timeStamp + "."
							+ budgetInXlsFileName.split("\\.")[1];
			}
			final FileStoreMapper outPutFileStore = fileStoreService.store(budgetInXls, budgetOutPutFileName,
					budgetInXlsContentType, FinancialConstants.MODULE_NAME_APPCONFIG);
			persistenceService.persist(outPutFileStore);

			outPutFileStoreId = outPutFileStore.getFileStoreId();
			return true;
		} catch (FileNotFoundException e) {
			// throw new ValidationException(Arrays.asList(new
			// ValidationError(e.getMessage(), e.getMessage())));
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			// throw new ValidationException(Arrays.asList(new
			// ValidationError(e.getMessage(), e.getMessage())));
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	private Boolean isBudgetRowValid(final HSSFRow row) {

		HSSFCell cellFund = row.getCell(FUNDCODE_CELL_INDEX);
		HSSFCell cellFunction = row.getCell(FUNCTIONCODE_CELL_INDEX);
		HSSFCell cellDepartment = row.getCell(DEPARTMENTCODE_CELL_INDEX);
		HSSFCell cellGlcode = row.getCell(GLCODE_CELL_INDEX);

		HSSFCell cellReAmount = row.getCell(REAMOUNT_CELL_INDEX);
		HSSFCell cellBeAmount = row.getCell(BEAMOUNT_CELL_INDEX);
		HSSFCell cellPlanningPercent = row.getCell(PLANNINGPERCENTAGE_CELL_INDEX);

		if (cellFund == null || cellFunction == null || cellDepartment == null || cellGlcode == null
				|| cellReAmount == null || cellBeAmount == null || cellPlanningPercent == null)
			return false;
		else
			return true;
	}

	private int getLastFilledRow(final HSSFSheet sheet) {

		int lastFilledRow = sheet.getLastRowNum();

		for (int i = sheet.getLastRowNum(); i >= DATA_STARTING_ROW_INDEX; i--) {

			if (isRowEmpty(sheet.getRow(lastFilledRow)))
				lastFilledRow = lastFilledRow - 1;
			else
				break;

		}
		return lastFilledRow;
	}

	private List<BudgetUpload> removeEmptyRows(List<BudgetUpload> budgetUploadList) {
		List<BudgetUpload> tempList = new ArrayList<>();
		for (BudgetUpload budget : budgetUploadList) {
			if (!budget.getErrorReason().equalsIgnoreCase("Empty Record"))
				tempList.add(budget);

		}
		return tempList;
	}

	private List<BudgetUpload> validateMasterData(List<BudgetUpload> budgetUploadList) {
		List<BudgetUpload> tempList = new ArrayList<>();
		try {
			String error = "";
			Map<String, Fund> fundMap = new HashMap<String, Fund>();
			Map<String, CFunction> functionMap = new HashMap<String, CFunction>();
			Map<String, Department> departmentMap = new HashMap<String, Department>();
			Map<String, CChartOfAccounts> coaMap = new HashMap<String, CChartOfAccounts>();
			List<Fund> fundList = fundDAO.findAllActiveIsLeafFunds();
			List<CFunction> functionList = functionDAO.getAllActiveFunctions();
			List<Department> departmentList = masterDataCache.get("egi-department");
			List<CChartOfAccounts> coaList = chartOfAccountsService.findAll();
			for (Fund fund : fundList)
				fundMap.put(fund.getCode(), fund);
			for (CFunction function : functionList)
				functionMap.put(function.getCode(), function);
			for (Department department : departmentList)
				departmentMap.put(department.getCode(), department);
			for (CChartOfAccounts coa : coaList)
				coaMap.put(coa.getGlcode(), coa);

			for (BudgetUpload budget : budgetUploadList) {
				error = "";
				if (budget.getFundCode() != null && budget.getFundCode().isEmpty())
					error = error + getText("error.while.checking.fund.value");
				else if (fundMap.get(budget.getFundCode()) == null)
					error = error + getText("fund.is.not.exist") + budget.getFundCode();
				else
					budget.setFund(fundMap.get(budget.getFundCode()));
				if (budget.getFunctionCode() != null && budget.getFunctionCode().isEmpty())
					error = error + getText("error.while.checking.function.value");
				else if (functionMap.get(budget.getFunctionCode()) == null)
					error = error + " " + getText("function.is.not.exist") + budget.getFunctionCode();
				else
					budget.setFunction(functionMap.get(budget.getFunctionCode()));

				if (budget.getDeptCode() != null && budget.getFundCode().isEmpty())
					error = error + getText("error.while.checking.dept.value");
				else if (departmentMap.get(budget.getDeptCode()) == null)
					error = error + " " + getText("department.is.not.exist") + budget.getDeptCode();
				else
					budget.setDeptCode(budget.getDeptCode());

				if (budget.getBudgetHead() != null && budget.getBudgetHead().isEmpty())
					error = error + getText("error.while.checking.coa.value");
				else if (coaMap.get(budget.getBudgetHead()) == null)
					error = error + " " + getText("coa.is.not.exist") + budget.getBudgetHead();
				else
					budget.setCoa(coaMap.get(budget.getBudgetHead()));

				if (budget.getBeAmount() != null && budget.getBeAmount().compareTo(new BigDecimal(0)) == 0)
					error = error + " " + getText("error.while.checking.be.value");

				if (budget.getReAmount() != null && budget.getReAmount().compareTo(new BigDecimal(0)) == 0)
					error = error + " " + getText("error.while.checking.re.value");

				budget.setErrorReason(error);
				if (!error.equalsIgnoreCase("")) {
					errorInMasterData = true;
				}
				tempList.add(budget);
			}
		} catch (final ValidationException e) {
			throw new ValidationException(Arrays
					.asList(new ValidationError(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getMessage())));
		} /*
			 * catch (final Exception e) { throw new ValidationException(Arrays.asList(new
			 * ValidationError(e.getMessage(), e.getMessage()))); }
			 */
		return tempList;
	}

	private List<BudgetUpload> validateDuplicateData(List<BudgetUpload> budgetUploadList) {
		List<BudgetUpload> tempList = new ArrayList<>();
		try {
			String error = "";
			Map<String, BudgetUpload> budgetUploadMap = new HashMap<String, BudgetUpload>();
			for (BudgetUpload budget : budgetUploadList) {
				if (budget.getFundCode() != null && budget.getFunctionCode() != null && budget.getDeptCode() != null
						&& budget.getBudgetHead() != null && !budget.getFundCode().equalsIgnoreCase("")
						&& !budget.getFunctionCode().equalsIgnoreCase("") && !budget.getDeptCode().equalsIgnoreCase("")
						&& !budget.getBudgetHead().equalsIgnoreCase(""))
					if (budgetUploadMap.get(budget.getFundCode() + "-" + budget.getFunctionCode() + "-"
							+ budget.getDeptCode() + "-" + budget.getBudgetHead()) == null)
						budgetUploadMap.put(budget.getFundCode() + "-" + budget.getFunctionCode() + "-"
								+ budget.getDeptCode() + "-" + budget.getBudgetHead(), budget);
					else {
						budget.setErrorReason(budget.getErrorReason() + getText("duplicate.record"));
						errorInMasterData = true;
					}
				else if (budget.getFundCode() == null && budget.getFunctionCode() == null
						&& budget.getDeptCode() == null && budget.getBudgetHead() == null) {
					budget.setErrorReason(getText("empty.record"));
				} else {
					budget.setErrorReason(getText("empty.record"));
				}

				tempList.add(budget);
			}
		} catch (final ValidationException e) {
			throw new ValidationException(Arrays
					.asList(new ValidationError(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getMessage())));
		} /*
			 * catch (final Exception e) { throw new ValidationException(Arrays.asList(new
			 * ValidationError(e.getMessage(), e.getMessage()))); }
			 */
		return tempList;
	}

	private List<BudgetUpload> loadToBudgetUpload(HSSFSheet sheet) {
		List<BudgetUpload> budgetUploadList = new ArrayList<BudgetUpload>();
		try {

			for (int i = DATA_STARTING_ROW_INDEX; i <= sheet.getLastRowNum(); i++)
				if (!isRowEmpty(sheet.getRow(i))) {
					budgetUploadList.add(getBudgetUpload(sheet.getRow(i)));
					isBudgetUploadFileEmpty = false;
				}
		} catch (final ValidationException e) {
			throw new ValidationException(Arrays
					.asList(new ValidationError(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getMessage())));
		} /*
			 * catch (final Exception e) { throw new ValidationException(Arrays.asList(new
			 * ValidationError(e.getMessage(), e.getMessage()))); }
			 */
		return budgetUploadList;

	}

	public static boolean isRowEmpty(HSSFRow row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			HSSFCell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK)
				return false;
		}
		return true;
	}

	private BudgetUpload getBudgetUpload(HSSFRow row) {
		BudgetUpload budget = new BudgetUpload();
		try {
			if (row != null) {
				budget.setFundCode(getStrValue(row.getCell(FUNDCODE_CELL_INDEX)) == null ? ""
						: getStrValue(row.getCell(FUNDCODE_CELL_INDEX)));
				budget.setDeptCode(getStrValue(row.getCell(DEPARTMENTCODE_CELL_INDEX)) == null ? ""
						: getStrValue(row.getCell(DEPARTMENTCODE_CELL_INDEX)));
				budget.setFunctionCode(getStrValue(row.getCell(FUNCTIONCODE_CELL_INDEX)) == null ? ""
						: getStrValue(row.getCell(FUNCTIONCODE_CELL_INDEX)));
				budget.setBudgetHead(getStrValue(row.getCell(GLCODE_CELL_INDEX)) == null ? ""
						: getStrValue(row.getCell(GLCODE_CELL_INDEX)));
				budget.setReAmount(
						BigDecimal.valueOf(Long.valueOf(getStrValue(row.getCell(REAMOUNT_CELL_INDEX)) == null ? "0"
								: getStrValue(row.getCell(REAMOUNT_CELL_INDEX)))));
				budget.setBeAmount(
						BigDecimal.valueOf(Long.valueOf(getStrValue(row.getCell(BEAMOUNT_CELL_INDEX)) == null ? "0"
								: getStrValue(row.getCell(BEAMOUNT_CELL_INDEX)))));
				budget.setPlanningPercentage(getNumericValue(row.getCell(PLANNINGPERCENTAGE_CELL_INDEX)) == null ? 0
						: getNumericValue(row.getCell(PLANNINGPERCENTAGE_CELL_INDEX)).longValue());
			}
		} catch (final ValidationException e) {
			throw new ValidationException(Arrays
					.asList(new ValidationError(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getMessage())));
		} /*
			 * catch (final Exception e) { throw new ValidationException(Arrays.asList(new
			 * ValidationError(e.getMessage(), e.getMessage()))); }
			 */
		return budget;
	}

	private boolean validateFinancialYears(CFinancialYear reFYear, CFinancialYear beFYear, String beYear) {
		try {

			if (reFYear == null)
				throw new ValidationException(Arrays
						.asList(new ValidationError(getText("re.year.is.not.exist"), getText("re.year.is.not.exist"))));

			if (beFYear == null)
				throw new ValidationException(Arrays
						.asList(new ValidationError(getText("be.year.is.not.exist"), getText("be.year.is.not.exist"))));
			if (beFYear.getFinYearRange().equalsIgnoreCase(beYear))
				return true;
			else
				return false;
		} catch (final ValidationException e) {
			throw new ValidationException(Arrays
					.asList(new ValidationError(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getMessage())));
		} /*
			 * catch (final Exception e) { throw new ValidationException(Arrays.asList(new
			 * ValidationError(getText("year.is.not.exist"),
			 * getText("year.is.not.exist")))); }
			 */
	}

	@Override
	public void validate() {

	}

	private String getStrValue(final HSSFCell cell) {
		if (cell == null && cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
			return null;
		double numericCellValue = 0d;
		String strValue = "";
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_NUMERIC:
			numericCellValue = cell.getNumericCellValue();
			final DecimalFormat decimalFormat = new DecimalFormat("#");
			strValue = decimalFormat.format(numericCellValue);
			break;
		case HSSFCell.CELL_TYPE_STRING:
			strValue = cell.getStringCellValue();
			break;
		}
		return strValue.trim().isEmpty() ? null : strValue.trim();

	}

	private BigDecimal getNumericValue(final HSSFCell cell) {
		if (cell == null && cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
			return null;
		double numericCellValue = 0d;
		BigDecimal bigDecimalValue = BigDecimal.ZERO;
		String strValue = "";

		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_NUMERIC:
			numericCellValue = cell.getNumericCellValue();
			bigDecimalValue = BigDecimal.valueOf(numericCellValue);
			break;
		case HSSFCell.CELL_TYPE_STRING:
			strValue = cell.getStringCellValue();
			strValue = strValue.replaceAll("[^\\p{L}\\p{Nd}]", "");
			if (strValue != null && strValue.contains("E+")) {
				final String[] split = strValue.split("E+");
				String mantissa = split[0].replaceAll(".", "");
				final int exp = Integer.parseInt(split[1]);
				while (mantissa.length() <= exp + 1)
					mantissa += "0";
				numericCellValue = Double.parseDouble(mantissa);
				bigDecimalValue = BigDecimal.valueOf(numericCellValue);
			} else if (strValue != null && strValue.contains(","))
				strValue = strValue.replaceAll(",", "");
			// Ignore the error and continue Since in numric field we find empty or non
			// numeric value
			try {
				numericCellValue = Double.parseDouble(strValue);
				bigDecimalValue = BigDecimal.valueOf(numericCellValue);
			} catch (final NumberFormatException e) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Found : Non numeric value in Numeric Field :" + strValue + ":");
			}
			break;
		}
		return bigDecimalValue;

	}

	public File getBudgetInXls() {
		return budgetInXls;
	}

	public void setBudgetInXls(File budgetInXls) {
		this.budgetInXls = budgetInXls;
	}

	public String getOriginalFileStoreId() {
		return originalFileStoreId;
	}

	public void setOriginalFileStoreId(String originalFileStoreId) {
		this.originalFileStoreId = originalFileStoreId;
	}

	public void setBudgetInXlsFileName(String budgetInXlsFileName) {
		this.budgetInXlsFileName = budgetInXlsFileName;
	}

	public void setBudgetInXlsContentType(String budgetInXlsContentType) {
		this.budgetInXlsContentType = budgetInXlsContentType;
	}

	public String getOutPutFileStoreId() {
		return outPutFileStoreId;
	}

	public void setOutPutFileStoreId(String outPutFileStoreId) {
		this.outPutFileStoreId = outPutFileStoreId;
	}

	public List<FileStoreMapper> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<FileStoreMapper> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public List<FileStoreMapper> getOutPutFiles() {
		return outPutFiles;
	}

	public void setOutPutFiles(List<FileStoreMapper> outPutFiles) {
		this.outPutFiles = outPutFiles;
	}

	public String getBudgetOriginalFileName() {
		return budgetOriginalFileName;
	}

	public void setBudgetOriginalFileName(String budgetOriginalFileName) {
		this.budgetOriginalFileName = budgetOriginalFileName;
	}

	public String getBudgetOutPutFileName() {
		return budgetOutPutFileName;
	}

	public void setBudgetOutPutFileName(String budgetOutPutFileName) {
		this.budgetOutPutFileName = budgetOutPutFileName;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

}
