<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<%@ include file="/WEB-INF/routes/category-routes.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<%
    request.setAttribute("category", ((IHttpResponse<?>) request.getAttribute("response")).body());
%>

<div class="main">
    <div class="col-md-6">
        <div class="mb-3">
            <label for="inputId" class="form-label">ID</label>
            <input type="text" name="id" class="form-control col-md-3" id="inputId"
                   value="${ category.id }" readonly="readonly"/>
        </div>
        <div class="mb-3">
            <label for="inputName" class="form-label">NAME</label>
            <input type="text" name="name" class="form-control" id="inputName"
                   placeholder="name" value="${ category.name }" autocomplete="name" required minlength="4"
                   readonly="readonly"/>
        </div>

        <div class="align-end">
            <jsp:include page="/WEB-INF/view/components/buttons/customButton.jsp">
                <jsp:param name="btnLabel" value="Back"/>
                <jsp:param name="btnType" value="button"/>
                <jsp:param name="btnClass" value="btn btn-light"/>
                <jsp:param name="btnIcon" value="bi bi-arrow-left"/>
                <jsp:param name="btnOnclick" value="onclick='window.location.href=`${baseLink}${version}${ listCategory }`'"/>
                <jsp:param name="btnId" value="id='backButton'"/>
            </jsp:include>

            <span class="mr-2"></span>

            <a type="button" href="${baseLink}${version}${ editCategory }/${ category.id }" class="btn btn-success">
                Edit <i class="bi bi-pencil-square"></i>
            </a>
        </div>
    </div>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>