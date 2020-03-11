package com.pengtoolbox.cfw.datahandling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject.ForeignKeyDefinition;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * Class used to create SQL statements for a CFWObject.
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWSQL {
	
	private static Logger logger = CFWLog.getLogger(CFWSQL.class.getName());
	private static HashMap<String, String> queryCache = new HashMap<String, String>();
	private static HashMap<String, Integer> cacheHitCount = new HashMap<String, Integer>();
	private String queryName = null;
	
	private CFWObject object;
	private LinkedHashMap<String, CFWField<?>> fields;
	private LinkedHashMap<String, String> columnSubqueries;
	
	private StringBuilder query = new StringBuilder();
	private ArrayList<Object> values = new ArrayList<Object>();
	
	private ResultSet result = null;
	
	public CFWSQL(CFWObject object) {
		if(object != null) {
			this.object = object;
			this.fields = object.getFields();
		}
	} 
	
	/****************************************************************
	 * Reset this object and make it ready for another execution.
	 ****************************************************************/
	public CFWSQL reset() {
		query = new StringBuilder();
		values = new ArrayList<Object>();
		queryName = null;
		return this;
	}
	
	/****************************************************************
	 * Returns the current String representation of the query without
	 * caching the statement.
	 ****************************************************************/
	public String getStatementString() {
		return this.query.toString();
	}
	
	/****************************************************************
	 * Builds the statement while handling the caching.
	 ****************************************************************/
	public String getStatementCached() {
		String statement;
		
		if(isQueryCached()) {
			statement = queryCache.get(queryName);
			
			if(cacheHitCount.containsKey(statement)) {
				cacheHitCount.put(statement, cacheHitCount.get(statement)+1);
			}else {
				cacheHitCount.put(statement, 1);
			}
		}else {
			statement = query.toString();
			if(queryName != null) {
				queryCache.put(queryName, statement);
				cacheHitCount.put(statement, 1);
			}
		}
		
		return statement;
	}
	
	/****************************************************************
	 * Caches the query with the specified name for lower performance
	 * impact.
	 * @param Class of the class using the query.
	 * @param name of the query
	 ****************************************************************/
	public CFWSQL queryCache(Class<?> clazz, String name) {
		this.queryName = clazz.getName()+"."+name;
		return this;
	}
	
	/****************************************************************
	 * Check if the fieldname is valid.
	 * @return true if valid, false otherwise
	 ****************************************************************/
	private boolean isQueryCached() {
		
		if(queryName != null && queryCache.containsKey(queryName)) {
			return true;
		}
		
		return false;
	}
		
	
	/****************************************************************
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean createTable() {
		//------------------------------------
		// Create Table
		if(object.getPrimaryField() == null) {
			new CFWLog(logger)
				.method("createTable")
				.severe("CFWObjects need a primary field to create a table out of them. ", new IllegalStateException());
		}
		
		//------------------------------------
		// Create Table
		boolean success = true;
		String createTableSQL = "CREATE TABLE IF NOT EXISTS "+object.getTableName();
		success &= CFWDB.preparedExecute(createTableSQL);
		
		//------------------------------------
		// Create Columns
		for(CFWField<?> field : fields.values()) {
			if(field.getColumnDefinition() != null) {
				String addColumnIsRenamable = "ALTER TABLE "+object.getTableName()
				 +" ADD COLUMN IF NOT EXISTS "+field.getName()+" "+field.getColumnDefinition();
				success &= CFWDB.preparedExecute(addColumnIsRenamable);
			}else {
				new CFWLog(logger)
					.method("createTable")
					.severe("The field "+field.getName()+" is missing a columnDefinition. Use CFWField.setColumnDefinition(). ");
				success &= false;
			}
		}
		
		//------------------------------------
		// Create ForeignKeys
		for(ForeignKeyDefinition fkd : object.getForeignKeys()) {
			String foreignTable;
			try {
				foreignTable = fkd.foreignObject.newInstance().getTableName();
					
				// ALTER TABLE PUBLIC.CORE_USERROLE_TO_PARAMETER ADD CONSTRAINT IF NOT EXISTS PUBLIC.CURTBP_USER_ID FOREIGN KEY(USER_ID) REFERENCES PUBLIC.CORE_USER(ID) NOCHECK;
				String createForeignKeysSQL = "ALTER TABLE "+object.getTableName()
				  + " ADD CONSTRAINT IF NOT EXISTS PUBLIC.FK_"+object.getTableName()+"_"+fkd.fieldname
				  + " FOREIGN KEY ("+fkd.fieldname
				  + ") REFERENCES "+foreignTable+"("+fkd.foreignFieldname+") ON DELETE "+fkd.ondelete;
			
				success &= CFWDB.preparedExecute(createForeignKeysSQL);
				
			} catch (Exception e) {
				new CFWLog(logger)
				.method("createTable")
				.severe("An error occured trying to create foreign keys for table: "+object.getTableName(), e);
			} 
			
		}
		
		return success;
		
	}
	
	/****************************************************************
	 * Renames a table.

	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean renameTable(String oldname, String newname) {
		
		String renameTable = "ALTER TABLE IF EXISTS "+oldname+" RENAME TO "+newname;
		return CFWDB.preparedExecute(renameTable);
	}
	
	/****************************************************************
	 * Renames a column.

	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean renameColumn(String tablename, String oldname, String newname) {
		
		String renameColumn = "ALTER TABLE IF EXISTS "+tablename+" ALTER COLUMN "+oldname+" RENAME TO "+newname;
		return CFWDB.preparedExecute(renameColumn);
	}
	
	/****************************************************************
	 * Renames a foreignkey.
	 *
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean renameForeignKey(String oldTablename, String oldFieldname, String newTablename, String newFieldname) {
		
		String renameForeignKey = "ALTER TABLE IF EXISTS "+oldTablename+
				" RENAME CONSTRAINT FK_"+oldTablename+"_"+oldFieldname
			  + " TO FK_"+newTablename+"_"+newFieldname;

		return CFWDB.preparedExecute(renameForeignKey);
	}
	
	
	/****************************************************************
	 * Add a column subquery which will be added to the select statement.
	 * This methd has to be called before you call the select*() method.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL columnSubquery(String asName, String query) {
		if(columnSubqueries == null) {
			columnSubqueries = new LinkedHashMap<String, String>();
		}
		columnSubqueries.put(asName, query);
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private String getColumnSubqueriesString() {
		if(columnSubqueries != null && !columnSubqueries.isEmpty() ) {
			StringBuilder builder = new StringBuilder();
			for(Entry<String, String> entry : columnSubqueries.entrySet()) {
				builder.append(", (")
				.append(entry.getValue())
				.append(") AS ")
				.append(entry.getKey());
			}
			
			return builder.toString();
		}
		return "";
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	private boolean hasColumnSubqueries() {
		if(columnSubqueries != null && !columnSubqueries.isEmpty() ) {
			return true;
		}
		return false;
	}
	
	/****************************************************************
	 * Begins a SELECT * statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL select() {
		if(!isQueryCached()) {
			if(!this.hasColumnSubqueries()) {
				query.append("SELECT * FROM "+object.getTableName());
			}else {
				query.append("SELECT * "+this.getColumnSubqueriesString()+" FROM "+object.getTableName());
			}
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param field names
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL select(String ...fieldnames) {
		if(!isQueryCached()) {
			query.append("SELECT");
			//---------------------------------
			// Add Fields
			for(String fieldname : fieldnames) {
					query.append(" ").append(fieldname).append(",");
			}
			query.deleteCharAt(query.length()-1);
			
			//---------------------------------
			// Add Column Subqueries
			if(this.hasColumnSubqueries()) {
				query.append(this.getColumnSubqueriesString());
			}
			
			query.append(" FROM "+object.getTableName());
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a SELECT statement including all fields except the 
	 * ones specified by the parameter.
	 * @param fieldnames
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL selectWithout(String ...fieldnames) {
		if(!isQueryCached()) {
			query.append("SELECT");
			
			//---------------------------------
			// Add Fields
			Arrays.sort(fieldnames);
			for(String name : fields.keySet()) {
				//add if name is not in fieldnames
				if(Arrays.binarySearch(fieldnames, name) < 0) {
					query.append(" ").append(name).append(",");
				}
			}
			query.deleteCharAt(query.length()-1);
			
			//---------------------------------
			// Add Column Subqueries
			if(this.hasColumnSubqueries()) {
				query.append(this.getColumnSubqueriesString());
			}
			
			query.append(" FROM "+object.getTableName());
		}
		return this;
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return boolean
	 ****************************************************************/
	public boolean insert() {

		return insert(fields.keySet().toArray(new String[] {}));
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return boolean
	 ****************************************************************/
	public boolean insert(String ...fieldnames) {
		
			StringBuilder columnNames = new StringBuilder("(");
			StringBuilder placeholders = new StringBuilder("(");
			
			for(String fieldname : fieldnames) {
				CFWField<?> field = fields.get(fieldname);
				if(field != object.getPrimaryField()) {
					if(!isQueryCached()) {
						columnNames.append(field.getName()).append(",");
						placeholders.append("?,");
					}
					values.add(field.getValue());
				}
			}
			
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1).append(")");
			placeholders.deleteCharAt(placeholders.length()-1).append(")");
			if(!isQueryCached()) {	
				query.append("INSERT INTO "+object.getTableName()+" "+columnNames
					  + " VALUES "+placeholders+";");
			}

		return this.execute();
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return  primary key or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey() {

		return insertGetPrimaryKey(fields.keySet().toArray(new String[] {}));
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return  id or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey(String ...fieldnames) {
		
			StringBuilder columnNames = new StringBuilder("(");
			StringBuilder placeholders = new StringBuilder("(");
			
			for(String fieldname : fieldnames) {
				CFWField<?> field = fields.get(fieldname);
				if(field != object.getPrimaryField()) {
					if(!isQueryCached()) {
						columnNames.append(field.getName()).append(",");
						placeholders.append("?,");
					}
					values.add(field.getValue());
				}
			}
			
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1).append(")");
			placeholders.deleteCharAt(placeholders.length()-1).append(")");
			if(!isQueryCached()) {	
				query.append("INSERT INTO "+object.getTableName()+" "+columnNames
					  + " VALUES "+placeholders+";");
			}

		return this.executeInsertGetPrimaryKey();
	}
	
	/****************************************************************
	 * Creates an update statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean update() {
		return update(fields.keySet().toArray(new String[] {}));
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields.
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean update(String ...fieldnames) {
		
		StringBuilder columnNames = new StringBuilder();
		StringBuilder placeholders = new StringBuilder();
		
		for(String fieldname : fieldnames) {
			CFWField<?> field = fields.get(fieldname);
			if(!field.equals(object.getPrimaryField())) {
				
				if(!isQueryCached()) {
					columnNames.append(field.getName()).append(",");
					placeholders.append("?,");
				}
				values.add(field.getValue());
			}
		}
		
		if(!isQueryCached()) {
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1);
			placeholders.deleteCharAt(placeholders.length()-1);
		}
		
		values.add(object.getPrimaryField().getValue());
		
		if(!isQueryCached()) {
			query.append("UPDATE "+object.getTableName()+" SET ("+columnNames
					  + ") = ("+placeholders+")"
					  +" WHERE "
					  + object.getPrimaryField().getName()+" = ?");
		}
		return this.execute();
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields.
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean updateWithout(String ...fieldnames) {
		
		StringBuilder columnNames = new StringBuilder();
		StringBuilder placeholders = new StringBuilder();
		Arrays.sort(fieldnames);
		for(String name : fields.keySet()) {
			//add if name is not in fieldnames
			if(Arrays.binarySearch(fieldnames, name) < 0) {
				CFWField<?> field = fields.get(name);
				if(!field.equals(object.getPrimaryField())) {
					
					if(!isQueryCached()) {
						columnNames.append(field.getName()).append(",");
						placeholders.append("?,");
					}
					values.add(field.getValue());
				}
			}
		}
		
		if(!isQueryCached()) {
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1);
			placeholders.deleteCharAt(placeholders.length()-1);
		}
		
		values.add(object.getPrimaryField().getValue());
		
		if(!isQueryCached()) {
			query.append("UPDATE "+object.getTableName()+" SET ("+columnNames
					  + ") = ("+placeholders+")"
					  +" WHERE "
					  + object.getPrimaryField().getName()+" = ?");
		}
		return this.execute();
	}
	
	/****************************************************************
	 * Begins a DELETE statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL delete() {
		if(!isQueryCached()) {		
			query.append("DELETE FROM "+object.getTableName());
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a DELETE TOP  statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL deleteTop(int count) {
		if(!isQueryCached()) {		
			query.append("DELETE TOP ? FROM "+object.getTableName());
		}
		values.add(count);
		
		return this;
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL where(String fieldname, Object value) {
		return where(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL where(String fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(value == null) {
				return whereIsNull(fieldname);
			}
			if(isCaseSensitive) {
				query.append(" WHERE "+fieldname).append(" = ?");	
			}else {
				query.append(" WHERE LOWER("+fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL like(String fieldname, Object value) {
		if(!isQueryCached()) {
			if(value == null) {
				return whereIsNull(fieldname);
			}
			query.append(" WHERE ").append(fieldname).append(" LIKE ?");	
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereIn(String fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" WHERE "+fieldname).append(" IN(?)");
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereIsNull(String fieldname) {
		if(!isQueryCached()) {
			query.append(" WHERE "+fieldname).append(" IS NULL");
		}
		return this;
	}

	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?,?...) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereIn(String fieldname, Object ...values) {
			
		StringBuilder placeholders = new StringBuilder();
		for(Object value : values) {
			placeholders.append("?,");
			this.values.add(value);
		}
		placeholders.deleteCharAt(placeholders.length()-1);
		
		if(!isQueryCached()) {
			query.append(" WHERE "+fieldname).append(" IN(").append(placeholders).append(")");
		}
		
		return this;
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereArrayContains(String fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" WHERE ARRAY_CONTAINS("+fieldname).append(", ?) ");
		}
		
		values.add(value);
		return this;
	}
	/****************************************************************
	 * Begins a SELECT COUNT(*) statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL selectCount() {
		if(!isQueryCached()) {
			query.append("SELECT COUNT(*) FROM "+object.getTableName());
		}
		return this;
	}
		
	/****************************************************************
	 * Adds a AND clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL and(String fieldname, Object value) {
		return and(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL and(String fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(isCaseSensitive) {
				query.append(" AND "+fieldname).append(" = ?");	
			}else {
				query.append(" AND LOWER("+fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a OR clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL or(String fieldname, Object value) {
		return or(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL or(String fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(isCaseSensitive) {
				query.append(" OR "+fieldname).append(" = ?");	
			}else {
				query.append(" OR LOWER("+fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a UNION to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL union() {
		if(!isQueryCached()) {
			query.append(" UNION ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a UNION ALL to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL unionAll() {
		if(!isQueryCached()) {
			query.append(" UNION ALL ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a NULLS FIRST to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL nullsFirst() {
		if(!isQueryCached()) {
			query.append(" NULLS FIRST");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderby(String fieldname) {
		if(!isQueryCached()) {
			if(fields.get(fieldname).getValueClass() == String.class) {
				query.append(" ORDER BY LOWER("+fieldname+")");
			}else {
				query.append(" ORDER BY "+fieldname);
			}
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderby(String... fieldnames) {
		if(!isQueryCached()) {
			query.append(" ORDER BY");
			for(String fieldname : fieldnames) {
				if(fields.get(fieldname).getValueClass() == String.class) {
					query.append(" LOWER("+fieldname+"),");
				}else {
					query.append(" "+fieldname+",");
				}
			}
			query.deleteCharAt(query.length()-1);
			
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings. Sort order is descending.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderbyDesc(String fieldname) {
		if(!isQueryCached()) {				
			if(fields.get(fieldname).getValueClass() == String.class) {
				query.append(" ORDER BY LOWER("+fieldname+") DESC");
			}else {
				query.append(" ORDER BY "+fieldname+" DESC");
			}
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a LIMIT statement
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL limit(int limit) {
		if(!isQueryCached()) {
			query.append(" LIMIT ?");
		}
		values.add(limit);
		return this;
	}
	
	/****************************************************************
	 * Adds an OFFSET statement
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL offset(int offset) {
		if(!isQueryCached()) {
			query.append(" OFFSET ?");
		}
		values.add(offset);
		return this;
	}
	
	/****************************************************************
	 * Adds a custom part to the query and values for the binding.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL loadSQLResource(String packageName, String filename, Object... params) {
		
		if(!isQueryCached()) {
			String queryPart = CFW.Files.readPackageResource(packageName, filename);
			query.append(queryPart);
		}
		
		for(Object param : params) {
			values.add(param);
		}
		return this;
	}
	
	
	/****************************************************************
	 * Adds a custom part to the query and values for the binding.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL custom(String queryPart, Object... params) {
		if(!isQueryCached()) {
			query.append(" ").append(queryPart).append(" ");
		}
		
		for(Object param : params) {
			values.add(param);
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a custom part to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL custom(String queryPart) {
		if(!isQueryCached()) {
			query.append(queryPart);
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a custom part to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL append(CFWSQL partialQuery) {
		if(partialQuery != null) {
			if(!isQueryCached()) {
				query.append(partialQuery.getStatementString());
			}
			System.out.println("### Values: "+Arrays.toString(partialQuery.values.toArray()));
			values.addAll(partialQuery.values);
		}
		return this;
	}
	
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean execute() {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		if(statement.trim().startsWith("SELECT")) {
			result = CFWDB.preparedExecuteQuery(statement, values.toArray());
			if(result != null) {
				return true;
			}else {
				return false;
			}
		}else {
			return CFWDB.preparedExecute(statement, values.toArray());
		}
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean executeBatch() {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		boolean success = CFWDB.preparedExecuteBatch(statement, values.toArray());
		
		CFWDB.close(result);
		
		return success;

	}
	
	/****************************************************************
	 * Executes the query as an insert and returns the first generated 
	 * Key of the new record. (what is a primary key in most cases)
	 * 
	 * @return integer or null
	 ****************************************************************/
	private Integer executeInsertGetPrimaryKey() {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		if(statement.trim().startsWith("INSERT")) {
			return CFWDB.preparedInsertGetKey(statement, object.getPrimaryField().getName(), values.toArray());
			
		}else {
			new CFWLog(logger)
			.method("executeInsertGetKey")
			.severe("The query is not an insert statement: "+statement);
			
			return null;
		}
		
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean executeDelete() {
		
		boolean success = this.execute();
		CFWDB.close(result);
		
		return success;
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return int count or -1 on error
	 ****************************************************************/
	public int getCount() {
		
		try {
			
			this.execute();
			if(result != null) {	
				//----------------------------
				// Handle Caching
				String statement = getStatementCached();
				
				//----------------------------
				//Get Count
				if(statement.toString().trim().contains("SELECT COUNT(*)")) {
					if(result.next()) {
						return result.getInt(1);
					}
				}else {
					  result.last();    // moves cursor to the last row
					  return result.getRow();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			CFWDB.close(result);
		}
		return -1;
	}
	
	/****************************************************************
	 * Executes the query and returns the result set.
	 * Don't forget to close the connection using CFW.DB.close();
	 * @return ResultSet or null 
	 ****************************************************************/
	public ResultSet getResultSet() {
		
		if(this.execute()) {
			return result;
		}else {
			return null;
		}
	}
	
	/****************************************************************
	 * Executes the query and returns the result set.
	 * @return CFWObject the resulting object
	 ****************************************************************/
	public CFWObject getFirstObject() {
		
		try {
			if(this.execute()) {
				if(result.next()) {
					CFWObject object = this.object.getClass().newInstance();
					object.mapResultSet(result);
					return object;
				}
			}
		}catch (SQLException | InstantiationException | IllegalAccessException e) {
			new CFWLog(logger)
			.method("getFirstObject")
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}

		
		return null;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as Objects.
	 ****************************************************************/
	public ArrayList<CFWObject> getObjectList() {
		
		ArrayList<CFWObject> objectArray = new ArrayList<CFWObject>();
		
		if(this.execute()) {
			
			if(result == null) {
				return objectArray;
			}
			
			try {
				while(result.next()) {
					CFWObject current = object.getClass().newInstance();
					current.mapResultSet(result);
					objectArray.add(current);
				}
			} catch (SQLException | InstantiationException | IllegalAccessException e) {
				new CFWLog(logger)
				.method("getObjectList")
				.severe("Error reading object from database.", e);
				
			}finally {
				CFWDB.close(result);
			}
			
		}
		
		return objectArray;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a key value map.
	 ****************************************************************/
	public HashMap<Object, Object> getKeyValueMap(String keyColumnName, String valueColumnName) {
		
		HashMap<Object, Object> keyValueMap = new HashMap<Object, Object>();
		
		if(this.execute()) {
			
			if(result == null) {
				return keyValueMap;
			}
			
			try {
				while(result.next()) {
					Object key = result.getObject(keyColumnName);
					Object value = result.getObject(valueColumnName);
					keyValueMap.put(key, value);
				}
			} catch (SQLException e) {
				new CFWLog(logger)
				.method("getKeyValueMap")
				.severe("Error reading object from database.", e);
				
			}finally {
				CFWDB.close(result);
			}
			
		}
		
		return keyValueMap;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array.
	 ***************************************************************/
	public String[] getAsStringArray(String columnName) {
		return getAsStringArrayList(columnName).toArray(new String[] {});
	}
	/***************************************************************
	 * Execute the Query and gets the result as a string array list.
	 ***************************************************************/
	public ArrayList<String> getAsStringArrayList(String columnName) {
		
		ArrayList<String> stringArray = new ArrayList<String>();
		
		if(this.execute()) {
			
			if(result == null) {
				return stringArray;
			}
			
			try {
				while(result.next()) {
					Object value = result.getObject(columnName);
					stringArray.add(value.toString());
				}
			} catch (SQLException e) {
				new CFWLog(logger)
				.method("getAsStringArray")
				.severe("Error reading object from database.", e);
				
			}finally {
				CFWDB.close(result);
			}
			
		}
		
		return stringArray;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array list.
	 ***************************************************************/
	public LinkedHashMap<Object, Object> getAsLinkedHashMap(String keyColumnName, String valueColumnName) {
		
		LinkedHashMap<Object, Object>  resultMap = new LinkedHashMap<Object, Object>();
		
		if(this.execute()) {
			
			if(result == null) {
				return resultMap;
			}
			
			try {
				while(result.next()) {
					Object key = result.getObject(keyColumnName);
					Object value = result.getObject(valueColumnName);
					resultMap.put(key, value);
				}
			} catch (SQLException e) {
				new CFWLog(logger)
				.method("getAsLinkedHashMap")
				.severe("Error reading object from database.", e);
				
			}finally {
				CFWDB.close(result);
			}
			
		}
		
		return resultMap;
		
	}
		
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public String getAsJSON() {
		
		this.execute();
		String	string = CFWDB.resultSetToJSON(result);
		CFWDB.close(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public String getAsCSV() {
		
		this.execute();
		String string = CFWDB.resultSetToCSV(result, ";");
		CFWDB.close(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as XML string.
	 ****************************************************************/
	public String getAsXML() {
		
		this.execute();
		String	string = CFWDB.resultSetToXML(result);
		CFWDB.close(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as XML string.
	 ****************************************************************/
	public static String getStatisticsAsJSON() {
		
		StringBuilder json = new StringBuilder("[");
		
		if(!queryCache.isEmpty()) {
			for(String key : queryCache.keySet()) {
				String statement = queryCache.get(key);
				int count = cacheHitCount.get(statement);
				json.append("{ \"name\": \"").append(CFW.JSON.escapeString(key)).append("\", ")
				.append("\"query\": \"").append(CFW.JSON.escapeString(statement)).append("\", ")
				.append("\"count\": ").append(count).append("},");
			}
			json.deleteCharAt(json.length()-1); //remove last comma
		}
		
		return json.append("]").toString();
		
	}
	
	
}
