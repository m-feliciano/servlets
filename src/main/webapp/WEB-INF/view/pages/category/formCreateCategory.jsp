<jsp:include page="/WEB-INF/view/components/header.jsp"/>
<%@ include file="/WEB-INF/routes/category-routes.jspf" %>

<div class="main">
    <form action="${baseLink}${version}${ createCategory }" method="post">
        <div class="col-md-6">
            <div class="mb-3">
                <label for="inputName" class="form-label">NAME</label>
                <input type="text" name="name" class="form-control" id="inputName" placeholder="name" required
                       minlength="4"/>
            </div>

            <jsp:include page="/WEB-INF/view/components/buttons/backButton.jsp"/>
            <span class="mr-2"></span>
            <jsp:include page="/WEB-INF/view/components/buttons/saveButton.jsp"/>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>