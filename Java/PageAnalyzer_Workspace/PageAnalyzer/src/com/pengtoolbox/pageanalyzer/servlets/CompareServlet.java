package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.pageanalyzer._main.PA;
import com.pengtoolbox.pageanalyzer.logging.PALogger;
import com.pengtoolbox.pageanalyzer.response.TemplateHTMLDefault;
import com.pengtoolbox.pageanalyzer.utils.H2Utils;

public class CompareServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = PALogger.getLogger(CompareServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		PALogger log = new PALogger(logger, request).method("doPost");
		log.info(request.getRequestURL().toString());
			
		TemplateHTMLDefault html = new TemplateHTMLDefault(request, "Compare Results");
		StringBuffer content = html.getContent();

		content.append("<div id=\"comparison\"></div>");
		
		//Comma separated IDs
		String resultIDs = request.getParameter("resultids");
		
		//---------------------------------
		// Create array with json results
		String arrayString = "[]";
		
		if(resultIDs.matches("(\\d,?)+")) {
			arrayString = H2Utils.getResultListForComparison(resultIDs);
		}else {
			html.addAlert(PA.ALERT_ERROR, "Result IDs '"+resultIDs+"' is not a string of comma separated numbers.");
		}

		StringBuffer javascript = html.getJavascript();
		javascript.append("<script>");
		javascript.append("		var DATA_TO_COMPARE = "+arrayString+";");
		javascript.append("</script>");
		javascript.append("<script defer>initialize();</script>");

    }
	

}