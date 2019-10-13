package com.pengtoolbox.cfw.db.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.api.APIDefinition;
import com.pengtoolbox.cfw.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.api.ReturnFormat;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.validation.LengthValidator;

public class Configuration extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CONFIG";
	public static final String FILE_CACHING = "Cache Files";
	public static final String THEME = "Theme";
	public static final String CODE_THEME = "Code Theme";
	
	public enum ConfigFields{
		PK_ID,
		CATEGORY,
		NAME,
		DESCRIPTION,
		TYPE,
		VALUE,
		OPTIONS
	}

	private static Logger logger = CFWLog.getLogger(Configuration.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ConfigFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.apiFieldType(FormFieldType.NUMBER)
									.setDescription("The id of the configuration.")
									.setValue(-999);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, ConfigFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(255)")
									.setDescription("The category of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, ConfigFields.NAME.toString())
									.setColumnDefinition("VARCHAR(255) UNIQUE")
									.setDescription("The name of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, ConfigFields.DESCRIPTION.toString())
											.setColumnDefinition("VARCHAR(4096)")
											.setDescription("A description of the configuration.")
											.addValidator(new LengthValidator(-1, 4096));
	
	private CFWField<String> type = CFWField.newString(FormFieldType.SELECT, ConfigFields.TYPE.toString())
			.setColumnDefinition("VARCHAR(32)")
			.setDescription("The form field type of the configuration.")
			.setOptions(FormFieldType.values())
			.addValidator(new LengthValidator(1, 32));
	
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, ConfigFields.VALUE.toString())
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The current value of the field. Can be null.")
			.addValidator(new LengthValidator(1, 1024));
	
	private CFWField<Object[]> options = CFWField.newArray(FormFieldType.NONE, ConfigFields.OPTIONS.toString())
			.setColumnDefinition("ARRAY")
			.setDescription("The options available for the configuration(optional field).");
	
	public Configuration() {
		initialize();
	}
	
	public Configuration(String category, String name) {
		initialize();
		this.category.setValue(category);
		this.name.setValue(name);
	}
	
	public Configuration(ResultSet result) throws SQLException {
		initialize();
		this.mapResultSet(result);	
	}
	
	private void initialize() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, name, description, type, value, options, category);
	}
	
	public void initDBSecond() {
		//-----------------------------------------
		// 
		//-----------------------------------------
		if(!CFW.DB.Config.checkConfigExists(Configuration.FILE_CACHING)) {
			CFW.DB.Config.create(
				new Configuration("Core Framework", Configuration.FILE_CACHING)
					.description("Enables the caching of files read from the disk.")
					.type(FormFieldType.BOOLEAN)
					.value("true")
			);
		}
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		if(!CFW.DB.Config.checkConfigExists(Configuration.THEME)) {
			CFW.DB.Config.create(
				new Configuration("Core Framework", Configuration.THEME)
					.description("Set the application look and feel. 'Slate' is the default and recommended theme, all others are not 100% tested.")
					.type(FormFieldType.SELECT)
					.options(new String[]{"darkblue", "flatly", "lumen", "materia", "minty", "pulse", "sandstone", "simplex", "sketchy", "slate", "spacelab", "superhero", "united"})
					.value("slate")
			);
		}
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		if(!CFW.DB.Config.checkConfigExists(Configuration.CODE_THEME)) {
			CFW.DB.Config.create(
				new Configuration("Core Framework", Configuration.CODE_THEME)
					.description("Set the style for the code highlighting.")
					.type(FormFieldType.SELECT)
					.options(new String[]{"androidstudio", "arduino-light", "magula", "pojoaque", "sunburst", "zenburn"})
					.value("zenburn")
			);
		}
				
				
		CFW.DB.Config.updateCache();
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
						
		String[] inputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.DESCRIPTION.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
						ConfigFields.OPTIONS.toString(),
				};

		//----------------------------------
		// fetchJSON
		APIDefinitionFetch fetchJsonAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchJSON",
						inputFields,
						outputFields,
						ReturnFormat.JSON
				);
		
		apis.add(fetchJsonAPI);
		
		//----------------------------------
		// fetchCSV
		APIDefinitionFetch fetchCSVAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchCSV",
						inputFields,
						outputFields,
						ReturnFormat.CSV
				);
		
		apis.add(fetchCSVAPI);
		
		//----------------------------------
		// fetchXML
		APIDefinitionFetch fetchXMLAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchXML",
						inputFields,
						outputFields,
						ReturnFormat.XML
				);
		
		apis.add(fetchXMLAPI);
		return apis;
	}
	
	public int id() {
		return id.getValue();
	}
	
	public Configuration id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Configuration category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public Configuration name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Configuration description(String description) {
		this.description.setValue(description);
		return this;
	}

	public String type() {
		return type.getValue();
	}

	public Configuration type(FormFieldType type) {
		this.type.setValue(type.toString());
		return this;
	}

	public String value() {
		return value.getValue();
	}

	public Configuration value(String value) {
		this.value.setValue(value);
		return this;
	}

	public Object[] options() {
		return options.getValue();
	}

	public Configuration options(Object[] options) {
		this.options.setValue(options);
		return this;
	}
	
	



	
	
}
