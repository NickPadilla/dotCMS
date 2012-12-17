var elToAdd;

var include_script = function(file, callOnload, nextFile, fileType, loadCallback) {
	if (fileType == "css" || fileType == "js") {
		
		var html_doc = document.getElementsByTagName('head')[0];
		
		if (fileType == "js") {
			elToAdd = document.createElement('script');
			elToAdd.setAttribute('type', 'text/javascript');
			elToAdd.setAttribute('src', file);
		} else if (fileType == "css") {
			elToAdd = document.createElement('link');
			elToAdd.setAttribute("rel", "stylesheet");
			elToAdd.setAttribute("type", "text/css");
			elToAdd.setAttribute("href", file);
		}
		
		html_doc.appendChild(elToAdd);
		elToAdd.onreadystatechange = function () {
			if (elToAdd.readyState == 'complete') {
		        
			}
	    };
	    elToAdd.onload = function () {
	    	if (nextFile) {
	    		include_script(nextFile, true, null, fileType, loadCallback);
    		}
    		if (callOnload && loadCallback) {
				loadCallback();
    		}
	    };
	}
    return false;
};