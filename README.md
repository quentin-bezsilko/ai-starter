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

POST /api/rag/upload	
file (Form-Data, PDF)	Indexe un nouveau PDF ou remplace automatiquement sa version existante dans PGVector.
GET	/api/rag/ask	
question (obligatoire) fileName (optionnel)	Pose une question sur l'ensemble des PDF (ou uniquement sur fileName) et retourne la réponse + les sources exactes.
GET	/api/rag/documents	
Liste tous les PDF actuellement indexés ainsi que leur nombre de chunks.
DELETE /api/rag/documents/{fileName}	
fileName (Path variable)	Supprime tous les vecteurs associés à un PDF donné dans PGVector.

Le PdfDocumentService sauvegarde / remplace le contenu d'un PDF dans le Vector Postgres

Recette OK

---

## Sprint 3

/!\ Penser à améliorer les performances d'Ollama (ex: augmenter la mémoire allouée, vérifier les paramètres de thread, etc.) pour éviter les lenteurs lors des appels LLM.

🚀 API Conversations

- Consultation
- Suppression
- Historique

---

## Sprint 4

🚀 Observabilité

- Opentelemetry
- Prometheus
- Grafana
- Loki
- Tempo
- Dashboards métier IA

Part 2 : Les métriques indispensables à mettre en place
Pour piloter efficacement ton application Spring Boot (spécialement orientée IA/RAG/MCP), voici les compteurs et histogrammes clés à exposer via Micrometer :

1. Performance de l'API & Infrastructure
   http_server_requests_seconds (Histogramme) : Latence des requêtes HTTP par endpoint (percentiles p50, p95, p99).

http_server_requests_errors_total (Compteur) : Nombre d'erreurs HTTP (4xx, 5xx) ventilées par type d'erreur.

jvm_memory_used_bytes / jvm_gc_memory_allocated_bytes_total : Surveillance de la mémoire JVM (crucial avec Spring Boot sous forte charge).

2. Métriques spécifiques au RAG
   rag_retrieval_duration_seconds (Histogramme) : Temps pris pour interroger la base vectorielle.

rag_chunks_retrieved_count (Histogramme / Jauge) : Nombre de documents/chunks ramenés par la recherche (pour vérifier qu'on ne sature pas la fenêtre de contexte).

rag_similarity_score (Histogramme) : Score de similarité moyen des documents trouvés (pour détecter si le système récupère du contenu pertinent ou du bruit).

rag_zero_results_total (Compteur) : Nombre de requêtes où la base vectorielle n'a rien trouvé du tout.

3. Métriques FinOps & LLM (Tokens & Coûts)
   llm_prompt_tokens_total (Compteur) : Nombre cumulé de tokens envoyés en entrée aux modèles.

llm_completion_tokens_total (Compteur) : Nombre cumulé de tokens générés en sortie par les modèles.

llm_inference_duration_seconds (Histogramme) : Temps de réponse global du LLM et temps par token (pour mesurer la réactivité).

4. Métriques des MCP (Appels d'outils)
   mcp_tool_calls_total (Compteur) : Nombre d'appels effectués par outil MCP (étiqueté par nom d'outil : ex: tool=search_db, tool=calculate_tax).

mcp_tool_errors_total (Compteur) : Nombre d'échecs lors de l'exécution des MCP (timeouts, erreurs de payload).

mcp_tool_duration_seconds (Histogramme) : Temps d'exécution de chaque outil externe appelé par l'agent.

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

## Spring 7

- Créer les tests unitaires
- Passer Sonar sur le projet