# 📋 Demo Instructions – Banking System Event-Driven

**Duração estimada**: 5-10 minutos  
**Objetivo**: Demonstrar o fluxo completo de um pagamento e como Event Sourcing + SAGA Pattern funcionam na prática.

## 🔧 Preparação (antes da demo)

### 1. Verificar se infraestrutura está rodando

```bash
# Verificar containers
docker ps

# Deve ver:
# - bank-postgres (PostgreSQL)
# - kafka
# - zookeeper
# - kafdrop (UI Kafka)
```

### 2. Verificar se serviços estão rodando

```bash
# Terminal com payment-service (porta 8080)
curl http://localhost:8080/actuator/health

# Terminal com merchant-service (porta 8081)
curl http://localhost:8081/actuator/health

# Terminal com notification-service (porta 8082)
curl http://localhost:8082/actuator/health

# Todos devem responder com {"status":"UP"}
```

### 3. Preparar Kafdrop

- Abra **http://localhost:9000** em outro abadigo/aba do navegador
- Deixe aberta para acompanhar mensagens Kafka em tempo real durante a demo

---

## 📍 Script de Demo (5 minutos)

### Passo 1: Criar um Merchant (1 minuto)

**O que fazer**: Criar um novo comerciante no sistema.

**Explique**: "Começamos registrando um merchant (lojista) que receberá pagamentos. Note que cada merchant tem um saldo inicial e moeda."

**Comando**:

```bash
curl -X POST http://localhost:8081/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Electronics Store",
    "email": "store@techelectronics.com",
    "phone": "+55 11 99999-8888",
    "initialBalance": 5000.00,
    "currency": "BRL"
  }'
```

**Resposta esperada**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Tech Electronics Store",
  "email": "store@techelectronics.com",
  "phone": "+55 11 99999-8888",
  "balance": 5000.00,
  "currency": "BRL"
}
```

**Salve o `id` retornado como `MERCHANT_ID`**.

**O que observar**:

- Resposta HTTP 201 CREATED
- Saldo inicial de 5000.00 BRL

---

### Passo 2: Verificar histórico de eventos do merchant (30 segundos)

**O que fazer**: Consultar o histórico de eventos do merchant (Event Sourcing).

**Explique**: "Este é um exemplo de Event Sourcing: cada mudança fica registrada imutavelmente. Vemos que o merchant foi registrado com saldo inicial de 5000."

**Comando**:

```bash
curl http://localhost:8081/merchants/MERCHANT_ID/events | jq .
```

**Resposta esperada**:

```json
[
  {
    "id": "...",
    "merchantId": "550e8400-e29b-41d4-a716-446655440001",
    "balanceChange": 5000.00,
    "newBalance": 5000.00,
    "eventType": "MERCHANT_REGISTERED",
    "description": "Merchant registered with initial balance",
    "eventDateTime": "2025-12-03T15:45:30",
    "createdAt": "2025-12-03T15:45:30"
  }
]
```

---

### Passo 3: Criar um Pagamento (1 minuto)

**O que fazer**: Um cliente faz um pagamento para o merchant.

**Explique**: "Agora, um cliente deseja fazer um pagamento de 350 BRL para este merchant. Veremos como o sistema processa isso de forma assíncrona via Kafka."

**Comando**:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "payerId": "00000000-0000-0000-0000-000000000001",
    "payerEmail": "customer@example.com",
    "payeeId": "MERCHANT_ID",
    "amount": 350.00,
    "currency": "BRL"
  }'
```

**Resposta esperada**:

```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440002",
  "message": "Payment being processed"
}
```

**Salve o `paymentId` como `PAYMENT_ID`**.

**O que observar**:

- A resposta é imediata com status HTTP 202 ACCEPTED
- Mensagem: "Payment being processed" (processamento assíncrono)
- Ainda não sabemos se foi aprovado (acontece nos serviços consumidores)

---

### Passo 4: Visualizar evento no Kafka (1 minuto)

**O que fazer**: Abrir Kafdrop para ver o evento publicado.

**Explique**: "O payment-service acaba de publicar um `PaymentCreatedEvent` no Kafka, no tópico 'payment-created'. Vamos visualizar a mensagem em tempo real."

**URL**:

```
http://localhost:9000
```

**Passos**:

1. Clique em **Topics** no menu esquerdo
2. Procure pelo tópico **`payment-created`**
3. Clique nele
4. Vá para **Messages**
5. Veja o último evento (mais recente)

**O que observar na mensagem**:

```json
{
  "eventId": "...",
  "eventDateTime": "2025-12-03T15:46:00",
  "paymentId": "550e8400-e29b-41d4-a716-446655440002",
  "payerId": "00000000-0000-0000-0000-000000000001",
  "payerEmail": "customer@example.com",
  "payeeId": "MERCHANT_ID",
  "amount": 350.00,
  "currency": "BRL",
  "status": "PENDING"
}
```

**Explique**: "Este evento foi publicado pelo payment-service. Note que contém TODOS os dados necessários para os consumidores (merchant e notification) agirem. Isso é desacoplamento: nenhum serviço precisa chamar outro diretamente."

---

### Passo 5: Consultar histórico de eventos do pagamento (30 segundos)

**O que fazer**: Ver o Event Store do pagamento.

**Explique**: "Similar ao merchant, cada pagamento tem seu próprio histórico de eventos. Aqui vemos o evento PAYMENT_CREATED que foi persistido."

**Comando**:

```bash
curl http://localhost:8080/payments/PAYMENT_ID/events | jq .
```

**Resposta esperada**:

```json
[
  {
    "id": "...",
    "paymentId": "550e8400-e29b-41d4-a716-446655440002",
    "payerId": "00000000-0000-0000-0000-000000000001",
    "payeeId": "MERCHANT_ID",
    "amount": 350.00,
    "currency": "BRL",
    "status": "PENDING",
    "eventType": "PAYMENT_CREATED",
    "eventDateTime": "2025-12-03T15:46:00",
    "createdAt": "2025-12-03T15:46:00"
  }
]
```

---

### Passo 6: Verificar saldo atualizado do merchant (30 segundos)

**O que fazer**: Consultar o saldo atual do merchant.

**Explique**: "Enquanto estávamos visualizando eventos, o merchant-service consumiu o PaymentCreatedEvent e creditou 350 BRL ao saldo do merchant. Isso é eventual consistency: não é instantâneo, mas logo fica sincronizado. O saldo saiu de 5000 para 5350."

**Comando**:

```bash
curl http://localhost:8081/merchants/MERCHANT_ID | jq .
```

**Resposta esperada**:

```json
{
  "id": "MERCHANT_ID",
  "name": "Tech Electronics Store",
  "email": "store@techelectronics.com",
  "phone": "+55 11 99999-8888",
  "balance": 5350.00,
  "currency": "BRL"
}
```

**O que observar**:

- Saldo agora é **5350.00** (5000 inicial + 350 do pagamento)

---

### Passo 7: Histórico de eventos do merchant (atualizado) (30 segundos)

**O que fazer**: Consultar novamente o histórico de eventos do merchant.

**Explique**: "Agora o histórico do merchant tem UM NOVO EVENTO: PAYMENT_RECEIVED. Vemos exatamente quanto foi creditado (350), qual é o novo saldo (5350), e quando aconteceu. Isso é Event Sourcing em ação: auditoria completa."

**Comando**:

```bash
curl http://localhost:8081/merchants/MERCHANT_ID/events | jq .
```

**Resposta esperada**:

```json
[
  {
    "eventType": "MERCHANT_REGISTERED",
    "balanceChange": 5000.00,
    "newBalance": 5000.00,
    "eventDateTime": "2025-12-03T15:45:30"
  },
  {
    "eventType": "PAYMENT_RECEIVED",
    "balanceChange": 350.00,
    "newBalance": 5350.00,
    "eventDateTime": "2025-12-03T15:46:05"
  }
]
```

**O que observar**:

- Dois eventos: REGISTERED (saldo inicial) e PAYMENT_RECEIVED (pagamento recebido)
- Saldo atual é a soma: 5000 + 350 = 5350
- Timestamps mostram a sequência temporal exata

---

### Passo 8: Visualizar evento de resposta no Kafka (1 minuto)

**O que fazer**: Ver o evento que merchant-service publicou em resposta.

**Explique**: "O merchant-service processou o evento e publicou uma resposta: PaymentProcessedEvent com status APPROVED. Vamos ver no Kafka."

**URL**:

```
http://localhost:9000 → Topics → payment-processed
```

**O que observar na mensagem**:

```json
{
  "eventId": "...",
  "eventDateTime": "2025-12-03T15:46:05",
  "paymentId": "550e8400-e29b-41d4-a716-446655440002",
  "payerId": "00000000-0000-0000-0000-000000000001",
  "payerEmail": "customer@example.com",
  "status": "APPROVED",
  "description": "Payment successfully processed by the merchant."
}
```

**Explique**: "Este é o evento de resposta. O notification-service está escutando este tópico e vai usar para enviar um e-mail ao cliente. Isso é SAGA Pattern: cada serviço faz sua parte de forma independente."

---

## 🎯 Pontos-chave a destacar durante a demo

### 1. **Desacoplamento**

- "O payment-service não conhece merchant-service nem notification-service"
- "Se adicionar novo serviço (ex: analytics), payment-service não muda"
- "Tudo via Kafka: broker central, low coupling"

### 2. **Eventual Consistency**

- "O saldo do merchant não atualiza instantaneamente"
- "Mas em alguns milissegundos, o evento é consumido e saldo fica consistente"
- "Isso é o trade-off: consistency eventual, não imediata"

### 3. **Event Sourcing (Auditoria)**

- "Cada mudança fica registrada imutavelmente"
- "Possível reconstruir saldo: 5000 (inicial) + 350 (pagamento) = 5350"
- "Importante para conformidade, debug, compliance"

### 4. **SAGA Pattern (Transação distribuída)**

- "O pagamento passou por 3 etapas (payment criado → merchant creditou → notification enviou)"
- "Sem orquestrador central: cada serviço reage a eventos (coreografia)"
- "Se merchant falhar, publica REJECTED e payment atualiza status"

### 5. **Microsserviços reais**

- "Cada serviço tem seu próprio banco (postgres)"
- "Escalam independentemente"
- "Falha de um não derruba os outros"

---

## 🐛 Troubleshooting

### "Connection refused no postgres"

```bash
docker-compose up -d
docker logs bank-postgres
```

### "Kafka broker not available"

```bash
docker ps | grep kafka
docker logs <kafka-container-id>
```

### "Topic payment-created não tem mensagens"

- Certifique-se que payment-service está rodando
- Verifique logs de payment-service
- Tente criar outro pagamento

### "Merchant saldo não atualizou"

- Aguarde 2-5 segundos (eventual consistency)
- Verifique se merchant-service está rodando
- Verifique logs de merchant-service para erro

### "Email não foi enviado"

- notification-service provavelmente usa fake notifier
- Verifique logs: procure por `[CONSOLE EMAIL]`
- Para envio real, configurar SMTP no application.yml

---

## 📊 Tempo sugerido

| Passo | Duração | Total |
|-------|---------|-------|
| 1. Criar merchant | 1 min | 1 min |
| 2. Verificar eventos merchant | 30 seg | 1,5 min |
| 3. Criar pagamento | 1 min | 2,5 min |
| 4. Visualizar Kafka (payment-created) | 1 min | 3,5 min |
| 5. Consultar payment events | 30 seg | 4 min |
| 6. Verificar saldo atualizado | 30 seg | 4,5 min |
| 7. Histórico merchant (atualizado) | 30 seg | 5 min |
| 8. Visualizar Kafka (payment-processed) | 1 min | 6 min |
| **Buffer para explicações** | 2-3 min | **8-9 min** |

---

## 💡 Perguntas que podem ser feitas

### "E se o merchant falhar?"

"O evento fica no Kafka. Quando merchant-service volta, processa o evento da fila. Se validação falhar, publica REJECTED e payment atualiza status."

### "E a consistência do saldo?"

"Eventual consistency: nem sempre sincronizado em tempo real, mas converge logo. É o trade-off de escalabilidade."

### "Como garantir que o evento não seja processado 2x?"

"Cada serviço deve ser idempotente. Merchant-service pode verificar se já creditou este pagamento via ID."

### "Precisa de orquestrador central?"

"Não! SAGA coreografado = cada serviço reage. Se ficar complexo, pode evoluir para SAGA orquestrada (Temporal, Camunda)."

---

**Boa sorte na demo! 🚀**

