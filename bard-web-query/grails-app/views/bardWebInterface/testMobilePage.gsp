<%--
  Created by IntelliJ IDEA.
  User: gwalzer
  Date: 11/19/12
  Time: 3:33 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Page</title>
    <r:require modules="jqueryMobile"/>
    <r:layoutResources/>
</head>

<body>

<div data-role="page">

    <div data-role="header">
        <h1>My Title</h1>
    </div><!-- /header -->

    <div data-role="content">
        <p>Hello world</p>
    </div><!-- /content -->

    <ul data-role="listview" data-inset="true" data-filter="true">
        <li><a href="#">Acura</a></li>
        <li><a href="#">Audi</a></li>
        <li><a href="#">BMW</a></li>
        <li><a href="#">Cadillac</a></li>
        <li><a href="#">Ferrari</a></li>
    </ul>

</div><!-- /page -->
<r:layoutResources/>
</body>
</html>