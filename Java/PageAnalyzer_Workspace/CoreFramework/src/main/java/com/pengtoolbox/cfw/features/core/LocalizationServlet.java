package com.pengtoolbox.cfw.features.core;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class LocalizationServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
	
		//-----------------------
		// Response Settings
		response.addHeader("Cache-Control", "max-age="+CFWProperties.BROWSER_RESOURCE_MAXAGE);
		
		//-----------------------
		// Fetch Assembly
		String localeID = request.getParameter("id");
		Properties languagePack = CFW.Localization.getLanguagePackForRequest();

		int fileEtag = languagePack.hashCode();
		
		JSONResponse json = new JSONResponse();
		
		if(languagePack != null) {
			//-----------------------
			// Check ETag
			String requestEtag = request.getHeader("If-None-Match");
			if(requestEtag != null) {
				// gzip handler will append "--gzip", therefore check on starts with
				if(requestEtag.startsWith(""+fileEtag)) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
			}
			
			//-----------------------
			// Return Assembly
			json.getContent().append(CFW.JSON.toJSON(languagePack));
			response.addHeader("ETag", ""+fileEtag);
			
	        response.setStatus(HttpServletResponse.SC_OK);
	        json.setSuccess(true);
	    }else {
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    	json.setSuccess(false);
	    }
		
		//Done by RequestHandler
		//CFW.writeLocalized(request, response);
    }
}