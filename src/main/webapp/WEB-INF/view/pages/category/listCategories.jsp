<%@ include file="/WEB-INF/routes/category-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("categories", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<title>Categories</title>

<div class="main">
    <c:if test="${ empty categories }">
        <div class="d-flex flex-row-reverse mb20 mb-4">
            <a type="button" href="${baseLink}${version}${ newCategory }" class="btn btn btn-success">New</a>
        </div>
        <p>No one new category created.</p>
    </c:if>
    <c:if test="${ not empty categories }">
        <div class="row">
            <div class="col-12">
                <table class="table table-striped table-bordered table-hover mb-0">
                    <caption class="pb-0">${categories.size()} records</caption>
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">NAME</th>
                        <th scope="col"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${ categories }" var="category">
                        <input type="hidden" name="id" id="id${ category.id }" value="${ category.id }">
                        <tr>
                            <th width="10%" scope="row">${ category.id }</th>
                            <td width=50%>${ category.name }</td>
                            <td width=25%>
<%--                                <form action="${ listCategory }" method="get" class="d-inline">--%>
<%--                                    <input type="hidden" name="id" value="${ category.id }">--%>
<%--                                    <button type="submit" class="btn btn-auto btn-primary">--%>
<%--                                        <i class="bi bi-eye"></i>--%>
<%--                                    </button>--%>
<%--                                </form>--%>
                                <a type="button" href="${baseLink}${version}${ listCategory }/${ category.id }" class="btn btn-auto btn-primary">
                                    <i class="bi bi-eye"></i>
                                </a>
                                <form action="${baseLink}${version}${ deleteCategory }/${ category.id }" method="post" class="d-inline">
                                    <button type="submit" class="btn btn-auto btn-danger"
                                            onclick="return confirm('Are you sure?')">
                                        <i class="bi bi-trash3"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <div class="d-flex flex-row-reverse mb20">
                    <a type="button" href="${baseLink}${version}${ newCategory }" class="btn btn-success">
                        <i class="bi bi-plus-circle"></i> New
                    </a>
                </div>
            </div>
        </div>
    </c:if>
</div>
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>