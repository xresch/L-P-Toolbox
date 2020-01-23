package com.pengtoolbox.cfw.features.api;

import java.util.logging.Logger;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplication;
import com.pengtoolbox.cfw.features.cpusampling.StatsCPUSample;
import com.pengtoolbox.cfw.features.cpusampling.StatsCPUSampleSignature;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureAPI extends CFWAppFeature {

	private static Logger logger = CFWLog.getLogger(FeatureAPI.class.getName());
	
	public static final String PERMISSION_CFW_API = "API";
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.features.api.resources";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
    	
    	//----------------------------------
    	// Register Menu Entry

		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("API")
					.faicon("fas fa-code")
					.addPermission(PERMISSION_CFW_API)
					.href("./api")	
				, null);
	}

	@Override
	public void initializeDB() {
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
		//Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
		
		//-----------------------------------
		// 
		//-----------------------------------------
		// API
		//-----------------------------------------
		if(!CFW.DB.Permissions.checkExistsByName(PERMISSION_CFW_API)) {
			CFW.DB.Permissions.create(new Permission(PERMISSION_CFW_API, "user")
				.description("User can access the API.")
				.isDeletable(false)
			);
			
			Permission permission = CFW.DB.Permissions.selectByName(PERMISSION_CFW_API);
			CFW.DB.RolePermissionMap.addPermissionToRole(permission, adminRole, true);
		}

	}

	@Override
	public void addFeature(CFWApplication app) {	
    	ServletContextHandler appContext = app.getSecureContext();

    	appContext.addServlet(ServletAPI.class,  "/api");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
