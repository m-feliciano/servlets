<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.dev.servlet.interfaces.IServletResponse" %>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    IServletResponse servletResponse = (IServletResponse) request.getAttribute("response");
    request.setAttribute("categories", servletResponse.getResponse("categories"));
    request.setAttribute("products", servletResponse.getResponse("products"));
    request.setAttribute("totalPrice", servletResponse.getResponse("totalPrice"));
%>

<title>Products</title>

<div class="main">
    <jsp:include page="/WEB-INF/view/components/search.jsp">
        <jsp:param name="placeholder" value="Search product"/>
        <jsp:param name="action" value="${ listProduct }"/>
        <jsp:param name="onclear" value="${ listProduct }"/>
        <jsp:param name="categories" value="${ categories }"/>
    </jsp:include>

    <c:if test="${ empty products }">
        <p>Products not found.</p>
    </c:if>

    <c:if test="${ not empty products }">
        <div class="row">
            <div class="col-12">
                <div class="table-responsive">
                    <table class="table table-striped table-bordered table-hover mb-0">
                        <thead class="thead-dark">
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">IMAGE</th>
                            <th scope="col">NAME</th>
                            <th scope="col">DESCRIPTION</th>
                            <th scope="col">PRICE</th>
                            <th scope="col">REGISTER</th>
                            <th scope="col"></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${ products }" var="product">
                            <fmt:formatNumber value="${product.price}" type="currency" minFractionDigits="2"
                                              var="parsedPrice"/>
                            <tr>
                                <th class="w-8" scope="row">${ product.id }</th>
                                <td class="text-center w-9">
                                    <a href="${ listProduct }?id=${ product.id }" target="_blank">
                                        <c:choose>
                                            <c:when test="${empty product.url }">
                                                <img class="img-thumbnail img-square-min"
                                                     src="<c:url value='/assets/no_image_available.png'/>"
                                                     alt="no available">
                                            </c:when>
                                            <c:otherwise>
                                                <img class="img-thumbnail img-square-min" src="${ product.url }"
                                                     alt="Product ${ product.name }">
                                            </c:otherwise>
                                        </c:choose>
                                    </a>
                                </td>
                                <td class="w-20">${ product.name }</td>
                                <td class="w-25">${ product.description }</td>
                                <td class="w-10">${ parsedPrice }</td>
                                <td class="w-14">${ product.registerDate }</td>
                                <td class="w-14">
                                    <form action="${ listProduct }" method="get" class="d-inline">
                                        <input type="hidden" name="id" value="${ product.id }"/>
                                        <button type="submit" class="btn btn-auto btn-primary">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </form>
                                    <form action="${ listInventory }" method="get" class="d-inline">
                                        <input type="hidden" name="k" value="product"/>
                                        <input type="hidden" name="q" value="${ product.id }"/>

                                        <button type="submit" class="btn btn-auto btn-info">
                                            <i class="bi bi-search"></i>
                                        </button>
                                    </form>
                                    <form class="d-inline">
                                        <button type="submit" class="btn btn-auto btn-secondary" disabled>
                                            <i class="bi bi-box"></i>
                                        </button>
                                    </form>
                                    <form action="${ deleteProduct }/${ product.id }" method="post" class="d-inline">
                                        <button type="submit" class="btn btn-auto btn-danger"
                                                onclick="return confirm('Are you sure?')">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <caption class="pb-0 caption">${query.getPagination().getTotalRecords()} records |
                            <fmt:formatNumber value="${totalPrice}" type="currency" minFractionDigits="2"/></caption>
                    </table>
                </div>
            </div>

            <jsp:include page="/WEB-INF/view/components/pagination.jsp">
                <jsp:param name="totalRecords" value="${query.getPagination().getTotalRecords()}"/>
                <jsp:param name="currentPage" value="${query.getPagination().getCurrentPage()}"/>
                <jsp:param name="totalPages" value="${query.getPagination().getTotalPages()}"/>
                <jsp:param name="pageSize" value="${query.getPagination().getPageSize()}"/>
                <jsp:param name="href" value="${listProduct}"/>
            </jsp:include>
        </div>
    </c:if>

    <div class="d-flex flex-row-reverse mb20">
        <a type="button" href="${ newProduct }" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> New
        </a>
    </div>
</div>
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>