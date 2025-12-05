# 🏦 Banking System – Event-Driven Architecture with SAGA Pattern

### Equipe
- Alan Souza | https://github.com/Alan-VSouza
- Ana Livia Turazzi | https://github.com/Turazzi
- Pedro Candido | https://github.com/PeCandido

![Java](https://img.shields.io/badge/java-17+-yellow)
![Spring Boot](https://img.shields.io/badge/springboot-3.x-green)
![Kafka](https://img.shields.io/badge/kafka-3.x-red)
![PostgreSQL](https://img.shields.io/badge/postgresql-15+-blue)

Um sistema bancário simplificado que demonstra, **na prática**, como arquiteturas modernas baseadas em eventos funcionam em microsserviços.

## 🎯 Visão Geral

Este projeto implementa um **sistema de processamento de pagamentos distribuído** usando:
- **Arquitetura Baseada em Eventos com Broker** com Apache Kafka como broker
- **Event Sourcing** para auditoria completa e imutável
- **SAGA Pattern (Coreografia)** para coordenar transações entre serviços
- **Microsserviços independentes** com bancos de dados próprios

### Diagrama C4-Model nível de Container
<img width="1043" height="754" alt="container diagram" src="https://github.com/user-attachments/assets/059a2f38-9963-43e7-a64b-f2678843b485" />

## 🏗️ Arquitetura

## 📦 Services

### Core Module
- **Responsabilidade**: Definir contratos compartilhados entre os services

### Payment Service (Porta 8080)
- **Responsabilidade**: Criar e gerenciar pagamentos
- **Eventos produzidos**: `PaymentCreatedEvent`, `PaymentProcessedEvent`
- **Endpoints**:
  - `POST /payments` – Criar pagamento
  - `GET /payments` – Consultar pagamentos
  - `GET /payments/{id}` – Consultar pagamento
  - `GET /payments/{paymentId}/events` – Histórico de eventos (Event Sourcing)

### Merchant Service (Porta 8081)
- **Responsabilidade**: Gerenciar comerciantes e saldos
- **Eventos consumidos**: `PaymentCreatedEvent`
- **Eventos produzidos**: `PaymentProcessedEvent` (APPROVED/REJECTED)
- **Endpoints**:
  - `POST /merchants` – Registrar merchant
  - `GET /merchants/{merchantId}` – Consultar merchant
  - `GET /merchants/{merchantId}/balance` – Consultar balanço
  - `GET /merchants/{merchantId}/events` – Histórico de eventos
  - `GET /merchants/{merchantId}/debit` – Consultar débitos

### Notification Service (Porto 8082)

- **Responsabilidade**: Enviar notificações por e-mail
- **Eventos consumidos**: `PaymentProcessedEvent`

## 🛠️ Stack Tecnológico

| Componente | Versão | Uso |
|-----------|--------|-----|
| **Java** | 17+ | Linguagem |
| **Spring Boot** | 3.x | Framework |
| **PostgreSQL** | 15-alpine | Banco de dados |
| **Apache Kafka** | 7.5.0 | Event broker |
| **Docker** | Latest | Containerização |
| **Maven** | 3.8+ | Build |

## 📚 Documentação

### ADRs (Architecture Decision Records)
- [Documento com as ADRs](./ADRs.md)

### Diagramas C4-Model
- [Diagrama de Contexto](./C4-Model/Context.puml) – Contexto do sistema
- [Diagrama de Contêiner](./C4-Model/Container.puml) – Decomposição em contêiner dos serviços
- [Diagrama de Componentes - Payment Service](./C4-Model/Component-PaymentSVC.puml) - Decomposição em componentes do serviço de pagamento
- [Diagrama de Componentes - Merchant Service](./C4-Model/Component-MerchantSVC.puml) - Decomposição em componentes do serviço comercial
- [Diagrama de Componentes - Notification Service](./C4-Model/Component-NotificationSVC.puml) - Decomposição em componentes do serviço de notificação

## 🔑 Conceitos-chave

### Event-Driven Architecture
- Serviços comunicam-se via **eventos assíncronos**
- Baixo acoplamento: produtor não conhece consumidores
- Alta escalabilidade: cada serviço escala independentemente

### Event Sourcing
- Persistir **sequência imutável** de eventos
- Auditoria completa: possível reconstruir estado em qualquer ponto no tempo

### SAGA Pattern com Orquestração
- Coordenar **transações distribuídas** sem transação ACID global
- **Orquestrado**: Orquestrador central (PaymentSaga) que gerencia os microsserviços
- **Compensação**: se um passo falha, desfazer passos anteriores

## Rodando o sistema
## Front-End
### ✅ Pré-requisitos
- Node.js 18+  
- npm 9+ (ou yarn, se preferir)  
- Backend e serviços (Kafka, Postgres, etc.) já rodando, normalmente via `docker-compose` na raiz do projeto.

### 📦 Instalação
Dentro da pasta `frontend/`:

# Entrar na pasta do frontend
cd frontend

# Instalar dependências
npm install
ou
yarn install

## Rodando em modo desenvolvimento
npm start
ou
yarn start

- A aplicação ficará disponível em:  
  `http://localhost:3000`
  
- Certifique-se de que os services Java estejam rodando:
  - `payment-service` (porta 8081)
  - `merchant-service` (porta 8082)
  - `notification-service` (se usado)
    
- A comunicação é feita via chamadas REST, por padrão:
  - `http://localhost:8081/api/payments`
  - `http://localhost:8082/api/merchants`

## Rodando build de produção
npm run build
ou
yarn build

## Principais telas
- **Dashboard principal**
  - Lista de merchants e seus saldos
  - Formulário para criar novos pagamentos (Saga Pattern)
  - 
- **Event Broker Monitor**
  - Visualização de tópicos Kafka: `payment-created`, `payment-processed`, `merchant-events`
    
- **Event Timeline**
  - Timeline unificada de `PaymentEvent` e `MerchantEvent` (approve/reject, debit/credit)

## Back-End
### Pré-requisitos
java -version 17 ou superior
mvn -version
docker --version
docker-compose --version

### 1. Clonar o repositório

git clone https://github.com/PeCandido/BankingSystem-EventDriven-SagaPattern.git
cd BankingSystem-EventDriven-SagaPattern

### 2. Iniciar infraestrutura (Kafka, PostgreSQL)
docker-compose up -d

Verifica se está rodando:
# PostgreSQL
docker ps | grep postgres

# Kafka + Kafdrop
docker ps | grep kafka

### 3. Compilar projeto
mvn clean install

### 4. Rodar serviços
**Terminal 1 – Payment Service**:
cd payment-service
mvn spring-boot:run

**Terminal 2 – Merchant Service**:
cd merchant-service  
mvn spring-boot:run

**Terminal 3 – Notification Service**:
cd notification-service
mvn spring-boot:run
