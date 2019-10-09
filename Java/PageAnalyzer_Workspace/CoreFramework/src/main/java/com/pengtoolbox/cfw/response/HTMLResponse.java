package com.pengtoolbox.cfw.response;

import java.util.Collection;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage;
import com.pengtoolbox.cfw.response.bootstrap.BTFooter;
import com.pengtoolbox.cfw.response.bootstrap.BTMenu;
import com.pengtoolbox.cfw.utils.CFWFiles;

public class HTMLResponse extends AbstractHTMLResponse {
	
	public static Logger logger = CFWLog.getLogger(HTMLResponse.class.getName());
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	public HTMLResponse(String pageTitle){
		
		super();
		
		this.pageTitle = pageTitle;
		
		//this.addCSSFile(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + ".css", "bootstrap.min.css");
		this.addCSSFile(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".css", "bootstrap-theme-"+CFW.Properties.APPLICATION_THEME.toLowerCase()+".css");
		//this.addCSSFile(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".css", "jquery-ui.min.css");
		this.addCSSFile(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".css", "cfw.css");
		this.addCSSFile(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".css", "font-awesome.css");
		this.addCSSFile(FileDefinition.HandlingType.FILE, "./resources/css", "custom.css");
		
		this.addJSFileBottomAssembly(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".js", "jquery-3.4.1.min.js");
		//this.addJSFileBottomAssembly(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".js", "jquery-ui-1.12.3.min.js");
		this.addJSFileBottomAssembly(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".js", "bootstrap.bundle.min.js");
		this.addJSFileBottomAssembly(FileDefinition.HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH + ".js", "cfw.js");
		this.addJSFileBottomAssembly(FileDefinition.HandlingType.FILE, "./resources/js", "custom.js");
		      
	}
		
	@Override
	public StringBuffer buildResponse() {
		
		StringBuffer buildedPage = new StringBuffer();
		
		buildedPage.append("<html>\n");
		
			buildedPage.append("<head>\n");
				buildedPage.append("<meta charset=\"utf-8\">");
				buildedPage.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
		    	buildedPage.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
				buildedPage.append("<title>").append(this.pageTitle).append("</title>");
				buildedPage.append(head);
				if(assemblyCSS.hasFiles()) {
					buildedPage.append("<link rel=\"stylesheet\" href=\""+assemblyCSS.assemble().cache().getAssemblyServletPath()+"\" />");
				}
				if(headjs.hasFiles()) {
					buildedPage.append("<script src=\""+headjs.assemble().cache().getAssemblyServletPath()+"\"></script>");
				}
			buildedPage.append("</head>\n");
			
			buildedPage.append("<body ng-app=\"omApp\">\n");
			
				//--------------------------
				// Menubar
				
				this.appendSectionTitle(buildedPage, "Menubar");
				buildedPage.append("");
				SessionData sessionData = CFW.Context.Request.getSessionData();
				BTMenu menu = sessionData.getMenu();
				if(menu != null) {
					buildedPage.append(menu.getHTML());
				}
				
				//--------------------------
				// Messages
				this.appendSectionTitle(buildedPage, "Messages");
				buildedPage.append("<div id=\"cfw-messages\">");
					
				Collection<AlertMessage> messageArray = CFW.Context.Request.getAlertMessages();
					if(messageArray != null) {
						for(AlertMessage message : messageArray) {
							buildedPage.append(message.createHTML());
						}
					}
				
				buildedPage.append("</div>");
				
				//--------------------------
				// Content
				this.appendSectionTitle(buildedPage, "Content");
				buildedPage.append("<div id=\"cfw-content\"> ");
					buildedPage.append("<div id=\"cfw-container\" class=\"container\">");
						buildedPage.append(this.content);
					buildedPage.append("</div>");
				buildedPage.append("</div>");
				
				//--------------------------
				// Footer
				this.appendSectionTitle(buildedPage, "Footer");

				BTFooter footer = sessionData.getFooter();
				if(footer != null) {
					buildedPage.append(footer.getHTML());
				}
				
//				String footerTemplate = CFWFiles.getFileContent(request,CFW.PATH_TEMPLATE_FOOTER);
//				if(footerTemplate != null){
//					String customFooterInserted = footerTemplate.replace("{!customFooter!}", this.footer);	
//					buildedPage.append(customFooterInserted);
//				}else{
//					buildedPage.append("<!-- FAILED TO LOAD FOOTER! -->");
//				}

				//--------------------------
				// JavascriptData
				this.appendSectionTitle(buildedPage, "Javascript Data");
				buildedPage.append("<div id=\"javaScriptData\" style=\"display: none;\">");
				buildedPage.append(this.javascriptData);
				buildedPage.append("</div>");
				
				//--------------------------
				// Javascript
				this.appendSectionTitle(buildedPage, "Javascript");
				buildedPage.append("<div id=\"javascripts\">");
				
				if(bottomjs.hasFiles()) {
					buildedPage.append("<script src=\""+bottomjs.assemble().cache().getAssemblyServletPath()+"\"></script>");
				}
				
				for(FileDefinition fileDef : singleJSBottom) {
					buildedPage.append("\n").append(fileDef.getJavascriptTag()).append("\n");
				}
				
				buildedPage.append(this.javascript);
				buildedPage.append("</div>");
				
				//--------------------------
				// Support Info
				this.appendSectionTitle(buildedPage, "Support Info");
				
				String supportInfoTemplate = CFWFiles.getFileContent(request,CFW.PATH_TEMPLATE_SUPPORTINFO);
				
				if(supportInfoTemplate != null){
					String supportInfoInserted = supportInfoTemplate.replace("{!supportInfo!}", this.supportInfo);	
					buildedPage.append(supportInfoInserted);
				}else{
					buildedPage.append("<!-- FAILED TO LOAD SUPPORT INFO! -->");
				}
					
				
				
			buildedPage.append("</body>\n");
			
		buildedPage.append("</html>");
		
		return buildedPage;
	}

}
