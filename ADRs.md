## 1. Escolha da Arquitetura Baseada em Eventos com Broker
### Status: aceita
### Contexto:
O BankingSystem requer a integração de múltiplos microsserviços 
(payment, merchant, notification) que possuem diferentes tempos 
de processamento e requisitos de disponibilidade. A comunicação síncrona 
tradicional (HTTP/REST) entre esses serviços criaria um acoplamento 
temporal forte: se o serviço de notificação estiver lento ou indisponível, 
o serviço de pagamentos ficaria bloqueado ou falharia, degradando a experiência
do usuário final. Além disso, o sistema precisa lidar com picos 
de tráfego sem perder solicitações e garantir que eventos passados 
possam ser reprocessados em caso de erros lógicos.

### Decisão:
Adotaremos uma Arquitetura Baseada em Eventos utilizando um Broker 
de Mensagens Assíncrono (Apache Kafka) como pilar de comunicação entre 
os microsserviços. Dessa forma, o Broker atua como um buffer, acumulando 
mensagens durante picos de tráfego, permitindo que os consumidores processem 
os dados em seu próprio ritmo sem sobrecarregar o sistema. Além disso, o uso
do Kafka permite que os eventos sejam persistidos por mais tempo, viabilizando
melhor auditoria dos eventos.

### Consequências:
A introdução de um componente de middleware (Broker Kafka) adiciona 
complexidade infraestrutural e operacional ao ambiente de deploy. O aumento 
no custo de manutenção da infraestrutura é justificado, pois a resiliência 
do sistema evita que a falha de um serviço periférico (como envio de e-mails) 
derrube o fluxo crítico de pagamentos.

### Notas: 
Autor: Pedro Candido. Aprovado por: Alan Souza e Ana Turazzi. Última atualização: 01/12/2025


## 2. Escolha do Saga Pattern Coreografado
### Status: aceita
### Contexto:
O sistema BankingSystem é composto por microsserviços distribuídos 
e independentes (payment-service, merchant-service, notification-service). 
Um fluxo de pagamento requer a coordenação sequencial desses serviços. 
Como cada serviço possui seu próprio banco de dados, 
não é possível utilizar transações ACID tradicionais para garantir 
a consistência global. É necessário um mecanismo para gerenciar 
transações de longa duração, garantir a consistência eventual dos 
dados e executar compensações (rollbacks) caso alguma etapa do fluxo 
falhe.

### Decisão:
Será utilizado o padrão arquitetural Saga Coreografado (Choreography-based Saga) 
para gerenciar as transações distribuídas entre os microsserviços, reduzindo o acoplamento entre
serviços e integrando nativamente com a infraestrutura de broker (Apache Kafka)

### Consequências:
A eliminação de um orquestrador central remove o ponto único de falha e o gargalo 
de processamento. O aumento na responsabilidade de cada serviço em gerenciar 
suas próprias transações de compensação é justificado, pois escalabilidade 
independente é vital para evitar que o sistema pare por falha em um único 
componente e garante a consistência eventual dos dados.

### Notas:
Autor: Alan Souza e Pedro Candido. Aprovado por: Ana Turazzi. Última atualização: 02/12/2025

## 3.
### Status: aceita
### Contexto:
O sistema BankingSystem é composto por microsserviços distribuídos independentes (payment-service, merchant-service, notification-service). Um fluxo de pagamento requer coordenação sequencial desses serviços. Como cada serviço possui seu próprio banco de dados, não é possível utilizar transações ACID tradicionais para garantir consistência global. É necessário um mecanismo para gerenciar transações de longa duração, garantir consistência eventual dos dados e executar compensações (rollbacks) caso alguma etapa do fluxo falhe.

### Decisão:
Implementar o padrão **SAGA Pattern com Coreografia** utilizando Apache Kafka como broker de eventos. Cada serviço reage independentemente a eventos publicados, formando um fluxo orquestrado implicitamente sem orquestrador central.

### Consequências:
- **Positivas**: Eliminação de ponto único de falha, escalabilidade independente, fluxo natural de negócio, fácil adição de novos serviços
- **Negativas**: Dificuldade para rastrear fluxo completo, compensação complexa, possível processamento fora de ordem, maior complexidade de observabilidade

### Conformidades:
- ADR 1: Integração nativa com Apache Kafka como broker de mensagens
- ADR 2: Complementa Saga Pattern Coreografado com implementação concreta dos eventos
- Padrão MADR 2.1.1: Template de Architectural Decision Records