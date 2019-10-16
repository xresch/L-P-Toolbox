package com.pengtoolbox.cfw.db.usermanagement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.db.usermanagement.UserGroupMap.UserGroupMapFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBUserGroupMap {

	public static String TABLE_NAME = "CFW_USER_GROUP_MAP";
	
	public static Logger logger = CFWLog.getLogger(CFWDBUserGroupMap.class.getName());
	
	
	/********************************************************************************************
	 * Adds the user to the specified group.
	 * @param user
	 * @param groupname
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToGroup(User user, String groupname, boolean isDeletable) {
		return addUserToGroup(user, CFW.DB.Groups.selectByName(groupname), isDeletable);
	}
	
	/********************************************************************************************
	 * Adds the user to the specified group.
	 * @param user
	 * @param group
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToGroup(User user, Group group, boolean isDeletable) {
		
		if(user == null || group == null ) {
			new CFWLog(logger)
				.method("addUserToGroup")
				.warn("User and group cannot be null.");
			return false;
		}
		
		if(user.id() < 0 || group.id() < 0) {
			new CFWLog(logger)
				.method("addUserToGroup")
				.warn("User-ID and group-ID are not set correctly.");
			return false;
		}
		
		if(checkIsUserInGroup(user, group)) {
			new CFWLog(logger)
				.method("addUserToGroup")
				.warn("The user '"+user.username()+"' is already part of the group '"+group.name()+"'.");
			return false;
		}
		
		return addUserToGroup(user.id(), group.id(), isDeletable);
	}
	
	/********************************************************************************************
	 * Adds the user to the specified group.
	 * @param user
	 * @param group
	 * @param isdeletable, define if this association can be deleted
	 * @return return true if user was added, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean addUserToGroup(int userid, int groupid, boolean isDeletable) {
		String insertGroupSQL = "INSERT INTO "+TABLE_NAME+" ("
				  + UserGroupMapFields.FK_ID_USER +", "
				  + UserGroupMapFields.FK_ID_GROUP +", "
				  + UserGroupMapFields.IS_DELETABLE +" "
				  + ") VALUES (?,?,?);";
		
		return CFWDB.preparedExecute(insertGroupSQL, 
				userid,
				groupid,
				isDeletable
				);
	}
	
	/********************************************************************************************
	 * Remove a user from the group.
	 * @param user
	 * @param group
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromGroup(User user, Group group) {
		
		if(user == null || group == null ) {
			new CFWLog(logger)
				.method("removeUserFromGroup")
				.warn("User and group cannot be null.");
			return false;
		}
		
		if(!checkIsUserInGroup(user, group)) {
			new CFWLog(logger)
				.method("removeUserFromGroup")
				.warn("The user '"+user.username()+"' is not part of the group '"+group.name()+"' and cannot be removed.");
			return false;
		}
		
		return removeUserFromGroup(user.id(), group.id());
	}

	/********************************************************************************************
	 * Remove a user from the group.
	 * @param user
	 * @param group
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean removeUserFromGroup(int userID, int groupID) {
		String removeUserFromGroupSQL = "DELETE FROM "+TABLE_NAME
				+" WHERE "
				  + UserGroupMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + UserGroupMapFields.FK_ID_GROUP +" = ? "
				  + " AND "
				  + UserGroupMapFields.IS_DELETABLE +" = TRUE "
				  + ";";
		
		return CFWDB.preparedExecute(removeUserFromGroupSQL, 
				userID,
				groupID
				);
	}
	
	/********************************************************************************************
	 * Update if the user can be deleted.
	 * @param user
	 * @param group
	 * @return return true if user was removed, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean updateIsDeletable(int userID, int groupID, boolean isDeletable) {
		String removeUserFromGroupSQL = "UPDATE "+TABLE_NAME
				+" SET "+ UserGroupMapFields.IS_DELETABLE +" = ? "
				+" WHERE "
				  + UserGroupMapFields.FK_ID_USER +" = ? "
				  + " AND "
				  + UserGroupMapFields.FK_ID_GROUP +" = ? "
				  + ";";
		
		return CFWDB.preparedExecute(removeUserFromGroupSQL, 
				isDeletable,
				userID,
				groupID
				);
	}
	
	/****************************************************************
	 * Check if the user is in the given group.
	 * 
	 * @param group to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserInGroup(User user, Group group) {
		
		if(user != null && group != null) {
			return checkIsUserInGroup(user.id(), group.id());
		}else {
			new CFWLog(logger)
				.method("checkIsUserInGroup")
				.severe("The user and group cannot be null. User: '"+user+"', Group: '"+group+"'");
			
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the group exists by name.
	 * 
	 * @param group to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkIsUserInGroup(int userid, int groupid) {
		
		String checkIsUserInGroup = "SELECT COUNT(*) FROM "+TABLE_NAME
				+" WHERE "+UserGroupMapFields.FK_ID_USER+" = ?"
				+" AND "+UserGroupMapFields.FK_ID_GROUP+" = ?";
		
		ResultSet result = CFW.DB.preparedExecuteQuery(checkIsUserInGroup, userid, groupid);
		
		try {
			if(result != null && result.next()) {
				int count = result.getInt(1);
				return (count == 0) ? false : true;
			}
		} catch (Exception e) {
			new CFWLog(logger)
			.method("groupExists")
			.severe("Exception occured while checking of group exists.", e);
			
			return false;
		}finally {
			CFWDB.close(result);
		}
		
		
		return false;
	}
	

	/***************************************************************
	 * Select user groups by the user id.
	 * @param group
	 * @return Hashmap with groups(key=group name, value=group object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Group> selectGroupsForUser(User user) {
		if( user == null) {
			new CFWLog(logger)
				.method("create")
				.severe("The user cannot be null");
			return null;
		}
		
		return selectGroupsForUser(user.id());
	}
	/***************************************************************
	 * Select user groups by the user id.
	 * @param group
	 * @return Hashmap with groups(key=group name, value=group object), or null on exception
	 ****************************************************************/
	public static HashMap<String, Group> selectGroupsForUser(int userID) {
		
		
		String selectGroupsForUser = "SELECT * FROM "+Group.TABLE_NAME+" G "
				+ " INNER JOIN "+CFWDBUserGroupMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_GROUP = G.PK_ID "
				+ " WHERE M.FK_ID_USER = ?";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectGroupsForUser, 
				userID);
		
		HashMap<String, Group> groupMap = new HashMap<String, Group>(); 
		
		try {
			while(result != null && result.next()) {
				Group group = new Group(result);
				groupMap.put(group.name(), group);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.method("selectGroupsForUser")
			.severe("Error while selecting groups for the user with id '"+userID+"'.", e);
			return null;
		}finally {
			CFWDB.close(result);
		}
		
		return groupMap;
	
	}
	
	/***************************************************************
	 * Returns a list of all groups and if the user is part of them 
	 * as a json array.
	 * @param group
	 * @return Hashmap with groups(key=group name, value=group object), or null on exception
	 ****************************************************************/
	public static String getGroupMapForUserAsJSON(String userID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
			.method("deleteMultipleByID")
			.severe("The userID '"+userID+"' is not a number.");
			return "[]";
		}
		
		String selectGroupsForUser = "SELECT G.PK_ID, G.NAME, G.DESCRIPTION, M.FK_ID_USER AS ITEM_ID, M.IS_DELETABLE FROM "+Group.TABLE_NAME+" G "
				+ " LEFT JOIN "+CFWDBUserGroupMap.TABLE_NAME+" M "
				+ " ON M.FK_ID_GROUP = G.PK_ID "
				+ " AND M.FK_ID_USER = ?"
				+ " ORDER BY LOWER(G.NAME)";
		
		ResultSet result = CFWDB.preparedExecuteQuery(selectGroupsForUser, 
				userID);
		String json = CFWDB.resultSetToJSON(result);
		CFWDB.close(result);	
		return json;

	}
	
	/***************************************************************
	 * Remove the user from the group if it is a member of the group, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toogleUserInGroup(String userID, String groupID) {
		
		//----------------------------------
		// Check input format
		if(userID == null ^ !userID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInGroup")
			.severe("The userID '"+userID+"' is not a number.");
			return false;
		}
		
		//----------------------------------
		// Check input format
		if(groupID == null ^ !groupID.matches("\\d+")) {
			new CFWLog(logger)
			.method("toogleUserInGroup")
			.severe("The groupID '"+userID+"' is not a number.");
			return false;
		}
		
		return toggleUserInGroup(Integer.parseInt(userID), Integer.parseInt(groupID));
		
	}
	
	/***************************************************************
	 * Remove the user from the group if it is a member of the group, 
	 * add it otherwise.
	 ****************************************************************/
	public static boolean toggleUserInGroup(int userID, int groupID) {
		
		if(checkIsUserInGroup(userID, groupID)) {
			return removeUserFromGroup(userID, groupID);
		}else {
			return addUserToGroup(userID, groupID, true);
		}

	}
		
}
