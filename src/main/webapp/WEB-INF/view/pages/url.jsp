<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- Defining the URLs for the application -->
<c:url value="inventory" var="inventoryLink"/>
<c:url value="${inventoryLink}?action=delete" var="deleteItem"/>
<c:url value="${inventoryLink}?action=list" var="listInventories"/>
<c:url value="${inventoryLink}?action=new" var="newItem"/>
<c:url value="${inventoryLink}?action=edit" var="editItem"/>

<c:url value="product" var="productLink"/>
<c:url value="${productLink}?action=list" var="listProducts"/>
<c:url value="${productLink}?action=delete" var="deleteProduct"/>
<c:url value="${productLink}?action=new" var="newProduct"/>
<c:url value="${productLink}?action=edit" var="editProduct"/>

<c:url value="category" var="categoryLink"/>
<c:url value="${categoryLink}?action=delete" var="deleteCategory"/>
<c:url value="${categoryLink}?action=list" var="listCategories"/>
<c:url value="${categoryLink}?action=new" var="newCategory"/>
<c:url value="${categoryLink}?action=edit" var="editCategory"/>

<c:url value="login" var="loginLink"/>
<c:url value="${loginLink}?action=loginForm" var="loginPage"/>

<c:url value="user" var="userLink"/>
<c:url value="${userLink}?action=registerPage" var="registerPage"/>
<c:url value="${userLink}?action=edit" var="editUser"/>
<c:url value="${userLink}?action=delete" var="deleteUser"/>
<c:url value="${userLink}?action=list" var="listUsers"/>