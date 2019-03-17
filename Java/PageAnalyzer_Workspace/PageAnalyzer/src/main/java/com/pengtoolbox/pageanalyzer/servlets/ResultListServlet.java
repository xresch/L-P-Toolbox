package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWConfig;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.TemplateHTMLDefault;
import com.pengtoolbox.pageanalyzer.db.PageAnalyzerDB;

public class ResultListServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ResultListServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLog log = new CFWLog(logger, request).method("doPost");
		log.info(request.getRequestURL().toString());
			
		TemplateHTMLDefault html = new TemplateHTMLDefault(request, "View Result");
		StringBuffer content = html.getContent();

		String userID = "";
		
		if(CFWConfig.AUTHENTICATION_ENABLED) {
			SessionData data = (SessionData) request.getSession().getAttribute(CFW.SESSION_DATA); 
			if(data.isLoggedIn()) {
				userID = data.getUsername();
			}
		}else {
			userID = "anonymous";
		}
		String jsonResults = PageAnalyzerDB.getResultListForUser(request, userID);
		
		//TODO: Check User
		
		if (jsonResults == null) {
			html.addAlert(CFW.ALERT_ERROR, "Results could not be loaded.");
		}else {
									
			content.append("<div id=\"results\"></div>");
			
			StringBuffer javascript = html.getJavascript();
			javascript.append("<script defer>");
				javascript.append("initialize();");
				javascript.append("draw({data: 'resultlist', info: 'resultlist', view: ''})");
			javascript.append("</script>");
				
		}
        
    }
	

}