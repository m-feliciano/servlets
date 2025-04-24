<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("inventory", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<div class="main">
    <form action="${baseLink}${version}${ updateItem }/${inventory.id}" method="post">
        <div class="col-md-6">
            <div class="mb-3">
                <label for="inputItemId" class="form-label">ID</label>
                <input type="number" name="id" class="form-control" id="inputItemId"
                       placeholder="ID" value="${ inventory.id }" readonly="readonly" required minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputProductId" class="form-label">PRODUCT ID</label>
                <input type="number" name="productId" class="form-control" id="inputProductId"
                       placeholder="ID" value="${ inventory.getProduct().getId() }" required minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputQuantity" class="form-label">QUANTITY</label>
                <input type="number" name="quantity" class="form-control" id="inputQuantity"
                       placeholder="quantity" value="${ inventory.quantity }" required minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputDescription" class="form-label">DESCRIPTION</label>
                <textarea name="description" class="form-control" id="inputDescription"
                          placeholder="descripton" rows="6">${ inventory.description }</textarea>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger hidden-alert" role="alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <div class="align-end">
                <jsp:include page="/WEB-INF/view/components/buttons/backButton.jsp"/>
                <span class="mr-2"></span>
                <jsp:include page="/WEB-INF/view/components/buttons/saveButton.jsp"/>
            </div>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>