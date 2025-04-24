<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("categories", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<div class="main">
    <form action="${baseLink}${version}${ createProduct }" method="post">
        <div class="col-md-6">
            <div class="mb-3">
                <label for="inputName" class="form-label">NAME</label>
                <input type="text" name="name" class="form-control" id="inputName"
                       placeholder="name" required minlength="4"/>
            </div>
            <div class="mb-3">
                <label for="inputDescription" class="form-label">DESCRIPTION</label>
                <textarea name="description" class="form-control" id="inputDescription"
                          placeholder="simple Description" required></textarea>
            </div>
            <div class="mb-3">
                <div class="row justify-content-end">
                    <div class="col-md-6">
                        <label for="inputCategory" class="form-label">CATEGORY</label>
                        <select name="category" class="form-control text-center" id="inputCategory" required>
                            <option value="${null}" selected>${"< SELECT >"}</option>
                            <c:forEach items="${ categories }" var="category">
                                <option value="${category.id}">${category.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="inputPrice" class="form-label">PRICE</label>
                        <input name="price" class="form-control text-right" id="inputPrice"
                               placeholder="R$1000,00" min="0" max="999999" step="any"
                               pattern="^\s*(?:[1-9]\d{0,2}(?:\,\d{3})*|0)(?:.\d{1,2})?$"
                               title="Currency should only contain numbers and (comma/doc) e.g. 1000,00"
                               required/>
                    </div>
                </div>
            </div>
            <div class="mb-3">
                <label for="inputURL" class="form-label">IMAGE</label>
                <textarea name="url" class="form-control" id="inputURL" placeholder="URL"></textarea>
            </div>

            <jsp:include page="/WEB-INF/view/components/buttons/backButton.jsp"/>
            <span class="mr-2"></span>
            <jsp:include page="/WEB-INF/view/components/buttons/saveButton.jsp"/>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>