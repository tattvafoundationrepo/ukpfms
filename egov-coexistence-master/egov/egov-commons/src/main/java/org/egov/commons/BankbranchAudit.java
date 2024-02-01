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
package org.egov.commons;

public class BankbranchAudit {

	private String id;

	private String bankName;

	private String branchcode;

	private String branchname;

	private String branchaddress1;

	private String branchaddress2;

	private String branchcity;

	private String branchstate;

	private String branchpin;

	private String branchphone;

	private String branchfax;

	private String contactperson;

	private String isactive;

	private String narration;

	private String branchMICR;

	private String lastModifiedBy;

	private String lastModifiedDate;

	private String nameOfmodifyingUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBranchcode() {
		return branchcode;
	}

	public void setBranchcode(String branchcode) {
		this.branchcode = branchcode;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getBranchaddress1() {
		return branchaddress1;
	}

	public void setBranchaddress1(String branchaddress1) {
		this.branchaddress1 = branchaddress1;
	}

	public String getBranchaddress2() {
		return branchaddress2;
	}

	public void setBranchaddress2(String branchaddress2) {
		this.branchaddress2 = branchaddress2;
	}

	public String getBranchcity() {
		return branchcity;
	}

	public void setBranchcity(String branchcity) {
		this.branchcity = branchcity;
	}

	public String getBranchstate() {
		return branchstate;
	}

	public void setBranchstate(String branchstate) {
		this.branchstate = branchstate;
	}

	public String getBranchpin() {
		return branchpin;
	}

	public void setBranchpin(String branchpin) {
		this.branchpin = branchpin;
	}

	public String getBranchphone() {
		return branchphone;
	}

	public void setBranchphone(String branchphone) {
		this.branchphone = branchphone;
	}

	public String getBranchfax() {
		return branchfax;
	}

	public void setBranchfax(String branchfax) {
		this.branchfax = branchfax;
	}

	public String getContactperson() {
		return contactperson;
	}

	public void setContactperson(String contactperson) {
		this.contactperson = contactperson;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public String getBranchMICR() {
		return branchMICR;
	}

	public void setBranchMICR(String branchMICR) {
		this.branchMICR = branchMICR;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getNameOfmodifyingUser() {
		return nameOfmodifyingUser;
	}

	public void setNameOfmodifyingUser(String nameOfmodifyingUser) {
		this.nameOfmodifyingUser = nameOfmodifyingUser;
	}

}