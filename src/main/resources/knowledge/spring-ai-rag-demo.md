# Démonstration RAG avec Spring AI

Cette source locale sert à valider le pipeline RAG sans dépendre de la disponibilité d'un site externe. Elle décrit le fonctionnement attendu de la démonstration Spring AI du projet.

## Pipeline d'ingestion

Une source documentaire activée est lue, puis son contenu est découpé en fragments d'environ 800 tokens. Chaque fragment reçoit un embedding calculé par le modèle d'embedding Ollama configuré dans l'application. Les embeddings ont 768 dimensions et sont stockés dans PostgreSQL avec l'extension PgVector, avec le texte, l'index du fragment et les métadonnées du document.

Le document conserve son titre, son URL canonique, sa version, son type de contenu et une empreinte SHA-256 de son contenu. Une réingestion du même contenu à la même URL est ignorée. Si le contenu change, une nouvelle version du document est ajoutée.

## Recherche et génération

Pour répondre à une question, le service calcule l'embedding de la question, puis recherche les fragments les plus proches avec une distance cosinus. La configuration actuelle sélectionne au plus cinq fragments et écarte ceux dont la similarité est inférieure à 0,50.

Les fragments retenus sont ajoutés au prompt système comme contexte documentaire. Le modèle doit privilégier ce contexte, distinguer les connaissances générales des informations documentées et ne pas inventer de sources. La réponse RAG retourne également les titres, URLs, indices de fragments et scores de similarité des sources récupérées.

Si aucun fragment n'est suffisamment pertinent, le modèle est invité à signaler qu'aucune documentation n'a été trouvée et à répondre sans prétendre avoir consulté une source officielle. Cette route RAG ne partage pas actuellement la mémoire conversationnelle des routes de questions classiques.

## Exemple de démonstration

Après ingestion de cette source, demander : « Comment le projet utilise-t-il PgVector pour répondre à une question avec du contexte documentaire ? » La réponse doit expliquer l'embedding de la question, la recherche par similarité cosinus et l'ajout des fragments au prompt, avec cette source dans la liste des références.
