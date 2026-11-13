# ai-starter

ai-travel-assistant

src/main/java/com/qbe/aistarter

├── config
│   └── OpenAiConfiguration
│
├── controller
│   └── TravelController
│
├── service
│   └── TravelService
│
├── dto
│   ├── TravelRequest
│   └── TravelResponse
│
└── TravelAssistantApplication


POST /api/travel/generate
{
"destination": "Rome",
"duration": 5,
"budget": 1500,
"interests": [
"history",
"food"
]
}

Controller
↓
Service
↓
OpenAI
↓
Retour texte

Exemple de réponse :
{
"plan": "Jour 1..."
}


MVP (Minimum Viable Product) : L'idée est de développer la plus petite version possible du produit qui apporte déjà de la valeur.

Modifier la réponse pour qu'elle ressemble à ceci :

Réponse :
{
"destination": "Paris",
"summary": "...",
"days": [
{
"day": 1,
"activities": ["Tour Eiffel","Montmartre"]
}
]
}

L'appel renvoie des réponses. Il faut améliorer globalement le projet.
Donner un meilleur prompt, structurer la réponse pour qu'elle soit plus facile à utiliser dans le front-end.
Demander à l'IA Copilot si d'autres améliorations sont possibles pour le projet.

record TripRequest(
String destination,
int durationInDays,
double budgetInEuros,
List<String> interests
) {}

public record TripResponse(
TripSummary summary,
List<TripDay> days,
BudgetBreakdown budgetBreakdown,
List<String> travelTips
) {}

public record TripSummary(
String destination,
int durationInDays,
double budgetInEuros,
double estimatedTotalCost,
String overview
) {}

public record TripDay(
int dayNumber,
String theme,
List<Activity> morning,
List<Activity> afternoon,
List<Activity> evening,
double estimatedDailyCost
) {}

public record Activity(
String name,
String description,
String location,
double estimatedCost,
int durationMinutes,
String reason
) {}

public record BudgetBreakdown(
double accommodation,
double food,
double transportation,
double activities,
double miscellaneous,
double total
) {}


Scénarios à rajouter : 
1. Conversation contextuelle
ChatMemory, MessageChatMemoryAdvisor (ok)

2. Ajouter un profil (ok, à tester)
  (solo, en couple, en famille avec X enfants, luxury traveller, budget traveler, etc.) pour personnaliser les recommandations.

3. Générer un PDF en sortie (ok, à tester)

4. Très important : ajouter du RAG (VectorStore et EmbeddingModel dans Spring AI) (ok, à tester)
   Question -> Recherche documentaire -> Contexte injecté -> Réponse
   La réponse provient des documents fournis par l'utilisateur (PDF, DOCX, TXT, CSV, etc.) et non pas uniquement de l'IA.

5. Export ICS (importable dans un calendrier Outlook, Google Calendar, Apple Calendar, etc.)
  
6. Intégrer un MCP filesystem ou postgresql (très important) (ok, à tester)
   Répondre à des questions comme "Quels sont les voyages créés cette semaine ?"

7. Génération d'images avec DALL-E, Stable Diffusion ou autre.
Prompt : "Generate a travel poster for this trip."
Réponse : Paris vacation poster
L'image peut être utilisée dans le PDF ou sur le front-end.

8. Nouvelle fonctionnalité de comparaison de destinations : 
l'utilisateur peut demander à l'IA de comparer deux destinations et de fournir un résumé des avantages et inconvénients 
de chaque destination.

9. Agent multi-étapes
A la place d'un prompt unique, l'IA peut poser des questions à l'utilisateur pour affiner la réponse.
- Analyse du budget
- Recherche des activités disponibles
- Répartition journalière des activités
- Calcul des coûts estimés
- Génération du plan de voyage final
Dans SpringAI : Advisor, Tool Calling, Function Calling

Stack :
Spring Boot
Spring AI
Ollama
Chat Memory
RAG (Vector Store)
PDF Export
MCP Filesystem

Architecture finale :

┌───────────────────────┐
│ Frontend Angular      │
└──────────┬────────────┘
│
▼
┌───────────────────────┐
│ Spring Boot API       │
└──────────┬────────────┘
│
▼
┌───────────────────────┐
│ Spring AI Agent       │
└──────┬───────┬────────┘
│       │
│       ├──────── Chat Memory
│
├──────── Vector Store (RAG)
│
├──────── MCP Filesystem
│
├──────── MCP PostgreSQL
│
├──────── PDF Export
│
└──────── Calendar Export
│
▼
Ollama
│
▼
Llama3