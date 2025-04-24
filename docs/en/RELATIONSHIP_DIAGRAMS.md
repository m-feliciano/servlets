# Class Relationship Diagrams

## 1. Domain Entities Diagram

```mermaid
erDiagram
    User ||--o{ Product : owns
    User ||--o{ Category : owns
    User ||--o{ Inventory : owns
    Product }o--|| Category : belongs_to
    Product ||--o{ Inventory : has_stock
    User ||--|| Credentials : embeds
    User ||--o{ UserPerfis : has_roles

    User {
        Long id PK
        String status
        String imgUrl
        String config
        String token
        List-Long perfis
    }
    
    Credentials {
        String login UK
        String password
    }
    
    Product {
        Long id PK
        String name
        String description
        String url
        Date registerDate
        BigDecimal price
        String status
        Long user_id FK
        Long category_id FK
    }
    
    Category {
        Long id PK
        String name
        String status
        Long user_id FK
    }
    
    Inventory {
        Long id PK
        Integer quantity
        String description
        String status
        Long product_id FK
        Long user_id FK
    }
```

## 2. Layered Architecture Diagram

```mermaid
graph TB
    subgraph "Presentation Layer"
        A[Controllers]
        A1[BaseController]
        A2[ProductController]
        A3[UserController]
        A4[LoginController]
        A5[CategoryController]
    end
    
    subgraph "Application Layer"
        B[Services]
        B1[IProductService]
        B2[ProductServiceImpl]
        B3[ProductServiceProxyImpl]
        B4[IUserService]
        B5[ICategoryService]
    end
    
    subgraph "Domain Layer"
        C[Domain Models]
        C1[Product]
        C2[User]
        C3[Category]
        C4[Inventory]
        D[Repository Interfaces]
        D1[ICrudRepository]
        D2[IPagination]
    end
    
    subgraph "Infrastructure Layer"
        E[Persistence]
        E1[BaseDAO]
        E2[ProductDAO]
        E3[UserDAO]
        F[Security]
        F1[AuthFilter]
        F2[XSSFilter]
        G[External Services]
        G1[WebScrapeService]
    end
    
    A --> B
    B --> C
    B --> D
    E --> D
    F --> A
    G --> B
```

## 3. Cache System Diagram (Decorator Pattern)

```mermaid
classDiagram
    class ICrudRepository~T,ID~ {
        <<interface>>
        +findById(ID) T
        +findAll(T) Collection~T~
        +save(T) T
        +update(T) T
        +delete(T) boolean
    }
    
    class ProductDAO {
        +findById(ID) T
        +findAll(T) Collection~T~
        +save(T) T
        +update(T) T
        +delete(T) boolean
    }
    
    class CachedServiceDecorator~T,ID~ {
        -ICrudRepository~T,ID~ decorated
        -String cacheKeyPrefix
        -String cacheToken
        +findById(ID) T
        +findAll(T) Collection~T~
        +save(T) T
        +update(T) T
        +delete(T) boolean
        +invalidateCache() void
    }
    
    class CacheUtils {
        +getObject(String key, String token) T
        +setObject(String key, String token, T object) void
        +invalidate(String key, String token) void
        +cleanupUnusedCaches() void
    }
    
    ICrudRepository <|.. ProductDAO
    ICrudRepository <|.. CachedServiceDecorator
    CachedServiceDecorator o-- ICrudRepository
    CachedServiceDecorator --> CacheUtils
```

## 4. Request Flow Diagram

```mermaid
sequenceDiagram
    participant Client
    participant AuthFilter
    participant XSSFilter
    participant ServletDispatcher
    participant BaseController
    participant ProductController
    participant ProductServiceProxy
    participant CachedDecorator
    participant ProductService
    participant ProductDAO
    participant Database
    
    Client->>AuthFilter: HTTP Request
    AuthFilter->>AuthFilter: Validate JWT Token
    AuthFilter->>XSSFilter: Authorized Request
    XSSFilter->>XSSFilter: Sanitize Input
    XSSFilter->>ServletDispatcher: Clean Request
    ServletDispatcher->>BaseController: Route Request
    BaseController->>BaseController: Validate & Parse
    BaseController->>ProductController: Execute Method
    ProductController->>ProductServiceProxy: Business Call
    ProductServiceProxy->>CachedDecorator: Decorated Call
    CachedDecorator->>CachedDecorator: Check Cache
    alt Cache Miss
        CachedDecorator->>ProductService: Fetch from Service
        ProductService->>ProductDAO: Data Access
        ProductDAO->>Database: SQL Query
        Database-->>ProductDAO: ResultSet
        ProductDAO-->>ProductService: Entity
        ProductService-->>CachedDecorator: Result
        CachedDecorator->>CachedDecorator: Store in Cache
    else Cache Hit
        CachedDecorator->>CachedDecorator: Return Cached Data
    end
    CachedDecorator-->>ProductServiceProxy: Data
    ProductServiceProxy-->>ProductController: DTO
    ProductController-->>BaseController: Response
    BaseController-->>ServletDispatcher: HTTP Response
    ServletDispatcher-->>XSSFilter: Response
    XSSFilter-->>AuthFilter: Response
    AuthFilter-->>Client: HTTP Response
```

## 5. Security System Diagram

```mermaid
graph TD
    A[HTTP Request] --> B[AuthFilter]
    B --> C{Valid JWT Token?}
    C -->|No| D{Pre-authorized Path?}
    D -->|No| E[Redirect to Login]
    D -->|Yes| F[XSSFilter]
    C -->|Yes| F[XSSFilter]
    F --> G[XSSRequestWrapper]
    G --> H[Input Sanitization]
    H --> I[ServletDispatcher]
    I --> J[Rate Limiter]
    J --> K{Rate Limit OK?}
    K -->|No| L[429 Too Many Requests]
    K -->|Yes| M[Controller]
    
    subgraph "Security Components"
        N[CryptoUtils]
        O[SecurityRequestWrapper]
        P[PasswordEncryptFilter]
    end
    
    B --> N
    G --> O
    F --> P
```

## 6. Web Scraping Architecture Diagram

```mermaid
classDiagram
    class IWebScrapeService~TResponse~ {
        <<interface>>
        +scrape(WebScrapeRequest) Optional~TResponse~
    }
    
    class WebScrapeService {
        +scrape(WebScrapeRequest) Optional~TResponse~
    }
    
    class WebScrapeServiceRegistry {
        +register(String, IWebScrapeService) void
        +getService(String) IWebScrapeService
        +getAllServices() Map
    }
    
    class ProductWebScrapeApiClient {
        +scrape(WebScrapeRequest) Optional~WebScrapingResponse~
        -buildRequest(WebScrapeRequest) OkHttpRequest
        -parseResponse(Response) WebScrapingResponse
    }
    
    class ScrapeApiClient {
        #OkHttpClient client
        +execute(OkHttpRequest) Response
    }
    
    class WebScrapeBuilder {
        +fromUrl(String) WebScrapeRequest
        +withHeaders(Map) WebScrapeRequest
        +build() WebScrapeRequest
    }
    
    IWebScrapeService <|.. WebScrapeService
    IWebScrapeService <|.. ProductWebScrapeApiClient
    WebScrapeServiceRegistry o-- IWebScrapeService
    ProductWebScrapeApiClient --|> ScrapeApiClient
    WebScrapeBuilder --> WebScrapeRequest
```

## 7. DTOs and Mappers Diagram

```mermaid
classDiagram
    class DataTransferObject~ID~ {
        <<abstract>>
        +getId() ID
        +setId(ID) void
    }
    
    class ProductDTO {
        -Long id
        -String name
        -String description
        -String url
        -Date registerDate
        -BigDecimal price
        -UserDTO user
        -String status
        -CategoryDTO category
    }
    
    class UserDTO {
        -Long id
        -String status
        -String imgUrl
        -String config
        -List~Long~ perfis
    }
    
    class CategoryDTO {
        -Long id
        -String name
        -String status
        -UserDTO user
    }
    
    class Mapper~E,D~ {
        <<interface>>
        +toDTO(E entity) D
        +toEntity(D dto) E
        +toDTO(Collection~E~) Collection~D~
        +toEntity(Collection~D~) Collection~E~
    }
    
    class ProductMapper {
        +toDTO(Product) ProductDTO
        +toEntity(ProductDTO) Product
        +toDTO(Collection~Product~) Collection~ProductDTO~
        +toEntity(Collection~ProductDTO~) Collection~Product~
    }
    
    DataTransferObject <|-- ProductDTO
    DataTransferObject <|-- UserDTO
    DataTransferObject <|-- CategoryDTO
    Mapper <|.. ProductMapper
    ProductMapper --> ProductDTO
    ProductMapper --> Product
```

## 8. Validation and Annotations Diagram

```mermaid
graph TD
    A["@RequestMapping"] --> B[BaseRouterController]
    B --> C[Method Resolution]
    C --> D["@Validator Array"]
    D --> E[RequestValidator]
    E --> F["@Constraints"]
    F --> G[ConstraintValidator]
    G --> H{Validation OK?}
    H -->|Yes| I[Execute Method]
    H -->|No| J[ValidationException]
    
    subgraph "Custom Annotations"
        K["@Controller"]
        L["@Property"]
        M["@Validator"]
        N["@Constraints"]
        A --> K
        D --> M
        F --> N
    end
    
    subgraph "Validation Framework"
        O[RequestValidator]
        P[ConstraintValidator]
        Q[ValidationResult]
        E --> O
        G --> P
    end
```

## 9. Rate Limiting Diagram

```mermaid
stateDiagram-v2
    [*] --> CheckBucket: Incoming Request
    CheckBucket --> HasTokens: Check Available Tokens
    HasTokens --> AllowRequest: Tokens Available
    HasTokens --> RejectRequest: No Tokens
    AllowRequest --> ConsumeToken: Process Request
    ConsumeToken --> LeakTokens: Background Process
    LeakTokens --> [*]: Continue
    RejectRequest --> [*]: 429 Too Many Requests
    
    note right of LeakTokens
        Leaky Bucket Algorithm:
        - Fixed leak rate
        - Configurable bucket size
        - Smooth traffic shaping
    end note
```

## 10. Configuration and Dependency Injection Diagram

```mermaid
graph TD
    A[CDI Container] --> B[EntityManagerProducer]
    B --> C["@Produces EntityManager"]
    C --> D[BaseDAO Injection]
    
    A --> E["@Named Beans"]
    E --> F[Service Implementations]
    F --> G[Controller Injection]
    
    A --> H[Configuration]
    H --> I[PropertiesUtil]
    I --> J["@Property Injection"]
    
    subgraph "Bean Scopes"
        K["@Singleton"]
        L["@ApplicationScoped"]
        M["@RequestScoped"]
        E --> K
        E --> L
        E --> M
    end
```

These diagrams show the main relationships and flows between system classes, demonstrating how Clean Architecture and design patterns work together to create a robust and well-structured application.