<%@ include file="/includes/taglibs.jsp"%>
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

<script>
	function validateWorkFlowApprover(name,errorDivId) {
		document.getElementById("workFlowAction").value=name;
	    var approverPosId = document.getElementById("approverPositionId");
	    if(approverPosId) {
			var approver = approverPosId.options[approverPosId.selectedIndex].text; 
			document.getElementById("approverName").value= approver.split('~')[0];
		}     
	   return  onSubmit();
	}

	function validateWorkFlowApprover(name) {
	    document.getElementById("workFlowAction").value=name;
	    var approverPosId = document.getElementById("approverPositionId");
	    if(approverPosId && approverPosId.value != -1 && approverPosId.value != "") {
			var approver = approverPosId.options[approverPosId.selectedIndex].text; 
			document.getElementById("approverName").value= approver.split('~')[0];
		}   
	    if ((name=="Reject" || name=="reject")) {
	    	var approverComments = document.getElementById("approverComments").value;
	    	if (approverComments == null || approverComments == "") {
	    		bootbox.alert("<s:text name='msg.please.enter.approver.remarks'/>");
				return false;
	    	}
		}
	    if ((name=="Cancel" || name=="cancel")) {
	    	var approverComments = document.getElementById("approverComments").value;
	    	if (approverComments == null || approverComments == "") {
	    		bootbox.alert("<s:text name='msg.please.enter.approver.remarks'/> ");
				return false;
	    	}
		}
		<s:if test="%{getNextAction()!='END'}">
	    if((name=="Forward" || name=="forward") && approverPosId && (approverPosId.value == -1 || approverPosId.value == "")) {
	    	bootbox.alert("<s:text name='msg.please.select.approver'/> ");
			return false;
	    }
	    
	    if((name=="Create And Approve")) {

		    if(!validateCutOff())
	    		return false;
	    }
	    
	    </s:if>
	    return  onSubmit();
	}
</script>
<div class="buttonbottom" align="center">
	<s:hidden id="workFlowAction" name="workFlowAction" />
	<table style="width: 100%; text-align: center;">
		<tr>
			<td><s:iterator value="%{getValidActions()}" var="validAction">
					<s:if test="%{validAction!=''}">
						<s:submit type="submit" cssClass="buttonsubmit"
							value="%{validAction}" id="%{validAction}" name="%{validAction}"
							onclick="return validateWorkFlowApprover('%{validAction}','jsValidationErrors');" />
					</s:if>
				</s:iterator> <input type="button" name="button2" id="button2" value="<s:text name='lbl.close'/>"
				class="button" onclick="window.parent.postMessage('close','*');window.close();" /></td>
		</tr>
	</table>
</div>