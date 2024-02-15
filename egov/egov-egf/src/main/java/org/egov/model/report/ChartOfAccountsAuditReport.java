package org.egov.model.report;

import java.util.Date;

import javax.persistence.Column;

import org.hibernate.validator.constraints.SafeHtml;

public class ChartOfAccountsAuditReport {

	private String id;

	private String rev;

	private String glcode;

	private String name;

	private String description;

	private Boolean isActiveForPosting;

	private String parentId;

	private String purposeId;

	@SafeHtml
	private String purpose;

	@SafeHtml
	private String operation;

	@SafeHtml
	private String type;

	@SafeHtml
	private String classCoa;

	@SafeHtml
	private String classification;

	private Boolean functionReqd;

	private Boolean budgetCheckReq;

	private String scheduleId;

	private String recieptScheduleId;

	private String recieptOperation;

	private String paymentScheduleId;

	private String paymentOperation;

	private String detailType;

	@SafeHtml
	private String majorCode;

	private String fieScheduleId;

	private String fieOperation;

	private Date createDate;

	private String createdBy;

	private String lastModifiedDate;

	private String lastModifiedBy;

	private String version;

	private String revType;	
	
    private String nameOfmodifyingUser;        

	public String getNameOfmodifyingUser() {
		return nameOfmodifyingUser;
	}

	public void setNameOfmodifyingUser(String nameOfmodifyingUser) {
		this.nameOfmodifyingUser = nameOfmodifyingUser;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getGlcode() {
		return glcode;
	}

	public void setGlcode(String glcode) {
		this.glcode = glcode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsActiveForPosting() {
		return isActiveForPosting;
	}

	public void setIsActiveForPosting(Boolean isActiveForPosting) {
		this.isActiveForPosting = isActiveForPosting;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getPurposeId() {
		return purposeId;
	}

	public void setPurposeId(String purposeId) {
		this.purposeId = purposeId;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassCoa() {
		return classCoa;
	}

	public void setClassCoa(String classCoa) {
		this.classCoa = classCoa;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public Boolean getFunctionReqd() {
		return functionReqd;
	}

	public void setFunctionReqd(Boolean functionReqd) {
		this.functionReqd = functionReqd;
	}

	public Boolean getBudgetCheckReq() {
		return budgetCheckReq;
	}

	public void setBudgetCheckReq(Boolean budgetCheckReq) {
		this.budgetCheckReq = budgetCheckReq;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getRecieptScheduleId() {
		return recieptScheduleId;
	}

	public void setRecieptScheduleId(String recieptScheduleId) {
		this.recieptScheduleId = recieptScheduleId;
	}

	public String getPaymentScheduleId() {
		return paymentScheduleId;
	}

	public void setPaymentScheduleId(String paymentScheduleId) {
		this.paymentScheduleId = paymentScheduleId;
	}

	public String getPaymentOperation() {
		return paymentOperation;
	}

	public void setPaymentOperation(String paymentOperation) {
		this.paymentOperation = paymentOperation;
	}

	public String getMajorCode() {
		return majorCode;
	}

	public void setMajorCode(String majorCode) {
		this.majorCode = majorCode;
	}

	public String getFieScheduleId() {
		return fieScheduleId;
	}

	public void setFieScheduleId(String fieScheduleId) {
		this.fieScheduleId = fieScheduleId;
	}

	public String getFieOperation() {
		return fieOperation;
	}

	public void setFieOperation(String fieOperation) {
		this.fieOperation = fieOperation;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}	

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRevType() {
		return revType;
	}

	public void setRevType(String revType) {
		this.revType = revType;
	}

	public String getRecieptOperation() {
		return recieptOperation;
	}

	public void setRecieptOperation(String recieptOperation) {
		this.recieptOperation = recieptOperation;
	}

	public String getDetailType() {
		return detailType;
	}

	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}

}
