package com.pengtoolbox.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWObject;

public class SerializerCFWObject implements JsonSerializer<CFWObject> {

	@Override
	public JsonElement serialize(CFWObject object, Type type, JsonSerializationContext context) {
		
		JsonObject result = new JsonObject();
		
		for(CFWField field : object.getFields().values()) {
			CFW.JSON.addFieldAsProperty(result, field);
		}
		
		JsonArray children = new JsonArray();
		for (CFWObject child : object.getChildObjects().values()) {
			children.add(CFW.JSON.objectToJsonElement(child));
		}
		
		result.add("children", children);
		
		return result;
	}

}
