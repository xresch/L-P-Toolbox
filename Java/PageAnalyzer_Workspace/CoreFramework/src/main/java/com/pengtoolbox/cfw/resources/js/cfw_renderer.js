
/******************************************************************
 * 
 ******************************************************************/

CFW.render.registerRenderer("html",
	new CFWRenderer(
		function (renderDefinition) {

			if( renderDefinition.data instanceof Element
			|| typeof renderDefinition.data == "string"){
				return renderDefinition.data;
			}else{
				return CFW.format.objectToHTMLList(renderDefinition.data);
			}
		})
);

/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("json",
	new CFWRenderer(
		function (renderDef) {
			var wrapperDiv = $('<div class="flex-grow-1">');
			
			var randomID = CFW.utils.randomString(16);
			return wrapperDiv.append('<pre id="json-'+randomID+'"><code>'+JSON.stringify(renderDef.data, null, 2)+'</code></pre><script>hljs.highlightBlock($("#json-'+randomID+'").get(0));</script>');
		}
	)
);
/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("csv",
	new CFWRenderer(
		function (renderDef) {
		}
	)
);


/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("alerttiles",
	new CFWRenderer(
		function (renderDef) {
					
			//-----------------------------------
			// Check Data
			if(renderDef.datatype != "array"){
				return "<span>Unable to convert data into alert tiles.</span>";
			}
			
			//-----------------------------------
			// Render Specific settings
			var defaultSettings = {
				sizefactor: 1,
				showlabels: false, 
			};
			
			var settings = Object.assign({}, defaultSettings, renderDef.rendererSettings.alerttiles);

			
			//===================================================
			// Create Alert Tiles
			//===================================================
			var allTiles = $('<div class="d-flex flex-row flex-grow-1 flex-wrap">');

			if(!settings.showlabels){
				allTiles.addClass('align-items-start');
			}
			for(var i = 0; i < renderDef.data.length; i++ ){
				var currentRecord = renderDef.data[i];
				var currentTile = $('<div class="d-flex p-3 m-1">');
				
				//-------------------------
				// Add Styles
				if(renderDef.bgstylefield != null){
					currentTile.addClass('bg-'+currentRecord[renderDef.bgstylefield]);
				}
				
				if(renderDef.textstylefield != null){
					currentTile.addClass('text-'+currentRecord[renderDef.textstylefield]);
				}
				
				//-------------------------
				// Create Tile
				if(settings.showlabels){
					currentTile.addClass('flex-column flex-grow-1 justify-content-center align-items-center');

					//-------------------------
					// Create Title
					var tileTitle = '';
					for(var j = 0; j < renderDef.titlefields.length; j++){
						var fieldname = renderDef.titlefields[j];
						tileTitle += currentRecord[fieldname];
						if(j < renderDef.titlefields.length-1){
							tileTitle += renderDef.titledelimiter;
						}
						
					}
					currentTile.append('<p class="text-center" style="font-size: '+18*settings.sizefactor+'px;"><b>'+tileTitle+'</b></p>');
					//-------------------------
					// Add field Values as Cells
					for(var key in renderDef.visiblefields){
						var fieldname = renderDef.visiblefields[key];
						var value = currentRecord[fieldname];
						
						if(renderDef.customizers[fieldname] == null){
							if(value != null){
								currentTile.append('<span style="font-size: '+10*settings.sizefactor+'px;"><strong>'+renderDef.labels[fieldname]+':&nbsp;</strong>'+value+'</span>');
							}
						}else{
							var customizer = renderDef.customizers[fieldname];
							currentTile.append('<span><strong style="font-size: '+12*settings.sizefactor+'px;">'+renderDef.labels[fieldname]+':&nbsp;</strong>'+customizer(currentRecord, value)+'</span>');
						}
					}
				} else {
					currentTile.css('width', 50*settings.sizefactor+"px");
					currentTile.css('height', 50*settings.sizefactor+"px");
				}

				allTiles.append(currentTile);
			}
			
			return allTiles;
		
		}
	)
);


/******************************************************************
 * 
 ******************************************************************/
CFW.render.registerRenderer("table",
	new CFWRenderer(
		function (renderDef) {
			
			// renderDef.rendererSettings.table same as CFWTable Settings
//			{
//				filterable: true,
//				responsive: true,
//				hover: true,
//				striped: true,
//				narrow: false,
//				stickyheader: false, 
//			 }
			
			//-----------------------------------
			// Check Data
			if(renderDef.datatype != "array"){
				return "<span>Unable to convert data into table.</span>";
			}
			
			//===================================================
			// Create Table
			//===================================================
			var cfwTable = new CFWTable(renderDef.rendererSettings.table);
			
			//-----------------------------------
			// Create Headers
			var selectorGroupClass;
			if(renderDef.bulkActions != null){
				selectorGroupClass = "table-checkboxes-"+CFW.utils.randomString(16);
				var checkbox = $('<input type="checkbox" onclick="$(\'.'+selectorGroupClass+':visible\').prop(\'checked\', $(this).is(\':checked\') )" >');
				
				cfwTable.addHeader(checkbox);
			}
			
			for(var key in renderDef.visiblefields){
				var fieldname = renderDef.visiblefields[key];
				cfwTable.addHeader(renderDef.labels[fieldname]);
			}
			
			for(var key in renderDef.actions){
				cfwTable.addHeader("&nbsp;");
			}
			
			//-----------------------------------
			// Print Records
			var count = renderDef.data.length;
			
			for(var i = 0; i < count; i++ ){
				var currentRecord = renderDef.data[i];
				var row = $('<tr class="cfwRecordContainer">');
				
				//-------------------------
				// Add Styles
				if(renderDef.bgstylefield != null){
					row.addClass('table-'+currentRecord[renderDef.bgstylefield]);
				}
				
				if(renderDef.textstylefield != null){
					if(currentRecord[renderDef.textstylefield] != null){
						row.addClass('text-'+currentRecord[renderDef.textstylefield]);
					}else{
						if(renderDef.bgstylefield != null && currentRecord[renderDef.bgstylefield] != null){
							row.addClass('text-dark');
						}
					}
				}
				
				//-------------------------
				// Checkboxes for selects
				var cellHTML = '';
				if(renderDef.bulkActions != null){
					
					var value = "";
					if(renderDef.idfield != null){
						value = currentRecord[renderDef.idfield];
					}
					var checkboxCell = $('<td>');
					var checkbox = $('<input class="'+selectorGroupClass+'" type="checkbox" value="'+value+'">');
					checkbox.data('idfield', renderDef.idfield);
					checkbox.data('record', currentRecord);
					checkboxCell.append(checkbox);
					row.append(checkboxCell);
				}
				
				//-------------------------
				// Add field Values as Cells
				for(var key in renderDef.visiblefields){
					var fieldname = renderDef.visiblefields[key];
					var value = currentRecord[fieldname];
					
					if(renderDef.customizers[fieldname] == null){
						if(value != null){
							cellHTML += '<td>'+value+'</td>';
						}else{
							cellHTML += '<td>&nbsp;</td>';
						}
					}else{
						var customizer = renderDef.customizers[fieldname];
						cellHTML += '<td>'+customizer(currentRecord, value)+'</td>';
					}
				}
				
				//-------------------------
				// Add Action buttons
				var id = null;
				if(renderDef.idfield != null){
					id = currentRecord[renderDef.idfield];
				}
				for(var fieldKey in renderDef.actions){
					
					cellHTML += '<td>'+renderDef.actions[fieldKey](currentRecord, id )+'</td>';
				}
				row.append(cellHTML);
				cfwTable.addRow(row);
			}
			
			//----------------------------------
			// Create multi buttons
			if(renderDef.bulkActions == null){
				return cfwTable.getTable();
			}else{
				var wrapperDiv = cfwTable.getTable();
				
				var actionsDivTop  = $('<div class="m-1">');
				var actionsDivBottom  = $('<div class="m-1">');
				for(var buttonLabel in renderDef.bulkActions){
					//----------------------------
					// Top 
					if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
						var func = renderDef.bulkActions[buttonLabel];
						var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
						button.data('checkboxSelector', '.'+selectorGroupClass); 
						button.data("function", func); 
						actionsDivTop.append(button);
					}
					
					//----------------------------
					// Bottom
					if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
						var func = renderDef.bulkActions[buttonLabel];
						var button = $('<button class="btn btn-sm btn-primary mr-1" onclick="cfw_internal_executeMultiAction(this)">'+buttonLabel+'</button>');
						button.data('checkboxSelector', '.'+selectorGroupClass); 
						button.data("function", func); 
						actionsDivBottom.append(button);
					}
				}
				
				if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'top' ){
					wrapperDiv.prepend(actionsDivTop);
				}
				if(renderDef.bulkActionsPos == 'both' || renderDef.bulkActionsPos == 'bottom' ){
					wrapperDiv.append(actionsDivBottom);
				}
				
				return wrapperDiv;
			}
	})
);

/******************************************************************
 * Execute a multi action.
 * Element needs the following JQuery.data() attributes:
 *   - checkboxSelector: JQuery selection string without ":checked"
 *   - function: the function that should be executed
 ******************************************************************/
function cfw_internal_executeMultiAction(buttonElement){
	
	var checkboxSelector = $(buttonElement).data('checkboxSelector');
	var callbackFunction = $(buttonElement).data('function');
		
	var recordContainerArray = [];
	var valuesArray = [];
	var recordsArray = [];
	
	$.each($(checkboxSelector+':checked'), function(){
		valuesArray.push( $(this).val() );
		recordsArray.push( $(this).data('record') );
		recordContainerArray.push( $(this).closest('.cfwRecordContainer').get(0) );
	});
	
	callbackFunction(recordContainerArray, recordsArray, valuesArray);
	
}