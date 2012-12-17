/*****************************************************************************
 * callbacks in FileBrowserDialog.js and HostFolderTreeReadStoreModel.js
 * check for and call this function
 *****************************************************************************/

var dialogCreated = false;
// IMPORTANT: make sure "/ajax/content-list-populator.dot" 
// exists on each site, otherwise, this tool wil NOT work
var contentPopulatorUrl = "/ajax/content-list-populator.dot";
// IMPORTANT: adding any new key/value pairs here must also be added to contentListPopulator.vtl in resources.org
var contentTypesMap = {
		'NewsArticle' : 'Article',
		'Physician' : 'Physician',
		'Eventmlh' : 'Event',
		'LeadershipBio' : 'Leadership Bio',
		'Facility' : 'Facility'
};

var previousClassNames = '';


var mlhAfterFolderCallback = function() {

	//make dojo play nice
	(function($) {
		
		
		var contentLinkClick = function(e) {
			e.preventDefault();
			$(".mceWrapper:visible iframe").contents().find("#href").val($(this).attr('href'));
			$.browser.chrome = /chrome/.test(navigator.userAgent.toLowerCase());
			if ($.browser.chrome) {
				$('.mlh-close-click:visible').before('<span class="chrome-specific-message" style="float:right; margin-right: 30px; color: green">Success!! ... alas, you are in a Chrome browser, so you must manually click close -></span>');
			}
			$('.mlh-close-click:visible').click();
			return false;
		}
		
		var resetAll = function() {
			//TODO: remove all classes and click triggers so no mixups here
			//$('.mlh-close-click').removeClass('mlh-close-click');
			//$('.mlh-folder-click').removeClass('mlh-folder-click');
			
			if ($('#selected-folder-inode-value').length < 1) {
				$('body').append('<input type="hidden" value="" id="selected-folder-inode-value"/>')
			}
			if ($('#current-selected-host-name').length < 1) {
				$('body').append('<input type="hidden" value="" id="current-selected-host-name"/>')
			}
			if ($('#full-folder-path-for-content').length < 1) {
				$('body').append('<input type="hidden" value="" id="full-folder-path-for-content"/>')
			}
			if ($("#pages-and-files-message").length < 1) {
				var pagesAndFilesMessage = $("<div/>", {
					"css" : {
						"position" : "absolute",
						"top" : "-6px",
						"left" : "60px",
						"height" : "12px",
						"width" : "100px",
						"font-size" : "9px",
						"background-color" : "transparent",
						"color" : "#8E8E8E"
					},
					"id" : "pages-and-files-message",
					"text" : "pages and files"
				});
				
				$('div.filterBox:visible input[type="text"]').after(pagesAndFilesMessage);
			}
			
			$('.chrome-specific-message').remove();
		};
		
		if ($('div.clearlooks2 iframe').length > 0) {
			/*******************************************************************************
			* BEGIN helper and handler functions
			********************************************************************************/
				
				/***********************************************************************
				* Builds a folderInfo object with info about the folder clicked and the root/host of the folder.
				************************************************************************/
				var getSelectedPathInfo = function(folderNodeClicked) {
					
					var digitTreeNodeId = folderNodeClicked.closest('.dijitTreeNode').attr("id");
					var inodeStartIndex = digitTreeNodeId.indexOf("treeNode-") + 9;
					var selectedFolderInode = "";
					if (digitTreeNodeId != null && inodeStartIndex > -1) {
						selectedFolderInode = digitTreeNodeId.substring(inodeStartIndex);
					}
					
					var folderInfo = {
						identifier: selectedFolderInode
					};
					
					var fullPathToSelected = "";
					var maxParents = 1000;
					var parentCount = 0;
					var pathArray = new Array();
					//load current folder name first
					pathArray.push($.trim(folderNodeClicked.find('span.dijitTreeLabel').text()));
					
					
					var parentTreeNode = folderNodeClicked.closest('.dijitTreeNode');
					var nextParent = folderNodeClicked.closest('.dijitTreeContainer').siblings('.dijitTreeRow').find('.dijitTreeContent');
					var nextParentLabel = nextParent.find('span.dijitTreeLabel');
					var atParentFolder = parentTreeNode.hasClass('dijitTreeIsRoot');
					
					if (atParentFolder) {
						folderInfo.hostName = $.trim(parentTreeNode.children('.dijitTreeRow').find('span.dijitTreeLabel').text());
						var rootId = parentTreeNode.attr('id');
						var rootIdStartIndex = rootId.indexOf("treeNode-") + 9;
						folderInfo.hostId = rootId.substring(rootIdStartIndex);
					}
					
					while(nextParentLabel.length > 0 && !atParentFolder && parentCount < maxParents) {
						
						pathArray.push($.trim(nextParentLabel.text()));
						parentCount++;
						
						//setup up nextParent to read
						parentTreeNode = nextParent.closest('.dijitTreeNode');
						nextParent = nextParent.closest('.dijitTreeContainer').siblings('.dijitTreeRow').find('.dijitTreeContent');
						nextParentLabel = nextParent.find('span.dijitTreeLabel');
						atParentFolder = parentTreeNode.hasClass('dijitTreeIsRoot');
						if (atParentFolder) {
							folderInfo.hostName = $.trim(parentTreeNode.children('.dijitTreeRow').find('span.dijitTreeLabel').text());
							var rootId = parentTreeNode.attr('id');
							var rootIdStartIndex = rootId.indexOf("treeNode-") + 9;
							folderInfo.hostId = rootId.substring(rootIdStartIndex);
						}
					}
					pathArray.reverse();
					fullPathToSelected = "http://" + pathArray.join("/") + "/";
					folderInfo.path = fullPathToSelected;
					
					return folderInfo;
				};
				
				var createContentTypeSelector = function() {
					
					
					//contentTypeWrapper.append(numResultsReturned)
					//$('body').append(contentTypeWrapper);
				};				
				
				/*************************************
				* Folder click actions
				**************************************/
				var mlhFolderClickHandler = function() {
					var folderClicked = $(this);
					var tablesWrapper = $('div.filterBox:visible').siblings("div").find(".listingTable").parent();
					
					//unmark last selected folder and mark this current folder as selected
					$('.mlh-current-selected-folder').removeClass('mlh-current-selected-folder');
					folderClicked.addClass('mlh-current-selected-folder');
					
					var contentLoadingImage = $("<img/>", {
						'class' : 'dijitTreeExpando mlh-content-loading dijitTreeExpandoLoading',
						'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
						'css' : {
							'display' : 'inline-block'
						},
					});
					if (folderClicked.find('.mlh-content-loading').length < 1) {
						//folderClicked.append(contentLoadingImage);
						previousClassNames = $('.mlh-current-selected-folder img').attr('class');
						$('.mlh-current-selected-folder img').removeClass('dijitFolderClosed dijitFolderOpened dijitHostClosed dijitHostOpened');
						$('.mlh-current-selected-folder img').addClass('dijitTreeExpandoLoading');
					}
					
					
					$('.content-urls li').remove();
					if (tablesWrapper.find('.content-list-wrapper').length < 1) {
						/***********************************************************************************************************************************************************
						* BEGIN content results section creation
						************************************************************************************************************************************************************/
							//clear out any existing search results before adding a new set/list
							//$('.additional-search-results, .mlh-content-open-closed').remove();
							//$('.mlh-pages-and-files').removeClass('mlh-pages-and-files');
						
							
							var listWrapper = $('<div/>', {
								'css' : {
									'border-top' : '1px solid #A7A7A7',
									'display' : 'inline-block',
									'width' : '725px',
									'height' : '380px',
									'overflow' : 'auto',
									'margin-top' : '10px'
								},
								'class' : 'additional-search-results content-list-wrapper'
							});
							var listWrapperHeader = $('<h3/>', {
								'text' : 'Content',
								'class' : 'additional-search-results',
								'css' : {
									'width' : '80px',
									'float' : 'left'
								}
							});
							var contentWildcard = $('<input/>',{
								'id' : 'content-wildcard',
								'css' : {
									'font-size' : '12px',
									'padding' : '0',
									'margin' : '3px 0 0 0',
									'width' : '540px',
									'height' : '16px'
								},
								'maxlength' : '50',
								'keyup': function(e){
									e.stopPropagation();
									var key = e.keyCode || e.which
									if (key == 13) {
										var contentLoadingImage = $("<img/>", {
											'class' : 'dijitTreeExpando mlh-content-loading dijitTreeExpandoLoading',
											'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
											'css' : {
												'display' : 'inline-block'
											},
										});
										if ($('.mlh-current-selected-folder .mlh-content-loading').length < 1) {
											//$('.mlh-current-selected-folder').append(contentLoadingImage);
											previousClassNames = $('.mlh-current-selected-folder img').attr('class');
											$('.mlh-current-selected-folder img').removeClass('dijitFolderClosed dijitFolderOpened dijitHostClosed dijitHostOpened');
											$('.mlh-current-selected-folder img').addClass('dijitTreeExpandoLoading');
											//dijitFolderClosed, dijitFolderOpened, dijitHostClosed, dijitHostOpened, 
										}
										getAndLoadContentResults();
									}
								},
								'keydown': function(e) {
									e.stopPropagation();
								},
								'keypress': function(e) {
									e.stopPropagation();
								}
							});
							var contentResultsReturned = $('<span/>', {
								'class' : 'additional-search-results content-results-returned',
								'css' : {
									'font-size' : '10px',
									'width' : '575',
								}
							});
							var tablelistHeader = $('<h3/>', {
								'text' : 'Pages and Files',
								'class' : 'additional-search-results'
							});
							var pageFileListOpened = $("<img/>", {
								'class' : 'dijitTreeExpando dijitTreeExpandoOpened mlh-content-open-closed',
								'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
								'css' : {
									'float' : 'left'
								},
								'click' : function() {
									if ($(this).parent().find('.listingTable.mlh-pages-and-files').length < 1) {
										if ($(this).parent().find('.listingTable:visible').length > 0) {
											$(this).parent().find('.listingTable:visible').addClass('mlh-pages-and-files');
										} else {
											$(this).parent().siblings('div[dojoattachpoint="noResults"]').remove();
											if ($('.mlh-no-results').length < 1) {
												var noResults = $("<div/>", {
													'css' : {
														'text-align' : 'center'
													},
													'class' : 'mlh-pages-and-files additional-search-results mlh-no-results',
													'text' : 'No Results'
												});
												$(this).parent().find('.listingTable:first').before(noResults);
											}
										}
									} else {
										//then remove any noResults from previous searches
										$('.mlh-no-results').remove();
									}
									
									var openClosedClassAfter = $(this).hasClass('dijitTreeExpandoOpened') ? 'dijitTreeExpandoClosed' : 'dijitTreeExpandoOpened';
									var openClosedClassCurrent = $(this).hasClass('dijitTreeExpandoOpened') ? 'dijitTreeExpandoOpened' : 'dijitTreeExpandoClosed';
									$(this).removeClass(openClosedClassCurrent).addClass('dijitTreeExpandoLoading');
									var imageClicked = $(this);
									
									if ($(this).parent().find('.mlh-pages-and-files').length > 0) {
										$(this).parent().find('.mlh-pages-and-files').slideToggle('fast', function() {
											imageClicked.removeClass('dijitTreeExpandoLoading').addClass(openClosedClassAfter);
										});
									} else {
										imageClicked.removeClass('dijitTreeExpandoLoading').addClass(openClosedClassAfter);
									}
								}
							});
							var contentListClosed = $("<img/>", {
								'class' : 'dijitTreeExpando dijitTreeExpandoClosed mlh-content-open-closed',
								'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
								'css' : {
									'float' : 'left'
								},
								'click' : function() {
									var openClosedClassAfter = $(this).hasClass('dijitTreeExpandoOpened') ? 'dijitTreeExpandoClosed' : 'dijitTreeExpandoOpened';
									var openClosedClassCurrent = $(this).hasClass('dijitTreeExpandoOpened') ? 'dijitTreeExpandoOpened' : 'dijitTreeExpandoClosed';
									$(this).removeClass(openClosedClassCurrent).addClass('dijitTreeExpandoLoading');
									var imageClicked = $(this);
									$('.content-urls').slideToggle('fast', function(){
										imageClicked.removeClass('dijitTreeExpandoLoading').addClass(openClosedClassAfter);
									});
								}
							});
							var contentTypeToPull = $('<select/>', {
								'id': 'content-type-to-pull',
								'css': {
									'font-size' : '10px',
									'font-weight' : 'bold',
									'margin' : '0px 10px 0px 0px'
								},
								'change': function() {
									//$('#content-wildcard').val('');
									var contentLoadingImage = $("<img/>", {
										'class' : 'dijitTreeExpando mlh-content-loading dijitTreeExpandoLoading',
										'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
										'css' : {
											'display' : 'inline-block'
										},
									});
									if ($('.mlh-current-selected-folder .mlh-content-loading').length < 1) {
										//$('.mlh-current-selected-folder').append(contentLoadingImage);
										previousClassNames = $('.mlh-current-selected-folder img').attr('class');
										$('.mlh-current-selected-folder img').removeClass('dijitFolderClosed dijitFolderOpened dijitHostClosed dijitHostOpened');
										$('.mlh-current-selected-folder img').addClass('dijitTreeExpandoLoading');
									}
									getAndLoadContentResults();
								}
							});
							var contentControlsWrapper = $('<div/>', {
								'css' : {
									'clear' : 'both',
									'margin' : '4px 0px 0px 25px',
									'width' : '700px'
								},
								'class' : 'content-control-wrapper'
							});
							
							//use "contentTypesMap" to build the select options
							$.each(contentTypesMap , function(key, value){
							    contentTypeToPull.append($('<option value="' + key + '">' + value + '</option>'));
							});
							
							contentControlsWrapper.append(contentTypeToPull).append(contentResultsReturned);
							
						/***********************************************************************************************************************************************************
						* END content results section creation
						************************************************************************************************************************************************************/
						//Add content results section
						tablesWrapper.append(listWrapper.append(contentListClosed).append(listWrapperHeader).append("Filter: ").append(contentWildcard).append(contentControlsWrapper).append("<br/>")).prepend(tablelistHeader).prepend(pageFileListOpened);
					}
					getAndLoadContentResults();
				};
				
				var getAndLoadContentResults = function() {
					
					var contentLoadingImage = $("<img/>", {
						'class' : 'dijitTreeExpando mlh-content-loading dijitTreeExpandoLoading',
						'src' : '/html/js/dojo/release/dojo/dojo/resources/blank.gif',
						'css' : {
							'display' : 'inline-block'
						},
					});
					//clear results returned info/message and show loading icon
					$('.content-results-returned').html('').append(contentLoadingImage);
					
					var tablesWrapper = $('div.filterBox:visible').siblings("div").find(".listingTable").parent();
					if ($('.content-urls').length < 1) {
						var ul = $('<ul/>', {
							'class' : 'content-urls',
							'css' : {
								'background-color' : '#EFEFEF',
								'display' : 'none',
								'clear' : 'both',
								'height' : '320px',
								'margin-left' : '25px',
								'width' : '670px',
								'overflow' : 'auto'
							}
						});
						$('.content-list-wrapper').append(ul);
					} else {
						//clear out any existing search results
						$('.content-urls li').remove();
					}
					$('.chrome-specific-message').remove();
					
					var folderLastClicked = $('.mlh-current-selected-folder');
					var folderInfo = getSelectedPathInfo(folderLastClicked);
					$('#selected-folder-inode-value').val(folderInfo.identifier);
					$('#current-selected-host-name').val(folderInfo.hostName);
					$('#full-folder-path-for-content').val(folderInfo.path);
					
					var params = {
						folderInode : $('#selected-folder-inode-value').val(),
						contentType : $('#content-type-to-pull option:selected').val(),
						contentWildcard: $('#content-wildcard').val()
					};
					$.get(contentPopulatorUrl, params, function(data) {
						var urlCount = 0;
						try {
							var jsonData;
							if (data.indexOf('contentList') > -1 || data.indexOf('errorSummary') > -1) {
								jsonData = JSON.parse(data);
								if (jsonData.contentList) {
									
									$.each(jsonData.contentList, function(i,val){
										//var hrefUrl = "http://" + $('#current-selected-host-name').val() + val.uri;
										var hrefUrl = val.uri;
										
										var liMain = $('<li/>', {
											'css': {
												'border-bottom': '1px solid #ACBEC6',
												'padding': '8px 0px 8px 8px'
											}
										});
										
										var pagesUl = $('<ul/>', {
											'css': {
												'list-style-type' : 'none'
											}
										});
										
										liMain.append(pagesUl);
										
										if (val.pageList) {
											
											$.each(val.pageList, function(pageCount, page){
												var li = $('<li/>', {
													'css': {
														'border-bottom': '1px solid #E0E0E0',
														'padding': '8px 0px 8px 8px'
													}
												});
												var pageUriVal =  (page.pageUri ? page.pageUri : val.contentTitle);
												var contentLink = $('<a/>', {
													'class': 'content-href',
													'href': pageUriVal,
													'title': val.contentTitle,
													'alt': val.contentTitle,
													'text': pageUriVal,
													'click': contentLinkClick
												});
												var contentPreview = $('<a/>', {
													'class': 'preview',
													'target': '_blank',
													'href': pageUriVal,
													'css': {
														'font-size': '10px',
														'margin-left': '30px'
													},
													'text': 'preview'
												});
												var contentUriInput = $('<input/>', {
													'class': 'content-uri',
													'type': 'hidden',
													'value': pageUriVal
												});
												var contentTitleInput = $('<input/>', {
													'class': 'content-title',
													'type': 'hidden',
													'value': val.contentTitle
												});
												li.append(contentLink).append(contentPreview).append(contentUriInput).append(contentTitleInput);
												pagesUl.append(li);
											});
											
										} 
										
										var li = $('<li/>', {
											'css': {
												'border-bottom': '1px solid #E0E0E0',
												'padding': '8px 0px 8px 8px'
											}
										});
										
										var contentLink = $('<a/>', {
											'class': 'content-href',
											'href': hrefUrl,
											'title': val.contentTitle,
											'alt': val.contentTitle,
											'text': (val.contentTitle ? val.contentTitle : hrefUrl),
											'click': contentLinkClick
										});
										var contentPreview = $('<a/>', {
											'class': 'preview',
											'target': '_blank',
											'href': hrefUrl,
											'css': {
												'font-size': '10px',
												'margin-left': '30px'
											},
											'text': 'preview'
										});
										var contentUriInput = $('<input/>', {
											'class': 'content-uri',
											'type': 'hidden',
											'value': hrefUrl
										});
										var contentTitleInput = $('<input/>', {
											'class': 'content-title',
											'type': 'hidden',
											'value': val.contentTitle
										});
										li.append(contentLink).append(contentPreview).append(contentUriInput).append(contentTitleInput)
										pagesUl.append(li);
									
										$('.content-urls').append(liMain);
										urlCount++;
									});
									
									//$('.content-list-wrapper').append(ul);
									$('.content-list-wrapper .mlh-content-open-closed.dijitTreeExpandoClosed').click();
								} else {
									var errorDetail= $("<div/>", {
										'class': 'error-detail',
										'css': {
											'font-size': '10px',
											'margin-top': '30px'
										},
										'text': ((jsonData.error) ? jsonData.error : "No contentDetails or error found in response")
									});								
									var errorSummary= $("<div/>", {
										'class': 'error-summary',
										'css': {
											'font-size': '14px',
											'font-weight': 'bold',
											'margin-top': '30px'
										},
										'text': ((jsonData.error) ? jsonData.errorSummary : "Fatal Error")
									});
									var li = $('<li/>', {
										'css': {
											'border-bottom': '1px solid #ACBEC6',
											'padding': '8px 0px 8px 8px'
										}
									});
									$('.content-urls').append(li.append(errorSummary).append(errorDetail))
									//$('.content-list-wrapper').append();
									$('.content-list-wrapper .mlh-content-open-closed.dijitTreeExpandoClosed').click();
								}
							} else {
								var li = $('<li/>', {
									'css': {
										'border-bottom': '1px solid #ACBEC6',
										'padding': '8px 0px 8px 8px'
									}
								});
								var errorDetail = "<div class='error-detail' style='font-size: 10px; margin-top: 30px;'>Bad response data from " + contentPopulatorUrl + ": <br/><br/></div>";
								var errorSummary = "<div class='error-summary' style='font-size: 14px; font-weight: bold'>Fatal Error</div>";
								//$('.content-list-wrapper').append($('.content-urls').append(li.append(errorSummary).append(errorDetail)));
								$('.content-urls').append(li.append(errorSummary).append(errorDetail))
								$('.content-list-wrapper .mlh-content-open-closed.dijitTreeExpandoClosed').click();
							}
							$('.content-results-returned').html('').append("<b>" + urlCount + "</b> ").append($("#content-type-to-pull option:selected").text() + "s Found: " + " in " + $('#full-folder-path-for-content').val());
						} catch(excep) {
							var li = $('<li/>', {
								'css': {
									'border-bottom': '1px solid #ACBEC6',
									'padding': '8px 0px 8px 8px'
								}
							});
							var errorDetail = "<div class='error-detail' style='font-size: 10px; margin-top: 30px;'>Bad response data: <br/><br/><b>" + data + "</b><br/><br/>" + excep + "</div>";
							var errorSummary = "<div class='error-summary' style='font-size: 14px; font-weight: bold'>Fatal Error</div>";
							//$('.content-list-wrapper').append($('.content-urls').append(li.append(errorSummary).append(errorDetail)));
							$('.content-urls').append(li.append(errorSummary).append(errorDetail))
							$('.content-list-wrapper .mlh-content-open-closed.dijitTreeExpandoClosed').click();
							$('.content-results-returned').text('Exception caught');
						}
						
						tablesWrapper.show('fast', function(){
							if (tablesWrapper.find('.listingTable:visible').length < 1 && tablesWrapper.find('.mlh-pages-and-files').length < 1) {
								tablesWrapper.siblings('div[dojoattachpoint="noResults"]').remove();
								var noResults = $("<div/>", {
									'css' : {
										'text-align' : 'center'
									},
									'class' : 'mlh-pages-and-files additional-search-results mlh-no-results',
									'text' : 'No Results'
								});
								tablesWrapper.find('.listingTable:first').before(noResults);
							} else {
								$('.mlh-no-results').remove();
							}
						});
						
						$('.mlh-content-loading').remove();
						$('.mlh-current-selected-folder img').attr('class', previousClassNames);
						//$('.mlh-current-selected-folder img').removeClass('dijitTreeExpandoLoading');
					});
				}
				
			/*******************************************************************************
			* END helper and handler functions
			********************************************************************************/
				
				
			/*******************************************************************************
			* BEGIN main actions
			********************************************************************************/
			if ($('.dijitTreeRow:visible').length > 0) {
				
				resetAll();
				
				createContentTypeSelector();
				
				// mark the close icon so that we can call it later with the 'mlh-close-click' class name
				$('.dijitDialogCloseIcon:not(.mlh-close-click)').addClass('mlh-close-click');
				
				/**************************************
				* Folder click action
				***************************************/
				$('.dijitTreeRow .dijitTreeContent:not(.mlh-folder-click)').addClass('mlh-folder-click').bind('click.mlhFolderClick', mlhFolderClickHandler);
			}
			
		} else {
			resetAll();
		}
	})(mlhjq);
};

