<%@ include file="/WEB-INF/jspf/common-imports.jspf" %>
<jsp:include page="/WEB-INF/view/components/header.jsp"/>

<div class="main">
    <form>
        <div class="col-md-6">
            <div class="mb-3">
                <label for="inputItemId" class="form-label">ID</label>
                <input type="number" name="id" class="form-control" id="inputItemId"
                       placeholder="ID" value="${ item.id }" readonly="readonly" required minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputProductId" class="form-label">PRODUCT ID</label>
                <input type="number" name="productId" class="form-control" id="inputProductId"
                       placeholder="ID" value="${ item.getProduct().getId() }" readonly="readonly" required
                       minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputQuantity" class="form-label">QUANTITY</label>
                <input type="number" name="quantity" class="form-control" id="inputQuantity"
                       placeholder="quantity" value="${ item.quantity }" readonly="readonly" required minlength="1"/>
            </div>
            <div class="mb-3">
                <label for="inputDescription" class="form-label">DESCRIPTION</label>
                <textarea name="description" class="form-control" id="inputDescription"
                          placeholder="descripton" required readonly
                          rows="4" minlength="4">${ item.description }</textarea>
            </div>

            <!-- action -->
            <a type="button" href="${ editItem }/${ item.id }" class="btn btn-success">
                Edit <i class="bi bi-pencil-square"></i>
            </a>
            <a type="button" href="${ listInventories }" class="btn btn-light">
                Go back
            </a>
        </div>
    </form>
</div>
<!-- footer -->
<jsp:include page="/WEB-INF/view/components/footer.jsp"/>