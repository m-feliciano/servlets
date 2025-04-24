<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("inventory", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<div class="main">
    <div class="col-md-6">
        <div class="mb-3">
            <label for="inputItemId" class="form-label">ID</label>
            <input type="number" name="id" class="form-control" id="inputItemId"
                   placeholder="ID" value="${ inventory.id }" readonly="readonly" required minlength="1"/>
        </div>
        <div class="mb-3">
            <label for="inputProductId" class="form-label">PRODUCT ID</label>
            <input type="number" name="productId" class="form-control" id="inputProductId"
                   placeholder="ID" value="${ inventory.getProduct().getId() }" readonly="readonly" required
                   minlength="1"/>
        </div>
        <div class="mb-3">
            <label for="inputQuantity" class="form-label">QUANTITY</label>
            <input type="number" name="quantity" class="form-control" id="inputQuantity"
                   placeholder="quantity" value="${ inventory.quantity }" readonly="readonly" required minlength="1"/>
        </div>
        <div class="mb-3">
            <label for="inputDescription" class="form-label">DESCRIPTION</label>
            <textarea name="description" class="form-control" id="inputDescription"
                      placeholder="descripton" required readonly
                      rows="6" minlength="4">${ inventory.description }</textarea>
        </div>

        <div class="align-end">
            <jsp:include page="/WEB-INF/view/components/buttons/customButton.jsp">
                <jsp:param name="btnLabel" value="Back"/>
                <jsp:param name="btnType" value="button"/>
                <jsp:param name="btnClass" value="btn btn-light"/>
                <jsp:param name="btnIcon" value="bi bi-arrow-left"/>
                <jsp:param name="btnOnclick" value="onclick='window.location.href=`${baseLink}${version}${ listInventory }`'"/>
                <jsp:param name="btnId" value="id='backButton'"/>
            </jsp:include>

            <span class="mr-2"></span>

            <a type="button" href="${baseLink}${version}${ editItem }/${ inventory.id }" class="btn btn-success">Edit
                <i class="bi bi-pencil-square"></i>
            </a>
        </div>
    </div>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>