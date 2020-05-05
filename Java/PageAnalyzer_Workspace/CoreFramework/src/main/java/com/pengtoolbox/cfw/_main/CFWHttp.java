package com.pengtoolbox.cfw._main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWHttp {
	
	public static Logger logger = CFWLog.getLogger(CFWHttp.class.getName());
	
	public static String encode(String toEncode) {
		
		try {
			return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			new CFWLog(logger)
				.method("encode")
				.severe("Exception while encoding: "+e.getMessage(), e);	
		}
		
		return toEncode;
	}
	
	/******************************************************************************************************
	 * Returns an encoded parameter string with leading '&'.
	 ******************************************************************************************************/
	public static String  encode(String paramName, String paramValue) {
		
		return "&" + encode(paramName) + "=" + encode(paramValue);
	}
	
	/******************************************************************************************************
	 * Redirects to the referer of the request.
	 * @throws IOException 
	 ******************************************************************************************************/
	public static void redirectToReferer( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(request.getHeader("referer")));
	}
	
	/******************************************************************************************************
	 * Redirects to the specified url.
	 * @throws IOException 
	 ******************************************************************************************************/
	public static void redirectToURL(HttpServletResponse response, String url ) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(url));
	}
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result as a String.
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static String sendGETRequest(String url) {
	
		StringBuffer buffer = new StringBuffer();
		BufferedReader in = null;
		try {
			URL getURL = new URL(url);
			
	        URLConnection connection = getURL.openConnection();
	        //logger.info(connection.getHeaderField("Response"));
	        
	        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String inputLine;
	
	        while ((inputLine = in.readLine()) != null) {
	        	buffer.append(inputLine);
	        	buffer.append("\n");
	        }
	    
		} catch (Exception e) {
			new CFWLog(logger)
				.method("sendGETRequest")
				.severe("Exception occured.", e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					new CFWLog(logger)
					.method("sendGETRequest")
					.severe("Exception occured while closing stream.", e);
				}
			}
		}
	    
		return buffer.toString();
	
	}
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result as a String.
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
//	public static URL createProxiedURL(String url) {
//		
//
//		//Proxy instance, proxy ip = 10.0.0.1 with port 8080
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
//		//conn = new URL(url).openConnection(proxy);
//	}
	
	/******************************************************************************************************
	 * Creates a map of all cookies in a request.
	 * @param request
	 * @return HashMap containing the key value pairs of the cookies in the response
	 ******************************************************************************************************/
	public static HashMap<String,String> getCookiesAsMap(HttpServletRequest request) {
		
		HashMap<String,String> cookieMap = new HashMap<String,String>();
		
		for(Cookie cookie : request.getCookies()) {
			cookieMap.put(cookie.getName(), cookie.getValue());
		}
		
		return cookieMap;
	}
	
	/******************************************************************************************************
	 * Get the cookie
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static Cookie getRequestCookie(HttpServletRequest request, String cookieKey) {
		
		if(request.getCookies() != null){
			for(Cookie cookie : request.getCookies()){
				if(cookie.getName().equals(cookieKey)){
					return cookie;
				}
			}
		}
		
		return null;
		
	}
	
	/******************************************************************************************************
	 * Get the body content of the request.
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static String getRequestBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
	
	
}
