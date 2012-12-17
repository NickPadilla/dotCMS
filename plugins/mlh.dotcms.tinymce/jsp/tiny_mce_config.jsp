<%@page import="com.dotmarketing.portlets.files.model.File"%>
<%@page import="com.dotmarketing.beans.Host"%>
<%@page import="com.dotmarketing.util.Config"%>
<%@page import="com.dotmarketing.business.APILocator"%>
<%@page import="com.dotmarketing.util.UtilMethods"%>
<%@page import="com.dotmarketing.business.web.WebAPILocator"%>
<%@page import="com.dotmarketing.util.InodeUtils"%>
<%@page import="com.liferay.portal.model.User"%>
<% User userb= com.liferay.portal.util.PortalUtil.getUser(request); %>

var tinyMCEProps = {
			mode : "exact",
			elements : "initwysiwyg",
			<% /*Set the WYSIWYG THEME*/
    		   String theme =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_THEME");
    		   if(UtilMethods.isSet(theme)){
    		   %>
    		   <%="theme : \""+theme+"\","%>
    		   <%
    		   }else{
    		   %>
    		   <%="theme : \"advanced\","%>
    		   <%
    		   }
    		%>
			language:'<%= userb.getLanguageId().substring(0,2) %>',
    		<% /*Set the PLUGINS THEME*/
    		   String plugins1 =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_PLUGINS1");
    		   String plugins2 =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_PLUGINS2");
    		   if (UtilMethods.isSet(plugins1) && UtilMethods.isSet(plugins2)) {
    		   %>
    		   <%="plugins : \""+plugins1 + "," + plugins2 + "\","%>
    		   <% } else { %>
    		   <%="plugins : \""+plugins1 + "\","%>
    		   <% } %>
    		   
    		   <%/*Set the WYSIWYG ADVANCED BUTTONS*/
    		   int numberOfButtons = 0;
    		   try {
    			   numberOfButtons = Integer.parseInt(APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_NUMBER_THEME_BUTTONS"));
    		   }catch(Exception e){    			   
    		   }
    		   if(numberOfButtons > 0){
    			   boolean isButtonSet =false;
    			   for(int i =1;i <= numberOfButtons;i++){
    				   // get default buttons
    				   String button =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_THEME_BUTTON"+i);
    	    		   if(UtilMethods.isSet(button)){
    	    			   isButtonSet =true;
    	    		   %>
    	    		   <%="theme_advanced_buttons"+i+" : \""+button+"\","%>
    	    		   <%
    	    		   }
    	    		   
    	    		   // get add buttons
    				   button =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_THEME_BUTTON_ADD"+i);
    	    		   if(UtilMethods.isSet(button)){
    	    			   isButtonSet =true;
    	    		   %>
    	    		   <%="theme_advanced_buttons"+i+"_add : \""+button+"\","%>
    	    		   <%
    	    		   }
    	    		   
    	    		   // get add_before buttons
    				   button =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_THEME_BUTTON_ADD_BEFORE"+i);
    	    		   if(UtilMethods.isSet(button)){
    	    			   isButtonSet =true;
    	    		   %>
    	    		   <%="theme_advanced_buttons"+i+"_add_before : \""+button+"\","%>
    	    		   <%
    	    		   }
    			   }
    		   } else {
    			   %>
  	    		 <%="theme_advanced_buttons1 : \"bold,italic,underline,|,justifyleft,justifycenter,justifyfull,justifyright,|,formatselect\","%>
  	    		 <%
    		   }   		  
    		%>
    		spellchecker_languages : "+English=en_US,Español=es_ES",
    		spellchecker_rpc_url : "/servlets/jmyspell-spellchecker",
    		<% /*Set the WYSIWYG BLOCK FORMATS*/
    		   String blockformats =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_BLOCKFORMATS");
    		   if(UtilMethods.isSet(blockformats)){
    		   %>
    		   <%="theme_advanced_blockformats : \""+blockformats+"\","%>
    		   <%
    		   }else{
    		   %>
    		   <%="theme_advanced_blockformats : \"p,div,h1,h2,h3,h4,h5,h6,blockquote,dt,dd,code,samp,pre\","%>
    		   <%
    		   }
    		%>
    		theme_advanced_disable : "styleselect",
            theme_advanced_toolbar_location : "top",
            theme_advanced_toolbar_align : "left",
            theme_advanced_statusbar_location : "bottom",
            <% /*Set the WYSIWYG DATE FORMATS*/
    		   String dateformat =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_DATEFORMAT");
    		   if(UtilMethods.isSet(dateformat)){
    		   %>
    		   <%="plugin_insertdate_dateFormat : \""+dateformat+"\","%>
    		   <%
    		   }else{
    		   %>
    		   <%="plugin_insertdate_dateFormat : \"%Y-%m-%d\","%>
    		   <%
    		   }
    		%>
            <% /*Set the WYSIWYG TIME FORMATS*/
    		   String timeformats =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_TIMEFORMAT");
    		   if(UtilMethods.isSet(timeformats)){
    		   %>
    		   <%="plugin_insertdate_timeFormat : \""+timeformats+"\","%>
    		   <%
    		   }else{
    		   %>
    		   <%="plugin_insertdate_timeFormat : \"%H:%M:%S\","%>
    		   <%
    		   }
    		%>
            <% /*Set the WYSIWYG paste_use_dialog*/
    		   String pasteusedialog =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_PASTEUSEDIALOG");
    		   if(UtilMethods.isSet(pasteusedialog)){
    		   %>
    		   <%="paste_use_dialog : "+pasteusedialog+","%>
    		   <%
    		   }else{
    		   %>
    		   <%="paste_use_dialog : true,"%>
    		   <%
    		   }
    		%>
            gecko_spellcheck : true,
            theme_advanced_resizing : true,
            theme_advanced_resize_horizontal : true,
            theme_advanced_link_targets : "",
            paste_auto_cleanup_on_paste : true,
            paste_convert_headers_to_strong : true,
            paste_strip_class_attributes : "all",
            convert_urls : true,
            cleanup : true,
            urlconverter_callback : "cmsURLConverter",
            verify_css_classes : false,
            <%
            //Get the default CSS file if doesn't exist load the /html/css/base.css
            String useHostValues = APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_CSS_USE_HOST_VALUES");
	        Host host = WebAPILocator.getHostWebAPI().getCurrentHost(request);
	        String cssPath = "";
    		if(useHostValues.equals("true")){
    			// find the css file for the current host
    			int numberOfConfiguredHosts = 0;
    			try{
    				numberOfConfiguredHosts = Integer.parseInt(APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_CSS_NUM_OF_HOSTS"));
    			}catch(Exception ex){
    				
    			}
    			System.out.println("here is the current host : " + host.getHostname());
    			for(int i = 1; i <= numberOfConfiguredHosts; i++){
    				String[] hostAndCssPath = APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_CSS_HOST"+i).split(",");
    				if(hostAndCssPath[0].equals(host.getHostname())){
    					cssPath = hostAndCssPath[1];
    					System.out.println("we found a matched host : " + cssPath);
    					break;
    				}
    			}
    		}else{
    			cssPath = APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_CSS");
    		}
	        // below we use ?new Date() so that we can fool IE to not cache the .css
			if(!cssPath.isEmpty()){%>	        
	        <%="content_css : \"" + cssPath+"?"+new java.util.Date().getTime() + "\","%>	
	        <%}else{%>
	        <%="content_css : \"/home/css/base.css?"+new java.util.Date().getTime() + "\","%>
	        <%}
	        %>
	        <%
	        String bodyClass = APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_CSS_BODY_CLASS");
	        if(bodyClass != null && !bodyClass.isEmpty()){%>
	        	<%="body_class : \""+ bodyClass + "\","%>
	        <%}%>     
            trim_span_elements : false,
            apply_source_formatting : true,
            valid_elements : "*[*]",
			file_browser_callback : "cmsFileBrowser",
			entity_encoding : "named",
			entities: "160,nbsp,161,iexcl,162,cent,163,pound,164,curren,165,yen,166,brvbar,167,sect,168,uml,169,copy,170,ordf,171,laquo,172,not,173,shy,174,reg,175,macr,176,deg,177,plusmn,178,sup2,179,sup3,180,acute,181,micro,182,para,183,middot,184,cedil,185,sup1,186,ordm,187,raquo,188,frac14,189,frac12,190,frac34,191,iquest,192,Agrave,193,Aacute,194,Acirc,195,Atilde,196,Auml,197,Aring,198,AElig,199,Ccedil,200,Egrave,201,Eacute,202,Ecirc,203,Euml,204,Igrave,205,Iacute,206,Icirc,207,Iuml,208,ETH,209,Ntilde,210,Ograve,211,Oacute,212,Ocirc,213,Otilde,214,Ouml,215,times,216,Oslash,217,Ugrave,218,Uacute,219,Ucirc,220,Uuml,221,Yacute,222,THORN,223,szlig,224,agrave,225,aacute,226,acirc,227,atilde,228,auml,229,aring,230,aelig,231,ccedil,232,egrave,233,eacute,234,ecirc,235,euml,236,igrave,237,iacute,238,icirc,239,iuml,240,eth,241,ntilde,242,ograve,243,oacute,244,ocirc,245,otilde,246,ouml,247,divide,248,oslash,249,ugrave,250,uacute,251,ucirc,252,uuml,253,yacute,254,thorn,255,yuml,402,fnof,913,Alpha,914,Beta,915,Gamma,916,Delta,917,Epsilon,918,Zeta,919,Eta,920,Theta,921,Iota,922,Kappa,923,Lambda,924,Mu,925,Nu,926,Xi,927,Omicron,928,Pi,929,Rho,931,Sigma,932,Tau,933,Upsilon,934,Phi,935,Chi,936,Psi,937,Omega,945,alpha,946,beta,947,gamma,948,delta,949,epsilon,950,zeta,951,eta,952,theta,953,iota,954,kappa,955,lambda,956,mu,957,nu,958,xi,959,omicron,960,pi,961,rho,962,sigmaf,963,sigma,964,tau,965,upsilon,966,phi,967,chi,968,psi,969,omega,977,thetasym,978,upsih,982,piv,8226,bull,8230,hellip,8242,prime,8243,Prime,8254,oline,8260,frasl,8472,weierp,8465,image,8476,real,8482,trade,8501,alefsym,8592,larr,8593,uarr,8594,rarr,8595,darr,8596,harr,8629,crarr,8656,lArr,8657,uArr,8658,rArr,8659,dArr,8660,hArr,8704,forall,8706,part,8707,exist,8709,empty,8711,nabla,8712,isin,8713,notin,8715,ni,8719,prod,8721,sum,8722,minus,8727,lowast,8730,radic,8733,prop,8734,infin,8736,ang,8743,and,8744,or,8745,cap,8746,cup,8747,int,8756,there4,8764,sim,8773,cong,8776,asymp,8800,ne,8801,equiv,8804,le,8805,ge,8834,sub,8835,sup,8836,nsub,8838,sube,8839,supe,8853,oplus,8855,otimes,8869,perp,8901,sdot,8968,lceil,8969,rceil,8970,lfloor,8971,rfloor,9001,lang,9002,rang,9674,loz,9824,spades,9827,clubs,9829,hearts,9830,diams,34,quot,38,amp,60,lt,62,gt,338,OElig,339,oelig,352,Scaron,353,scaron,376,Yuml,710,circ,732,tilde,8194,ensp,8195,emsp,8201,thinsp,8204,zwnj,8205,zwj,8206,lrm,8207,rlm,8211,ndash,8212,mdash,8216,lsquo,8217,rsquo,8218,sbquo,8220,ldquo,8221,rdquo,8222,bdquo,8224,dagger,8225,Dagger,8240,permil,8249,lsaquo,8250,rsaquo,8364,euro",
			relative_urls : true,
			document_base_url : "/",
			<% /*Set the WYSIWYG TIME FORMATS*/
    		   String width =APILocator.getPluginAPI().loadProperty("mlh.dotcms.tinymce", "WYSIWYG_WIDTH");
    		   if(UtilMethods.isSet(width)){
    		   %>
    		   <%="width : "+width%>
    		   <%
    		   }else{
    		   %>
    		   <%="width : 600"%>
    		   <%
    		   }
    		%>
		};		