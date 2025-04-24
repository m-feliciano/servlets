<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<footer id="footer" class="text-center text-lg-start bg-light text-muted">
    <!-- Copyright -->
        <div id="copyright" class="text-center">
            <a class="text-dark" href="https://github.com/m-feliciano" target="_blank">
                <img src="<c:url value='/resources/assets/github.svg'/>"
                     width="30" height="30" class="d-inline-block align-top" alt="Github logo">
            </a>

            <c:if test="${not empty systemVersion}">
                <span class="float-right">
                    <span class="text-muted text-center">Version: <c:out value="${systemVersion}"/></span>
                </span>
            </c:if>
        </div>
</footer>
</div>

<script src="<c:url value='/resources/js/backButton.js'/>"></script>
</body>
</html>
