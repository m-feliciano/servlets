<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:url value="/category" var="categoryServlet" />
<fmt:setLocale value="pt-BR" scope="application"/>

<!-- header -->
<jsp:include page="../components/header.jsp" />
<div class="main">
	<form action="${ categoryServlet }" method="post">
		<div class="col-md-6">
			<div class="mb-3">
				<label for="inputId" class="form-label">Product ID</label> 
				<input type="text" name="id" class="form-control col-md-3" id="inputId"
					value="${ category.id }" readonly="readonly" />
			</div>
			
			<div class="mb-3">
				<label for="inputName" class="form-label">Category name</label> 
				<input type="text" name="name" class="form-control" id="inputName"
					placeholder="name" value="${ category.name }" autocomplete="name" required minlength="4" />
			</div>
			
			<!-- action -->
			<input type="hidden" name="action" value="UpdateCategory">
			<button type="submit" class="btn btn-primary">Submit</button>
			<a type="button" href="${ categoryServlet }?action=ListCategory&id=${category.id }" class="btn btn-light">
				Go back
			</a>
		</div>
	</form>
</div>
<!-- footer -->
<jsp:include page="../components/footer.jsp" />