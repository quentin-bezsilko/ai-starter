# AI Starter - État d'avancement et Roadmap

## Objectif du projet

Développer un assistant IA basé sur Spring AI permettant de répondre à des questions de géographie et de météorologie à travers :

- un LLM local via Ollama
- une mémoire conversationnelle persistée
- des MCP (Model Context Providers) métier
- des APIs externes
- un futur moteur RAG
- des capacités agentiques avancées

---

# Fonctionnalités déjà implémentées

## Chat IA

### Implémenté

- Spring Boot 4.1
- Java 25
- Spring AI 2.x
- Ollama
- Modèle llama3.2

### Validation effectuée

Questions simples :

```text
Quelle est la capitale de l'Espagne ?
```

Réponse correcte obtenue.

---

## Mémoire conversationnelle

### Implémenté

- Gestion des conversations
- Gestion des messages
- Stockage PostgreSQL
- ChatMemoryAdvisor
- ConversationId propagé dans les appels

### Validation effectuée

Question :

```text
Parlons de l'Espagne.
```

Puis :

```text
Quelle est la capitale ?
```

Réponse :

```text
Madrid
```

Validation :

✅ récupération du contexte précédent

✅ mémoire persistée

✅ intégration Spring AI fonctionnelle

---

## MCP Géographie

### Implémenté

Services exposés au LLM :

```java
getCapital(String country)

getEuropeanCapitals()

getAmericanCapitals()

getCities(String countryCode)
```

### Sources

Données géographiques stockées en base PostgreSQL.

### Validation en cours

Exemples :

```text
Quelle est la capitale de l'Espagne ?
```

```text
Liste les villes françaises.
```

```text
Quelles sont les capitales européennes ?
```

---

## MCP Météo

### Implémenté

Services exposés :

```java
getCurrentWeather(String city)

getAverageSummerTemperature(String city)
```

---

## API externe

### Déjà implémentée

Contrairement à l'idée initiale, le projet possède déjà une intégration API externe.

### Fournisseur

Open Meteo

### Client

```java
OpenMeteoClient
```

### Fonctionnalités

```java
WeatherResponseDto getCurrentWeather(...)
```

```java
Double getAverageSummerTemperature(...)
```

### Exemple

Question :

```text
Quel temps fait-il à Madrid ?
```

Le MCP appelle :

```java
OpenMeteoClient
```

qui appelle :

```text
https://api.open-meteo.com
```

Validation attendue :

✅ Tool Calling

✅ Appel HTTP externe

✅ Injection du résultat dans la réponse du LLM

---

## Persistance des conversations

### Déjà implémentée

Tables :

```text
conversation
message
```

### Fonctionnel

- création de conversation
- historisation des échanges
- mémoire conversationnelle persistée

---

## Modes de réponse

### Déjà implémentés

Profil :

```text
SIMPLE
NORMAL
EXPERT
```

Utilisation via templates de prompts.

### Objectif

Adapter le niveau de détail sans changer la logique métier.

Exemple :

Question :

```text
Quelle est la capitale de l'Espagne ?
```

Mode SIMPLE :

```text
Madrid.
```

Mode NORMAL :

```text
Madrid est la capitale de l'Espagne.
```

Mode EXPERT :

```text
Madrid est la capitale de l'Espagne depuis 1561 et constitue l'un des principaux centres économiques et politiques du pays.
```

---

# Prochain chantier majeur

## RAG (Retrieval Augmented Generation)

### Pourquoi ?

Concept majeur de Spring AI encore absent du projet.

Le MCP répond aux questions structurées.

Le RAG permettra :

- l'exploitation documentaire
- la recherche sémantique
- les réponses contextualisées

---

## Cas d'usage

Exemple :

Documents :

```text
climate-guide.pdf
european-countries.pdf
travel-guide.md
```

Question :

```text
Quels sont les avantages climatiques de Madrid en été ?
```

Le système :

```text
Question
   ↓
Recherche vectorielle
   ↓
Documents pertinents
   ↓
Prompt enrichi
   ↓
LLM
```

---

## Architecture cible

```text
Utilisateur
        │
        ▼
Spring AI ChatClient
        │
        ├─────────────► Chat Memory
        │                    │
        │                    ▼
        │              PostgreSQL
        │
        ├─────────────► MCP Geography
        │
        ├─────────────► MCP Weather
        │                    │
        │                    ▼
        │             Open Meteo API
        │
        └─────────────► RAG
                             │
                             ▼
                       Vector Store
```

---

## Choix recommandés

### SimpleVectorStore

Pour démarrer rapidement.

---

### PgVector

Pour une solution plus proche de la production.

Exemple :

```text
PostgreSQL
+
pgvector
```

Permet :

- embeddings
- similarité vectorielle
- recherche documentaire avancée

---

# Fonctionnalités à développer

## API de gestion des conversations

Comme les données existent déjà.

### Endpoints

```http
GET /conversations
```

Liste des conversations.

---

```http
GET /conversations/{id}
```

Détail complet.

---

```http
DELETE /conversations/{id}
```

Suppression.

---

```http
GET /conversations/{id}/messages
```

Historique détaillé.

---

# Observabilité

## Importance

Sujet très apprécié dans un projet Spring AI moderne.

---

## Métriques à exposer

### Spring Boot Actuator

Déjà présent.

Ajouter :

```xml
micrometer-registry-prometheus
```

---

### Indicateurs

Nombre de :

- questions
- conversations
- appels MCP
- appels Open Meteo
- erreurs

---

### Temps de réponse

Mesurer :

```text
Prompt → Réponse
```

et

```text
MCP → Réponse
```

---

### Consommation LLM

Suivre :

- input tokens
- output tokens
- temps de génération

---

## Stack recommandée

```text
Spring Boot
Actuator
Micrometer
Prometheus
Grafana
```

---

# Multi MCP

## Situation actuelle

```text
MCP Geography
MCP Weather
```

---

## Évolutions possibles

### Time MCP

```java
getCurrentTime(country)
```

---

### Currency MCP

```java
getCurrency(country)
```

---

### Language MCP

```java
translate(text, targetLanguage)
```

---

### Travel MCP

```java
getTopAttractions(city)
```

---

## Scénario cible

Question :

```text
Quelle est la capitale de l'Espagne,
quelle est la météo actuelle,
l'heure locale
et la devise utilisée ?
```

Le LLM orchestre automatiquement plusieurs MCP.

---

# Agent complet

## Objectif

Passer d'un simple chatbot à un agent capable d'exécuter plusieurs tâches successives.

---

## Exemple

Question :

```text
Prépare-moi un voyage de 3 jours à Madrid en juin.
```

Le système :

### Étape 1

```java
getCapital("Spain")
```

### Étape 2

```java
getCurrentWeather("Madrid")
```

### Étape 3

```java
getAverageSummerTemperature("Madrid")
```

### Étape 4

Recherche documentaire via RAG.

---

### Réponse finale

```text
Programme jour par jour

Météo estimée

Conseils vestimentaires

Lieux touristiques

Informations pratiques
```

---

# Priorités recommandées

## Sprint 1

✅ Chat

✅ Mémoire conversationnelle

✅ MCP Géographie

✅ MCP Météo

✅ API Open Meteo

✅ Modes de réponse

✅ Persistance PostgreSQL

---

## Sprint 2

🚀 Mise en place du RAG

- Embeddings
- Vector Store
- Indexation documentaire
- Recherche sémantique

---

## Sprint 3

🚀 API Conversations

- Consultation
- Suppression
- Historique

---

## Sprint 4

🚀 Observabilité

- Prometheus
- Grafana
- Dashboards métier IA

---

## Sprint 5

🚀 Multi MCP

- Time
- Currency
- Translation
- Travel

---

## Sprint 6

🚀 Agent IA complet

- Tool chaining
- Raisonnement multi-étapes
- RAG + MCP combinés
- Génération de plans et recommandations