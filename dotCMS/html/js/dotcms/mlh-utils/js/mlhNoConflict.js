mlhjq = jQuery.noConflict();

var mlhProxied = window.alert;
window.alert = function() {
	// do something here
	if (arguments != null && arguments.length == 1 && arguments[0] == 'node is null') {

		if (mlhjq('#mlh-overridden-errors').length < 1) {
			var hiddenErrorMessages = mlhjq('<div/>',{
				'id' : 'mlh-overridden-errors',
				'css' : {
					'height' : '20px',
					'width' : '156px',
					'background-color' : '#2C548D',
					'color' : '#FEF411',
					'padding' : '1px',
					'position' : 'fixed',
					'bottom' : '5px',
					'right' : '5px'
			       },
			       'click' : function(){
					var mlhOverriddenErrors = mlhjq(this).find('input').val().split("|");
					//mlhjq('#mlh-hidden-javascript-errors').html(mlhOverriddenErrors.length);
				        mlhjq('#mlh-hidden-javascript-errors').html('');
				       var mlhErrors = '';
					mlhjq.each(mlhOverriddenErrors, function(index, value) {
						if (index > 0)  {
							mlhErrors += '<br/>';
						}
						mlhErrors += value;
					});
					mlhjq('#mlh-hidden-javascript-errors').html(mlhErrors).slideToggle();
			       },
			      'text' : 'Show JavaScript Errors'
			});
			var hiddenErrorMessageInput = mlhjq('<input/>', {
				'type' : 'hidden'
			});
			var mlhHiddenMessages = mlhjq('<div/>', {
				'id' : 'mlh-hidden-javascript-errors',
				'css': {
					'height' : '300px',
					'width' : '300px',
					'background-color' : '#2C548D',
					'color' : '#FEF411',
					'position' : 'fixed',
					'bottom' : '30px',
					'right' : '5px',
					'display' : 'none',
					'padding-left' : '5px',
					'padding-top' : '5px'
				}
			});
			mlhjq('body').append(hiddenErrorMessages.append(hiddenErrorMessageInput)).append(mlhHiddenMessages);
		}
		if (mlhjq('#mlh-overridden-errors input').val() == '') {
			mlhjq('#mlh-overridden-errors input').val(arguments[0])
		} else {
			mlhjq('#mlh-overridden-errors input').val(mlhjq('#mlh-overridden-errors input').val() + '|' + arguments[0]);
		}
		return true;
	} else {
		return mlhProxied.apply(this, arguments);
	}
};
/**
* hoverIntent r6 // 2011.02.26 // jQuery 1.5.1+
* <http://cherne.net/brian/resources/jquery.hoverIntent.html>
* 
* @param  f  onMouseOver function || An object with configuration options
* @param  g  onMouseOut function  || Nothing (use configuration options object)
* @author    Brian Cherne brian(at)cherne(dot)net
*/
(function($){$.fn.hoverIntent=function(f,g){var cfg={sensitivity:7,interval:100,timeout:0};cfg=$.extend(cfg,g?{over:f,out:g}:f);var cX,cY,pX,pY;var track=function(ev){cX=ev.pageX;cY=ev.pageY};var compare=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);if((Math.abs(pX-cX)+Math.abs(pY-cY))<cfg.sensitivity){$(ob).unbind("mousemove",track);ob.hoverIntent_s=1;return cfg.over.apply(ob,[ev])}else{pX=cX;pY=cY;ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}};var delay=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);ob.hoverIntent_s=0;return cfg.out.apply(ob,[ev])};var handleHover=function(e){var ev=jQuery.extend({},e);var ob=this;if(ob.hoverIntent_t){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t)}if(e.type=="mouseenter"){pX=ev.pageX;pY=ev.pageY;$(ob).bind("mousemove",track);if(ob.hoverIntent_s!=1){ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}}else{$(ob).unbind("mousemove",track);if(ob.hoverIntent_s==1){ob.hoverIntent_t=setTimeout(function(){delay(ev,ob)},cfg.timeout)}}};return this.bind('mouseenter',handleHover).bind('mouseleave',handleHover)}})(jQuery);

mlhjq(document).ready(function(){
	
	
	var hostSelectDialogOut = function() {
		if (!mlhjq('#subNavHost_popup').is(":visible")) {
			mlhjq('#hostSelectDialog .cancelIcon').closest('.dijitButtonNode').click();
		} else {
			setTimeout(hostSelectDialogOut, 2000);
		}
	}
	
	var hoverIntentConfig = {    
		over: function(){
			mlhjq('.changeHost .chevronExpandIcon').click();
		},   
		timeout: 500, // number = milliseconds delay before onMouseOut    
		out: function(){
			//mlhjq('#hostSelectDialog .cancelIcon').siblings('span.dijitButtonText').click()
		
			var hostSelectHoverConfig = {
				over: function(){
					//do nothing
				},
				timeout: 1000,
				out: hostSelectDialogOut
			};
			
			mlhjq('#hostSelectDialog').hoverIntent(hostSelectHoverConfig)
			

		}   
	}
	
	var modifyActionButtons = function() {
		
		var mlhShowHideActionButtons = mlhjq('<span/>',{
			'css': {
				'color' : '#FFFFFF',
				'cursor' : 'pointer',
				'display' : 'inline-block',
				'height' : '18px',
				'font-weight' : 'bold',
				'padding' : '3px',
				'position': 'fixed', 
				'bottom': '50px',
				'left': '5px',
				'background-color': '#2C548D',
				'z-index': '1001'
			},
			'id': 'mlh-show-hide-action-buttons',
			'click': function(e) {
				mlhjq('#editContentletButtonRow').slideToggle();
			},
			'text' : 'Show/Hide buttons'
		});
		
		mlhjq('#editContentletButtonRow').after(mlhShowHideActionButtons)
		
		mlhjq('#editContentletButtonRow').css({
			'position': 'fixed', 
			'bottom': '5px',
			'left': '5px',
			'background-color': '#333333',
			'z-index': '1001'
		});
		
		
		//mlhjq('#mlh-show-hide-action-buttons').css({
			
		//});
	};
	
	/************************************************************************************************
	 * Modifies the referer when it does NOT have a '?' in it...since dotCMS
	 * incorrectly always appends a '&' after the referer value in 'edit_contentlet_js_inc.jsp'
	 ************************************************************************************************/
	var mlhRefererFix = function() {
		if (mlhjq('#_EXT_BROWSER_referer').length > 0) {
			var currentReferer = mlhjq('#_EXT_BROWSER_referer').val();
			if (currentReferer.indexOf("?") < 0) {
				mlhjq('#_EXT_BROWSER_referer').val(currentReferer + "?mlhRefererFix=blah");
			}
		}
	};
	
	
	mlhjq('.changeHost').hoverIntent(hoverIntentConfig);
	
	modifyActionButtons();
	
	mlhRefererFix();
	
});