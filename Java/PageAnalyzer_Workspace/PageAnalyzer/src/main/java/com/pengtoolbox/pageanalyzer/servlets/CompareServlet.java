package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.logging.CFWLogger;
import com.pengtoolbox.cfw.response.TemplateHTMLDefault;
import com.pengtoolbox.pageanalyzer.db.PageAnalyzerDB;

public class CompareServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLogger.getLogger(CompareServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLogger log = new CFWLogger(logger, request).method("doPost");
		log.info(request.getRequestURL().toString());
			
		TemplateHTMLDefault html = new TemplateHTMLDefault(request, "Compare Results");
		StringBuffer content = html.getContent();

		content.append("<div id=\"results\"></div>");
		
		//Comma separated IDs
		String resultIDs = request.getParameter("resultids");
		
		//---------------------------------
		// Create array with json results
		String arrayString = "[]";
		
		if(!resultIDs.matches("(\\d,?)+")) {
			html.addAlert(CFW.ALERT_ERROR, "Result IDs '"+resultIDs+"' is not a string of comma separated numbers.");
		}

		StringBuffer javascript = html.getJavascript();
		
		javascript.append("<script defer>");
			javascript.append("initialize();");
			javascript.append("draw({data: 'compareyslow', info: 'compare', view: 'yslow'})");
		javascript.append("</script>");

    }
	

}