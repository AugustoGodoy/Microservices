# Microserviços com Spring Boot

Este projeto implementa uma arquitetura de microserviços com Spring Boot, incluindo comunicação com APIs externas, Circuit Breaker, Cache e resiliência.

## Arquitetura

### Serviços Implementados:

1. **Config Service** (Porta 8888) - Servidor de configuração
2. **Discovery Service** (Porta 8761) - Eureka Server
3. **Currency Service** (Porta 8100) - Serviço de cotações com integração BCB
4. **Product Service** (Porta 8000) - Serviço de produtos
5. **Greeting Service** (Porta 8200) - Serviço de saudação

## Funcionalidades Implementadas

### Currency Service:
- Integração com API do Banco Central do Brasil (BCB)
- Circuit Breaker com fallback para banco local
- Cache com Caffeine para otimização
- Campo `environment` indicando origem da cotação (API BCB, Local Database, Cache)

### Product Service:
- Circuit Breaker na comunicação com Currency Service
- Cache para conversões de produtos
- Fallback com `convertedPrice = -1` quando Currency Service indisponível

## Tecnologias Utilizadas

- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **OpenFeign** - Comunicação entre serviços
- **Resilience4j** - Circuit Breaker
- **Caffeine** - Cache
- **Eureka** - Service Discovery
- **PostgreSQL** - Banco de dados
- **Flyway** - Migração de banco

## Pré-requisitos

- Java 21
- Maven 3.6+
- PostgreSQL
- Git

## Como Executar

### 1. Configurar Banco de Dados

Crie os bancos de dados:
```sql
CREATE DATABASE db_currency;
CREATE DATABASE db_product;
```

### 2. Executar os Serviços (em ordem)

#### 1. Config Service
```bash
cd microservices/config-service
./mvnw spring-boot:run
```

#### 2. Discovery Service
```bash
cd microservices/discovery-service
./mvnw spring-boot:run
```

#### 3. Currency Service
```bash
cd microservices/currency-service
./mvnw spring-boot:run
```

#### 4. Product Service
```bash
cd microservices/product-service
./mvnw spring-boot:run
```

#### 5. Greeting Service
```bash
cd microservices/greeting-service
./mvnw spring-boot:run
```

## Testando a Aplicação

### 1. Testar Currency Service
```bash
curl "http://localhost:8100/currency/100/USD/BRL"
curl "http://localhost:8100/currency/50/EUR/BRL"
```

### 2. Testar Product Service
```bash
curl "http://localhost:8000/products/1/BRL"
curl "http://localhost:8000/products/2/USD"
```

### 3. Verificar Eureka Dashboard
Acesse: http://localhost:8761

## Monitoramento

### Actuator Endpoints
- **Currency Service**: http://localhost:8100/actuator
- **Product Service**: http://localhost:8000/actuator

### Health Checks
- **Currency Service**: http://localhost:8100/actuator/health
- **Product Service**: http://localhost:8000/actuator/health

## Configurações

### Circuit Breaker
- **Failure Rate Threshold**: 50%
- **Wait Duration**: 30s
- **Sliding Window**: 10 calls
- **Minimum Calls**: 5

### Cache
- **Currency Service**: 30 minutos, máximo 100 entradas
- **Product Service**: 15 minutos, máximo 100 entradas

## Observações

1. **API BCB**: O serviço tenta buscar cotações da API do Banco Central do Brasil
2. **Fallback**: Em caso de falha, utiliza dados do banco local
3. **Cache**: Reduz chamadas repetidas para APIs externas
4. **Resiliência**: Sistema continua funcionando mesmo com falhas parciais
