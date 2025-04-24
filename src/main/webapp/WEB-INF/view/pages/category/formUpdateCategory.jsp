<%@ include file="/WEB-INF/routes/category-routes.jspf" %>
<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("category", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>


<div class="main">
    <form action="${baseLink}${version}${ updateCategory }/${category.id}" method="post">
        <div class="col-md-6">
            <div class="mb-3">
                <label for="inputId" class="form-label">ID</label>
                <input type="text" name="id" class="form-control col-md-3" id="inputId"
                       value="${ category.id }" readonly="readonly"/>
            </div>

            <div class="mb-3">
                <label for="inputName" class="form-label">NAME</label>
                <input type="text" name="name" class="form-control" id="inputName"
                       placeholder="name" value="${ category.name }" autocomplete="name" required minlength="4"/>
            </div>

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