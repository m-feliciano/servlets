# 📚 Project Documentation

This folder contains comprehensive documentation for the Servlets project in both English and Portuguese.

## 📁 Folder Structure

```
docs/
├── en/                           # 🇺🇸 English Documentation
│   ├── COMPLETE_CLASS_ANALYSIS.md   # Complete analysis of all 106 classes
│   ├── RELATIONSHIP_DIAGRAMS.md     # Visual diagrams with Mermaid
│   └── PROJECT_SUMMARY.md           # Executive summary
└── pt-BR/                        # 🇧🇷 Portuguese Documentation
    ├── ANALISE_COMPLETA_CLASSES.md  # Análise completa das 106 classes
    ├── DIAGRAMAS_RELACIONAMENTOS.md # Diagramas visuais com Mermaid
    └── RESUMO_PROJETO.md             # Resumo executivo
```

## 📋 Documentation Content

### 1. **Complete Class Analysis** / **Análise Completa de Classes**
- **106 classes cataloged** with their responsibilities
- **Architectural structure** by layers (Domain, Application, Infrastructure, Adapters)
- **10+ design patterns** identified and explained
- **Complete technology stack** utilized
- **Advanced features** (multi-user cache, security, rate limiting)
- **Strengths** and improvement opportunities

### 2. **Relationship Diagrams** / **Diagramas de Relacionamentos**
- **Entity relationships** (ERD)
- **Layered architecture** with dependencies
- **Cache system** (Decorator Pattern)
- **Request flow** (Sequence Diagram)
- **Security system** and authentication
- **Web scraping architecture**
- **DTOs and Mappers**
- **Validation and custom annotations**

### 3. **Project Summary** / **Resumo do Projeto**
- **Main project characteristics**
- **Technologies used** with versions
- **Implemented functionalities**
- **Quality and testing** (53 passing tests)
- **Ideal use cases**

## 🎯 Purpose

This documentation serves as:
- 📖 **Complete technical guide** for the project
- 🏗️ **Architectural reference** for new developers
- 📊 **Foundation for decisions** on system evolution
- 🎓 **Study material** for Java EE best practices

## 🔧 Technical Details

### Architecture Highlights
- ☕ **Java 17** + Jakarta EE/CDI
- 🗄️ **Hibernate/JPA** + PostgreSQL
- 🔐 **JWT Authentication** + Role-based access
- ⚡ **EhCache** with user isolation
- 🧪 **JUnit 5** + Mockito (53 tests)
- 🕷️ **Extensible web scraping** framework

### Security & Performance
- ✅ Multi-layer JWT authentication with roles
- ✅ XSS filters and input sanitization
- ✅ Rate limiting with Leaky Bucket algorithm
- ✅ Multi-level cache with token isolation
- ✅ Efficient pagination for large volumes
- ✅ Strategic lazy loading in relationships

---

📝 **Note**: All diagrams use Mermaid syntax and can be rendered in GitHub, GitLab, or any Mermaid-compatible viewer.