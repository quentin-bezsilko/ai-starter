# Assistant code

Le dépôt évolue d’une démo de planification de voyage vers un assistant technique Spring AI. Il couvre les questions-réponses avec profils, la mémoire conversationnelle, l’historisation et un pipeline RAG. Celui-ci accepte une source Markdown locale pour une démo reproductible ainsi que des sources Web limitées par hôte autorisé et profondeur de crawl.

Ce que fait le projet aujourd’hui

L’application est un service Spring Boot exposant une API REST sous le préfixe /aistarter, avec documentation OpenAPI/Swagger. Le parcours de question-réponse principal est :

• POST /aistarter/api/v1/assistant/question envoie la question au modèle Ollama avec un prompt système et un profil — simple, pédagogique ou expert.
• POST /aistarter/api/v1/assistant/question/simple utilise le même service, mais sans prompt de profil.
• Si aucun identifiant de conversation n’est fourni, le service en crée un. Il enregistre ensuite la question et la réponse dans PostgreSQL, dans les tables applicatives de conversation et de messages.
• En parallèle, MessageChatMemoryAdvisor conserve le contexte de conversation dans le stockage Spring AI, limité aux 20 derniers messages. Le prompt simple bénéficie lui aussi de cette mémoire.

La réponse est actuellement du texte libre (conversationId, answer, date de génération). Le format structuré JSON décrit dans le README n’est pas encore développé. Il n’existe pas non plus de route pour consulter l’historique des messages.

La branche RAG ajoute un autre parcours : POST /aistarter/api/v1/assistant/question/rag. Il calcule l’embedding de la question, recherche jusqu’à cinq fragments dans PgVector selon leur similarité cosinus — seuil actuel de 0,50 — puis ajoute les fragments trouvés au prompt. La réponse inclut les sources utilisées. Ce parcours ne reprend pas l’identifiant ni la mémoire de conversation des autres routes.

Pour l’ingestion, les migrations créent des tables de sources, documents et fragments, et insèrent des sources pour Spring Boot, Spring AI, Kafka, Angular, PostgreSQL et Liquibase. Le service d’ingestion lit un document, le découpe en fragments d’environ 800 tokens, génère les embeddings, puis enregistre le tout dans PostgreSQL/PgVector. Il calcule aussi un hash pour éviter de réingérer un contenu identique et suit le statut de l’ingestion. Des routes permettent de lister les sources et de lancer l’ingestion d’une source ou de toutes les sources.

L’environnement Docker Compose prévoit PostgreSQL, Liquibase, Ollama et l’application. Les migrations sont gérées par Liquibase ; l’initialisation automatique des tables de mémoire et du vector store Spring AI est désactivée. Actuator n’expose actuellement que health et info.

État d’avancement et principaux écarts

Des traces de l’ancienne démo subsistent — par exemple `TravelMcp`, dont les outils sont commentés, et le nom `ai-travel-assistant` dans Compose.

Le README historique indique que la mémoire, la sauvegarde en base et les profils ont été testés manuellement. Un test unitaire couvre maintenant les règles d’URL du lecteur Web ; les parcours complets avec PostgreSQL, Ollama et PgVector restent à valider. Le POM exige une couverture JaCoCo de 85 % pendant `verify`.

Les lecteurs Markdown et Web produisent chacun des documents indexables ; le lecteur Web vérifie l’hôte exact, refuse les redirections, limite la profondeur à 3 et le crawl à 50 pages. La persistance des documents et fragments est maintenant séparée dans un service transactionnel. L’ingestion reste synchrone et les parcours complets doivent être vérifiés avec les services Docker.

Le tool calling/MCP, la génération d’artefacts/PDF et le serveur MCP ne sont pas actifs dans la version actuelle. La compilation de l’état courant n’a pas pu être confirmée : l’exécution du wrapper Maven a échoué à cause du chemin réseau UNC utilisé par l’environnement Windows.

Reste à valider pour terminer la démo Spring AI

1. Confirmer compilation, migrations Liquibase et démarrage Docker complet.
2. Ingérer la source Markdown locale, vérifier les fragments en base et tester une réponse avec ses sources.
3. Vérifier les cas sans résultat, les réingestions idempotentes et une source Web réelle à faible profondeur.
4. Ajouter les tests d’intégration manquants ; garder structured output, outils/MCP, PDF et observabilité pour une étape ultérieure.

TODO :
- (ok) tester la conversation avec la mémoire (vérifier s'il se souvient du prénom par exemple)
- (ok) tester la sauvegarde en BDD des messages et des UUID de conversation
- (ok) tester le profil des messages (expert, pedagogique et simple)
- avancer sur la suite des développements

1. Web service REST + Prompt (ok)
Exemple de prompt :
"Agis comme un Lead Developer Java / Spring possédant 15+ années d'expérience.

Le code doit être :

- production ready
- testable
- maintenable
- conforme aux principes SOLID
- conforme aux bonnes pratiques Spring Boot 4.1
- conforme aux recommandations SonarQube
- adapté à une application d'entreprise à fort volume

Privilégie toujours la qualité de conception, la lisibilité et l'évolutivité plutôt que la rapidité d'implémentation.

Si une approche plus robuste existe, explique-la avant de coder.

%s"

Puis tester avec les questions suivantes :
- Explique Kafka Streams
- Explique les records Java
- Explique les différences entre RAG et Fine-Tuning

2. Structured Answer in JSON format
   public record ExpertAnswerDto(
   String summary,
   List<String> keyPoints,
   List<String> recommendations,
   List<String> references
   ) {}

3. Historisation Postgresql (ok)
- sauvegarder l'historique de conversation (les messages)

Question -> Sauvegarde de la question -> LLM -> Sauvegarde de la réponse
Permet de retrouver l'historique de conversation et d'analyser les questions posées et les réponses fournies.

4. Mémoire conversationnelle (ok)
   Question 1 : Je développe un projet Kafka
   Question 2 : Quelle stratégie de retry me conseilles-tu ?

Le système comprend le contexte de la question 1 pour mieux répondre à la question 2.
Spring AI : MessageChatMemoryAdvisor

5. Personnalisation du profil (ok)

6. Documentation RAG
Documentation officielle Spring Boot, Spring AI, Kafka, Kafka Streams, Angular, PostgreSQL, Liquibase
Ingestion pdf, md, html et txt. 
Embeddings + Vector Store (PgVector)
=> TEST KO : Ingestion des documents ne fonctionne pas (problème de certificat)

7. Sources Documentaires Versionnées
Avec Spring AI 1.1, Kafka 4

8. MCP / Tool Calling

@Tool
findDependency(...)

@Tool
analyzeSonarIssue(...)

@Tool
analyzeException(...)

9. Génération d'artifacts
L'assistant devient un vrai copilote technique (Document d'architecture, ADR, Guide technique, PDF)

10. MCP Server
L'assistant devient réutilisable par : GitHub Copilot, Claude Code, Cursor, autres agents MCP
    searchDocumentation(), analyzeJavaClass(), findSpringBean()

Angular
↓
Spring Boot
↓
Controller
↓
Assistant Service
↓
Chat Client
↓
Advisors
↓
Memory
↓
RAG
↓
Tools
↓
LLM

✅ Structured Output
✅ PostgreSQL
✅ Liquibase
✅ Chat Memory
✅ PgVector
✅ RAG
✅ Tool Calling
✅ MCP
✅ PDF
✅ Observabilité

Pour un assistant expert développeur, le vrai ordre de priorité est :

Web Service
Prompt système robuste
Structured Output
Historisation PostgreSQL
Mémoire conversationnelle
RAG sur documentation technique
Tool Calling
MCP

Le RAG sur la documentation Spring, Spring AI, Kafka et Angular apportera beaucoup plus de valeur métier 
que la mémoire ou le MCP. C'est donc là que j'investirais le plus d'efforts après les premières étapes.


coding-mentor

├── AGENT.md
├── README.md
├── ARCHITECTURE.md
├── PRD.md
│
├── docker
│   ├── ollama
│   ├── postgres
│   └── grafana
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.qbe.codingmentor
│   │   │
│   │   │   ├── CodingMentorApplication.java
│   │   │
│   │   │   ├── config
│   │   │   │   ├── AiConfig.java
│   │   │   │   ├── ChatClientConfig.java
│   │   │   │   ├── OllamaConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │
│   │   │   ├── controller
│   │   │   │   ├── AssistantController.java
│   │   │   │   ├── ConversationController.java
│   │   │   │   └── KnowledgeController.java
│   │   │
│   │   │   ├── service
│   │   │   │   ├── AssistantService.java
│   │   │   │   ├── ConversationService.java
│   │   │   │   ├── MemoryService.java
│   │   │   │   ├── RagService.java
│   │   │   │   ├── PdfService.java
│   │   │   │   └── EmbeddingService.java
│   │   │
│   │   │   ├── advisor
│   │   │   │   ├── DeveloperProfileAdvisor.java
│   │   │   │   ├── ExpertSystemAdvisor.java
│   │   │   │   └── ReferenceAdvisor.java
│   │   │
│   │   │   ├── prompt
│   │   │   │   ├── system
│   │   │   │   │   ├── expert-developer.st
│   │   │   │   │   ├── code-review.st
│   │   │   │   │   └── architecture-review.st
│   │   │   │   │
│   │   │   │   └── user
│   │   │
│   │   │   ├── dto
│   │   │   │
│   │   │   │   ├── request
│   │   │   │   │   ├── AskQuestionRequest.java
│   │   │   │   │   └── CreateConversationRequest.java
│   │   │   │   │
│   │   │   │   ├── response
│   │   │   │   │   ├── ExpertAnswerResponse.java
│   │   │   │   │   ├── ConversationResponse.java
│   │   │   │   │   └── ReferenceResponse.java
│   │   │   │   │
│   │   │   │   └── structured
│   │   │   │       ├── RecommendationDto.java
│   │   │   │       ├── ReferenceDto.java
│   │   │   │       ├── CodeIssueDto.java
│   │   │   │       └── ExpertAnswerDto.java
│   │   │
│   │   │   ├── entity
│   │   │   │   ├── Conversation.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── UserProfile.java
│   │   │   │   └── KnowledgeDocument.java
│   │   │
│   │   │   ├── repository
│   │   │   │   ├── ConversationRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   ├── UserProfileRepository.java
│   │   │   │   └── KnowledgeRepository.java
│   │   │
│   │   │   ├── memory
│   │   │   │   ├── PostgresChatMemoryStore.java
│   │   │   │   ├── ConversationMemoryManager.java
│   │   │   │   └── MemoryMapper.java
│   │   │
│   │   │   ├── rag
│   │   │   │   ├── DocumentIngestionService.java
│   │   │   │   ├── VectorStoreService.java
│   │   │   │   ├── EmbeddingPipeline.java
│   │   │   │   └── MetadataBuilder.java
│   │   │
│   │   │   ├── tools
│   │   │   │   ├── CodeReviewTool.java
│   │   │   │   ├── MavenTool.java
│   │   │   │   ├── SonarTool.java
│   │   │   │   ├── StackTraceTool.java
│   │   │   │   └── DependencyTool.java
│   │   │
│   │   │   ├── mcp
│   │   │   │   ├── McpConfiguration.java
│   │   │   │   ├── DocumentationTool.java
│   │   │   │   ├── ArchitectureTool.java
│   │   │   │   └── SearchKnowledgeTool.java
│   │   │
│   │   │   ├── exception
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── NotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   │   └── mapper
│   │   │       ├── ConversationMapper.java
│   │   │       └── MessageMapper.java
│   │
│   │   └── resources
│   │
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │
│   │       ├── db
│   │       │   └── changelog
│   │       │       ├── db.changelog-master.yml
│   │       │       ├── 001-create-conversation.yml
│   │       │       ├── 002-create-message.yml
│   │       │       ├── 003-user-profile.yml
│   │       │       └── 004-knowledge-document.yml
│   │
│   │       ├── prompts
│   │       │   ├── expert-developer.txt
│   │       │   ├── architecture-review.txt
│   │       │   └── code-review.txt
│   │
│   │       └── knowledge
│   │           ├── spring
│   │           ├── spring-ai
│   │           ├── kafka
│   │           ├── java
│   │           └── angular
│   │
│   └── test
│       └── java
│           └── com.qbe.codingmentor
│               ├── controller
│               ├── service
│               ├── rag
│               ├── tools
│               └── integration
│
├── scripts
│   ├── ingest-docs.sh
│   ├── start-local.sh
│   └── reset-db.sh
│
├── docs
│   ├── rag.md
│   ├── mcp.md
│   ├── architecture.md
│   └── prompt-engineering.md
│
└── docker-compose.yml

## Démonstration RAG locale

La migration Liquibase ajoute une source Markdown locale (`Spring AI RAG demo (local Markdown)`) afin de valider l'ingestion et la recherche sans dépendre d'un site externe. Les sources Web utilisent un lecteur limité à l'hôte `allowed_host`, aux pages HTML, à une profondeur maximale de 3 et à 50 pages par ingestion. Les redirections ne sont pas suivies.

Depuis la racine du dépôt, construire le JAR puis démarrer les services :

```shell
./mvnw -DskipTests package
docker compose --file docker/docker-compose.yml --env-file docker/.env up --build
```

Une fois l'application démarrée, lister les sources, puis copier l'UUID de la source locale :

```shell
curl http://localhost:8080/aistarter/api/v1/documentation/sources
curl -X POST http://localhost:8080/aistarter/api/v1/documentation/sources/<UUID>/ingest
```

Poser une question sur le document ingéré :

```shell
curl -X POST http://localhost:8080/aistarter/api/v1/assistant/question/rag \
  -H "Content-Type: application/json" \
  -d '{"question":"Comment le projet utilise-t-il PgVector pour répondre avec du contexte documentaire ?","responseLevel":"EXPERT"}'
```

La réponse contient le texte généré et les fragments sources avec leur URL et leur score de similarité. La première ingestion génère les embeddings via Ollama ; les suivantes ignorent les pages dont l'URL et le contenu n'ont pas changé. Pour éviter les téléchargements et le temps de traitement variables, utiliser l'UUID de la source Markdown plutôt que la route `ingest-all` pendant la démo.
