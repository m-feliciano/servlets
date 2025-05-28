<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="currentPage" value="${param.currentPage != null ? param.currentPage : 1}"/>
<c:set var="totalRecords" value="${param.totalRecords}"/>
<c:set var="totalPages" value="${param.totalPages}"/>
<c:set var="pageSize" value="${param.pageSize}"/>
<c:set var="sort" value="${param.sort}"/>
<c:set var="direction" value="${param.direction}"/>
<c:set var="linkhref" value="${param.href}"/>

<div class="row w-100 text-center">
    <c:if test="${totalPages > 1}">
        <div class="col-12">
            <div class="col-md-24">
                <nav aria-label="Page navigation">
                    <ul class="pagination justify-content-center">
                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                            <a class="page-link" style="color: #343a40;" aria-label="Previous"
                                href="${linkhref}?page=${currentPage - 1}&limit=${pageSize}&sort=${sort}&order=${direction}">
                                <i class="bi bi-arrow-left"></i>
                                <span>Previous</span>
                            </a>
                        </li>

                        <c:forEach begin="${currentPage > 3 ? currentPage - 2 : 1}"
                                   end="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}"
                                   var="i">
                            <c:choose>
                                <c:when test="${currentPage == i}">
                                    <li class="page-item active" aria-current="page">
                                        <a class="page-link"
                                           style="background-color: #343a40; border-color: #343a40"
                                           disabled="true" tabindex="-1">${i}
                                        </a>
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <li class="page-item">
                                        <a class="page-link" style="color: #343a40;"
                                           href="${linkhref}?page=${i}&limit=${pageSize}&sort=${sort}&order=${direction}">${i}
                                        </a>
                                    </li>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                            <a class="page-link" style="color: #343a40;" aria-label="Next"
                               href="${linkhref}?page=${currentPage + 1}&limit=${pageSize}&sort=${sort}&order=${direction}">
                                <i class="bi bi-arrow-right"></i>
                                <span>Next</span>
                            </a>
                        </li>

                        <c:if test="${totalPages > 4}">
                            <li class="page-item" style="margin-left: 10px">
                                <form class="form-inline" action="${linkhref}" method="get">
                                    <input type="hidden" name="limit" value="${pageSize}"/>
                                    <div class="input-group">
                                        <div>
                                        <span class="input-group-text"
                                              style="background: #fff; border: none">Go to:</span>
                                        </div>
                                        <div>
                                            <label for="page" class="sr-only">Page</label>
                                            <input type="number" id="page" name="page" class="form-control"
                                                   value="${currentPage}" min="1" max="${totalPages}"
                                                   style="width: 100px;"/>
                                        </div>
                                        <button type="submit" class="btn btn-auto btn-black ml5">
                                        <span style="font-size: 14px">
                                           <i class="bi bi-arrow-right"></i>
                                        </span>
                                        </button>
                                    </div>
                                </form>
                            </li>
                        </c:if>
                        <li class="page-item nohover" style="margin-left: 20px">
                            <span class="page-link" style="color: #343a40; border: none">
                                <span>Page ${currentPage} of ${totalPages}</span>
                            </span>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>
    </c:if>
</div>