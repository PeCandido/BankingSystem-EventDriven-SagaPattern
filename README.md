# 🏦 Banking System – Event-Driven Architecture with SAGA Pattern

![Build Status](https://img.shields.io/badge/status-active-brightgreen)
![Java](https://img.shields.io/badge/java-17+-blue)
![Spring Boot](https://img.shields.io/badge/springboot-3.x-green)
![Kafka](https://img.shields.io/badge/kafka-3.x-red)

Um sistema bancário simplificado que demonstra, **na prática**, como arquiteturas modernas baseadas em eventos funcionam em microsserviços.

## 🎯 Visão Geral

Este projeto implementa um **sistema de processamento de pagamentos distribuído** usando:

- ✅ **Arquitetura Baseada em Eventos** com Kafka como broker
- ✅ **Event Sourcing** para auditoria completa e imutável
- ✅ **SAGA Pattern (Coreografia)** para coordenar transações entre serviços
- ✅ **Microsserviços independentes** com bancos de dados próprios
- ✅ **Modelo C4** para documentar a arquitetura

### 📊 Fluxo principal

```
┌─────────────┐
│   Payer     │
│  (Customer) │
└──────┬──────┘
       │ POST /payments
       ▼
┌──────────────────────────────────────────────────────────────┐
│                  BANKING SYSTEM                              │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  1. Payment Service                                 │    │
│  │  - Cria pagamento (status: PENDING)                │    │
│  │  - Publica PaymentCreatedEvent                      │    │
│  │  - Persiste em payment_events (Event Sourcing)      │    │
│  └─────────────────────────────────────────────────────┘    │
│                    │                                         │
│                    │ Kafka: payment-created                 │
│                    ▼                                         │
│  ┌──────────────────────────┐  ┌──────────────────────────┐ │
│  │ 2. Merchant Service      │  │ 3. Notification Service  │ │
│  │ - Recebe evento          │  │ - Recebe evento          │ │
│  │ - Credita saldo          │  │ - Envia e-mail           │ │
│  │ - Publica resposta       │  │ - Persiste notificação   │ │
│  │   (APPROVED/REJECTED)    │  │                          │ │
│  └──────────────────────────┘  └──────────────────────────┘ │
│                    │                                         │
│                    │ Kafka: payment-processed                │
│                    ▼                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Payment Service (atualiza status)                  │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
       │
       │ SMTP
       ▼
    ┌─────────────────┐
    │  Email Server   │
    └─────────────────┘
```

## 🏗️ Arquitetura

### Decisões arquiteturais (ADRs)

Este projeto documenta **3 decisões críticas**:

| ADR | Tema | Decisão |
|-----|------|---------|
| [ADR 1](./ADR_1_EventDriven.md) | Estilo arquitetural | **Arquitetura Baseada em Eventos com Broker (Kafka)** |
| [ADR 2](./ADR_2_EventSourcing.md) | Elemento adicional | **Event Sourcing** para auditoria e reconstrução de estado |
| [ADR 3](./ADR_3_SAGAPattern.md) | Elemento adicional | **SAGA Pattern (Coreografia)** para coordenar transações |

### Modelos C4

- **[Context.puml](./Context.puml)**: Nível 1 – Contexto de sistema
- **[Container.puml](./Container.puml)**: Nível 2 – Decomposição em microsserviços

## 📦 Componentes

### Payment Service (Porto 8080)

- **Responsabilidade**: Criar e gerenciar pagamentos
- **Banco**: PostgreSQL (tabelas: `payments`, `payment_events`)
- **Eventos produzidos**: `PaymentCreatedEvent`, `PaymentProcessedEvent` (após validação do merchant)
- **Endpoints**:
  - `POST /payments` – Criar pagamento
  - `GET /payments/{id}` – Consultar pagamento
  - `GET /payments?payerId=...` – Listar pagamentos por pagador
  - `GET /payments/{id}/events` – Histórico de eventos (Event Sourcing)

### Merchant Service (Porto 8081)

- **Responsabilidade**: Gerenciar comerciantes e saldos
- **Banco**: PostgreSQL (tabelas: `merchants`, `merchant_events`)
- **Eventos consumidos**: `PaymentCreatedEvent`
- **Eventos produzidos**: `PaymentProcessedEvent` (APPROVED/REJECTED)
- **Endpoints**:
  - `POST /merchants` – Registrar merchant
  - `GET /merchants/{id}` – Consultar merchant
  - `GET /merchants/email/{email}` – Buscar por e-mail
  - `GET /merchants/phone/{phone}` – Buscar por telefone
  - `GET /merchants/{id}/events` – Histórico de eventos

### Notification Service (Porto 8082)

- **Responsabilidade**: Enviar notificações por e-mail
- **Banco**: PostgreSQL (tabela: `notifications`)
- **Eventos consumidos**: `PaymentProcessedEvent`
- **Implementações de e-mail**:
  - `ConsoleEmailNotifier` – Fake (loga no console para testes)
  - `StmpEmailNotifier` – Real (envia via SMTP)

### Core Module (`core-common`)

- **Responsabilidade**: Definir contratos compartilhados
- **Conteúdo**:
  - `PaymentStatus` enum
  - `BaseEvent` classe abstrata
  - `PaymentCreatedEvent`, `PaymentProcessedEvent`

## 🛠️ Stack Tecnológico

| Componente | Versão | Uso |
|-----------|--------|-----|
| **Java** | 17+ | Linguagem |
| **Spring Boot** | 3.x | Framework |
| **Spring Data JPA** | - | ORM |
| **Spring Kafka** | - | Message broker |
| **PostgreSQL** | 14+ | Banco de dados |
| **Apache Kafka** | 3.x | Event broker |
| **Docker** | Latest | Containerização |
| **Maven** | 3.8+ | Build |

## 🚀 Início rápido

### Pré-requisitos

```bash
# Verificar Java
java -version  # Deve ser 17 ou superior

# Verificar Maven
mvn -version

# Verificar Docker
docker --version
docker-compose --version
```

### 1. Clonar o repositório

```bash
git clone https://github.com/PeCandido/BankingSystem-EventDriven-SagaPattern.git
cd BankingSystem-EventDriven-SagaPattern
```

### 2. Iniciar infraestrutura (Kafka, PostgreSQL)

```bash
docker-compose up -d
```

Verifica se está rodando:

```bash
# PostgreSQL
docker ps | grep postgres

# Kafka
docker ps | grep kafka

# Verificar se DB foi criado
docker exec -it bank-postgres psql -U bank_user -d bank_db -c "\dt"
```

### 3. Compilar projeto

```bash
mvn clean install
```

### 4. Rodar serviços

**Terminal 1 – Payment Service**:

```bash
cd payment-service
mvn spring-boot:run
# Ou rodar via IDE/IDE Run Configuration
```

**Terminal 2 – Merchant Service**:

```bash
cd merchant-service
mvn spring-boot:run
```

**Terminal 3 – Notification Service**:

```bash
cd notification-service
mvn spring-boot:run
```

Verifique os logs para confirmar que subiram sem erros.

### 5. Testar sistema

Veja [DEMO_INSTRUCTIONS.md](./DEMO_INSTRUCTIONS.md) para script completo.

Resumo rápido:

```bash
# Criar merchant
curl -X POST http://localhost:8081/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Store",
    "email": "store@example.com",
    "phone": "+55 11 98765-4321",
    "initialBalance": 5000.00,
    "currency": "BRL"
  }'
# Salve o merchantId retornado

# Criar pagamento
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "payerId": "00000000-0000-0000-0000-000000000001",
    "payerEmail": "customer@example.com",
    "payeeId": "MERCHANT_ID_AQUI",
    "amount": 350.00,
    "currency": "BRL"
  }'
# Salve o paymentId retornado

# Consultar histórico de eventos do pagamento
curl http://localhost:8080/payments/PAYMENT_ID_AQUI/events

# Consultar saldo atualizado do merchant
curl http://localhost:8081/merchants/MERCHANT_ID_AQUI

# Consultar histórico de eventos do merchant
curl http://localhost:8081/merchants/MERCHANT_ID_AQUI/events

# Visualizar mensagens Kafka
# Abra http://localhost:9000 (Kafdrop)
```

## 📚 Documentação

### ADRs (Architecture Decision Records)

- [ADR 1: Arquitetura Baseada em Eventos com Kafka](./ADR_1_EventDriven.md)
- [ADR 2: Event Sourcing](./ADR_2_EventSourcing.md)
- [ADR 3: SAGA Pattern com Coreografia](./ADR_3_SAGAPattern.md)

### Diagramas

- [Context.puml](./Context.puml) – Contexto do sistema
- [Container.puml](./Container.puml) – Decomposição em componentes

### Executar demo

- [DEMO_INSTRUCTIONS.md](./DEMO_INSTRUCTIONS.md) – Script passo a passo

## 🔑 Conceitos-chave

### Event-Driven Architecture

- Serviços comunicam-se via **eventos assíncronos**
- Baixo acoplamento: produtor não conhece consumidores
- Alta escalabilidade: cada serviço escala independentemente

### Event Sourcing

- Persistir **sequência imutável** de eventos
- Auditoria completa: possível reconstruir estado em qualquer ponto no tempo
- Conformidade regulatória (importante em fintech)

### SAGA Pattern

- Coordenar **transações distribuídas** sem transação ACID global
- **Coreografia**: cada serviço reage a eventos (sem orquestrador central)
- **Compensação**: se um passo falha, desfazer passos anteriores

### Eventual Consistency

- Estado não é sincronizado instantaneamente
- Saldos podem estar "levemente desatualizados"
- Totalmente sincronizado após propagação de eventos

## 🧪 Testes

```bash
# Rodar testes unitários
mvn test

# Rodar testes de integração (requer Docker)
mvn verify
```

## 📋 Checklist de apresentação

- [x] Arquitetura documentada (ADRs)
- [x] Diagramas C4 (Context + Container)
- [x] Código funcional (3 microsserviços)
- [x] Event Sourcing implementado
- [x] SAGA coreografado implementado
- [x] Demo em tempo real possível
- [ ] Testes automatizados (future)
- [ ] Observabilidade completa (Jaeger, Prometheus) – future
- [ ] Dead Letter Queues para tratamento de erros – future
- [ ] Compensações automáticas – future

## 👥 Equipe

- **Integrante 1**: [Nome]
- **Integrante 2**: [Nome]
- **Integrante 3**: [Nome]

## 📄 Licença

Este projeto foi desenvolvido como trabalho prático da disciplina **Arquitetura de Software** (Prof. Dr. Lucas Oliveira) do **IFSP – Câmpus São Carlos**.

## 🤝 Contribuindo

Este é um projeto educacional. Sugestões e melhorias são bem-vindas!

## 📞 Suporte

Para dúvidas ou issues:

1. Abra uma issue no GitHub
2. Consulte a documentação (ADRs, C4 Model)
3. Verifique [DEMO_INSTRUCTIONS.md](./DEMO_INSTRUCTIONS.md) para troubleshooting

---

**Última atualização**: Dezembro 2025

