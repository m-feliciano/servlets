<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="java.util.*,entities.Inventory"%>

<c:url value="/inventory?action=ListItem" var="listItem" />
<c:url value="/inventory?action=DeleteItem" var="deleteItem" />
<c:url value="/inventory?action=NewItem" var="newItem" />
<c:url value="/product?action=ListProduct" var="listProduct" />
<c:url value="/category?action=ListCategory" var="listCategory" />
<c:url value="/inventory?action=ListItem" var="inventoryListItemServlet" />
<c:url value="/inventory?action=ListItemsByDescription" var="listItemsByDescription" />

<fmt:setLocale value="pt-BR" scope="application"/>

<jsp:include page="../../components/header.jsp" />
<div class="main">
	<c:if test="${ empty items }">
		<p>No one new item created.</p>
	</c:if>
	<form class="form-inline d-flex flex-row-reverse mb-2" action="${ listItemsByDescription }" method="post">	
		<div class="mb-3">
			<input type="text" name="description" class="form-control" id="inputSearchItem" 
				placeholder="simple description" required minlength="1" />
			<input type="hidden" name="action" value="ListItemsByDescription">
			<button type="submit" class="btn btn-primary">Search</button>
			<a type="button" 
				href="${ listItems }?action=ListItems" 
				class="btn btn-light">Clean</a>
		</div>
	</form>
	<c:if test="${ not empty items }">
		<div class="row">
			<div class="col-12">
				<table class="table table-striped table-bordered table-hover mb-0">
					<caption class="pb-0">${items.size()} records found</caption>
					<thead class="thead-dark">
						<tr>
							<th scope="col">#</th>
							<th scope="col">PRODUCT</th>
							<th scope="col">PRODUCT NAME</th>
							<th scope="col">CATEGORY</th>
							<th scope="col">CATEGORY NAME</th>
							<th scope="col">QUANTITY</th>
							<th scope="col">DESCRIPTION</th>
							<th scope="col"></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${ items }" var="item">	
							<tr>
								<th width="5%" scope="row">${ item.id }</th>
								<td width="5%">${ item.productId }</td>
								<td width="20%">
									<a style="text-decoration: none; color: inherit;  padding: 2rem 0;" 
										href="${ listProduct }&id=${ item.productId }">${ item.productName }</a>
								</td>
								<td width="10%">${ item.categoryId }</td>
								<td width="15%">
									<a style="text-decoration: none; color: inherit; padding: 2rem 0;" 
										href="${ listCategory }&id=${ item.categoryId }">${ item.categoryName }</a>
								</td>
								<td width="10%">${ item.quantity }</td>
								<td width="15%">${ item.description }</td>
								<td width="15%">
									<a type="button" href="${ listItem }&id=${ item.id }" 
										class="btn btn-primary">
										<i class="bi bi-eye"></i>
									</a>
									<a type="button" href="${ deleteItem }&id=${ item.id }"
										class="btn btn-danger">
										<i class="bi bi-trash3"></i>
									</a>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				<div class="d-flex flex-row-reverse mt-0">
					<a type="button" href="${ newItem }" class="btn btn-success">New</a>	
				</div>
			</div>
		</div>
	</c:if>	
</div>
<jsp:include page="../../components/footer.jsp" />