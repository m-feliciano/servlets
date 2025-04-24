<%@ page import="com.dev.servlet.domain.transfer.response.IHttpResponse" %>
<!DOCTYPE html>

<%
    request.setAttribute("error", ((IHttpResponse<?>) request.getAttribute("response")).error());
%>

<html lang="en">
<%@ include file="/WEB-INF/fragments/head-loginform.jspf" %>
<body>
<div class="login-container">
    <h2 class="text-center">Login</h2>
    <form action="${baseLink}${version}${login}" method="post">
        <div class="mb-3">
            <label for="inputLogin" class="form-label">Email</label>
            <input type="email" id="inputLogin" name="login" class="form-control" placeholder="Email" required>
        </div>
        <div class="mb-3">
            <label for="inputPassword" class="form-label">Password</label>
            <input type="password" id="inputPassword" name="password" class="form-control" placeholder="Password"
                   required minlength="3">
        </div>
        <c:if test="${not empty error or not empty info}">
            <div class="alert ${not empty error ? 'alert-danger' : 'alert-success'}" role="alert">
                <c:out value="${error != null ? error : info}"/>
            </div>
        </c:if>
        <div class="d-grid">
            <button type="submit" class="btn btn-primary">Login</button>
        </div>
        <div class="mt-3 text-center">
            <button type="button" class="btn btn-link" onclick="window.location.href='${baseLink}${version}${registerPage}'">Sign up</button>
        </div>
    </form>
</div>
</body>
</html>