<%@ page import="com.dev.servlet.domain.transfer.response.IServletResponse" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/routes/inventory-routes.jspf" %>
<%@ include file="/WEB-INF/routes/product-routes.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    IServletResponse servletResponse = (IServletResponse) request.getAttribute("response");
    request.setAttribute("categories", servletResponse.getEntity("categories"));
    request.setAttribute("items", servletResponse.getEntity("items"));
%>

<title>Inventory</title>

<div class="main">
    <jsp:include page="/WEB-INF/view/components/search.jsp">
        <jsp:param name="placeholder" value="Search inventory"/>
        <jsp:param name="action" value="${baseLink}${version}${ listInventory }"/>
        <jsp:param name="onclear" value="${baseLink}${version}${ listInventory }"/>
    </jsp:include>

    <c:if test="${ empty items }">
        <p>Inventories not found</p>
    </c:if>
    <c:if test="${ not empty items }">
        <c:set var="total" value="${0}"/>
        <div class="row">
            <div class="col-12">
                <table class="table table-striped table-bordered table-hover mb-0">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">PRODUCT</th>
                        <th scope="col">QUANTITY</th>
                        <th scope="col">DESCRIPTION</th>
                        <th scope="col">PRICE</th>
                        <th scope="col"></th>
                    </tr>
                    </thead>
                    <tbody>

                    <c:forEach items="${ items }" var="inventory">
                        <fmt:formatNumber value="${ inventory.getProduct().getPrice() * inventory.getQuantity() }"
                                          type="currency" minFractionDigits="2" var="parsedPrice"/>
                        <c:set var="total"
                               value="${total + inventory.getProduct().getPrice() * inventory.getQuantity()}"/>

                        <tr>
                            <th class="w-7" scope="row">${ inventory.id }</th>
                            <td class="w-20">
                                <a style="text-decoration: none; color: inherit;  padding: 2rem 0;"
                                   href="${baseLink}${version}${ listProduct }/${ inventory.getProduct().getId() }"
                                   target="_blank">${ inventory.getProduct().getName() }</a>
                            </td>
                            <td class="w-10">${ inventory.quantity }</td>
                            <td class="w-25">${ inventory.description }</td>
                            <td class="w-10">${ parsedPrice }</td>
                            <td class="w-10">
                                <a href="${baseLink}${version}${ listInventory }/${ inventory.id }" class="btn btn-auto btn-primary">
                                    <i class="bi bi-eye"></i>
                                </a>
                                <form action="${baseLink}${version}${ deleteItem }/${ inventory.id }" method="post" class="d-inline">
                                    <button type="submit" class="btn btn-auto btn-danger"
                                            onclick="return confirm('Are you sure?')">
                                        <i class="bi bi-trash3"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <caption class="pb-0">${items.size()} records | <fmt:formatNumber
                            value="${total}" type="currency" minFractionDigits="2"/></caption>
                </table>
            </div>
        </div>
    </c:if>
    <div class="d-flex flex-row-reverse mb20 mt-0">
        <a type="button" href="${baseLink}${version}${ newItem }" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> New
        </a>
    </div>
</div>
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>