<%@ include file="/WEB-INF/routes/user-routes.jspf" %>
<%@ include file="/WEB-INF/routes/auth-routes.jspf" %>

<!DOCTYPE html>
<html lang="en">
<%@ include file="/WEB-INF/fragments/head-loginform.jspf" %>
<body>
<div class="login-container">
    <h2 class="text-center">Sign up</h2>
    <form action="${baseLink}${version}${registerUser}" method="post">
        <div class="mb-3">
            <label for="inputLogin" class="form-label">Email</label>
            <input type="text" id="inputLogin" name="login" value="${email}" class="form-control" placeholder="Email"
                   required>
        </div>
        <div class="mb-3">
            <label for="inputPassword" class="form-label">Password</label>
            <input type="password" id="inputPassword" name="password" class="form-control" placeholder="Password"
                   required minlength="6">
        </div>
        <div class="mb-3">
            <label for="inputConfirmPassword" class="form-label">Confirm password</label>
            <input type="password" id="inputConfirmPassword" name="confirmPassword" class="form-control"
                   placeholder="Confirm" required
            <%--                                                                       pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}"--%>
                   minlength="6">
        </div>
        <c:if test="${not empty error or not empty info}">
            <div class="alert ${not empty error ? 'alert-danger' : 'alert-success'}" role="alert">
                <c:out value="${error != null ? error : info}"/>
            </div>
        </c:if>
        <div class="d-grid text-center">
            <button type="submit" class="btn btn-primary">Sign up</button>
        </div>
        <div class="mt-3 text-center">
            <button type="button" class="btn btn-link" onclick="window.location.href='${baseLink}${version}${loginPage}'">Login</button>
        </div>
    </form>
</div>
</body>
</html>