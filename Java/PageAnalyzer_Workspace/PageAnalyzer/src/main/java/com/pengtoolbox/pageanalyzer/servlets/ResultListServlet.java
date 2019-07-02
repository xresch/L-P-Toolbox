package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWConfig;
import com.pengtoolbox.cfw._main.CFWContextRequest;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.TemplateHTMLDefault;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
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
		CFWLog log = new CFWLog(logger).method("doPost");
		log.info(request.getRequestURL().toString());
			
		TemplateHTMLDefault html = new TemplateHTMLDefault("View Result");
		StringBuffer content = html.getContent();

		String username = "";
		
		if(CFWConfig.AUTHENTICATION_ENABLED) {
			SessionData data = CFW.Context.Request.getSessionData(); 
			if(data.isLoggedIn()) {
				username = data.getUser().username();
			}
		}else {
			username = "anonymous";
		}
		String jsonResults = PageAnalyzerDB.getResultListForUser(username);
		
		//TODO: Check User
		
		if (jsonResults == null) {
			CFWContextRequest.addAlert(AlertMessage.MessageType.ERROR, "Results could not be loaded.");
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