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
/*
jQuery('#btnsearch').click(function(e) {
	callAjaxSearch();
});
*/
jQuery('#btnNewContractor').click(function(e) {
	var screenWidth = window.screen.availWidth;
	var screenHeight = window.screen.availHeight;
	var windowFeatures = 'width=' + screenWidth + ',height=' + screenHeight;
	//window.open('/services/EGF/contractor/newContractorform', '', 'width=800, height=600');
	window.open('/services/EGF/contractor/newContractorform', '', windowFeatures);
});

$(document).ready(function() {

	refreshViewFromRadionOptionValue($('input[name=contractorType]:checked').val(), true);

	$('input[type=radio][name=contractorType]').change(function() {
		refreshViewFromRadionOptionValue(this.value, false);
	});

});

function refreshViewFromRadionOptionValue(optionValue, isFromPageLoad) {
	if (optionValue === "FIRM") {
		$('#registerationNo').show();
		$("#registerationNo").attr('required', 'required');

	}
	else if (optionValue === "INDIVIDUALS") {
		$('#registerationNo').hide();
		$('*[required]').removeAttr('required');
	}
}
function getFormData($form) {
	var unindexed_array = $form.serializeArray();
	var indexed_array = {};

	$.map(unindexed_array, function(n, i) {
		indexed_array[n['name']] = n['value'];
	});

	return indexed_array;
}

function callAjaxSearch() {
	drillDowntableContainer = jQuery("#resultTable");
	jQuery('.report-section').removeClass('display-hide');
	reportdatatable = drillDowntableContainer
		.dataTable({
			ajax: {
				url: "/services/EGF/contractor/ajaxsearch/" + $('#mode').val(),
				type: "POST",
				"data": getFormData(jQuery('form'))
			},
			"fnRowCallback": function(row, data, index) {
				$(row).on(
					'click',
					function() {
						console.log(data.id);
						window.open('/services/EGF/contractor/' + $('#mode').val()
							+ '/' + data.id, '',
							'width=800, height=600');
					});
			},
			"bDestroy": true,
			dom: "<'row'<'col-xs-12 pull-right'f>r>t<'row buttons-margin'<'col-md-3 col-xs-6'i><'col-md-3  col-xs-6'l><'col-md-3 col-xs-6'B><'col-md-3 col-xs-6 text-right'p>>",
			buttons: [
				{
					extend: 'print',
					title: 'Contractor Master',
					filename: 'Contractor Master'
				}, {
					extend: 'pdf',
					title: 'Contractor Master',
					filename: 'Contractor Master'
				}, {
					extend: 'excel',
					message: 'Contractor Master',
					filename: 'Contractor Master'
				}
			],
			aaSorting: [],
			columns: [{
				"data": "name",
				"sClass": "text-left"
			}, {
				"data": "code",
				"sClass": "text-left"
			}, {
				"data": "status",
				"sClass": "text-left"
			}]
		});
}

function callAjaxForContractorData() {
	var screenWidth = window.screen.availWidth;
	var screenHeight = window.screen.availHeight;
	var windowFeatures = 'width=' + screenWidth + ',height=' + screenHeight;
	drillDowntableContainer = jQuery("#resultTable");
	jQuery('.report-section').removeClass('display-hide');
	reportdatatable = drillDowntableContainer
		.dataTable({
			ajax: {
				url: "/services/EGF/contractor/ajaxsearch/new/edit",
				type: "POST",
				"data": getFormData(jQuery('form'))
			},
			"bDestroy": true,
			dom: "<'row'<'col-xs-12 pull-right'f>r>t<'row buttons-margin'<'col-md-3 col-xs-6'i><'col-md-3 col-xs-6'l><'col-md-3 col-xs-6'B><'col-md-3 col-xs-6 text-right'p>>",
			buttons: [
				{
					extend: 'print',
					title: 'Contractor Master',
					filename: 'Contractor Master',
					exportOptions: {
						columns: [1, 2, 3] // Include only the columns you want in the Print option
					}
				}, {
					extend: 'pdf',
					title: 'Contractor Master',
					filename: 'Contractor Master',
					exportOptions: {
						columns: [1, 2, 3] // Include only the columns you want in the Print option
					}
				}, {
					extend: 'excel',
					message: 'Contractor Master',
					filename: 'Contractor Master',
					exportOptions: {
						columns: [1, 2, 3] // Include only the columns you want in the Print option
					}
				}
			],
			aaSorting: [],
			columns: [
				{
					"data": "id",
					"sClass": "text-center",
					"render": function(data, type, row) {
						// Create the "View" and "Edit" buttons for each row in the first column	
						//console.log("row : "+JSON.stringify(row));					
						var viewButton = '<button class="view-button" data-id="' + data + '">View</button>';
						var editButton = '<button class="edit-button" data-id="' + data + '">Edit</button>';
						//var viewButton = '<img src="view.png" alt="View" class="view-button" data-id="' + data + '">';
						//var editButton = '<img src="edit.png" alt="Edit" class="edit-button" data-id="' + data + '">';
						return viewButton + " " + editButton;
					}
				},
				{
					"data": "name",
					"sClass": "text-left"
				},
				{
					"data": "code",
					"sClass": "text-left"
				},
				{
					"data": "contactPerson",
					"sClass": "text-left"
				},
				{
					"data": "mobileNumber",
					"sClass": "text-left"
				},
				{
					"data": "tinNumber",
					"sClass": "text-left"
				},
				{
					"data": "status",
					"sClass": "text-left"
				}
			],
			columnDefs: [
				{
					targets: 0, // Target the first column
					width: "60px" // Set the width of the first column
				}
			]
		});

	// Handle button click events
	drillDowntableContainer.on('click', '.view-button', function() {

		var id = $(this).data('id');
		window.open('/services/EGF/contractor/view'
			+ '/' + id, '',
			windowFeatures);
	});

	drillDowntableContainer.on('click', '.edit-button', function() {

		var id = $(this).data('id');
		window.open('/services/EGF/contractor/edit'
			+ '/' + id, '',
			windowFeatures);
	});
}





