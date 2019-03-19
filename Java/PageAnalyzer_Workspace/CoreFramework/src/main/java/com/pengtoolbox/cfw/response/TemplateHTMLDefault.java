package com.pengtoolbox.cfw.response;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.SessionData;
import com.pengtoolbox.cfw.caching.FileAssembly;
import com.pengtoolbox.cfw.caching.FileAssembly.HandlingType;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.utils.FileUtils;

public class TemplateHTMLDefault extends AbstractTemplateHTML {
	
	public static Logger logger = CFWLog.getLogger(TemplateHTMLDefault.class.getName());
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	public TemplateHTMLDefault(HttpServletRequest request, String pageTitle){
		
		super(request);
		
		this.pageTitle = pageTitle;
		
		this.addCSSFile(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/css/bootstrap.min.css");
		this.addCSSFile(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/css/bootstrap-theme.css");
		this.addCSSFile(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/css/font-awesome.css");
		this.addCSSFile(HandlingType.FILE, "./resources/css/custom.css");
		
		this.addJSFileBottom(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/js/jquery-2.2.3.js");
		this.addJSFileBottom(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/js/bootstrap.js");
		this.addJSFileBottom(HandlingType.JAR_RESOURCE, FileAssembly.CFW_JAR_RESOURCES_PATH + "/js/cfw.js");
		this.addJSFileBottom(HandlingType.FILE, "./resources/js/custom/custom.js");
		
		SessionData data = (SessionData)request.getSession().getAttribute(CFW.SESSION_DATA);
		if(data.isLoggedIn()) {
			this.setMenu(new StringBuffer("<li class=\"nav-item\"><a class=\"nav-link\" href=\"./logout\">Logout</a></li>"));
		}
      
	}
		
	@Override
	public StringBuffer buildResponse() {
		
		StringBuffer buildedPage = new StringBuffer();
		
		buildedPage.append("<html>\n");
		
			buildedPage.append("<head>\n");
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
				buildedPage.append(this.menu);
				
				String menuTemplate = FileUtils.getFileContent(request,CFW.PATH_TEMPLATE_MENU);
				if(menuTemplate != null){
					String customMenuInserted = menuTemplate.replace("{!customMenu!}", this.menu);	
					buildedPage.append(customMenuInserted);
				}else{
					buildedPage.append("<!-- FAILED TO LOAD MENU! -->");
				}
				
				//--------------------------
				// Messages
				this.appendSectionTitle(buildedPage, "Messages");
				buildedPage.append("<div id=\"messages\">");
				buildedPage.append(this.messages);
				buildedPage.append("</div>");
				
				//--------------------------
				// Content
				this.appendSectionTitle(buildedPage, "Content");
				buildedPage.append("<div id=\"pa-content\" class=\"container\">");
				buildedPage.append(this.content);
				buildedPage.append("</div>");
				
				//--------------------------
				// Footer
				this.appendSectionTitle(buildedPage, "Footer");

				String footerTemplate = FileUtils.getFileContent(request,CFW.PATH_TEMPLATE_FOOTER);
				if(footerTemplate != null){
					String customFooterInserted = footerTemplate.replace("{!customFooter!}", this.footer);	
					buildedPage.append(customFooterInserted);
				}else{
					buildedPage.append("<!-- FAILED TO LOAD FOOTER! -->");
				}

				//--------------------------
				// Javascript
				this.appendSectionTitle(buildedPage, "Javascript");
				buildedPage.append("<div id=\"javascripts\">");
				if(assemblyCSS.hasFiles()) {
					buildedPage.append("<script src=\""+bottomjs.assemble().cache().getAssemblyServletPath()+"\"></script>");
				}
				buildedPage.append(this.javascript);
				buildedPage.append("</div>");
				

				//--------------------------
				// JavascriptData
				this.appendSectionTitle(buildedPage, "Javascript Data");
				buildedPage.append("<div id=\"javaScriptData\" style=\"display: none;\">");
				buildedPage.append(this.javascriptData);
				buildedPage.append("</div>");
				
				//--------------------------
				// Support Info
				this.appendSectionTitle(buildedPage, "Support Info");
				
				String supportInfoTemplate = FileUtils.getFileContent(request,CFW.PATH_TEMPLATE_SUPPORTINFO);
				
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
