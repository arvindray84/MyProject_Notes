<!DOCTYPE html>
<html>

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Sat, 01 Dec 1997 00:00:00 GMT">
<title>Note Analyzer</title>
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.6.4/angular.min.js"></script>
	<script
		src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDwnzVWUsuJbZVgt99rWIWYMGFcU4zq5bI&libraries=places"
		async defer></script>
<link
	href="https://cdnjs.cloudflare.com/ajax/libs/angular-toastr/2.1.1/angular-toastr.css"
	rel="stylesheet" />
<link
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
	rel="stylesheet" />
<link
	href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.5.2/animate.css"
	rel="stylesheet" />
<link
	href="https://cdnjs.cloudflare.com/ajax/libs/angular-ui-grid/4.0.4/ui-grid.css"
	rel="stylesheet" />
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-formhelpers/2.3.0/css/bootstrap-formhelpers.css" />
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/angular-xeditable/0.7.1/css/xeditable.min.css" />
<link rel="shortcut icon" href="static/img/favicon.ico" />
<link rel="styleSheet" href="static/css/app.css" />
<link href="static/css/styles.css" rel="stylesheet">
<link rel="styleSheet" href="static/css/custom.css" />
<link rel="stylesheet" href="static/css/subscription-style.css">


<style>
/* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
#map {
	height: 100%;
}
/* Optional: Makes the sample page fill the window. */
html, body {
	height: 100%;
	margin: 0;
	padding: 0;
}
</style>
<link type="text/css" rel="stylesheet"
	href="https://fonts.googleapis.com/css?family=Roboto:300,400,500">
<style>
#locationField, #controls {
	position: relative;
	width: 480px;
}

#autocomplete {
	position: absolute;
	top: 0px;
	left: 0px;
	width: 99%;
}

.label {
	text-align: right;
	font-weight: bold;
	width: 100px;
	color: #303030;
}

#address {
	border: 1px solid #000090;
	background-color: #f0f0ff;
	width: 480px;
	padding-right: 2px;
}

#address td {
	font-size: 10pt;
}

.field {
	width: 99%;
}

.slimField {
	width: 80px;
}

.wideField {
	width: 200px;
}

#locationField {
	height: 20px;
	margin-bottom: 40px;
}
</style>


</head>
<body>
	<div ng-controller="NavbarCtrl"
		class="navbar navbar-default navbar-static-top">
		<div class="navbar-header">
			<a class="navbar-brand" href="/"><i class="ion-ios7-pulse-strong"></i>
				Note Analyzer</a>
		</div>
		<!-- <ul class="nav navbar-nav">
			<li><a href="/notes/#!/">Home</a></li>
			<li ng-if="isAuthenticated()"><a href="/notes/#!/profile">Profile</a></li>
		</ul>
		<ul ng-if="isAuthenticated()" class="nav navbar-nav">
			<li><a href="/notes/#!/noteDashboard">My Dashboard</a></li>
		</ul>
		<ul ng-if="isAuthenticated()" class="nav navbar-nav">
			<li><a href="/notes/#!/subscription">Calculator</a></li>
		</ul>
		<ul ng-if="!isAuthenticated()" class="nav navbar-nav pull-right">
			<li><a href="/notes/#!/login">Login</a></li>
			<li><a href="/notes/#!/signup">Sign up</a></li>
		</ul>
		<ul ng-if="isAuthenticated()" class="nav navbar-nav pull-right">
			<li><a href="/notes/#!/logout">Logout</a></li>
		</ul> -->

	</div>

	<p>You have successfully verified your account. Please <%-- <a href="<%=response.sendRedirect("welcome.jsp")%>"> --%>login</a> with your credential.</p>

	<!-- Third-party Libraries -->


	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.6.4/angular-animate.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-messages/1.6.4/angular-messages.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-resource/1.6.4/angular-resource.min.js"></script>

	<!-- Angular Material Library -->

	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-sanitize/1.6.4/angular-sanitize.min.js"></script>
	<!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/angular-ui-router/0.4.2/angular-ui-router.min.js"></script> -->
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-ui-router/1.0.3/angular-ui-router.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-toastr/1.7.0/angular-toastr.tpls.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-touch/1.6.4/angular-touch.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-ui-grid/4.0.4/ui-grid.min.js"></script>
	<script
		src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/2.5.0/ui-bootstrap-tpls.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-formhelpers/2.3.0/js/bootstrap-formhelpers.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/angular-xeditable/0.7.1/js/xeditable.min.js"></script>
	<!--[if lt IE 9]><script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->

	<!-- Application Code -->
	<script src="static/note.js"></script>
	<script src="static/js/directives/passwordStrength.js"></script>
	<script src="static/js/directives/passwordMatch.js"></script>
	<script src="static/js/constant/constant.js"></script>
	<script src="static/js/controller/home.js"></script>
	<script src="static/js/controller/login.js"></script>
	<script src="static/js/controller/signup.js"></script>
	<script src="static/js/controller/logout.js"></script>
	<script src="static/js/controller/profile.js"></script>
	<script src="static/js/controller/noteDetail.js"></script>
	<script src="static/js/service/userService.js"></script>
	<script src="static/js/service/noteService.js"></script>
	
	
</body>


</html>
