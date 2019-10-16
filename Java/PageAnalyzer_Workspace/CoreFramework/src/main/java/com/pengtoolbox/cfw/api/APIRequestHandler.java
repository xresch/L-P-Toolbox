package com.pengtoolbox.cfw.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class APIRequestHandler {
	

	public abstract void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition);

}
