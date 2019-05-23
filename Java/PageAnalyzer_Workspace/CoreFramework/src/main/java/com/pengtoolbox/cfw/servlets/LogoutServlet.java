package com.pengtoolbox.cfw.servlets;

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

public class LogoutServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(LogoutServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLog log = new CFWLog(logger, request).method("doGet");
		log.info(request.getRequestURL().toString());
		
		SessionData data = (SessionData)request.getSession().getAttribute(CFW.SESSION_DATA); 	
		data.setUsername("");
		data.triggerLogout();
		
		response.sendRedirect(response.encodeRedirectURL("./login"));
        
    }
	
}