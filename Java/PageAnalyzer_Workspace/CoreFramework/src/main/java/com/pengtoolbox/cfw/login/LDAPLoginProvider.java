package com.pengtoolbox.cfw.login;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.db.usermanagement.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class LDAPLoginProvider implements LoginProvider {

	public LDAPLoginProvider() {}
	
	@Override
	public User checkCredentials(String username, String password) {

		if(CFW.DB.Users.checkUsernameExists(username)) {
			//--------------------------------
			// Check User in DB			
			User user = CFW.DB.Users.selectByUsernameOrMail(username);
			if(user.isForeign()) {
				if(authenticateAgainstLDAP(username, password)) {
					return user;
				}
			}else {
				if(user.passwordValidation(password)) {
					return user;
				}
			}
		}else {
			//--------------------------------
			// Create User if password is correct
			
			if(authenticateAgainstLDAP(username, password))
			{
				User newUser = new User(username)
						.isForeign(true)
						.status("Active");
				
				CFW.DB.Users.create(newUser);
				User userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
				
				CFW.DB.UserGroupMap.addUserToGroup(userFromDB, CFW.DB.Groups.CFW_GROUP_USER, true);
				
				return userFromDB;
			}
		}
		
		return null;
	}
		
	/**********************************************************************
	 * Authenticate against the LDAP defined in the cfw.properties.
	 * @param username
	 * @param password
	 * @return true if credentials are valid, false otherwise
	 **********************************************************************/
	private boolean authenticateAgainstLDAP(String username, String password) {
		Properties props = new Properties(); 
		InitialDirContext context = null;
		String user = "";
		
		try {
		    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		    props.put(Context.PROVIDER_URL, CFWProperties.LDAP_URL);
		    props.put(Context.SECURITY_PRINCIPAL, CFWProperties.LDAP_USER);
		    props.put(Context.SECURITY_CREDENTIALS, CFWProperties.LDAP_PASSWORD);
	
		    context = new InitialDirContext(props);
	
		    SearchControls ctrls = new SearchControls();
		    ctrls.setReturningAttributes(new String[] {});
		    ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	
		    NamingEnumeration<javax.naming.directory.SearchResult> answers = context.search(CFWProperties.LDAP_SEARCHBASE, "("+CFWProperties.LDAP_USER_ATTRIBUTE+"=" + username + ")", ctrls);
		    if(answers.hasMore()) {
		    	javax.naming.directory.SearchResult result = answers.nextElement();
		    	user = result.getNameInNamespace();
		    }else {
		    	return false;
		    }
	
		}catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    
		
	    try {
	        props = new Properties();
	        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        props.put(Context.PROVIDER_URL, CFWProperties.LDAP_URL);
	        props.put(Context.SECURITY_PRINCIPAL, user);
	        props.put(Context.SECURITY_CREDENTIALS, password);

	        context = new InitialDirContext(props);
	        
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	

}
