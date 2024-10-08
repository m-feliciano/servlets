<%@ include file="/WEB-INF/jspf/common-imports.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<div class="main">
    <c:if test="${ empty categories }">
        <div class="d-flex flex-row-reverse mb20 mb-4">
            <a type="button" href="${ newCategory }" class="btn btn btn-success">New</a>
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
                        <tr>
                            <th width="10%" scope="row">${ category.id }</th>
                            <td width=50%>${ category.name }</td>
                            <td width=25%>
                                <a type="button" href="${ listCategories }/${ category.id }" class="btn btn-primary">
                                    <i class="bi bi-eye"></i>
                                </a>
                                <a type="button" href="${ deleteCategory }/${ category.id }" class="btn btn-danger">
                                    <i class="bi bi-trash3"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <div class="d-flex flex-row-reverse mb20">
                    <a type="button" href="${ newCategory }" class="btn btn-success">New</a>
                </div>
            </div>
        </div>
    </c:if>
</div>
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>