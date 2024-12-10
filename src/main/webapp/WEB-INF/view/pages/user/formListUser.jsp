<%@ include file="/WEB-INF/routes/user-routes.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<div class="main">
    <form action="${ updateUser }/${user.id}" method="post">
        <div class="col-md-6">
            <div class="row ml5 mb-3 justify-center">
                <div class="avatar mr-3 align-center">
                    <c:if test="${not empty user.imgUrl and user.imgUrl ne ''}">
                        <img src="${user.imgUrl}" alt="user" class="avatar-img rounded-circle">
                    </c:if>
                    <c:if test="${empty user.imgUrl or user.imgUrl eq ''}">
                        <img src="<c:url value='/assets/avatar2.png'/>" alt="user" class="avatar-img rounded-circle">
                    </c:if>
                </div>
                <div class="col-md-9">
                    <label for="imgUrl" class="form-label">Image</label>
                    <textarea rows="6" name="imgUrl" class="form-control" minlength="5"
                              maxlength="140" id="imgUrl" placeholder="URL">${user.imgUrl}
                    </textarea>
                </div>
            </div>
            <div class="mb-3">
                <label for="inputLogin" class="form-label">E-MAIL</label> <input
                    type="email" name="login" class="form-control" id="inputLogin"
                    placeholder="E-mail" value="${ user.login }" autocomplete="email"
                    required/>
            </div>
            <div class="mb-3">
                <label for="inputPassword" class="form-label">PASSWORD</label> <input
                    type="password" name="password" class="form-control"
                    autocomplete="off"
                    id="inputPassword" placeholder="password" required/>
            </div>

            <!--  <div class="mb-3">
                <label for="inputConfirmPaassword" class="form-label">CONFIRM PASSWORD
                </label>
                <input type="password" key="confirmPassword"
                    class="form-control" resourceId="inputConfirmPaassword"
                    placeholder="password" required />
            </div>
            -->
            <c:if test="${not empty error }">
                <div class="alert alert-danger hidden-alert" role="alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <div class="row justify-content-end mr-0 mb20">
                <jsp:include page="/WEB-INF/view/components/buttons/backButton.jsp"/>
                <span>&nbsp;</span>
                <jsp:include page="/WEB-INF/view/components/buttons/saveButton.jsp"/>
            </div>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>