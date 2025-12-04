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


## 2. Escolha do Saga Pattern Orquestrado
### Status: aceita
### Contexto:
O sistema BankingSystem é composto por microsserviços distribuídos 
e independentes (payment-service, merchant-service, notification-service). 
Um fluxo de pagamento requer a coordenação sequencial desses serviços. 
Como cada serviço possui seu próprio banco de dados, não é possível utilizar transações ACID 
tradicionais para garantir a consistência global. É necessário um mecanismo para gerenciar 
transações de longa duração, garantir a consistência eventual dos dados e executar 
compensações (rollbacks) caso alguma etapa do fluxo falhe.

### Decisão:
Será utilizado o padrão arquitetural Saga Orquestrado (Orchestrated-based Saga) 
para gerenciar as transações distribuídas entre os microsserviços, reduzindo o acoplamento entre
serviços e integrando nativamente com a infraestrutura de broker (Apache Kafka), assim centralizando o 
gerenciamento e a coordenação das transações distribuídas de pagamentos criados, processados e 
finalizados e garantindo a capacidade de recuperação em casos de falha.

### Consequências:
O uso do Saga com orquestração centraliza a lógica em um único ponto de falha potencial e gera maior
complexidade na manutenção devido à centralização. O aumento da complexidade e essa centralização
é justificado, pois a orquestração oferece melhor visibilidade de monitoramento do fluxo do saga, 
sendo fundamental para garantir consistência eventual dos dados e compensação (rollback) das 
transações em casos de falha.

### Notas:
Autor: Alan Souza e Pedro Candido. Aprovado por: Ana Turazzi. Última atualização: 04/12/2025

## 3. Uso do padrão arquitetural Event-Sourcing
### Status: aceita
### Contexto:
O sistema BankingSystem é composto por microsserviços distribuídos independentes que se comunicam entre
sí através de um broker de eventos. Entretanto, a auditoria desses eventos é fundamental, pois o 
acompanhamento das transações bancárias realizadas entre pagador e beneficiário é de extrema importância,
sendo necessário garantir a transparência nas operações e tornar possível a revisão de pagamentos 
realizados anteriormente, em que cada evento realizado é persistido no sistema.

### Decisão:
Será utilizado o padrão arquitetural Event-Sourcing para persistir as mudanças de estado das 
transações como uma sequência de eventos imutáveis, registrando cada alteração de forma incremental 
e integrando nativamente com a infraestrutura de broker (Apache Kafka), assim viabilizando a 
auditoria completa e a rastreabilidade das operações bancárias e garantindo a capacidade de 
reconstrução histórica (replay) dos dados em qualquer ponto do tempo.

### Consequências:
O uso do Event-Sourcing introduz maior complexidade na modelagem dos dados e exige maior 
capacidade de armazenamento, uma vez que o estado atual deve ser reconstruído a partir 
do histórico completo de eventos. O aumento da complexidade técnica e do volume de dados é 
justificado, pois o padrão oferece uma trilha de auditoria imutável e à prova de violação, 
sendo fundamental para garantir a rastreabilidade total das operações financeiras e permitir 
a reconstrução do estado do sistema para fins de correção e análise em casos de inconsistência.

### Notas:
Autor: Pedro Candido. Aprovado por: Alan Souza e Ana Turazzi. Última atualização: 20/11/2025