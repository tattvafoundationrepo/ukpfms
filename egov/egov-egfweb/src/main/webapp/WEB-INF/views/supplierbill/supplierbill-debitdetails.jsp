<%--
  ~    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) 2017  eGovernments Foundation
  ~
  ~     The updated version of eGov suite of products as by eGovernments Foundation
  ~     is available at http://www.egovernments.org
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program. If not, see http://www.gnu.org/licenses/ or
  ~     http://www.gnu.org/licenses/gpl.html .
  ~
  ~     In addition to the terms of the GPL license to be adhered to in using this
  ~     program, the following additional terms are to be complied with:
  ~
  ~         1) All versions of this program, verbatim or modified must carry this
  ~            Legal Notice.
  ~            Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~            derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~            For any further queries on attribution, including queries on brand guidelines,
  ~            please contact contact@egovernments.org
  ~
  ~         2) Any misrepresentation of the origin of the material is prohibited. It
  ~            is required that all modified versions of this material be marked in
  ~            reasonable ways as different from the original version.
  ~
  ~         3) This license does not grant any rights to any user of the program
  ~            with regards to rights under trademark law for use of the trade names
  ~            or trademarks of eGovernments Foundation.
  ~
  ~   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  ~
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<div class="panel-heading">
	<div class="panel-title">
		<spring:message code="lbl.debit.details" text="Debit details"/>
	</div>
</div>
<div class="panel-body">
<input type="hidden" value="${egBillregister.debitDetails.size()}" id="debitAmountrowcount"/>
	<table class="table table-bordered" id="tbldebitdetails">
		<thead>
			<tr>
				<th><spring:message code="lbl.account.code" text="Account Code"/></th>
				<th><spring:message code="lbl.account.head" text="Account Head"/></th>
				<%-- <th><spring:message code="lbl.budget" text="Budget"/></th> --%>
				<th><spring:message code="lbl.debit.amount" text="Debit Amount"/></th>
				<th><spring:message code="lbl.action" text="Action"/></th>
			</tr>
		</thead>
		<tbody>
			<c:choose>
				<c:when test="${egBillregister.debitDetails.size() == 0}">
					<tr id="debitdetailsrow">
						<td>
							<input type="text" id="debitDetails[0].debitGlcode" name="debitDetails[0].debitGlcode" class="form-control table-input debitDetailGlcode debitGlcode"  data-errormsg="Account Code is mandatory!" data-idx="0" data-optional="0"   placeholder="Type first 3 letters of Account code" >
							<form:hidden path="debitDetails[0].glcodeid" id="debitDetails[0].glcodeid" class="form-control table-input hidden-input debitdetailid"/> 
						</td>
						<td>
							<input type="text" id="debitDetails[0].debitAccountHead" name="debitDetails[0].debitAccountHead" class="form-control debitdetailname" disabled>  
						</td>
						
						 <%-- <td>
							<form:input path="debitDetails[0].debitbudget" id="debitDetails[0].debitbudget"  data-errormsg="Budget is mandatory!" onkeyup="decimalvalue(this);" onblur="calcualteBudgetAmount();" data-pattern="decimalvalue" data-idx="0" data-optional="0" class="form-control table-input text-right budgetAmount"   maxlength="12"  />
						</td>   --%>
						 
						<td>
							<form:input path="debitDetails[0].debitamount" id="debitDetails[0].debitamount"  data-errormsg="Debit Amount is mandatory!" onkeyup="decimalvalue(this);" onblur="calcualteNetpaybleAmount();" data-pattern="decimalvalue" data-idx="0" data-optional="0" class="form-control table-input text-right debitAmount"   maxlength="12"  />
						</td> 
						<td class="text-center"><span style="cursor:pointer;" onclick="addDebitDetailsRow();"><i class="fa fa-plus" aria-hidden="true"></i></span>
						 <span class="add-padding debit-delete-row" onclick="deleteDebitDetailsRow(this);"><i class="fa fa-trash"  aria-hidden="true" data-toggle="tooltip" title="" data-original-title="Delete!"></i></span> </td>
					</tr>
				</c:when>
				<c:otherwise>
						<c:forEach items="${egBillregister.debitDetails}" var="billDeatils" varStatus="item">
							<tr id="debitdetailsrow">
								<td>
									<input type="text" id="debitDetails[${item.index }].debitGlcode" name="debitDetails[${item.index }].debitGlcode" class="form-control table-input debitDetailGlcode debitGlcode"  data-errormsg="Account Code is mandatory!" data-idx="0" data-optional="0"   placeholder="Type first 3 letters of Account code" value="${billDeatils.chartOfAccounts.glcode }" >
									<form:hidden path="debitDetails[${item.index }].glcodeid" id="debitDetails[${item.index }].glcodeid" class="form-control table-input hidden-input debitdetailid" value="${billDeatils.glcodeid }"/> 
								</td>
								<td>
									<input type="text" id="debitDetails[${item.index }].debitAccountHead" name="debitDetails[${item.index }].debitAccountHead" class="form-control debitdetailname" disabled value="${billDeatils.chartOfAccounts.name }">  
								</td>
								<%-- 
									<td>
									<input type="text" id="debitDetails[${item.index }].debitBudget" name="debitDetails[${item.index }].debitBudget" class="form-control debitdetailname" disabled value="${billDeatils.chartOfAccounts.name }">  
								</td> --%>
								
								<%-- <td>
									<form:input path="debitDetails[${item.index }].debitbudget" id="debitDetails[${item.index }].debitbudget"  data-errormsg="Budget Amount is mandatory!" onkeyup="decimalvalue(this);" onblur="calcualteNetbudgetAmount();" data-pattern="decimalvalue" data-idx="0" data-optional="0" class="form-control table-input text-right budgetAmount"   maxlength="12" value="${billDeatils.creditamount }" />
								</td> 
				                   --%>
								<td>
									<form:input path="debitDetails[${item.index }].debitamount" id="debitDetails[${item.index }].debitamount"  data-errormsg="Debit Amount is mandatory!" onkeyup="decimalvalue(this);" onblur="calcualteNetpaybleAmount();" data-pattern="decimalvalue" data-idx="0" data-optional="0" class="form-control table-input text-right debitAmount"   maxlength="12" value="${billDeatils.creditamount }" />
								</td> 
								<td class="text-center"><span style="cursor:pointer;" onclick="addDebitDetailsRow();"><i class="fa fa-plus" aria-hidden="true"></i></span>
								 <span class="add-padding debit-delete-row" onclick="deleteDebitDetailsRow(this);"><i class="fa fa-trash"  aria-hidden="true" data-toggle="tooltip" title="" data-original-title="Delete!"></i></span> </td>
							</tr>
						</c:forEach>
				</c:otherwise>
			</c:choose>
		</tbody>
	</table>
</div>
