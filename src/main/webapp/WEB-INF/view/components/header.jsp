<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1" %>

<fmt:setLocale value="pt-BR" scope="session"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="description" content="Product management"/>
    <meta name="author" content="Feliciano"/>
    <meta name="keywords" content="Shopping"/>
    <meta name="robots" content="index, follow"/>
    <meta name="googlebot" content="index, follow"/>
    <meta name="google" content="notranslate"/>
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
          integrity="sha384-zCbKRCUGaJDkqS1kPbPd7TveP5iyJE0EjAuZQTgFLD2ylzuqKfdKlfG/eSrtxUkn"
          crossorigin="anonymous"/>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <link rel="stylesheet" href="<c:url value='/resources/css/styles.css'/>">
    <title></title>
</head>
<body>

<jsp:include page="/WEB-INF/view/components/navbar.jsp"/>

<div class="content">