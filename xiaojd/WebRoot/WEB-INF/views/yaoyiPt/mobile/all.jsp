<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="com.xiaojd.entity.hospital.EngPtUser"%>
<%@page import="com.xiaojd.entity.hospital.EngPtDelivery"%>
<%@page  import="java.util.ArrayList"%>
<%@page  import="java.util.List"%>
<% 
String path = request.getContextPath();
EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
List<EngPtDelivery>  ptDeliveryList = (List<EngPtDelivery>)request.getAttribute("ptDeliveryList");
if(ptDeliveryList ==null) {
	ptDeliveryList = new ArrayList<EngPtDelivery>();
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>配送中心平台</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0;" />
<meta content="yes" name="apple-mobile-web-app-capable" />

<link rel="stylesheet" type="text/css" href="<c:url value="/styles/mobile/bootstrap.min.css"/>"/>
<link rel="stylesheet" type="text/css" href="<c:url value="/styles/mobile/platform.css"/>"/>
 <script type="text/javascript" src="<c:url value="/scripts/mobile/jquery-1.8.3.min.js"/>"></script>
 <script type="text/javascript" src="<c:url value="/scripts/mobile/bootstrap.min.js"/>"></script>


<script type="text/javascript">
	$(function(){
		$('.alllist a').click(function (e) {
			  e.preventDefault()
			  $(this).tab('show')
			})
	});
	
	function ptDelivertyToDatail (DelivertyId) {
		window.location.href ='<%=path%>/mobilePtDelivertyToDatail?DelivertyId='+DelivertyId;
	}
</script>
</head>
<body>
	<div class="container">
		<div class="header">
			<a href="javascript:history.go(-1);" class="home">
		            <span class="header-icon header-icon-return"></span>
		            <span class="header-name">返回</span>
			</a>
			<div class="title" id="titleString">全部配送</div>
		</div>
		<div class="alllist">
			<ul class="nav nav-tabs">
			<span class='no_orders' >暂无订单</span>
				<li class="active"><a href="#all" data-toggle="tab">全部</a></li>
				<li><a href="#ok" data-toggle="tab">已完成</a></li>
				<li><a href="#cancel" data-toggle="tab">已撤销</a></li>
			</ul>
			<div id="tab-content" class="tab-content">
			
				<div id="all" class="tab-pane active">
				<%--       <span class='no_orders' >暂无订单</span> --%>
				     <c:forEach var="delivery"  items="${ptDeliveryList}" >
					     <div class="item" onclick=ptDelivertyToDatail('${delivery.id}')>
					       <c:if test="${delivery.cfStatus == 15}">
						    <div class="img imgok"></div>
						</c:if>
						  <c:if test="${delivery.cfStatus == 16}">
						   <div class="img imgcancel"></div>	
						</c:if>
						<c:if test="${delivery.cfStatus == 11 || delivery.cfStatus == 12}">
						  <div class="img imgnew"></div>	
						</c:if>
						<div><span>处方编号: </span><span>${delivery.cfId}</span></div>
						<div><span>处方时间: </span><span>${delivery.presDateTime}</span></div>
						<div><span>联系人: </span><span>${delivery.name}</span></div>
						<div><span>联系电话: </span><span>${delivery.phoneNo}</span></div>
						<div><span>配送地址: </span><span>${delivery.address}</span></div>
					</div>
				  </c:forEach>
				
				</div>
				<div id="ok" class="tab-pane">
				<%-- <span class='no_orders' >暂无订单</span> --%>
					 <c:forEach var="delivery"  items="${ptDeliveryList}" >
					   <c:if test="${ delivery.cfStatus == 15}">
					     <div class="item" onclick=ptDelivertyToDatail('${delivery.id}')>
						  <div class="img imgok"></div>	
						<div><span>处方编号: </span><span>${delivery.cfId}</span></div>
						<div><span>处方时间: </span><span>${delivery.presDateTime}</span></div>
						<div><span>联系人: </span><span>${delivery.name}</span></div>
						<div><span>联系电话: </span><span>${delivery.phoneNo}</span></div>
						<div><span>配送地址: </span><span>${delivery.address}</span></div>
					    </div>
					</c:if>
				  </c:forEach>
				</div>
				<div id="cancel" class="tab-pane">
				<%-- 	<span class='no_orders' >暂无订单</span> --%>
					   <c:forEach var="delivery"  items="${ptDeliveryList}" >
					    <c:if test="${delivery.cfStatus == 16}">
					     <div class="item" onclick=ptDelivertyToDatail('${delivery.id}')>
						  <div class="img imgcancel"></div>	
						<div><span>处方编号: </span><span>${delivery.cfId}</span></div>
						<div><span>处方时间: </span><span>${delivery.presDateTime}</span></div>
						<div><span>联系人: </span><span>${delivery.name}</span></div>
						<div><span>联系电话: </span><span>${delivery.phoneNo}</span></div>
						<div><span>配送地址: </span><span>${delivery.address}</span></div>
					     </div>
						</c:if>
				  </c:forEach>
				</div>
			</div>
		</div>
		<div class="footerbtn">
			<div style="display: block;border-bottom: solid 1px #FFF;border-top: solid 1px #cacaca;text-indent: -9999px;height: 0px;">line</div>
			<ul class="unstyled">
				<li><a href="<%=path%>/mobileNew"><span class="icon icon-new"></span><span class="name">新配送</span></a></li>
				<li><a href= "<%=path%>/mobileAll" class="active"><span class="icon icon-all"></span><span class="name">全部配送</span></a></li>
				<li><a href="<%=path%>/mobileUser"><span class="icon icon-user"></span><span class="name">个人中心</span></a></li>
			</ul>
		</div>
	</div>
</body>
</html>
