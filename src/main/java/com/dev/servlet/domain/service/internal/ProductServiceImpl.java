package com.dev.servlet.domain.service.internal;
import com.dev.servlet.domain.service.IBusinessService;
import com.dev.servlet.domain.transfer.dto.ProductDTO;
import com.dev.servlet.domain.transfer.records.Query;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.core.exception.ServiceException;
import com.dev.servlet.core.mapper.ProductMapper;
import com.dev.servlet.domain.model.Category;
import com.dev.servlet.domain.model.Inventory;
import com.dev.servlet.domain.model.Product;
import com.dev.servlet.domain.model.User;
import com.dev.servlet.domain.model.enums.Status;
import com.dev.servlet.domain.service.IProductService;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeServiceRegistry;
import com.dev.servlet.infrastructure.external.webscrape.builder.WebScrapeBuilder;
import com.dev.servlet.infrastructure.external.webscrape.transfer.ProductWebScrapeDTO;
import com.dev.servlet.infrastructure.persistence.dao.ProductDAO;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static com.dev.servlet.core.util.CryptoUtils.getUser;
import static com.dev.servlet.core.util.ThrowableUtils.throwServiceError;

@Slf4j
@NoArgsConstructor
@Model
@Named("productService")
public class ProductServiceImpl extends BaseServiceImpl<Product, Long> implements IProductService {
    @Inject
    private IBusinessService businessService;
    @Inject
    private WebScrapeServiceRegistry webScrapeServiceRegistry;

    @Inject
    public ProductServiceImpl(ProductDAO dao) {
        super(dao);
    }

    public ProductDAO getDAO() {
        return (ProductDAO) super.getBaseDAO();
    }

    @Override
    public Class<ProductDTO> getDataMapper() {
        return ProductDTO.class;
    }

    @Override
    public Product toEntity(Object object) {
        return ProductMapper.full((ProductDTO) object);
    }

    @Override
    public Product getEntity(Request request) {
        Product product = requestBody(request.getBody());
        product = Optional.ofNullable(product).orElse(new Product());
        String categoryId = request.getParameter("category");
        if (categoryId != null && !categoryId.isBlank()) {
            product.setCategory(new Category(Long.valueOf(categoryId)));
        }
        Query query = request.getQuery();
        if (query.getSearch() != null && query.getType() != null) {
            if (query.getType().equals("name")) {
                product.setName(query.getSearch());
            } else if (query.getType().equals("description")) {
                product.setDescription(query.getSearch());
            }
        }
        product.setUser(getUser(request.getToken()));
        return product;
    }

    @Override
    public ProductDTO create(Request request) {
        log.trace("");
        Product product = this.getEntity(request);
        product.setRegisterDate(new Date());
        product.setStatus(Status.ACTIVE.getValue());
        product = super.save(product);
        return ProductMapper.full(product);
    }

    @Override
    public List<Product> save(List<Product> products, String authorization) throws ServiceException {
        return baseDAO.save(products);
    }

    @Override
    public ProductDTO findById(Request request) throws ServiceException {
        log.trace("");
        Product filter = getEntity(request);
        Product product = this.findById(filter);
        return ProductMapper.full(product);
    }

    @Override
    public ProductDTO update(Request request) throws ServiceException {
        log.trace("");
        Product productRequest = getEntity(request);
        Product product = this.findById(productRequest);
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setUrl(productRequest.getUrl());
        product.setCategory(productRequest.getCategory());
        super.update(product);
        return ProductMapper.full(product);
    }

    @Override
    public boolean delete(Request request) throws ServiceException {
        log.trace("");
        Product productRequest = this.getEntity(request);
        Product product = this.findById(productRequest);
        Inventory inventory = new Inventory();
        inventory.setUser(productRequest.getUser());
        inventory.setProduct(product);
        if (businessService.hasInventory(inventory)) {
            throw new ServiceException(HttpServletResponse.SC_CONFLICT, "Product has inventory.");
        }
        super.delete(product);
        return true;
    }

    private Product findById(Product filter) throws ServiceException {
        Product product = super.find(filter);
        if (product == null) {
            throwServiceError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
        return product;
    }

    @Override
    public BigDecimal calculateTotalPriceFor(Product product) {
        return this.getDAO().calculateTotalPriceFor(product);
    }

    @Override
    @SneakyThrows
    public Optional<List<ProductDTO>> scrape(Request request, String url) {
        Optional<List<ProductWebScrapeDTO>> scrapeResponse = WebScrapeBuilder.<List<ProductWebScrapeDTO>>create()
                .withServiceType("product")
                .withUrl(url)
                .withRegistry(webScrapeServiceRegistry)
                .execute();
        if (scrapeResponse.isEmpty()) {
            log.warn("No products found in the web scrape response");
            return Optional.empty();
        }
        List<ProductWebScrapeDTO> res = scrapeResponse.get();
        log.info("Web scrape returned {} products", res.size());
        User user = getUser(request.getToken());
        List<Product> products = res.stream()
                .map(ProductMapper::fromWebScrapeDTO)
                .map(product -> prepareProductToSave(product, user))
                .toList();
        List<ProductDTO> response = new ArrayList<>();
        try {
            products = save(products, request.getToken());
            response.addAll(
                    products.stream()
                            .map(ProductMapper::full)
                            .toList());
        } catch (Exception e) {
            log.error("Error saving scraped products: {}", e.getMessage(), e);
        }
        return Optional.of(response);
    }

    private static Product prepareProductToSave(Product product, User user) {
        Date now = new Date();
        product.setRegisterDate(now);
        product.setStatus(Status.ACTIVE.getValue());
        product.setUser(user);
        return product;
    }
}
