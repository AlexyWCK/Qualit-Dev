# TP Exercice 1 : Analyser l'application Order Flow

_Document de réponse, généré en analysant le code source et la documentation du dépôt._

---

## Description rapide
Order Flow est une application composée de microservices, dont les principaux objectifs sont
- la gestion d'un catalogue de produits (Product Registry),
- la gestion d'une boutique (Store),
- une interface front (store-front) et des services de lecture/écriture séparés (CQRS).

Le projet est structuré avec des apps (Quarkus) et des libs partagées. Cela permet d'assurer la fiabilité des échanges.

---

## Tâche 1 : Ségrégation des responsabilités

### 1. Principaux domaines métiers

- **Gestion du catalogue (Product Registry)** : création, modification, retrait des produits (Register / Update / Retire).
- **Gestion de la boutique (Store)** : API métier pour la boutique (façade vers domain/read services).
- **Lecture/Projection (Read Models)** : indexation et lecture optimisée des représentations projetées des produits (ProductView).

Ces domaines sont séparés en bounded contexts implémentés par des microservices indépendants et par des bibliothèques (libs) partagées.

### 2. Conception des services (liens techniques)

- **Separation Command/Query (CQRS)** : Le service product-registry-domain-service s’occupe de tout ce qui modifie les produits (ajout, mise à jour, suppression).
Le service product-registry-read-service s’occupe uniquement de lire les infos grâce à des « projections » (des vues déjà préparées pour être lues).
- **Event Driven Architecture** : Quand quelque chose se passe (ex : un produit enregistré), un événement est créé et enregistré dans une table spéciale (event_log).
Cet événement sera ensuite envoyé à d’autres services, qui mettront leurs données à jour.
- **Shared kernel** : `libs/kernel` expose les entités du domaine (Product, ProductId, SkuId, ProductView, ProductEventV1 etc.).
- **Contracts** : Le dossier `libs/contracts/product-registry-contract` contient les DTO, c’est-à-dire les formats des données envoyées par les API.
Tous les services utilisent les mêmes formats pour être sûrs de bien se comprendre.

### 3. Responsabilités par module (exemples / fichiers clés)

- `apps/store-back`

  C’est le backend “principal” du store.

  Il expose des endpoints (API) et appelle les services qui gèrent les produits (écriture + lecture).

  Exemple : `ProductRpcResource` qui fait le lien entre l’API et la logique métier.

- `apps/store-front`

  C’est la partie front (Angular).

  Gère l’affichage, les pages, et envoie les requêtes au backend.

  En gros : tout ce que l’utilisateur voit.

- `libs/kernel`

  Contient les classes métier importantes et partagées : les produits, les IDs, les événements, les vues…

  C’est un peu la “base commune” utilisée par tous les services.

- `apps/product-registry-domain-service`

  Service d’écriture : ajoute, modifie ou retire des produits.

  C’est lui qui crée les événements et les enregistre quand quelque chose change.

- `apps/product-registry-read-service`

  Service de lecture : met à jour les vues (projections) à partir des événements.

  Sert à avoir des lectures rapides et déjà prêtes.

- `libs/bom-platform`

  Juste un module pour centraliser les versions des dépendances du projet.

  Permet que tout soit cohérent dans les autres modules.

- `libs/cqrs-support`

  Contient les outils pour gérer le CQRS et les événements.

  Fournit les classes de base, les interfaces, les repositories pour l’event log et l’outbox.

- `libs/sql`

  Contient les fichiers Liquibase pour créer les tables du projet (produits, event log, projections…).

  C'est la partie qui “structure la base de données”.

---

## Tâche 2 : Concepts principaux utilisés (implémentation)

### Concepts identifiés

- **DDD (Domain Driven Design)**
  - Aggregates / Entities : `Product` dans `libs/kernel`.
  - Value objects : `ProductId`, `SkuId`.
  - Invariants : méthodes métier qui vérifient l'état (ex: `Product.updateName()` and `retire()` throwing exceptions for invalid state changes).

 - **Architecture orientée événements**
  - `Event Log` (`libs/cqrs-support`) : l'entité `EventLogEntity` persiste les événements (champs `aggregateType`, `aggregateId`, `aggregateVersion`, `payload`).
  - `Outbox` (`OutboxEntity`) : enregistre les messages prêts à être publiés afin de garantir une livraison fiable vers d'autres services.

 - **CQRS (Command / Query Responsibility Segregation)**
  - Les écritures (commands) et les lectures (queries) sont séparées.
  - Les services d'écriture (ex. `RegisterProductService#handle`) persistant les agrégats et ajoutent des événements au `EventLog`.
  - Les services de lecture (ex. `ProductViewProjector`) consomment les événements et mettent à jour les vues (`ProductView`).

 - **Pattern Outbox pour une livraison fiable des événements**
  - Le côté écriture utilise une transaction (`@Transactional`) pour modifier l'état métier, écrire l'événement dans `event_log` et ajouter une entrée dans `outbox` pour publication asynchrone.
  - Le côté lecture interroge l'outbox (ex. `OutboxPartitionedPoller`) pour récupérer et appliquer les projections.

 - **Enveloppes d'événements et versioning**
  - Les événements sont versionnés (ex. `ProductEventV1`). Les enveloppes d'événement (`EventEnvelope`, `ProductEventV1Envelope`) conservent la séquence et l'horodatage pour assurer l'ordre et la traçabilité.

### Stockage & transactions

- **Stockage** : base de données relationnelle (SQL) ; les changements de schéma sont gérés via Liquibase (`libs/sql/`).
- **Transactions** : les opérations d'écriture sont encapsulées dans des transactions Quarkus (`@Transactional`) pour garantir l'atomicité de la sauvegarde métier et l'enregistrement dans `event_log`/`outbox`.
  - Exemple : `RegisterProductService#handle` est annoté `@Transactional` et réalise la sauvegarde du `Product`, l'ajout d'un `EventLogEntity` et la création d'un `OutboxEntity`.

