# delivery-system — Projeto Diamante 02

Sistema de pedidos de delivery com microsserviços (Java Advanced · FIAP). Cenário: promoção relâmpago de sexta às 20h, com estoque limitado, pagamento instável, avaliações em massa e um assistente de IA que responde sobre o cardápio.

## Integrantes

| Nome | RM |
|------|----|
| Lucas Matsubara | 565020 |
| João Pedro Pereira Camilo | 562005 |
| Pamella Christiny Chaves Brito | 565206 |

## Arquitetura

```
App React Native ──REST──► order-service :8080 ──LB + retry──► payment-service :8081
                              │    │                      └──► payment-service :8082
                              │    └──AMQP──► RabbitMQ (delivery.exchange → reviews.queue)
                              │                                   │
                              └──ChatClient──► OpenAI / LM Studio / Ollama
                                                                  ▼
App React Native ──REST──────────────────────────────► review-service :8083
                         (todos registrados no eureka-server :8761)
```

| Projeto | Porta | Responsabilidade |
|---------|-------|------------------|
| `eureka-server` | 8761 | Service registry |
| `order-service` | 8080 | Cardápio, pedidos com lock pessimista, chamada ao pagamento (load balance + retry), publicação de avaliações, assistente com Spring AI, rate limit |
| `payment-service` | 8081 / 8082 | Pagamento instável (falha em ~50% das chamadas), informa a porta que atendeu |
| `review-service` | 8083 | Consome avaliações, acumula em memória, grava no H2 a cada 5 s e expõe o ranking |
| RabbitMQ (Docker) | 5672 / 15672 | `delivery.exchange` (topic) → `reviews.queue` (durável), routing key `reviews.new` |

**Stack:** Java 25 · Spring Boot 4.1.1 · Spring Cloud 2025.1.2 · Spring AI 2.0.1 · Gradle 9.7.1 · Lombok · H2 · RabbitMQ 4.

Cada serviço é um projeto Gradle independente, organizado em `config`, `controller`, `service`, `model`, `repository`, `dto` e `exception`. Para abrir tudo no IntelliJ, abra a pasta raiz: a pasta `.idea` já vincula os quatro projetos.

## Pré-requisitos

- JDK 25 (se não tiver, o Gradle baixa sozinho pelo plugin foojay na primeira execução)
- Docker (para o RabbitMQ)
- Chave da OpenAI **ou** um modelo local (LM Studio / Ollama)

## Como rodar

### 1. RabbitMQ

```bash
docker compose up -d
```

Painel em http://localhost:15672 (usuário `myuser`, senha `secret`).

### 2. Chave do modelo de IA (variável de ambiente, nunca no repositório)

Defina a chave no mesmo terminal onde o order-service vai rodar (só ele usa o modelo de IA).

Linux / macOS / Git Bash:

```bash
export OPENAI_API_KEY="sua-chave"
```

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY = "sua-chave"
```

No IntelliJ: *Run configuration* do order-service → *Environment variables* → `OPENAI_API_KEY=sua-chave`.

Variáveis opcionais:

| Variável | Padrão | Uso |
|----------|--------|-----|
| `AI_MODEL` | `gpt-5-mini` | Modelo usado pelo assistente |
| `AI_BASE_URL` | `https://api.openai.com/v1` | Troque para usar modelo local |

Modelo local (sem chave): LM Studio → `AI_BASE_URL=http://localhost:1234/v1`; Ollama → `AI_BASE_URL=http://localhost:11434/v1`. Defina `AI_MODEL` com o nome do modelo carregado (ex.: `llama3.2`).

### 3. Serviços (um terminal para cada, nesta ordem)

```bash
cd eureka-server   && ./gradlew bootRun
cd payment-service && ./gradlew bootRun
cd payment-service && ./gradlew bootRun --args='--server.port=8082'
cd review-service  && ./gradlew bootRun
cd order-service   && ./gradlew bootRun     # no terminal onde a OPENAI_API_KEY foi definida
```

No Windows (cmd) use `gradlew.bat bootRun`. Painel do Eureka: http://localhost:8761 — devem aparecer `ORDER-SERVICE`, `PAYMENT-SERVICE` (2 instâncias) e `REVIEW-SERVICE`.

### 4. App React Native

Rode o app do professor ([joaocarloslima/diamante-delivery](https://github.com/joaocarloslima/diamante-delivery)) e informe o IP da máquina no botão de servidor. As portas 8080 e 8083 aceitam CORS, então o app também funciona no navegador (Expo web).

## Contrato da API

### order-service :8080

| Método | Rota | Corpo | Respostas |
|--------|------|-------|-----------|
| GET | `/dishes` | — | 200 lista de Dish |
| GET | `/dishes/{id}` | — | 200 Dish · 404 |
| POST | `/orders` | `{"dishId": 1, "quantity": 2}` | 201 Order · 400 · 404 · 409 · 429 · 502 |
| GET | `/orders/{id}` | — | 200 Order · 404 |
| POST | `/reviews` | `{"dishId": 1, "rating": 5, "comment": "Great"}` | 202 sem corpo · 400 · 404 |
| POST | `/assistant` | `{"question": "Tem opção vegana?"}` | 200 `{"answer": "..."}` |

### review-service :8083

| Método | Rota | Resposta |
|--------|------|----------|
| GET | `/reviews/ranking` | `[{"dishId": 1, "dishName": "House Burger", "average": 4.6, "count": 128}]` |

### payment-service :8081 / :8082 (interno)

`POST /payments` com `{"amount": 79.80}` → 200 `{"status": "APPROVED", "instance": 8081}` ou 500 (falha simulada).

Todos os erros seguem o formato `{"error": "Dish out of stock"}`.

## Como cada requisito foi atendido

**Eureka.** Os três serviços se registram pelo `spring.application.name`. O order-service chama `http://PAYMENT-SERVICE/payments`; nenhuma chamada entre serviços usa `localhost`.

**Load balance.** `RestTemplateConfig` cria um `RestTemplate` com `@LoadBalanced` (Spring Cloud LoadBalancer, round robin). As duas instâncias do pagamento têm `instance-id` diferente (`payment-service:8081` e `payment-service:8082`). O log do order-service mostra a instância que aprovou (`APPROVED by payment-service instance 8082`) e a que falhou (`{"error":"Simulated payment failure","instance":8081}`). O log de cada instância também mostra a porta.

**Retry.** `PaymentClient` é um bean separado (o `@Retryable` precisa passar pelo proxy do Spring), com `@Retryable(maxRetries = 4, delay = 300, multiplier = 2, jitter = 100, maxDelay = 2000)` e `@EnableResilientMethods` na classe principal. Os intervalos crescem 300 ms → 600 ms → 1,2 s → 2 s (teto), com até 100 ms de jitter. Cada nova tentativa passa de novo pelo load balancer e normalmente cai na outra instância. Se todas falharem, a exceção desfaz a transação: nenhum pedido é gravado, o estoque fica intacto e a resposta é **502**.

**Race condition.** `OrderService.create` roda em um único `@Transactional`: `findByIdForUpdate` com `@Lock(PESSIMISTIC_WRITE)` (vira `SELECT ... FOR UPDATE`), verificação do estoque, pagamento, baixa do estoque e gravação do pedido. Pedidos concorrentes do mesmo prato esperam na fila do lock e nunca leem o mesmo valor de estoque. O pagamento acontece com o lock seguro de propósito: se ele falhar, o rollback devolve tudo e o próximo da fila pega a unidade. Isso garante exatamente 10 pedidos confirmados e estoque final 0. Para a fila não estourar, o H2 usa `LOCK_TIMEOUT=60000` e o pool tem 20 conexões.

**Mensageria.** `RabbitConfig` declara explicitamente a `TopicExchange` `delivery.exchange`, a fila durável `reviews.queue`, a routing key `reviews.new` e o binding. `POST /reviews` valida (rating de 1 a 5, prato existente), publica um JSON com `dishId`, `dishName`, `rating` e `comment` e responde **202** sem gravar nada no banco.

**Backpressure.** No review-service, o `@RabbitListener` só acumula em um `ConcurrentHashMap` por prato (soma dos ratings e quantidade), com `merge` atômico. Um `@Scheduled` a cada 5 s retira cada entrada de forma atômica e grava na entidade `ReviewSummary` (uma linha por prato). Se a gravação falhar, a entrada volta para o buffer. `GET /reviews/ranking` lê do banco, ordenado pela média.

**Spring AI.** `ChatService` cria o `ChatClient` a partir do `ChatClient.Builder` com uma system message que define o papel: atendente do restaurante, respostas curtas em português e recusa educada fora do tema. A cada pergunta, o cardápio atual (nome, preço, estoque e descrição) é lido do banco e entra no prompt. A chave vem de `OPENAI_API_KEY`.

**Bônus: rate limit.** `POST /orders` aceita no máximo 20 requisições em qualquer janela de 1 segundo (implementação própria, janela deslizante, no pacote `ratelimit`). O excedente recebe **429** com `{"error": "..."}`. Pode ser desligado com `--delivery.rate-limit.enabled=false`.

## Testes rápidos

```bash
# 50 pedidos simultâneos no prato da promoção (id 1): exatamente 10 x 201 e estoque 0
./scripts/race-test.sh 50 1

# 500 avaliações em rajada e o ranking depois do flush
./scripts/reviews-burst.sh 500
```

No PowerShell 7: `./scripts/race-test.ps1 -Requests 50 -DishId 1`.

Com o rate limit ligado, parte da rajada de 50 recebe 429, e o resultado continua sendo 10 pedidos confirmados com estoque 0. Para ver apenas 201 e 409, suba o order-service com `./gradlew bootRun --args='--delivery.rate-limit.enabled=false'`. Para reiniciar o estoque, basta reiniciar o order-service (H2 em memória).

```bash
curl -X POST localhost:8080/assistant -H "Content-Type: application/json" -d '{"question": "Tem prato vegetariano até R$ 40?"}'
curl -X POST localhost:8080/assistant -H "Content-Type: application/json" -d '{"question": "Quem ganhou a Copa de 2002?"}'
```

Console do H2: http://localhost:8080/h2-console (`jdbc:h2:mem:orderdb`) e http://localhost:8083/h2-console (`jdbc:h2:mem:reviewdb`), usuário `sa`, sem senha.
