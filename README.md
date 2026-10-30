# eBank - Microservices bancaires

Application eBank réalisée avec Spring Boot, Spring Cloud et Angular. Le projet
gère les comptes bancaires, les transactions et les virements entre comptes.
Une interface Angular permet de tester les opérations depuis un navigateur.

## Fonctionnalités

- Gestion des comptes bancaires.
- Création et consultation de transactions.
- Mise à jour du solde après une transaction.
- Virement d'un compte source vers un compte destinataire.
- Validation des règles métier côté `transaction-service`.
- API REST et API GraphQL.
- Service de découverte Eureka.
- API Gateway avec routes explicites.
- Configuration centralisée avec Spring Cloud Config.
- Circuit breaker Resilience4j pour les appels vers `account-service`.
- Frontend Angular avec formulaires, validations et affichage des soldes.

## Architecture

```text
                         +------------------+
                         |  eBank Frontend  |
                         |  Angular :4200   |
                         +--------+---------+
                                  |
                                  v
                         +------------------+
                         |  API Gateway     |
                         |  :8080           |
                         +---+----------+---+
                             |          |
                   /account-service     | /transaction-service
                             |          |
                             v          v
                    +--------+--+  +----+-------------+
                    | Account   |  | Transaction      |
                    | Service   |  | Service          |
                    | :8081     |  | :8082            |
                    +-----+-----+  +----+-------------+
                          |             |
                          +------+------+ 
                                 |
                         +-------v--------+
                         | Eureka :8761   |
                         +----------------+

                         Config Server :8888
```

| Module | Port | Rôle |
|---|---:|---|
| `discovery-service` | `8761` | Registre Eureka |
| `config-service` | `8888` | Configuration centralisée |
| `account-service` | `8081` | Comptes et soldes |
| `transaction-service` | `8082` | Transactions et virements |
| `gateway-service` | `8080` | Point d'entrée HTTP |
| `ebank-frontend` | `4200` | Interface Angular |

## Prérequis

- Java 21 ou version compatible avec le projet.
- Maven (ou le Maven Wrapper fourni dans les services Java).
- Node.js et npm.
- Ports `4200`, `8080`, `8081`, `8082`, `8761` et `8888` disponibles.

Les services utilisent une base H2 en mémoire. Les données de démonstration sont
donc recréées à chaque redémarrage de `account-service`.

## Démarrage

Ouvrir un terminal par service et exécuter les commandes suivantes depuis la
racine du dépôt.

### 1. Eureka

```powershell
cd discovery-service
.\mvnw.cmd spring-boot:run
```

Interface Eureka :

```text
http://localhost:8761
```

### 2. Config Server

```powershell
cd config-service
.\mvnw.cmd spring-boot:run
```

Le serveur utilise une configuration native située dans
`config-service/src/main/resources/shared`.

### 3. Account service

```powershell
cd account-service
.\mvnw.cmd spring-boot:run
```

Deux comptes actifs de démonstration sont créés au démarrage. Leurs UUID sont
générés automatiquement : il faut donc les récupérer avec `GET /api/accounts`
avant de créer une transaction.

### 4. Transaction service

```powershell
cd transaction-service
.\mvnw.cmd spring-boot:run
```

### 5. Gateway

```powershell
cd gateway-service
.\mvnw.cmd spring-boot:run
```

Le gateway expose les routes suivantes :

```text
/account-service/**     -> http://localhost:8081/**
/transaction-service/** -> http://localhost:8082/**
```

Le préfixe du service est supprimé avant le transfert vers le backend.

### 6. Frontend Angular

```powershell
cd ebank-frontend
npm install
npm start
```

Ouvrir ensuite :

```text
http://localhost:4200
```

Le frontend appelle le gateway sur `http://localhost:8080`.

## API REST

### Comptes

Les appels directs au service compte utilisent le port `8081` :

```http
GET    http://localhost:8081/api/accounts
GET    http://localhost:8081/api/accounts/{id}
POST   http://localhost:8081/api/accounts
PUT    http://localhost:8081/api/accounts/{id}/update-balance?amount=100
```

Depuis le gateway, ajouter `/account-service` :

```http
GET http://localhost:8080/account-service/api/accounts
```

Exemple de création d'un compte :

```bash
curl -X POST http://localhost:8080/account-service/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerReference": "CUST-003",
    "currency": "EUR",
    "balance": 2500.0,
    "status": "ACTIVE"
  }'
```

### Transactions

Appels directs au service transaction :

```http
GET  http://localhost:8082/api/transactions
GET  http://localhost:8082/api/transactions/account/{accountId}
POST http://localhost:8082/api/transactions
POST http://localhost:8082/api/transactions/transfer
```

Depuis le gateway, ajouter `/transaction-service` :

```http
GET  http://localhost:8080/transaction-service/api/transactions
POST http://localhost:8080/transaction-service/api/transactions
POST http://localhost:8080/transaction-service/api/transactions/transfer
```

Créer une transaction :

```bash
curl -X POST http://localhost:8080/transaction-service/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "UUID_DU_COMPTE",
    "amount": 250.0,
    "currency": "EUR",
    "merchantCategory": "GROCERY",
    "country": "MA"
  }'
```

Effectuer un virement :

```bash
curl -X POST "http://localhost:8080/transaction-service/api/transactions/transfer?sourceAccountId=UUID_SOURCE&destAccountId=UUID_DESTINATION&amount=500"
```

## Règles métier

Les règles sont contrôlées par `transaction-service` avant l'enregistrement :

1. Le montant doit être strictement positif.
2. Le montant ne doit pas dépasser le plafond configuré
   (`bank.transaction.max-amount`, valeur locale par défaut : `1000`).
3. Le compte doit exister.
4. Le compte doit avoir le statut `ACTIVE`.
5. La devise de la transaction doit correspondre à celle du compte.
6. Le solde est consulté avant l'enregistrement.
7. Le solde doit être suffisant.
8. Après validation, le compte est débité et la transaction est enregistrée.
9. Pour un virement, le compte source est débité puis le compte destinataire est
   crédité.

Le frontend affiche également ces contrôles avant l'envoi, mais la validation
du backend reste la règle de référence.

## API GraphQL

Les deux services exposent GraphQL sur `/graphql` et GraphiQL est activé en
développement.

```text
Account service    http://localhost:8081/graphiql
Transaction service http://localhost:8082/graphiql
```

Exemple de requête GraphQL pour les comptes :

```graphql
query {
  accounts {
    id
    customerReference
    status
    currency
    balance
  }
}
```

Exemple de requête GraphQL pour les transactions :

```graphql
query {
  transactions {
    id
    accountId
    amount
    currency
    merchantCategory
    country
    createdAt
  }
}
```

## Documentation Swagger

Les services REST exposent leur documentation OpenAPI :

```text
Account service     http://localhost:8081/swagger-ui.html
Transaction service http://localhost:8082/swagger-ui.html
```

## Configuration importante

Le plafond local des transactions est défini dans
`transaction-service/src/main/resources/application.properties` :

```properties
bank.transaction.max-amount=1000
```

Le gateway utilise des routes HTTP explicites vers `localhost`. Le discovery
locator automatique est désactivé afin d'éviter que le gateway utilise un nom
d'hôte Eureka non résolvable sur certaines configurations Windows :

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: false
```

## Tests et build

Construire un service Java :

```powershell
cd transaction-service
.\mvnw.cmd clean verify
```

Construire le frontend :

```powershell
cd ebank-frontend
npm run build
```

Le build Angular est généré dans `ebank-frontend/dist/ebank-frontend`.

## Dépannage

### `500` sur une transaction

- Vérifier que l'UUID existe avec `GET http://localhost:8080/account-service/api/accounts`.
- Vérifier que le compte est `ACTIVE`.
- Vérifier que la devise est correcte.
- Vérifier que le solde est suffisant.
- Vérifier que le montant ne dépasse pas `1000` par défaut.

### `500` sur `/account-service/api/accounts` via le gateway

Vérifier que `gateway-service` utilise les routes explicites vers
`http://localhost:8081` et que le discovery locator est désactivé. Redémarrer le
gateway après toute modification de `application.yml`.

### `AccountRestClient` introuvable

`transaction-service` doit contenir `@EnableFeignClients` sur sa classe
principale et la dépendance `spring-cloud-starter-openfeign` dans son
`pom.xml`.

### Aucun compte trouvé après un redémarrage

H2 est configuré en mémoire. Les UUID des comptes de démonstration changent
après chaque redémarrage : consulter à nouveau `/api/accounts`.

## Technologies

- Java 21
- Spring Boot 4
- Spring Cloud Config
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway WebFlux
- OpenFeign
- Resilience4j
- Spring Data JPA
- H2
- Spring GraphQL
- Springdoc OpenAPI
- Angular 22

