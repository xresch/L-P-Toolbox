package com.pengtoolbox.cfw.db.usermanagement;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.pengtoolbox.cfw.db.usermanagement.CFWDBPermission.PermissionDBFields;

public class Permission {
	
	private int id;
	private String name;
	private String description;
	private boolean isDeletable = true;
	
	public Permission(String name) {
		this.name = name;
	}
	
	public Permission(ResultSet result) throws SQLException {
		
		this.id = result.getInt(PermissionDBFields.PK_ID.toString());
		this.name = result.getString(PermissionDBFields.NAME.toString());
		this.description = result.getString(PermissionDBFields.DESCRIPTION.toString());
		this.isDeletable = result.getBoolean(PermissionDBFields.IS_DELETABLE.toString());
		
	}

	public int id() {
		return id;
	}
	
	public Permission id(int id) {
		this.id = id;
		return this;
	}
	
	public String name() {
		return name;
	}
	
	public Permission name(String username) {
		this.name = username;
		return this;
	}
	
	
	
	public String description() {
		return description;
	}

	public Permission description(String description) {
		this.description = description;
		return this;
	}

	public boolean isDeletable() {
		return isDeletable;
	}
	
	public Permission isDeletable(boolean isDeletable) {
		this.isDeletable = isDeletable;
		return this;
	}	

	public String getKeyValueString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("\nid: "+id);
		builder.append("\nname: "+name);
		builder.append("\ndescription: "+description);
		builder.append("\nisDeletable: "+isDeletable);
		
		return builder.toString();
	}
	
	
}
