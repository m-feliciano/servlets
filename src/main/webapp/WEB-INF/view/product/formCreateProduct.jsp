<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:url value="/product" var="linkProductServlet" />
<!-- header -->
<jsp:include page="../components/header.jsp" />
<div class="main">
	<form action="${ linkProductServlet }" method="post">
		<div class="col-md-6">
			<div class="mb-3">
				<label for="inputName" class="form-label">Product name</label> 
				<input type="text" name="name" class="form-control" id="inputName" placeholder="name" required minlength="4" />
			</div>
			<div class="mb-3">
				<label for="inputDescription" class="form-label">Description</label> 
				<input type="text" name="description" class="form-control" id="inputDescription" placeholder="simple Description" required />
			</div>
			<div class="mb-3">
				<label for="inputPrice" class="form-label">Product Price</label> 
				<input name="price" class="form-control" id="inputPrice" 
					placeholder="1000,00" min="0" max="10000" step="any" 
					pattern="^\s*(?:[1-9]\d{0,2}(?:\,\d{3})*|0)(?:.\d{1,2})?$"
					title="Currency should only contain numbers and (comma/doc) e.g. 1000,00"
					required />
			</div>
			<!-- action -->
			<input type="hidden" name="action" value="CreateProduct">
			<button type="submit" class="btn btn-primary">Submit</button>
			<a type="button" href="${ linkProductServlet }?action=ListProducts" class="btn btn-light">
				Go back
			</a>
		</div>
	</form>
</div>
<!-- footer -->
<jsp:include page="../components/footer.jsp" />