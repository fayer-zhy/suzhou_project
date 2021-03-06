/*
 * hospital.js 1.0
 * Copyright (c) 2014 Zhan http://www.xiaojd.net/
 * Create Date: 2014-10-15
 * Modify Date: 2015-01-26
 * 功能说明：合理用药事前干预接口js版
 */
/*
var WshShell=new ActiveXObject("WScript.Shell"); 
WshShell.RegWrite("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ZoneMap\\Domains\\"+hospital_ip,"");
WshShell.RegWrite("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ZoneMap\\Domains\\"+hospital_ip+"\\http","2","REG_DWORD");
*/
(function($){
	$.extend({
		Engine: {
			IP: '127.0.0.1:8080',
			TIMEOUT: 3000,
			ret: '',
			ErrMsg: '<root><message><infos><saveOrEdit>0</saveOrEdit></infos></message><isSuccess>1</isSuccess></root>',
			SucMsg: '<root><message><infos><saveOrEdit>1</saveOrEdit></infos></message><isSuccess>1</isSuccess></root>',
			TimeoutMsg: '<root><message><infos><saveOrEdit>1</saveOrEdit></infos></message><isSuccess>0</isSuccess></root>',
			callback: function(){ return; },
			DlgHide: false,
			webbox: function(option){
				var _x,_y,m,allscreen=false;
				if(!option){ return; };
				if(!option['html']&&!option['iframe']){ return; };
				option['parent']='webox';
				option['locked']='locked';
				//$(document).ready(function(e){
					$('.webox').remove();
					$('.background').remove();
					var width=option['width']?option['width']:560;
					var height=option['height']?option['height']:280;
					$('body').append('<div class="background" style="display:none;position:fixed;_position:absolute;z-index:998;top:0px;left:0px;width:100%;_width:expression(document.documentElement.clientWidth);height:100%;_height:expression(document.documentElement.clientHeight);background:rgb(50,50,50);background:rgba(0, 0, 0, 0.5);"></div><div class="webox" style="width:'+width+'px;height:'+height+'px;display:none;position:fixed;_position:absolute;z-index:999;padding:3px;border:solid 1px #7DCBF2;border-radius:3px;_background:#7DCBF2;background:rgba(125, 203, 242, 1);"><div id="inside" style="height:'+height+'px;background-color:#FFFFFF;overflow:hidden;height:244px;"><h1 id="locked" onselectstart="return false;" style="font-family:微软雅黑,Arial;-moz-user-select:none;-webkit-user-select:none;position:relative;display:block;margin:0;padding:0;font-size:14px;line-height:26px;height:26px;padding-left:5px;font-family:Arial;background:#7DCBF2;font-weight:normal;cursor:move;font-weight:800;color:#FFF;">'+(option['title']?option['title']:'webox')+'<a class="span" href="javascript:void(0);" style="position:absolute;display:block;right:6px;top:0px;font-size:13px;width:40px;height:17px;cursor:pointer;display:inline-block;">[关闭]</a></h1>'+(option['iframe']?'<iframe class="w_iframe" src="'+option['iframe']+'" frameborder="0" width="100%" scrolling="yes" style="border:none;overflow-x:hidden;height:'+parseInt(height-30)+'px;"></iframe>':option['html']?option['html']:'')+'</div><div style="background:#FFF;padding-right:12px;border-top:1px solid #888;height:34px;text-align:right;"><input id="goonBtn" style="background: -moz-linear-gradient(center top , white 0%, #e0e0e0 90%, #f3f3f3 100%) repeat scroll 0 0 rgba(0, 0, 0, 0);border: 1px solid #888;border-radius: 3px;font-size:12px;font-family:微软雅黑;padding: 1px 0 4px;margin: 4px 0 0 10px;width: 100px;height: 26px;text-align: center;vertical-align: middle;" type="button" value="继续保存(F7)"/><input id="returnBtn" style="background: -moz-linear-gradient(center top , white 0%, #e0e0e0 90%, #f3f3f3 100%) repeat scroll 0 0 rgba(0, 0, 0, 0);border: 1px solid #888;border-radius: 3px;font-size:12px;font-family:微软雅黑;padding: 1px 0 4px;margin: 4px 0 0 10px;width: 100px;height: 26px;text-align: center;vertical-align: middle;" type="button" value="返回修改(F9)"/></div></div>');
					if(navigator.userAgent.indexOf('MSIE 7')>0||navigator.userAgent.indexOf('MSIE 8')>0){
						$('.webox').css({'filter':'progid:DXImageTransform.Microsoft.gradient(startColorstr=#7DCBF2,endColorstr=#7DCBF2)'});
					}
					//if(option['bgvisibel']){
						$('.background').fadeTo('slow',0.3);
					//};
					$('.webox').css({display:'block'});
					$('#'+option['locked']+' .span').click(function(){
						$('.webox').css({display:'none'});
						$('.background').css({display:'none'});
						$.Engine.callback.call(this, $.Engine.ErrMsg, 0);
						return false;
					});
					var marginLeft=parseInt(width/2);
					var marginTop=parseInt(height/2);
					var winWidth=parseInt((document.documentElement.clientWidth || document.body.clientWidth)/2);
					var winHeight=parseInt((document.documentElement.clientHeight || document.body.clientHeight)/2.2);
					var left=winWidth-marginLeft;
					var top=winHeight-marginTop;
					$('.webox').css({left:left,top:top});
					$('#'+option['locked']).mousedown(function(e){
						if(e.which){
							m=true;
							_x=e.pageX-parseInt($('.webox').css('left'));
							_y=e.pageY-parseInt($('.webox').css('top'));
						}
						}).dblclick(function(){
							if(allscreen){
								$('.webox').css({height:height,width:width});
								$('#inside').height(height);
								$('.w_iframe').height(height-30);
								$('.webox').css({left:left,top:top});
								allscreen = false;
							}else{
								allscreen=true;
								var screenHeight = $(window).height();
								var screenWidth = $(window).width();
								$('.webox').css({'width':screenWidth-18,'height':screenHeight-18,'top':'0px','left':'0px'});
								$('#inside').height(screenHeight-20);
								$('.w_iframe').height(screenHeight-50);
							}
						});
					//});
					$(document).mousemove(function(e){
						if(m && !allscreen){
							var x=e.pageX-_x;
							var y=e.pageY-_y;
							$('.webox').css({left:x});
							$('.webox').css({top:y});
						}
					}).mouseup(function(){
						m=false;
					});
					$('#goonBtn,#returnBtn').hover(function(){
						$(this).css({'background':'-moz-linear-gradient(center top , #ebebeb, #f3f3f3) repeat scroll 0 0 rgba(0, 0, 0, 0)','border-color':'#7e7e7e'});
					},function(){
						$(this).css({'background':'-moz-linear-gradient(center top , white 0%, #e0e0e0 90%, #f3f3f3 100%) repeat scroll 0 0 rgba(0, 0, 0, 0)','border-color':'#888'});
					});
					$('#goonBtn').click(function(e){
						$('.webox').css({display:'none'});
						$('.background').css({display:'none'});
						$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>1</saveOrEdit></infos>'), 1);
					});
					$('#returnBtn').click(function(e){
						$('.webox').css({display:'none'});
						$('.background').css({display:'none'});
						$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>0</saveOrEdit></infos>'), 0);
					});
					$(window).resize(function(){
						if(allscreen){
							var screenHeight = $(window).height();
							var screenWidth = $(window).width();
							$('.webox').css({'width':screenWidth-18,'height':screenHeight-18,'top':'0px','left':'0px'});
							$('#inside').height(screenHeight-20);
							$('.w_iframe').height(screenHeight-50);
						}
					});
					$(document).keydown(function(e){
						if(e.keyCode){
							if(e.keyCode == 118){
								if($('.webox').is(':visible')) $('#goonBtn').click();
								return false;
							}else if(e.keyCode == 120){
								if($('.webox').is(':visible')) $('#returnBtn').click();
								return false;
							}
						}
					});
			},
			showErrorMsg: function(msg){
				$.Engine.ret = $.Engine.ErrMsg; 
				this.webbox({title: '错误信息', html: '<div style="padding:4px 8px 0;color:red;">'+msg+'</div>'});
				$('#goonBtn').hide();
				$('#returnBtn').val('关闭(F9)');
			},
			concatMsg: function(xmlString){
				var html='';
				var xDoc;
				try{
					xDoc= $.parseXML(xmlString);
				}catch(e){
					this.showErrorMsg('合理用药系统数据错误：'+e);
				}
				if(xDoc){
					var infos = xDoc.getElementsByTagName('info');
					html+='<div style="height:206px!important;overflow-y:auto;padding:6px;">';
					for(var i=0; i<infos.length; i++){
						var severity = infos[i].getElementsByTagName('severity')[0].firstChild.nodeValue;
						html+='<div style="padding:4px 0 0;font-size:14px;"><font color="';
						var _color='';
						if(severity == '8'){
							_color='RED';
						}else if(severity == '5'){
							_color='#FF359A';
						}else if(severity == '4'){
							_color='BLACK';
						}else{
							_color='GRAY';
						}
						html += _color;
						html+='" face="微软雅黑">['+severity+'],'+((infos[i].getElementsByTagName('drugName')[0].firstChild==null)?'':'<a href="http://'+$.Engine.IP+'/xiaojd/getHisSms?ypmcid='+infos[i].getElementsByTagName('drugCode')[0].firstChild.nodeValue+'" target="_blank" style="text-decoration:underline;color:'+_color+'">'+infos[i].getElementsByTagName('drugName')[0].firstChild.nodeValue+'</a>')+': '+
							((infos[i].getElementsByTagName('message')[0].firstChild==null)?'':infos[i].getElementsByTagName('message')[0].firstChild.nodeValue)+
							((infos[i].getElementsByTagName('advice')[0].firstChild==null)?'':infos[i].getElementsByTagName('advice')[0].firstChild.nodeValue);
						html+='</font></div>';
					}
					return html+'</div>';
				}
			},
			run: function(inputXml, callback, dlgHide){
				$.Engine.callback = callback;
				$.Engine.DlgHide = dlgHide || false;
				try{
					var xmlText = $.parseXML(inputXml);
				}catch(e){
					this.showErrorMsg('调用合理用药接口的XML参数存在格式上错误：'+e);
					return false;
				}
				if(navigator.userAgent.indexOf("MSIE")>0){  
					var xdr;
					if (window.XDomainRequest) { //>=8.0
						xdr = new XDomainRequest();
				        if (xdr) {
				        	xdr.timeout = $.Engine.TIMEOUT;
				        	xdr.onerror = function(e){
				        		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
				        	};
				        	xdr.ontimeout = function(e){
				        		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
				        	};
				        	//xdr.onprogress = progres;
				        	xdr.onload =  function(){
				        		if(xdr.responseText && xdr.responseText.indexOf('<isSuccess>1</isSuccess>') >= 0){
				        			if(xdr.responseText.indexOf('<info>') >= 0){
				        				$.Engine.ret = xdr.responseText;
				        				if($.Engine.DlgHide){
				        					$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>1</saveOrEdit></infos>'), 1);
				        				}else{
				        					$.Engine.webbox({title: '警示信息', html: $.Engine.concatMsg(xdr.responseText)});
				        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0  //是否审历史医嘱，0为不审，1为审
				        							|| xdr.responseText.indexOf('<severity>8</severity>') >= 0){
												$('#goonBtn').hide();
											}else{
												$('#goonBtn').show();
											}
				        					
				        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0){
				        						$('#returnBtn').val('关闭(F9)');
				        					}else{
				        						$('#returnBtn').val('返回修改(F9)');
				        					}
				        				}
				        			}else{
				        				$.Engine.callback.call(this, $.Engine.SucMsg, 1);
				        			}
				        		}else{
				        			return false;
				        		}
			        	    };
				        	xdr.open("POST", 'http://'+$.Engine.IP+'/xiaojd/engine');
				        	xdr.send(inputXml);
				        }
					}else if(window.XMLHttpRequest) { //=7.0
						var xhr=new XMLHttpRequest();
					    if(xhr) {
					    	xhr.timeout = $.Engine.TIMEOUT;
					    	xhr.onerror = function(e){
					    		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
					    	};
					    	xhr.ontimeout = function(e){
					    		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
					    	};
					        xhr.open("POST",'http://'+$.Engine.IP+'/xiaojd/engine',false);
					        xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
					        xhr.send(inputXml);
					        xhr.onreadystatechange = function(){
					        	if(xhr.readyState == 4){
									if(xhr.responseText && xhr.responseText.indexOf('<isSuccess>1</isSuccess>') >= 0){
										if(xhr.responseText.indexOf('<info>') >= 0){
											$.Engine.ret = xhr.responseText;
											if($.Engine.DlgHide){
												$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>1</saveOrEdit></infos>'), 1);
											}else{
												$.Engine.webbox({title: '警示信息', html: $.Engine.concatMsg(xhr.responseText)});
					        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0  //是否审历史医嘱，0为不审，1为审
					        							|| xhr.responseText.indexOf('<severity>8</severity>') >= 0){
													$('#goonBtn').hide();
												}else{
													$('#goonBtn').show();
												}
					        					
					        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0){
					        						$('#returnBtn').val('关闭(F9)');
					        					}else{
					        						$('#returnBtn').val('返回修改(F9)');
					        					}
											}
										}else{
					        				$.Engine.callback.call(this, $.Engine.SucMsg, 1);
					        			}
					        		}else{
					        			return false;
					        		}
					        	}
					        }
					    }
					}else{//IE6
						//$.Engine.showErrorMsg("您使用的IE浏览器版本太低，请先升级浏览器！");
						var xhr=new ActiveXObject("Microsoft.XMLHttp");
					    if(xhr) {
							xhr.open("POST",'http://'+$.Engine.IP+'/xiaojd/engine',true);
					        xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
					        xhr.onreadystatechange = function(){
					        	if(xhr.readyState == 4){
									if(xhr.responseText && xhr.responseText.indexOf('<isSuccess>1</isSuccess>') >= 0){
										if(xhr.responseText.indexOf('<info>') >= 0){
											$.Engine.ret = xhr.responseText;
											if($.Engine.DlgHide){
												$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>1</saveOrEdit></infos>'), 1);
											}else{
												$.Engine.webbox({title: '警示信息', html: $.Engine.concatMsg(xhr.responseText)});
					        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0  //是否审历史医嘱，0为不审，1为审
					        							|| xhr.responseText.indexOf('<severity>8</severity>') >= 0){
													$('#goonBtn').hide();
												}else{
													$('#goonBtn').show();
												}
					        					
					        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0){
					        						$('#returnBtn').val('关闭(F9)');
					        					}else{
					        						$('#returnBtn').val('返回修改(F9)');
					        					}
											}
										}else{
					        				$.Engine.callback.call(this, $.Engine.SucMsg, 1);
					        			}
					        		}else{
					        			return false;
					        		}
					        	}
					        }
					        xhr.send(inputXml);
					    }
					}
				}else{
					var xhr=new XMLHttpRequest();
				    if(xhr) {
				    	xhr.timeout = $.Engine.TIMEOUT;
				    	xhr.onerror = function(e){
				    		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
				    	};
				    	xhr.ontimeout = function(){
				    		$.Engine.callback.call(this, $.Engine.TimeoutMsg, 1);
				    	};
				        xhr.open("POST",'http://'+$.Engine.IP+'/xiaojd/engine',true);
				        xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
				        xhr.send(inputXml);
				        xhr.onreadystatechange = function(){
				        	if(xhr.readyState == 4){
								if(xhr.responseText && xhr.responseText.indexOf('<isSuccess>1</isSuccess>') >= 0){
									if(xhr.responseText.indexOf('<info>') >= 0){
										$.Engine.ret = xhr.responseText;
										if($.Engine.DlgHide){
											$.Engine.callback.call(this, $.Engine.ret.replace('</infos>','<saveOrEdit>1</saveOrEdit></infos>'), 1);
										}else{
											$.Engine.webbox({title: '警示信息', html: $.Engine.concatMsg(xhr.responseText)});
				        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0  //是否审历史医嘱，0为不审，1为审
				        							|| xhr.responseText.indexOf('<severity>8</severity>') >= 0){
												$('#goonBtn').hide();
											}else{
												$('#goonBtn').show();
											}
				        					
				        					if(inputXml.indexOf('<containsHistory>1</containsHistory>') >= 0){
				        						$('#returnBtn').val('关闭(F9)');
				        					}else{
				        						$('#returnBtn').val('返回修改(F9)');
				        					}
										}
									}else{
				        				$.Engine.callback.call(this, $.Engine.SucMsg, 1);
				        			}
				        		}else{
				        			return false;
				        		}
				        	}
				        };
				    }
				}
			},//run end
			getSms: function(ypmcid){
//			  	if($.Engine.smsWindow != null){
//			  		$.Engine.smsWindow.window.close();
//				}
//			  	$.Engine.smsWindow = window.open('','window','height=500,width=860,top=0,left=0,toolbar=no,menubar=no,scrollbars=no,resizable=yes,location=no,status=no');
//			  	$.Engine.smsWindow.document.clear();
//			  	$.Engine.smsWindow.document.write('<iframe src="http://'+$.Engine.IP+'/xiaojd/getHisSms?ypmcid='+ypmcid+'" id="resultInfoFream" name="resultInfoFream" style="overflow:auto;background:#FFFFFA;" width="100%" height="100%"  marginwidth="0" marginheight="0" frameborder="0"></iframe>');
			  	var OpenWindow = window.open('','window','height=500,width=860,top=0,left=0,toolbar=no,titlebar=no,menubar=no,scrollbars=no,resizable=yes,location=no,status=no');
			  	if(OpenWindow){
			  		OpenWindow.document.innerHTML='';
					var el = OpenWindow.document.getElementById('resultInfoFream');
					if(el){
						var  p = el.parentNode;
						p.removeChild(el);
					}
					OpenWindow.document.write('<iframe src="http://'+$.Engine.IP+'/xiaojd/getHisSms?ypmcid='+ypmcid+'" id="resultInfoFream" name="resultInfoFream" style="overflow:auto;background:#FFFFFA;" width="100%" height="100%"  marginwidth="0" marginheight="0" frameborder="0"></iframe>');
					OpenWindow.document.body.leftMargin=0;
					OpenWindow.document.body.topMargin=0;
					OpenWindow.document.focus();
				}
			  	top.window = OpenWindow;
				return false;
			}
		}//engine end
	});
})(jQuery);