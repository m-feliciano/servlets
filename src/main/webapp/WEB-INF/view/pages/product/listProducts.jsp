<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IServletResponse" %>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    IServletResponse servletResponse = (IServletResponse) request.getAttribute("response");
    request.setAttribute("categories", servletResponse.getEntity("categories"));
    request.setAttribute("pageable", servletResponse.getEntity("pageable"));
    request.setAttribute("totalPrice", servletResponse.getEntity("totalPrice"));
%>

<title>Products</title>

<div class="main">
    <jsp:include page="/WEB-INF/view/components/search.jsp">
        <jsp:param name="placeholder" value="Search product"/>
        <jsp:param name="action" value="${baseLink}${version}${ listProduct }"/>
        <jsp:param name="onclear" value="${baseLink}${version}${ listProduct }"/>
        <jsp:param name="categories" value="${ categories }"/>
    </jsp:include>

    <c:if test="${ !pageable.getContent().iterator().hasNext() }">
        <p>Products not found.</p>
    </c:if>

    <c:if test="${ pageable.getContent().iterator().hasNext() }">
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
                        <c:forEach items="${ pageable.getContent() }" var="product">
                            <fmt:formatNumber value="${product.price}" type="currency" minFractionDigits="2" var="parsedPrice"/>
                            <fmt:parseDate value="${product.registerDate}" pattern="yyyy-MM-dd" var="parsedRegisterDate"/>
                            <fmt:formatDate value="${parsedRegisterDate}" pattern="dd/MM/yyyy" var="registerDate"/>

                            <tr>
                                <th class="w-8" scope="row">${ product.id }</th>
                                <td class="text-center w-9">
                                    <a href="${baseLink}${version}${ listProduct }/${ product.id }" target="_blank">
                                        <c:choose>
                                            <c:when test="${empty product.url }">
                                                <img class="img-thumbnail img-square-min"
                                                     src="<c:url value='/resources/assets/no_image_available.png'/>"
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
                                <td class="w-14">${ registerDate }</td>
                                <td class="w-14">
                                    <a href="${baseLink}${version}${ listProduct }/${ product.id }" class="btn btn-auto btn-primary">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                    <form action="${baseLink}${version}${ listInventory }" method="get" class="d-inline">
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
                                    <form action="${baseLink}${version}${ deleteProduct }/${ product.id }" method="post" class="d-inline">
                                        <button type="submit" class="btn btn-auto btn-danger"
                                                onclick="return confirm('Are you sure?')">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <caption class="pb-0 caption">${pageable.getTotalElements()} records |
                            <fmt:formatNumber value="${totalPrice}" type="currency" minFractionDigits="2"/></caption>
                    </table>
                </div>
            </div>

            <jsp:include page="/WEB-INF/view/components/pagination.jsp">
                <jsp:param name="totalRecords" value="${pageable.getTotalElements()}"/>
                <jsp:param name="currentPage" value="${pageable.getCurrentPage()}"/>
                <jsp:param name="totalPages" value="${pageable.getTotalPages()}"/>
                <jsp:param name="pageSize" value="${pageable.getPageSize()}"/>
                <jsp:param name="sort" value="${pageable.getSort().getField()}"/>
                <jsp:param name="direction" value="${pageable.getSort().getDirection().getValue()}"/>

                <jsp:param name="href" value="${baseLink}${version}${listProduct}"/>
            </jsp:include>
        </div>
    </c:if>

    <div class="d-flex flex-row-reverse mb20">
        <a type="button" href="${baseLink}${version}${ newProduct }" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> New
        </a>
    </div>
</div>
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>