<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="html" uri="/WEB-INF/tld/struts-html.tld" %>
<%@taglib prefix="bean" uri="/WEB-INF/tld/struts-bean.tld" %>
<%@taglib prefix="logic" uri="/WEB-INF/tld/struts-logic.tld" %>
<html>
<head>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/style.css" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Get Swift</title>

<style>
body {
    background-image: url("<%=request.getContextPath()%>/images/background_drones.jpg");
    background-size: cover;
}
</style>

<script language="JavaScript" type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js" ></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.ajax-cross-origin.min.js"></script>
<script language="JavaScript" type="text/javascript">

// This function collects the Drones and Packages data from the API
function getRandomData(methodName, fn) {
	var QueryURL = 'https://codetest.kube.getswift.co/' + methodName;
	$.ajax({
		  url: QueryURL,
		  crossOrigin: true,
		  async: false,
		  dataType: "application/json",
		  contentType: 'application/json',
		  type: "GET",
		  success: function (data) {
	          fn(data);
	        }
		});
}

// This function makes call to service class where packages are assigned to drones
function assignPackages() {
	var JSONObject;
	var assignURL = 'http://localhost:8080/DroneDeliveryGS/rest/assignPackage/assignPackagesToDrones';
	getRandomData('drones', function(dronesData) {
        
        getRandomData('packages', function(packagesData) {
            JSONObject= {"drones":dronesData, "packages":packagesData };
            $.ajax({
      		  url: assignURL,
      		  type: 'POST',
      			dataType: 'json',
      			data: JSON.stringify(JSONObject),
                contentType: 'application/json; charset=utf-8',
      		  success: function (data2) {
      			$('#assignedListDiv').html(JSON.stringify(data2));
      			$('#assignSuccess').show();
      		  }  
      		});
        });
    });
}

$( document ).ready(function() {
	$('#assignSuccess').hide();
});

</script>

</head>
<body style="padding: 0px; background-image: <%=request.getContextPath()%>/images/background_drones.jpg">
<br>
<button onclick="assignPackages();">Assign Packages To Drones</button>
<br>
<br>
<div id="assignSuccess">
<b>Packages assigned successfully. Please find the list below</b><br><br>

<table id="assignListTable" cellpadding="2" cellspacing="5" >
 	<tr>
 		<td id="assignedListDiv"></td>
 	</tr>
</table>
</div>

</body>
</html>