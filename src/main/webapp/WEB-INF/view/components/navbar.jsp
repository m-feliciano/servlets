<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ include file="/WEB-INF/routes/category-routes.jspf" %>
<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<%@ include file="/WEB-INF/routes/user-routes.jspf" %>
<%@ include file="/WEB-INF/routes/auth-routes.jspf" %>

<nav class="navbar navbar-expand-lg">
    <div class="collapse navbar-collapse nav-items" id="navbarTogglerDemo01">
        <a class="navbar-brand" href="${listProduct}">
            <img src="<c:url value='/assets/logo.svg'/>" width="30" height="30" class="d-inline-block align-top"
                 alt="Bootstrap logo">
            <%--            <span class="title-logo ml-2">Shopping</span>--%>
        </a>
        <ul class="navbar-nav mr-auto mt-2 mt-lg-0">
            <li class="nav-item">
                <a class="nav-link" href="${listProduct}">Products</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="${listCategory}">Categories</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="${listInventory}">Inventory</a>
            </li>
        </ul>
        <div class="d-flex">
            <div class="avatar mt-1 mx30">
                <a href="${listUser}?id=${user.id}">
                    <c:if test="${not empty user.imgUrl and user.imgUrl ne ''}">
                        <img src="${user.imgUrl}" alt="user" class="avatar-img rounded-circle">
                    </c:if>
                    <c:if test="${empty user.imgUrl or user.imgUrl eq ''}">
                        <img src="<c:url value='/assets/avatar2.png'/>" alt="user" class="avatar-img rounded-circle">
                    </c:if>
                </a>
            </div>
            <a class="nav-link btn-logout mr-2" href="${listUser}/?id=${user.id}">Perfil</a>
        </div>
        <a class="nav-link btn-logout" href="${logout}">Logout</a>
    </div>
</nav>