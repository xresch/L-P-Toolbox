/*************************************************************************
 * 
 * @author Reto Scheiwiller, 2018
 * 
 * Distributed under the MIT license
 *************************************************************************/

/**************************************************************************************
 * GLOBAL VARIABLES
 *************************************************************************************/
var GLOBAL_COUNTER=0;
var SUMMARY;
var RULES;
var STATS_BY_TYPE;
var STATS_PRIMED_CACHE;
var URL_PARAMETERS;

//-----------------------------------------
// Data Objects
var YSLOW_RESULT = null;
var RESULT_LIST = null;
var HAR_DATA = null;
COMPARE_YSLOW = null;

var GRADE_CLASS = {
	A: "success",
	B: "success",
	C: "warning",
	D: "warning",
	E: "danger",
	F: "danger",
	None: "info"
};

/******************************************************************
 * 
 * 
 * @param 
 * @returns 
 ******************************************************************/
function initialize(){
	
	URL_PARAMETERS = getURLParameters();
	
}

/******************************************************************
 * 
 * 
 * @param 
 * @returns 
 ******************************************************************/
function getURLParameters()
{
    var vars = {};
    var hash;
    
    var keyValuePairs = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < keyValuePairs.length; i++)
    {
        splitted = keyValuePairs[i].split('=');
        vars[splitted[0]] = splitted[1];
    }
    
    console.log(vars);
    return vars;
}

/******************************************************************
 * 
 *
 ******************************************************************/
function fetchData(args){
	
	//---------------------------------------
	// Check loading status and create URL
	//---------------------------------------
	var url = "./data";
	switch (args.data){
		case "yslowresult": 	if(YSLOW_RESULT != null) return;
								url += "?type="+args.data+"&resultid="+URL_PARAMETERS.resultid;
								break;
								
		case "resultlist":		if(RESULT_LIST != null) return;
								url += "?type="+args.data;
								break;
								
		case "har":				if(HAR_DATA != null) return;
								url += "?type="+args.data+"&resultid="+URL_PARAMETERS.resultid;
								break;
								
		case "compareyslow":	if(COMPARE_YSLOW != null) return;
								url += "?type="+args.data+"&resultids="+URL_PARAMETERS.resultids;
								break;						
								
	}
	
	//---------------------------------------
	// Fetch and Return Data
	//---------------------------------------
	$.get(url).done(function(data) {
		    		    
		    if(data.error != null){
				console.error(data.error);
				var errorDiv = $('<div>');
				errorDiv.attr("class", "bg-danger");
				errorDiv.append('<p>Sorry some error occured loading the data, be patient while nobody is looking into it.</p>');
				errorDiv.append('<p>'+data.error+'</p>');
				$("#results").append(errorDiv);
				return;
			}
		    
			switch (args.data){
				case "yslowresult": 	YSLOW_RESULT = data;
										prepareYSlowResults(YSLOW_RESULT);
										RULES = sortArrayByValueOfObject(RULES, "score");
										$(".result-view-tabs").css("visibility", "visible");
										draw(args);
										break;
										
				case "resultlist":		RESULT_LIST = data;
										draw(args);
										break;
										
				case "har":				HAR_DATA = data;
										prepareGanttData(HAR_DATA);
										draw(args);
										break;
				case "compareyslow":	COMPARE_YSLOW = data;
										draw(args);
										break;						
										
			}
		})
		  .fail(function() {
				var errorDiv = $('<div>');
				errorDiv.attr("class", "bg-danger");
				errorDiv.append('<p>Sorry some error occured loading the data, be patient while nobody is looking into it.</p>');
				$("#results").append(errorDiv);
		  });
}

/*******************************************************************************
 * Show Loading Animation
 ******************************************************************************/
function showLoader(isVisible){
	
	if(isVisible){
		$("#loading").css("visibility", "visible");
	}else{
		$("#loading").css("visibility", "hidden");
	}
}

/**************************************************************************************
 * 
 *************************************************************************************/
function getGrade(score){
	
	if		(score >= 90){return "A" }
	else if (score >= 80){return "B" }
	else if (score >= 70){return "C" }
	else if (score >= 60){return "D" }
	else if (score >= 50){return "E" }
	else if (score >= 0){return "F" }
	else {return "None" }
}


/******************************************************************
 * 
 * @param 
 * @returns 
 ******************************************************************/
function prepareYSlowResults(data){
	

//	"w": "size",
//	"o": "overall score",
//	"u": "url",
//	"r": "total number of requests",
//	"s": "space id of the page",
//	"i": "id of the ruleset used",
//	"lt": "page load time",
//	"w_c": "page weight with primed cache",
//	"r_c": "number of requests with primed cache",
	
	//===================================================
	// Load Summary Values
	//===================================================
	SUMMARY = {};

	SUMMARY.url				= decodeURIComponent(data.u);
	SUMMARY.size			= data.w;
	SUMMARY.sizeCached		= data.w_c;
	SUMMARY.totalScore		= data.o;
	SUMMARY.grade			= getGrade(SUMMARY.totalScore);
	SUMMARY.requests		= data.r;
	SUMMARY.requestsCached	= data.r_c;
	SUMMARY.ruleset			= data.i;
	SUMMARY.loadtime		= data.resp;

	
	//===================================================
	// Load Rules
	//===================================================
	RULES = [];
	for(key in data.g){

		var rule = {};
		rule.name 			= key;
		rule.score 			= data.g[key].score;
		rule.grade 			= getGrade(rule.score);
		rule.title 			= data.dictionary.rules[key].name;
		rule.description 	= data.dictionary.rules[key].info;
		rule.message 		= data.g[key].message;
		rule.components 	= data.g[key].components;
		rule.url		 	= data.g[key].url;
		rule.weight 		= data.dictionary.rules[key].weight;
			
		if(rule.score == undefined || rule.score == null) rule.score = "-";
		
		RULES.push(rule);
	}
	
	//===================================================
	// LoadStats
	//===================================================
	STATS_BY_TYPE = [];
	for(key in data.stats){

		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats[key].r;
		stats.size 		= data.stats[key].w;
		
		STATS_BY_TYPE.push(stats);
	}
	
	//===================================================
	// Load Stats with cache
	//===================================================
	STATS_PRIMED_CACHE = [];
	for(key in data.stats_c){
		
		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats_c[key].r;
		stats.size 		= data.stats_c[key].w;
		
		STATS_PRIMED_CACHE.push(stats);
	}
		
	//===================================================
	// Load Stats with cache
	//===================================================
	COMPONENTS = [];
	for(key in data.comps){
		
		var comp = {};
		
		comp.type 			= data.comps[key].type;		
		comp.size			= data.comps[key].size;
		//comp.gzipsize		= data.comps[key].gzip;
		comp.responsetime	= Math.round(data.comps[key].resp);
		comp.type 			= data.comps[key].type;
		comp.expires		= data.comps[key].expires;
		comp.url 			= decodeURIComponent(data.comps[key].url);
		
		//what's that?
		//comp.cr 			= data.comps[key].cr;
		
		COMPONENTS.push(comp);
	}
}

/******************************************************************
 * 
 * @param 
 * @returns 
 ******************************************************************/
function prepareGanttData(data){
	

	//----------------------------------
	// Variables
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	var firstDate;
	var lastDate;
	var totalTimeMillis;
	if(entries.length > 1){
		firstDate = new Date(entries[0].startedDateTime);
		console.log("FirstDate:"+firstDate.toString());
		
		last = entries.length -1;
		lastDate = new Date(entries[last].startedDateTime);
		lastDate = new Date(lastDate.valueOf() + Math.ceil(entries[last].time));
		console.log("LastDate:"+lastDate.toString());
		
		totalTimeMillis = lastDate.valueOf() - firstDate.valueOf();
		console.log("totalTimeMillis:"+totalTimeMillis);
	}
	
	//----------------------------------
	// Loop Data

//   "timings": {
//       "blocked": 0,
//        "dns": -1,
//        "connect": -1,
//        "send": 0,
//        "wait": 265,
//        "receive": 5,
//        "ssl": -1
//    },

	for(var i = 0; i < entriesCount; i++ ){
		var entry = entries[i];
		var startDate = new Date(entry.startedDateTime);
		var deltaMillis = startDate.valueOf() - firstDate.valueOf();
		var duration = entry.time;
		var timings = entry.timings;
		
		entry.ganttdata = {
			"time": duration,	
			"delta": deltaMillis,
			"percentdelta": deltaMillis / totalTimeMillis * 100,
			"percentblocked": (entry.timings.blocked > 0) 	? entry.timings.blocked / duration * 100 : 0,
			"percentdns": (entry.timings.dns > 0) 			? entry.timings.dns / duration * 100 : 0,
			"percentconnect": (entry.timings.connect > 0) 	? entry.timings.connect / duration * 100 : 0,
			"percentsend": (entry.timings.send > 0) 		? entry.timings.send / duration * 100 : 0,
			"percentwait": (entry.timings.wait > 0) 		? entry.timings.wait / duration * 100 : 0,
			"percentreceive": (entry.timings.recieve > 0) 	? entry.timings.recieve / duration * 100 : 0,
			"percentssl": (entry.timings.ssl > 0) 			? entry.timings.ssl / duration * 100 : 0,
			"percenttime": duration / totalTimeMillis * 100
		}
		
		console.log(entry.ganttdata);

		
	}

}

/**************************************************************************************
 * Tries to decode a URI and handles errors when they are thrown.
 * If URI cannot be decoded the input string is returned unchanged.
 *************************************************************************************/
function secureDecodeURI(uri){
	try{
		decoded = decodeURIComponent(uri);
	}catch(err){
		decoded = uri;
	}
	
	return decoded;
}

/**************************************************************************************
 * 
 *************************************************************************************/
function sortArrayByValueOfObject(array, key){
	array.sort(function(a, b) {
		
			var valueA = a[key];
			var valueB = b[key];
			
			if(isNaN(valueA)) valueA = 9999999;
			if(isNaN(valueB)) valueB = 9999999;
			
		return valueA - valueB;
	});
	
	return array;
}

/**************************************************************************************
 * filterTable
 *************************************************************************************/
function filterTable(searchField){
	
	var table = $(searchField).data("table");
	var input = searchField;
	
	filter = input.value.toUpperCase();
	
	table.find("tbody tr").each(function( index ) {
		  console.log( index + ": " + $(this).text() );
		  
		  if ($(this).html().toUpperCase().indexOf(filter) > -1) {
			  $(this).css("display", "");
		  } else {
			  $(this).css("display", "none");
			}
	});

}

/**************************************************************************************
 * Select Element Content
 *************************************************************************************/
function selectElementContent(el) {
    if (typeof window.getSelection != "undefined" && typeof document.createRange != "undefined") {
        var range = document.createRange();
        range.selectNodeContents(el);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.selection != "undefined" && typeof document.body.createTextRange != "undefined") {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.select();
    }
}

/******************************************************************
 * Format the yslow results as html.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printComparison(parent, data){
	
	compareTableData = [];
	//-----------------------------------------
	// Get distinct List of Rules
	//-----------------------------------------
	uniqueRuleList = {};
	for(key in data){
		
		for(ruleName in data[key].JSON_RESULT.g){
			console.log("RuleName"+ruleName);
			uniqueRuleList[ruleName] = {"Metric": ruleName};
		}
	}
	
	//-----------------------------------------
	// Create Rows
	//-----------------------------------------
	
	var urlRow = {"Metric": "URL"}; compareTableData.push(urlRow);
	var scoreRow = {"Metric": "Score"}; compareTableData.push(scoreRow);
	var gradeRow = {"Metric": "Grade"}; compareTableData.push(gradeRow);
	var loadtimeRow = {"Metric": "Load Time"}; compareTableData.push(loadtimeRow);
	var sizeRow = {"Metric": "Page Size"}; compareTableData.push(sizeRow);
	var sizeCachedRow = {"Metric": "Page Size Cached"}; compareTableData.push(sizeCachedRow);
	var requestCountRow = {"Metric": "Total Requests"}; compareTableData.push(requestCountRow);
	var requestsCachedRow = {"Metric": "Cached Requests"}; compareTableData.push(requestsCachedRow);
	
	//-------------------------------
	// Push rules to table
	for(ruleName in uniqueRuleList){
		compareTableData.push(uniqueRuleList[ruleName]);
	}
	for(key in data){
		
		var time = data[key].TIME;
		var result = data[key].JSON_RESULT;
		
		//----------------------------
		// URL Row
		url = secureDecodeURI(result.u);
		urlRow[time]	= '<a target="_blank" href="'+url+'">'+url+'</a>';
		
		//----------------------------
		// Score Row
		var score = result.o; 
		scoreRow[time]	 = score + "%";
		
		//----------------------------
		// Grade Row
		var grade = getGrade(score);
		gradeRow[time]	 = '<span class="badge btn-'+GRADE_CLASS[grade]+'">'+grade+'</span>';
		
		//----------------------------
		// Other Rows
		sizeRow[time]	 			= result.w + " Bytes";
		sizeCachedRow[time]	 		= result.w_c + " Bytes";
		loadtimeRow[time]	 		= result.resp + "ms";
		requestCountRow[time]	 	= result.r;
		requestsCachedRow[time]	 	= result.r_c;
		
		//----------------------------
		// Rule Rows
		for(ruleName in uniqueRuleList){
				if(typeof result.g !== 'undefined' && typeof result.g[ruleName] !== 'undefined'){
				var ruleScore = result.g[ruleName].score;
				var ruleGrade = getGrade(ruleScore);
				uniqueRuleList[ruleName][time] = '<span class="badge btn-'+GRADE_CLASS[ruleGrade]+'">'+ruleGrade+'&nbsp;&sol;&nbsp;' + ruleScore + "%</span>";
			}else{
				uniqueRuleList[ruleName][time] = "N/A";
			}
		}
	}
	
	printTable(parent,compareTableData, "Comparison");
	
}

/******************************************************************
 * Print the gantt chart for the entries.
 * 
 * @param parent JQuery object 
 * @param data HAR file data
 * @returns 
 ******************************************************************/
function printGanttChart(parent, data){
	
	//----------------------------------
	// Add title and description.
	parent.append("<h2>Gantt Chart</h2>");

	//----------------------------------
	// Create Table Filter
	var filter = $('<input type="text" class="form-control" onkeyup="filterTable(this)" placeholder="Filter Table...">');
	parent.append(filter);
	parent.append('<span style="font-size: xx-small;"><strong>Hint:</strong> The filter searches through the innerHTML of the table rows. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of a cell content(e.g. &quot;&gt;Test&lt;&quot; )</span>');
	
	//----------------------------------
	// Create Table Header
	headerRowString = '<thead><tr>';
		headerRowString += '<th>&nbsp;</th>';
		headerRowString += '<th>Gantt Chart</th>';
		headerRowString += '<th>Time(ms)</th>';
		headerRowString += '<th>URL</th>';
	headerRowString += '</tr></thead>';
	
	//----------------------------------
	// Create Table
	var table = $('<table class="table table-striped table-responsive">');

	table.append(headerRowString);
	
	parent.append(table);
	filter.data("table", table);
	
	//----------------------------------
	// Create Rows
	console.log(data);
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		var rowString = '<tr>';

		//--------------------------
		// Details Link
		rowString += '<td><a alt="Show Details"><i class="fa fa-search"></i></a></td>';
		
		//--------------------------
		// Gantt Chart
		
		var gd = currentEntry.ganttdata;
		rowString += '<td> <div class="ganttWrapper" style="width: 500px;">';
			rowString += '<div class="ganttBlock percentdelta" style="width: '+gd.percentdelta+'%">&nbsp;</div>';
			rowString += '<div class="ganttBlock ganttTimings" style="width: '+gd.percenttime+'%">';
				rowString += createGanttBar(currentEntry, "blocked");
				rowString += createGanttBar(currentEntry, "dns");
				rowString += createGanttBar(currentEntry, "connect");
				//rowString += createGanttBar(currentEntry, "ssl");
				rowString += createGanttBar(currentEntry, "send");
				rowString += createGanttBar(currentEntry, "wait");
				rowString += createGanttBar(currentEntry, "receive");
			rowString += '</div>';
		rowString += 	'</div></td>';
		// --------------------------
		// Other

		rowString += '<td>'+Math.round(currentEntry.time)+'</td>';
		rowString += '<td>'+secureDecodeURI(currentEntry.request.url)+'</td>';
		
		rowString += "</tr>";
		
		table.append(rowString);
	}
	parent.append(table);
	
}

/******************************************************************
 * Print the gantt chart for the entries.
 * 
 * @param parent JQuery object 
 * @param data HAR file data
 * @returns the HTML for the bar in the gantt chart
 ******************************************************************/
function createGanttBar(entry, metric){
	
	var percentString = "percent"+metric;
	
	if(entry.ganttdata[percentString] > 0){ 
		return '<div class="ganttBlock '+percentString+'" alt="test" style="width: '+entry.ganttdata[percentString]+'%">&nbsp;</div>'
	}else{
		return "";
	}
}

/******************************************************************
 * Print a list of results.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printResultList(parent, data){
	
	//----------------------------------
	// Add title and description.
	parent.append("<h2>Result History</h2>");
	parent.append("<p>Click on the eye symbol to open a result. Select multiple results and hit compare to get a comparison.</p>");
	
	//----------------------------------
	// Create Table Filter
	var filter = $('<input type="text" class="form-control" onkeyup="filterTable(this)" placeholder="Filter Table...">');
	parent.append(filter);
	parent.append('<span style="font-size: xx-small;"><strong>Hint:</strong> The filter searches through the innerHTML of the table rows. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of a cell content(e.g. &quot;&gt;Test&lt;&quot; )</span>');
	
	//----------------------------------
	// Create Table Header
	headerRowString = '<thead><tr>';
		headerRowString += '<th>&nbsp;</th>';
		headerRowString += '<th>ID</th>';
		headerRowString += '<th>Timestamp</th>';
		headerRowString += '<th>URL</th>';
		headerRowString += '<th>&nbsp;</th>';
		headerRowString += '<th>&nbsp;</th>';
		headerRowString += '<th>&nbsp;</th>';
	headerRowString += '</tr></thead>';
	
	//----------------------------------
	// Create Table
	var table = $('<table class="table table-striped table-responsive">');

	table.append(headerRowString);
	
	parent.append(table);
	filter.data("table", table);
	
	//----------------------------------
	// Create Rows
	var resultCount = data.length;
	for(var i = 0; i < resultCount; i++ ){
		var currentData = data[i];
		var rowString = '<tr>';
		
		rowString += '<td><input class="resultSelectionCheckbox" type="checkbox" onchange="resultSelectionChanged();" value="'+currentData.RESULT_ID+'" /></td>';
		rowString += '<td>'+currentData.RESULT_ID+'</td>';
		rowString += '<td>'+currentData.TIME+'</td>';
		
		url = secureDecodeURI(currentData.PAGE_URL);
		
		rowString += '<td>'+url+'</td>';
		
		rowString += '<td><a  alt="View Result" href="./resultview?resultid='+currentData.RESULT_ID+'"><i class="fa fa-eye"></i></a></td>';
		rowString += '<td><a  alt="View Gantt Chart" href="./ganttchart?resultid='+currentData.RESULT_ID+'"><i class="fas fa-sliders-h"></i></a></td>';
		
		rowString += '<td><a target="_blank" alt="Open URL" href="'+url+'"><i class="fa fa-link"></i></a></td>';
		
		rowString += '<td><a target="_blank" alt="Delete Result" href="./delete?resultids='+currentData.RESULT_ID+'"><i class="fa fa-trash text-danger"></i></a></td>';
		rowString += "</tr>";
		
		table.append(rowString);
	}
	parent.append(table);
	
	
	//----------------------------------
	// Create Button
	var compareButton = $('<a id="resultCompareButton" class="btn btn-primary" onclick="compareResults();" disabled="true">Compare</a>');
	var deleteButton = $('<a id="resultDeleteButton" class="btn btn-danger" onclick="deleteResults();" disabled="true">Delete</a>');
	
	parent.append(compareButton);
	parent.append(deleteButton);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function resultSelectionChanged(){
	
	console.log("changed");
	
	if($(".resultSelectionCheckbox:checked").size() > 1){
		$("#resultCompareButton").attr("disabled", false);
	}else{
		$("#resultCompareButton").attr("disabled", true);
	}
	
	if($(".resultSelectionCheckbox:checked").size() > 0){
		$("#resultDeleteButton").attr("disabled", false);
	}else{
		$("#resultDeleteButton").attr("disabled", true);
	}
	
	
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function compareResults(){
		
	var resultIDs = "";
	$.each($(".resultSelectionCheckbox:checked"), function(){
		resultIDs += $(this).val()+",";
	});
	resultIDs = resultIDs.slice(0,-1);
	
	self.location = "./compare?resultids="+resultIDs;
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function deleteResults(){
		
	var resultIDs = "";
	$.each($(".resultSelectionCheckbox:checked"), function(){
		resultIDs += $(this).val()+",";
	});
	resultIDs = resultIDs.slice(0,-1);
	
	self.location = "./delete?resultids="+resultIDs;
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function printRuleDetails(parent, rule){
	
	if(rule.grade != null){ 			parent.append('<p><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[rule.grade]+'">'+rule.grade+'</span></strong></p>');}	
	if(rule.score != null){ 			parent.append('<p><strong>Score:&nbsp;</strong>'+rule.score+'</p>');}
	if(rule.name != null){ 				parent.append('<p><strong>Name:&nbsp;</strong>'+rule.name+'</p>');}
	if(rule.title != null){ 			parent.append('<p><strong>Title:&nbsp;</strong>'+rule.title+'</p>');}
	if(rule.description != null){ 		parent.append('<p><strong>Description:&nbsp;</strong>'+rule.description+'</p>');}
	if(rule.weight != null){ 			parent.append('<p><strong>Weight:&nbsp;</strong>'+rule.weight+'</p>');}
	
	if(rule.message != null  
	&& rule.message != undefined
	&& rule.message.length > 0 ){  		parent.append('<p><strong>Message:&nbsp;</strong>'+rule.message+'</p>');}
	
	if(rule.components.length > 0){ 			
		parent.append('<p><strong>Details:</strong></p>');
		var list = $('<ul>');
		parent.append(list);
		for(var key in rule.components){
			var compText = "";
			try{
				compText = decodeURIComponent(rule.components[key]);
			}catch(err){
				compText = rule.components[key];
			}
			list.append('<li>'+compText+'</li>');
		}
	}
	
	if(rule.url != null){ parent.append('<p><strong>Read More:&nbsp;</strong><a target="_blank" href="'+rule.url+'">'+rule.url+'</a></p>');}
}
/**************************************************************************************
 * 
 *************************************************************************************/
function createRulePanel(rule){
	
	GLOBAL_COUNTER++;
	
	
	var panel = $(document.createElement("div"));
	panel.addClass("panel panel-"+GRADE_CLASS[rule.grade]);
	
	//----------------------------
	// Create Header
	var panelHeader = $(document.createElement("div"));
	panelHeader.addClass("panel-heading");
	panelHeader.attr("id", "panelHead"+GLOBAL_COUNTER);
	panelHeader.attr("role", "tab");
	panelHeader.append(
		'<span class="panel-title">'+
		/*style.icon+*/
		'<a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse'+GLOBAL_COUNTER+'" aria-expanded="false" aria-controls="collapse'+GLOBAL_COUNTER+'">'+
		'<strong>Grade '+rule.grade+' ('+rule.score+'%):</strong>&nbsp;'+rule.title+
		'</a></span>'
	); 
	panelHeader.append(
			'<span style="float: right;">(Rule: ' + rule.name+ ')</span>'
		); 
	
	panel.append(panelHeader);
	
	//----------------------------
	// Create Collapse Container
	var collapseContainer = $(document.createElement("div"));
	collapseContainer.addClass("panel-collapse collapse");
	collapseContainer.attr("id", "collapse"+GLOBAL_COUNTER);
	collapseContainer.attr("role", "tabpanel");
	collapseContainer.attr("aria-labelledby", "panelHead"+GLOBAL_COUNTER);
	
	panel.append(collapseContainer);
	
	//----------------------------
	// Create Body
	var panelBody = $(document.createElement("div"));
	panelBody.addClass("panel-body");
	collapseContainer.append(panelBody);
	
	printRuleDetails(panelBody, rule);
	
	return {
		panel: panel,
		panelHeader: panelHeader,
		panelBody: panelBody
	};
}


/******************************************************************
 * Format the yslow results as plain text.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printPlainText(parent){
	parent.append("<h3>Plain Text</h3>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		printRuleDetails(div, rule);
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results for a JIRA ticket.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printJIRAText(parent){
	parent.append("<h3>JIRA Ticket Text</h3>");
	parent.append("<p>The text for each rule can be copy &amp; pasted into a JIRA ticket description, it will be formatted accordingly.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		if(rule.title != null){ 			div.append('*Title:*&nbsp;'+rule.title+'</br>');}
		if(rule.grade != null){ 			div.append('*Grade:*&nbsp;'+rule.grade+'</br>');}	
		if(rule.score != null){ 			div.append('*Score:*&nbsp;'+rule.score+'%</br>');}
		if(rule.description != null){ 		div.append('*Description:*&nbsp;</strong>'+rule.description+'</br>');}
		
		if(rule.message != null  
	    && rule.message != undefined
	    && rule.message.length > 0 ){ 		div.append('*Message:*&nbsp;'+rule.message+'</br>');}
		
		if(rule.components.length > 0){ 			
			div.append('*Details:*</br>');
			for(var key in rule.components){
				var compText = "";
				try{
					compText = decodeURIComponent(rule.components[key]);
				}catch(err){
					compText = rule.components[key];
				}
				div.append('<li>'+compText+'</li>');
			}
		}
		
		if(rule.url != null){ div.append('*Read More:*&nbsp;</strong>'+rule.url+'</br>');}
		
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printCSV(parent, data){
	
	parent.append("<h2>CSV Export</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "selectElementContent(this)");
	pre.append(code);
	
	var headerRow = "";

		
	for(var key in data[0]){
		if(key != "components" && key != "description"){
			headerRow += key+';';
		}
	}
	code.append(headerRow+"</br>");
	
	var rowCount = data.length;
	for(var i = 0; i < rowCount; i++ ){
		var currentData = data[i];
		var row = "";
		
		for(var cellKey in currentData){
			if(cellKey != "components" && cellKey != "description"){
				row += '&quot;'+currentData[cellKey]+'&quot;;';
			}
		}
		code.append(row+"</br>");
	}
	parent.append(pre);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function printJSON(parent, data){
	
	parent.append("<h2>JSON</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "selectElementContent(this)");
	pre.append(code);
	
	code.text(JSON.stringify(data, 
		function(key, value) {
	    if (key == 'description') {
            // Ignore description to reduce output size
            return;
	    }
	    return value;
	},2));
	
}

/******************************************************************
 * Format the yslow results as html.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printTable(parent, data, title){
	
	parent.append("<h3>"+title+"</h3>");
	//parent.append("<p>Click on the panel title to expand for more details.</p>");
	
	var filter = $('<input type="text" class="form-control" onkeyup="filterTable(this)" placeholder="Filter Table...">');
	parent.append(filter);
	parent.append('<span style="font-size: xx-small;"><strong>Hint:</strong> The filter searches through the innerHTML of the table rows. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of a cell content(e.g. &quot;&gt;Test&lt;&quot; )</span>');
	
	var table = $('<table class="table table-striped table-responsive">');
	var header = $('<thead>');
	var headerRow = $('<tr>');
	
	header.append(headerRow);
	table.append(header);
	
	parent.append(table);
	filter.data("table", table);

	for(var key in data[0]){
		headerRow.append('<th>'+key+'</th>');
	}
	
	var ruleCount = data.length;
	for(var i = 0; i < ruleCount; i++ ){
		var currentData = data[i];
		var row = $('<tr>');
		
		for(var cellKey in currentData){
			if(cellKey != "components"){
				row.append('<td>'+currentData[cellKey]+'</td>');
			}else{
				var list = $('<ul>');
				for(var key in currentData.components){
					var compText = "";
					try{
						compText = decodeURIComponent(currentData.components[key]);
					}catch(err){
						compText = currentData.components[key];
					}
					list.append('<li>'+compText+'</li>');
				}
				var cell = $("<td>");
				cell.append(list);
				row.append(cell);
			}
		}
		table.append(row);
	}
	parent.append(table);
}

/******************************************************************
 * Format the yslow results as html.
 * 
 * @param 
 * @returns 
 ******************************************************************/
function printPanels(parent){
	
	parent.append("<h3>Panels</h3>");
	parent.append("<p>Click on the panel title to expand for more details.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var panelObject = createRulePanel(RULES[i]);
		
		if(parent != null){
			parent.append(panelObject.panel);
		}else{
			$("#content").append(panelObject.panel);
		}
	}
}

/**************************************************************************************
 * 
 *************************************************************************************/
function printSummary(parent){
	
	parent.append("<h3>Summary</h3>");
	
	var list = $("<ul>");
	
	if(SUMMARY.grade != null){ 				list.append('<li><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[SUMMARY.grade]+'">'+SUMMARY.grade+'</strong></li>');}
	if(SUMMARY.totalScore != null){ 		list.append('<li><strong>Total Score:&nbsp;</strong>'+SUMMARY.totalScore+'%</li>');}
	if(SUMMARY.url != null){ 				list.append('<li><strong>URL:&nbsp;</strong><a href="'+SUMMARY.url+'">'+SUMMARY.url+'</a></li>');}
	if(SUMMARY.size != null){ 				list.append('<li><strong>Page Size:&nbsp;</strong>'+SUMMARY.size+' Bytes</li>');}
	if(SUMMARY.sizeCached != null){ 		list.append('<li><strong>Page Size(cached):&nbsp;</strong>'+SUMMARY.sizeCached+' Bytes</li>');}
	if(SUMMARY.requests != null){ 			list.append('<li><strong>Request Count:&nbsp;</strong>'+SUMMARY.requests+'</li>');}
	if(SUMMARY.requestsCached != null){ 	list.append('<li><strong>Cached Requests Count:&nbsp;</strong>'+SUMMARY.requestsCached+'</li>');}
	if(SUMMARY.loadtime != null 
	&& SUMMARY.loadtime != "-1"){ 			list.append('<li><strong>Load Time:&nbsp;</strong>'+SUMMARY.loadtime+' ms</li>');}
	
	if(SUMMARY.ruleset != null){ 			list.append('<li><strong>YSlow Ruleset:&nbsp;</strong>'+SUMMARY.ruleset+'</li>');}
	
	parent.append(list);
	
}
/******************************************************************
 * 
 ******************************************************************/
function reset(){
	GLOBAL_COUNTER=0;
	$("#results").html("");
}

/******************************************************************
 * main method for formatting.
 * 
 * @param argument Array
 * @returns 
 ******************************************************************/
function draw(options){
	
	reset();
	
	showLoader(true);
	
	window.setTimeout( 
	function(){
	
		RESULTS_DIV = $("#results");
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch (options.data){
			case "yslowresult": 	if(YSLOW_RESULT == null) { fetchData(options);  return;} break;
			case "resultlist":		if(RESULT_LIST == null) { fetchData(options); return;} break;
			case "har":				if(HAR_DATA == null) { fetchData(options); return;} break;
			case "compareyslow":	if(COMPARE_YSLOW == null) { fetchData(options); return;} break;
			
		}
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch(options.info + options.view){
		
			case "resultlist":		printResultList($(RESULTS_DIV), RESULT_LIST);
									break;
									
			case "ganttchart":		printGanttChart($(RESULTS_DIV), HAR_DATA);
									break;	
			
			case "compareyslow":	printComparison($(RESULTS_DIV), COMPARE_YSLOW);
									break;	
									
			case "overview": 		printSummary(RESULTS_DIV);
									printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
									printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
									printPanels(RESULTS_DIV);
									break;
									
			case "gradepanels": 	printPanels(RESULTS_DIV);
						  			break;
						  			
			case "gradetable": 		printTable(RESULTS_DIV, RULES, "Table: Grade by Rules");
	  								break;
	  								
			case "gradeplaintext":	printPlainText(RESULTS_DIV);
									break;
									
			case "gradejira":		printJIRAText(RESULTS_DIV);
									break;
									
			case "gradecsv":		printCSV(RESULTS_DIV, RULES);
									break;
									
			case "gradejson":		printJSON(RESULTS_DIV, RULES);
									break;						
									
			case "statstable":		
				switch(options.stats){
					case "type": 			printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
											break;
									
					case "type_cached": 	printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
											break;
											
					case "components": 		printTable(RESULTS_DIV, COMPONENTS, "Components");
											break;
				}
				break;
								
			default:				RESULTS_DIV.text("Sorry some error occured, be patient while nobody is looking into it.");
		}
		showLoader(false);
	}, 100);
	
	
}