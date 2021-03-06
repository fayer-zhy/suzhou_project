<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% String path = request.getContextPath(); %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" " http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>智能化合理医保（用药）管理系统</title>
	<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-1.8.3.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/scripts/jquery.ui/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/scripts/ajaxfileupload.js"/>"></script>
<script type="text/javascript">      
jQuery(document).ready(function(){

// 	isLegalFiletype();
//	contactAjaxFileUpload();
});

//校验上传文件类型
function isLegalFiletype(filename) {
	var pattern = /(.xls|.zip)$/;
	if (!pattern.exec(filename)) {
		alert("文件格式不正确!");
		return false;
	}
	return true;
}

function contactAjaxFileUploadOrder() {
	if (isLegalFiletype($("#uploadToImportFileOrder").val())) {
		$.ajaxFileUpload({
			url : 'fileUploadToOrder', 
			secureuri : false,// 一般设置为false
			fileElementId : 'uploadToImportFileOrder', // 文件上传控件的id属性 <input
			dataType :'text',
			success : function(data) { // 服务器成功响应处理函数
				
					alert(data);
					return;
								
			},
			error : function(data, e) { // 服务器响应失败处理函数
				alert("上传失败");
				return;
			}
		});
	}
}


function contactAjaxFileUploadOrderDrug() {
	if (isLegalFiletype($("#uploadToImportFileOrderDrug").val())) {
		$.ajaxFileUpload({
			url : 'fileUploadToOrderDrug', 
			secureuri : false,// 一般设置为false
			fileElementId : 'uploadToImportFileOrderDrug', // 文件上传控件的id属性 <input
			dataType :'text',
			success : function(data) { // 服务器成功响应处理函数
				
					alert(data);
					return;
								
			},
			error : function(data, e) { // 服务器响应失败处理函数
				alert("上传失败");
				return;
			}
		});
	}
}
	</script>
</head>

<body>
	上传药瞩:<input id="uploadToImportFileOrder" name="uploadToImportFileOrder" type="file" value="请选择上传的文件">
	<input type="button" onclick="contactAjaxFileUploadOrder()" value="上传">
	<br><br>
	上传药瞩项目：
	<input id="uploadToImportFileOrderDrug" name="uploadToImportFileOrderDrug" type="file" value="请选择上传的文件">
	<input type="button" onclick="contactAjaxFileUploadOrderDrug()" value="上传">
</body>
</html>
