
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, © 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/

var CFW_DASHBOARDLIST_URL = "./list";

/******************************************************************
 * Reset the view.
 ******************************************************************/
function cfw_dashboardlist_reset(){
	var pillsTab = $("#pills-tab");
	
	if(pillsTab.length == 0){
		$("#cfw-container").append(
			'<ul class="nav nav-pills mb-3" id="pills-tab" role="tablist">'
				+'<li class="nav-item"><a class="nav-link active" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'mydashboards\'})"><i class="fas fa-user-circle mr-2"></i>My Dashboards</a></li>'
				+'<li class="nav-item"><a class="nav-link" data-toggle="pill" href="#" role="tab" onclick="cfw_dashboardlist_draw({tab: \'shareddashboards\'})"><i class="fas fa-share-alt mr-2"></i>Shared Dashboards</a></li>'
			+'</ul>'
			+'<div id="tab-content"></div>'
		);
	}
	$("#tab-content").html("");
}

/******************************************************************
 * Create Role
 ******************************************************************/
function cfw_dashboardlist_createDashboard(){
	
	var html = $('<div id="cfw-usermgmt-createDashboard">');	

	CFW.http.getForm('cfwCreateDashboardForm', html);
	
	CFW.ui.showModal(CFWL('cfw_dashboardlist_createDashboard', 
			CFWL("cfw_dashboardlist_createDashboard", "Create Dashboard")), 
			html, "CFW.cache.clearCache(); cfw_dashboardlist_draw({tab: 'mydashboards'})");
	
}
/******************************************************************
 * Edit Role
 ******************************************************************/
function cfw_dashboardlist_editDashboard(roleID){
	
	var allDiv = $('<div id="cfw-usermgmt">');	

	//-----------------------------------
	// Role Details
	//-----------------------------------
	var detailsDiv = $('<div id="cfw-usermgmt-details">');
	detailsDiv.append('<h2>'+CFWL('cfw_dashboardlist_dashboard', "Dashboard")+' Details</h2>');
	allDiv.append(detailsDiv);
	

	CFW.ui.showModal(
			CFWL("cfw_dashboardlist_editDashboard","Edit Dashboard"), 
			allDiv, 
			"CFW.cache.clearCache(); cfw_dashboardlist_draw({tab: 'mydashboards'})"
	);
	
	//-----------------------------------
	// Load Form
	//-----------------------------------
	CFW.http.createForm(CFW_DASHBOARDLIST_URL, {action: "getform", item: "editdashboard", id: roleID}, detailsDiv);
	
}

/******************************************************************
 * Delete
 ******************************************************************/
function cfw_dashboardlist_delete(item, ids){
	
	params = {action: "delete", item: item, ids: ids};
	CFW.http.getJSON(CFW_DASHBOARDLIST_URL, params, 
		function(data) {
			if(data.success){
				//CFW.ui.showSmallModal('Success!', '<span>The selected '+item+' were deleted.</span>');
				//clear cache and reload data
				CFW.cache.data[item] = null;
				cfw_dashboardlist_draw({tab: item});
			}else{
				CFW.ui.showSmallModal("Error!", '<span>The selected '+item+' could <b style="color: red">NOT</b> be deleted.</span>');
			}
	});
}

/******************************************************************
 * Print the list of quotes;
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_dashboardlist_printSharedDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'shareddashboards');
}


/******************************************************************
 * Print the list of quotes;
 * 
 * @param data as returned by CFW.http.getJSON()
 ******************************************************************/
function cfw_dashboardlist_printMyDashboards(data){
	cfw_dashboardlist_printDashboards(data, 'mydashboards');
}
/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_dashboardlist_printDashboards(data, type){
	
	parent = $("#tab-content");
	
	//--------------------------------
	// Button
	var createButton = $('<button class="btn btn-sm btn-success mb-2" onclick="cfw_dashboardlist_createDashboard()">'
							+ '<i class="fas fa-plus-circle"></i> '+ CFWL('cfw_dashboardlist_createDashboard')
					   + '</button>');
	
	parent.append(createButton);
	
	//--------------------------------
	// Table
	
	var cfwTable = new CFWTable();
	cfwTable.addHeaders(['ID', "Name", "Description"]);
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addAlert("info", "Hmm... seems there aren't any dashboards in the list.");
			return
		}

		//-----------------------------------
		// Render Data
		var rendererSettings = {
			 	idfield: 'PK_ID',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titledelimiter: ' ',
			 	visiblefields: ['NAME', 'DESCIPTION', 'IS_SHARED'],
			 	labels: {
			 		IS_SHARED: 'Shared'
			 	},
			 	customizers: {
			 		IS_SHARED: function(record, value) { 
			 			var isShared = value;
			 			if(isShared){
								return '<span class="badge badge-success m-1">true</span>';
						}else{
							return '<span class="badge badge-danger m-1">false</span>';
						}
			 			 
			 		}
			 	},
				actions: [ 
					//-------------------------
					// View Button
//					function (record, id){ 
//						return '<td><button class="btn btn-success btn-sm" data-id="'+id+'" alt="View" title="View" '
//						+'onclick="om_quotes_viewQuoteDetails(this);">'
//						+ '<i class="fa fa-eye"></i>'
//						+ '</button>&nbsp;</td>';
//					},
					//-------------------------
					// Edit Button
					function (record, id){ 
						var htmlString = '';
						if(JSDATA.userid == record.FK_ID_USER){
							htmlString += '<td><button class="btn btn-primary btn-sm" alt="Edit" title="Edit" '
								+'onclick="cfw_dashboardlist_editDashboard('+id+');">'
								+ '<i class="fa fa-pen"></i>'
								+ '</button></td>';
						}else{
							htmlString += '<td>&nbsp;</td>';
						}
						return htmlString;
					},
//					//-------------------------
//					// Add to list Button
//					function (record, id){ 
//						return '<td><button class="btn btn-warning btn-sm" alt="Add to List" title="Add to List" '
//							+'onclick="om_quotes_toggleQuoteInList('+id+');">'
//							+ '<i class="fas fa-star"></i>'
//						+ '</button>&nbsp;</td>';
//					},
					
					//-------------------------
					// Delete Button
					function (record, id){
						var htmlString = '';
						if(JSDATA.userid == record.FK_ID_USER){
							htmlString += '<td><button class="btn btn-danger btn-sm" alt="Delete" title="Delete" '
								+'onclick="CFW.ui.confirmExecute(\'Do you want to delete the dashboard?\', \'Delete\', \'cfw_dashboardlist_delete(\\\'mydashboards\\\','+id+');\')">'
								+ '<i class="fa fa-trash"></i>'
								+ '</button></td>';
						}else{
							htmlString += '<td>&nbsp;</td>';
						}
						return htmlString;
					},
				],
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",
				data: data.payload,
				rendererSettings: {
					table: {narrow: false, filterable: true}
				},
			};
				
		var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
		
		parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no users can be displayed.');
	}
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		tab: 'users|roles|permissions', 
 *  }
 * @return 
 ******************************************************************/

function cfw_dashboardlist_initialDraw(options){
	CFW.lang.loadLocalization();
	cfw_dashboardlist_draw(options);
}

function cfw_dashboardlist_draw(options){
	
	cfw_dashboardlist_reset();
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		switch(options.tab){
			case "mydashboards":		CFW.http.fetchAndCacheData(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "mydashboards"}, "mydashboards", cfw_dashboardlist_printMyDashboards);
										break;	
			case "shareddashboards":	CFW.http.fetchAndCacheData(CFW_DASHBOARDLIST_URL, {action: "fetch", item: "shareddashboards"}, "shareddashboards", cfw_dashboardlist_printSharedDashboards);
										break;
									
			default:				CFW.ui.addToastDanger('This tab is unknown: '+options.tab);
		}
		
		CFW.ui.toogleLoader(false);
	}, 50);
}