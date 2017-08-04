<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="com.xiaojd.entity.hospital.EngPtUser"%>
<%@page  import="com.xiaojd.entity.hospital.EngPtCf"%>
<%@page  import="com.xiaojd.entity.hospital.EngPtDrug"%>
<%@page  import="com.xiaojd.entity.hospital.EngPtDrug"%>
<%@page  import="com.xiaojd.entity.hospital.EngPtDelivery"%>
<%@page  import="com.xiaojd.entity.hospital.EngPtPharmacy"%>
<%@page  import="java.util.List"%>
<%
String path = request.getContextPath();
EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
List<EngPtDelivery>  ptDeliveryList = (List<EngPtDelivery>)request.getAttribute("ptDeliveryList");
EngPtCf cf = (EngPtCf)request.getAttribute("ptCf");
List<EngPtDrug> drugs = (List<EngPtDrug>)request.getAttribute("drugs");
List<String> areaList =(List<String>)request.getAttribute("areaList");
EngPtDelivery  ptDelivery = (EngPtDelivery)request.getAttribute("ptDelivery");
List<EngPtUser>  userList = (List<EngPtUser>)request.getAttribute("ptUserList");
%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>详情页面</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0;" />
	<meta content="yes" name="apple-mobile-web-app-capable" />
<link rel="stylesheet" type="text/css" href="<c:url value="/styles/mobile/bootstrap.min.css"/>"/>
<link rel="stylesheet" type="text/css" href="<c:url value="/styles/mobile/detail.css"/>"/>
 <script type="text/javascript" src="<c:url value="/scripts/mobile/jquery-1.8.3.min.js"/>"></script>
 <script type="text/javascript" src="<c:url value="/scripts/mobile/bootstrap.min.js"/>"></script>
</head>
<body>
	<div id="box">
		<div class="header">
			<a href="javascript:history.go(-1);" class="home">
		         <span class="header-icon header-icon-return"></span>
		         <span class="header-name">返回</span>
			</a>
			<div class="title">详情页面</div>
		</div>
		<div class="content">
			<div class="content_top">
				<div class="recipe">
					<span>处方：</span>
					<span class="user_recipe"><%= cf.getId()%></span>
				</div>
				<div class="name">
					<span>姓名：</span>
					<span class="user_namme"><%= ptDelivery.getName()%></span>
				</div>
				<div class="tel">
					<span>电话：</span>
					<span class="user_tel"><%= ptDelivery.getPhoneNo()%></span>
				</div>
				<div class="address">
					<span>地址：</span>
					<span clas='user_address'><%= ptDelivery.getAddress()%></span>
				</div>
				<div class="logistics">
					<span>派送员：</span><%if(ptDelivery.getCourierId()>0){ %><span> <%=ptDelivery.getCourierName() %></span><% }%>
					<% if(ptDelivery.getCourierId()==0) {%>
					<select id="courier" >
						<option value="<%=userList.get(0).getId()%>"><%=userList.get(0).getName()%></option>
					</select>
					<%}%>
					
				</div>
			</div>
			<div class="content_inner">
			  <c:forEach var="drug"  items="${drugs}" >
				<div class="inner_box">
					<div class="inner_name">
						<span>药物名称：</span>
							<span>${drug.drugName}</span>
					</div>
					<div class="size">
						<span>规&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格：</span>
						<span>${drug.spec}</span></div>
					<div class="way">
						<span>服用方法：</span>
						<span>${drug.adminRoute}/${drug.adminFrequency}//${drug.adminDose}</span>
					</div>
					<div class="price">
						<span>金&nbsp;额(元)：</span>
						<span>${drug.amount}</span>
					</div>
				</div>
				</c:forEach>
			</div>
			<div class="content_footer">
			     <div class="rt_btn "><a href="javascript:void(0)" class="statusName"><%=cf.getStatusName()%></a></div>
			     		<%if(cf.getStatus().endsWith("11") || cf.getStatus().endsWith("12")){ %><div class="rt_btn sendEndFail"><a onclick=sendEndFail('<%=cf.getId()%>')>配送取消</a></div><%}%>
				<%if(cf.getStatus().endsWith("11")){ %><div class="rt_btn startSend"><a onclick=startSend('<%=cf.getId()%>')>开始配送</a></div><%}%>
				<%if(cf.getStatus().endsWith("12")){ %><div class="status suceess sendEnd" style='text-align: center' onclick=sendEnd('<%=cf.getId()%>')>配送完成</div><%}%>
			</div>
		</div>
	</div>
</body>
</html>

<script type="text/javascript">
  function startSend(id) {
	  var courier = $('#courier').val();
	  $.ajax({
			type : "post",
			url : '<%=path%>/ptDelivertyToCourier',
			dataType : 'json',
			data:{'cfId':id,'courierId':courier},
			async : false,
			success : function(data) {
				$('.startSend').hide();
				$('.statusName').html("正在派送中");
			}
		}); 
  }
  
  function sendEnd(id) {
	  $.ajax({
			type : "post",
			url : '<%=path%>/changePtCfStatus',
			dataType : 'json',
			data:{'cfId':id,'status':15},
			async : false,
			success : function(data) {
				$('.sendEnd').hide();
				$('.sendEndFail').hide();
				$('.statusName').html("已经完成");
			}
		}); 
  }
  
  function sendEndFail(id) {
	  $.ajax({
			type : "post",
			url : '<%=path%>/changePtCfStatus',
			dataType : 'json',
			data:{'cfId':id,'status':16},
			async : false,
			success : function(data) {
				$('.sendEndFail').hide();
				$('.sendEnd').hide();
				$('.statusName').html("订单失败");
			}
		}); 
  }
</script>