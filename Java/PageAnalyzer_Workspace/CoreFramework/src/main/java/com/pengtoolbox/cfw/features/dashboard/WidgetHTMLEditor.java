package com.pengtoolbox.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.response.JSONResponse;

public class WidgetHTMLEditor extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "cfw_htmleditor";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.WYSIWYG, "content")
						.allowHTML(true)
						.setValue("<h2>Your Title</h2><p>Your fancy introduction.</p><ul><li>Some Bulletpoint</li><li>Another Bulletpoint</li></ul>")
						)
		;
	}

	@Override
	public void fetchData(JSONResponse response, JsonObject settings) { }

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_htmleditor.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { return null; }

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}

}
