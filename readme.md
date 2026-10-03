# Microsserviços com Spring Cloud — Atividade AT

**Aluno:** Felipe Moraes  
**Disciplina:** Microsserviços e DevOps com Spring Boot e Spring Cloud
**Repositório:** [Felipe-Moraes_DevOps_AT](https://github.com/FelipeS19/Felipe-Moraes_DevOps_AT)

---

##  Sobre o Projeto

O foco desta trabalho foi a criação do microsserviço **`fornecedores-service`** e sua integração completa ao ecossistema já existente.

---

## Stack Tecnológica

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Java 17 (Eclipse Temurin) |
| **Framework** | Spring Boot 3.2.5 |
| **Cloud** | Spring Cloud 2023.0.1 |
| **Discovery** | Netflix Eureka Server & Client |
| **Config** | Spring Cloud Config Server (native) |
| **Gateway** | Spring Cloud Gateway (WebFlux) |
| **Comunicação** | Spring Cloud OpenFeign |
| **Segurança** | JWT (jjwt) no Gateway Filter |
| **Banco de Dados** | H2 Database (in-memory) |
| **ORM** | Spring Data JPA / Hibernate 6 |
| **Containerização** | Docker & Docker Compose |
| **CI/CD** | GitHub Actions |

---

## Arquitetura

```
                        ┌─────────────────┐
                        │   Config Server  │
                        │    :8888         │
                        └────────┬────────┘
                                 │ fornecedores-service.properties
                                 │ produtos-service.properties
                                 │ ...
┌──────────┐            ┌────────▼────────┐
│  Client  │───:8085───▶│    Gateway      │
│ (Browser)│            │   + TokenFilter  │
└──────────┘            └────────┬────────┘
                                 │ roteamento dinâmico via Eureka
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
          ┌─────────────┐ ┌───────────┐ ┌──────────────┐
          │ fornecedores │ │ produtos  │ │   clientes   │
          │   :8084      │ │  :8081    │ │    :8083     │
          └──────┬──────┘ └───────────┘ └──────────────┘
                 │ OpenFeign
                 └──────────────▶ produtos-service
                    GET /produtos

        ┌───────────────────────────────────────┐
        │          Eureka Server :8761           │
        │  (Service Discovery - todos se        │
        │   registram aqui automaticamente)      │
        └───────────────────────────────────────┘
```

---


##  Endpoints `fornecedores-service`

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `GET` | `/fornecedores` | Lista todos os fornecedores | `200 OK` |
| `GET` | `/fornecedores/{id}` | Busca fornecedor por ID | `200 OK` / `404 Not Found` |
| `POST` | `/fornecedores` | Cadastra novo fornecedor (JSON) | `201 Created` |
| `GET` | `/fornecedores/produtos` | Lista produtos via OpenFeign | `200 OK` |

### Exemplo POST `/fornecedores`

```json
{
  "nome": "Fornecedor Exemplo LTDA",
  "cnpj": "12.345.678/0001-90"
}
```

### Acesso via API Gateway (porta 8085)

Todos os endpoints acima também são acessíveis pela porta **8085** do Gateway, prefixando com o nome do serviço:

```
GET  http://localhost:8085/fornecedores-service/fornecedores
GET  http://localhost:8085/fornecedores-service/fornecedores/{id}
POST http://localhost:8085/fornecedores-service/fornecedores
GET  http://localhost:8085/fornecedores-service/fornecedores/produtos
```

---

## Configuração Centralizada (Config Server)

O `fornecedores-service` busca suas propriedades no **Config Server** durante o startup:

```properties
# config-repo/fornecedores-service.properties
server.port=8084
spring.datasource.url=jdbc:h2:mem:fornecedoresdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

O perfil `docker` sobrescreve a URL do Eureka para apontar ao hostname do container:
```properties
# config-repo/fornecedores-service-docker.properties
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/
```
 `http://localhost:8888/fornecedores-service/default`

---

## Docker

### Dockerfile (fornecedores-service)

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Subindo toda a infraestrutura

```bash
# 1. Compilar todos os projetos
mvn clean package -DskipTests

# 2. Subir todos os containers
docker compose up --build
```

### Containers e Portas

| Container | Porta | Descrição |
|-----------|-------|-----------|
| `eureka-server` | 8761 | Service Discovery |
| `config-server` | 8888 | Configuração Centralizada |
| `produtos-service` | 8081 | CRUD Produtos |
| `vendas-service` | 8082 | Registro de Vendas |
| `clientes-service` | 8083 | CRUD Clientes |
| `fornecedores-service` | 8084 | CRUD Fornecedores |
| `gateway` | 8085 | API Gateway (ponto único de entrada) |
| `auth-service` | 8086 | Autenticação JWT |

---

## Comunicação entre Serviços (OpenFeign)

O `fornecedores-service` consulta o `produtos-service` utilizando **Spring Cloud OpenFeign**:

```java
@FeignClient(name = "produtos-service")
public interface ProdutoClient {

    @GetMapping("/produtos")
    List<ProdutoDTO> buscarTodos();
}
```

O Feign resolve o endereço automaticamente via **Eureka** — não há URL hardcoded.  
Endpoint: `GET /fornecedores/produtos`

---

## CI/CD — GitHub Actions

O workflow (`.github/workflows/maven.yml`) é acionado a cada **push** e executa:

1. **Checkout** do código
2. **Setup** do JDK 17 (Temurin) com cache do Maven
3. **Build** do `fornecedores-service` com `mvn package`

```yaml
name: Build and Test Fornecedores Service
on:
  push:
    branches: [ "main", "atividade-*" ]
  pull_request:
    branches: [ "main" ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    - name: Build with Maven
      run: cd fornecedores-service && mvn -B package --file pom.xml
```

---

## Rodar Localmente 

Suba na seguinte ordem (cada um em um terminal separado):

```bash
# 1. Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. Config Server
cd config-server && mvn spring-boot:run

# 3. Produtos Service
cd produtos-service && mvn spring-boot:run

# 4. Fornecedores Service
cd fornecedores-service && mvn spring-boot:run

# 5. Gateway (opcional, para testar roteamento)
cd gateway && mvn spring-boot:run
```

---

> **Projeto base:** [brunowbbs2/api-vendas (v10)](https://github.com/brunowbbs2/api-vendas/tree/v10)
