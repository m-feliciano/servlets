<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IServletResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>

<%
    IServletResponse servletResponse = (IServletResponse) request.getAttribute("response");
    request.setAttribute("categories", servletResponse.getEntity("categories"));
    request.setAttribute("product", servletResponse.getEntity("product"));
%>

<fmt:formatDate value="${product.registerDate}" pattern="dd/MM/yyyy" var="stdDate"/>

<div class="main">
    <form action="${baseLink}${version}${ updateProduct }/${product.id}" method="post">
        <div class="row">
            <div class="col-md-6">
                <div class="mb-3">
                    <div class="row">
                        <div class="col-md-6">
                            <label for="inputId" class="form-label">ID</label>
                            <input type="text" name="id" class="form-control text-right" id="inputId"
                                   value="${ product.id }" readonly/>
                        </div>
                        <div class="col-md-6">
                            <label for="inputRegisterDate" class="form-label">REGISTER</label>
                            <input type="text" name="register" class="form-control text-right" id="inputRegisterDate"
                                   value="${ stdDate }" required readonly/>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label for="inputName" class="form-label">NAME</label>
                    <input type="text" name="name" class="form-control" id="inputName"
                           placeholder="Product name" value="${ product.name }" autocomplete="name" required
                           minlength="4"/>
                </div>
                <div class="mb-3">
                    <label for="inputDescription" class="form-label">DESCRIPTION</label>
                    <textarea name="description" class="form-control" id="inputDescription"
                              placeholder="Simple Description" rows="6" cols="auto"
                              required>${ product.description }</textarea>
                </div>
                <div class="mb-3">
                    <div class="row justify-content-end">
                        <div class="col-md-6">
                            <label for="inputCategory" class="form-label">CATEGORY</label>
                            <select name="category" class="form-control text-center" id="inputCategory" required>
                                <option value="${null}" selected>${"< SELECT >"}</option>
                                <c:forEach items="${ categories }" var="category">
                                    <c:choose>
                                        <c:when test="${ category.id == product.category.id }">
                                            <option value="${ category.id }" selected>${ category.name }</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${ category.id }">${ category.name }</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label for="inputPrice" class="form-label">PRICE</label>
                            <input name="price" class="form-control" id="inputPrice"
                                   placeholder="1000,00" value="${ product.price }" min="0" max="10000" step="any"
                                   pattern="^\s*(?:[1-9]\d{0,2}(?:\,\d{3})*|0)(?:.\d{1,2})?$"
                                   title="Currency should only contain numbers and (comma/doc) e.g. 1000,00"
                                   required/>
                        </div>
                    </div>
                </div>
                <div class="mb-4">
                    <label for="inputUrl" class="form-label">URL</label>
                    <input type="text" name="url" class="form-control" id="inputUrl" placeholder="URL"
                           value="${ product.url }"/>
                </div>

                <div class="row flex-row-reverse mr-0 mb20">
                    <jsp:include page="/WEB-INF/view/components/buttons/saveButton.jsp"/>
                    <span class="mr-2"></span>
                    <jsp:include page="/WEB-INF/view/components/buttons/backButton.jsp"/>
                </div>
            </div>

            <div class="col-md-6">
                <div class="mb-3">
                    <label for="inputImage" class="form-label">IMAGES</label>
                    <input type="file" name="image" class="form-control" id="inputImage" accept="image/*"/>
                </div>
            </div>

            <%--            <div class="col-md-6">--%>
            <%--                <div class="mb-3">--%>
            <%--                    <label for="inputImage" class="form-label">IMAGES</label>--%>
            <%--                    <div class="row">--%>
            <%--                        <c:if test="${ not empty product.images }">--%>
            <%--                            <c:forEach items="${ product.images }" var="image">--%>
            <%--                                <div class="col-md-3">--%>
            <%--                                    <img src="${ image }" class="img-thumbnail" alt="Product Image"/>--%>
            <%--                                </div>--%>
            <%--                            </c:forEach>--%>
            <%--                        </c:if>--%>
            <%--                    </div>--%>
            <%--                </div>--%>
            <%--            </div>--%>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>