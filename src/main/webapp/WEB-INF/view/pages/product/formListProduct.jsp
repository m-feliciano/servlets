<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("product", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<fmt:formatNumber value="${product.price}" type="currency" minFractionDigits="2" var="parsedNumber"/>
<fmt:formatDate value="${product.registerDate}" pattern="dd/MM/yyyy" var="stdDate"/>

<title>Product: ${ product.name }</title>

<div class="main">
    <div class="row">
        <div class="col-md-6">
            <div class="mb-3">
                <div class="row">
                    <div class="col-md-6">
                        <label for="inputId" class="form-label">ID</label>
                        <input type="text" name="id" class="form-control text-right" id="inputId"
                               value="${ product.id }" readonly="readonly"/>
                    </div>
                    <div class="col-md-6">
                        <label for="inputRegisterDate" class="form-label">REGISTER</label>
                        <input type="text" name="register" class="form-control text-right" id="inputRegisterDate"
                               value="${ stdDate }" readonly/>
                    </div>
                </div>
            </div>
            <div class="mb-3">
                <label for="inputName" class="form-label">NAME</label>
                <input type="text" name="name" class="form-control" id="inputName"
                       placeholder="Product name" value="${ product.name }" autocomplete="name"
                       minlength="4" readonly/>
            </div>
            <div class="mb-4">
                <div class="row justify-content-end">
                    <div class="col-md-6">
                        <label for="inputCategory" class="form-label">CATEGORY</label>
                        <input type="text" name="category" class="form-control text-center" id="inputCategory"
                               value="${ product.category.name }" autocomplete="name" readonly/>
                    </div>
                    <div class="col-md-6">
                        <label for="inputPrice" class="form-label">PRICE</label>
                        <input name="price" class="form-control text-right" id="inputPrice"
                               placeholder="price" value="${parsedNumber}" readonly/>
                    </div>
                </div>
            </div>
            <div class="mb-3">
                <label for="inputDescription" class="form-label">DESCRIPTION</label>
                <textarea name="description" class="form-control" id="inputDescription"
                          placeholder="Simple Description" readonly rows="10" cols="auto">
                    ${ product.description }</textarea>
            </div>
            <c:if test="${ not empty product.url and not product.url eq '' }">
                <div class="mb-3">
                    <label for="inputUrl" class="form-label">URL</label>
                    <input type="text" name="url" class="form-control" id="inputUrl"
                           placeholder="URL" value="${ product.url }" readonly/>
                </div>
            </c:if>

            <div class="row justify-content-end mr-0 mb20">
                <jsp:include page="/WEB-INF/view/components/buttons/customButton.jsp">
                    <jsp:param name="btnLabel" value="Back"/>
                    <jsp:param name="btnType" value="button"/>
                    <jsp:param name="btnClass" value="btn btn-light"/>
                    <jsp:param name="btnIcon" value="bi bi-arrow-left"/>
                    <jsp:param name="btnOnclick" value="onclick='window.location.href=`${baseLink}${version}${ listProduct }`'"/>
                    <jsp:param name="btnId" value="id='backButton'"/>
                </jsp:include>

                <span class="mr-2"></span>

                <a type="button" href="${baseLink}${version}${ editProduct }/${ product.id }" class="btn btn-success">
                    Edit <i class="bi bi-pencil-square"></i>
                </a>
            </div>
        </div>

        <div class="col-md-6 text-center mt30">
            <div class="mb-3">
                <c:choose>
                    <c:when test="${ empty product.url }">
                        <img src="<c:url value='/resources/assets/no_image_available.png'/>"
                             class="img-thumbnail" alt="No available" width="60%" height="60%"/>
                    </c:when>
                    <c:otherwise>
                        <img src="${ product.url }" class="img-thumbnail" alt="Product" width="60%" height="60%"/>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>