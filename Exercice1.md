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

## Tâche 3 : Identifier les problèmes de qualité

### Installation MegaLinter

```bash
pnpm install mega-linter-runner -D -w
```

Modifier `pnpm-workspace.yaml` :
```yaml
catalog:
  mega-linter-runner: "^9.0.0"
```

Générer la configuration :
```bash
pnpm mega-linter-runner --install
```

### Configuration .mega-linter.yml

```yaml
APPLY_FIXES: none
CLEAR_REPORT_FOLDER: true
VALIDATE_ALL_CODEBASE: true

ENABLE: 
  - JAVA

DISABLE: 
  - REPOSITORY

FILTER_REGEX_EXCLUDE: '(build/|target/|\.gradle/|node_modules/)'
```

### Problèmes potentiels à identifier

**Structure du code**
- Couplage entre modules
- Dépendances circulaires
- Responsabilités mal définies

**Qualité du code**
- Violations de conventions Quarkus
- Code dupliqué
- Méthodes trop complexes
- Classes avec trop de responsabilités

**Configuration**
- Incohérences entre modules
- Dépendances en double
- Versions incompatibles

**Tests**
- Couverture insuffisante
- Tests manquants
- Tests obsolètes

**Documentation**
- Documentation manquante
- Javadoc incomplète
- README insuffisants

**Sécurité**
- Vulnérabilités dans les dépendances
- Validation d'entrées manquante

**Performance**
- Requêtes N+1
- Absence de caching
- Gestion inefficace des ressources

### Recommandations

1. Analyser module par module
2. Prioriser : Sécurité > Bugs > Style
3. Documenter les exceptions acceptables
4. Créer un plan de corrections
5. Établir des standards d'équipe

### Pipeline de traitement des événements

1. Le service de domaine exécute une commande, modifie l'agrégat et persiste l'état métier.
2. Le service de domaine ajoute un événement dans `event_log` (`EventLogEntity`) via `EventLogRepository`.
3. Dans la même transaction, le service crée une entrée dans l'outbox (`OutboxEntity`) via `OutboxRepository` pour publication asynchrone.
4. Les services de lecture (ou les consommateurs) interrogent l'outbox (`OutboxPartitionedPoller`) pour consommer les événements et mettre à jour les vues (`ProductView`) via des `Projector`.


### Formats d'échanges

- Les services exposent des endpoints REST et utilisent des DTO partagés (`libs/contracts/product-registry-contract`).
- La communication asynchrone inter-services est implémentée via le pattern Outbox (actuellement via la base de données + poller plutôt que via un broker comme Kafka).

### Implémentation des concepts (bibliothèques & modules)

 - `libs/cqrs-support` :
  - Implémente la persistance des événements (`EventLogEntity`) et l'outbox (`OutboxEntity`), les repositories associés et l'abstraction de projection (`Projector` et `ProjectionResult`).
  - Fournit des mappers JPA (`EventLogJpaMapper`) et des implémentations JPA comme `JpaEventLogRepository` et `JpaOutboxRepository` utilisées par les services domaines et de lecture.

- `libs/bom-platform` :
  - Centralise les versions des dépendances (BOM) pour assurer la compatibilité entre les modules.

- `libs/kernel` :
  - Définit le modèle du domaine partagé (`Product`, `ProductView`, `ProductEventV1`), propose des mappers (ex. `ProductEventJpaMapper`) et fournit les identifiants (`ProductId`, `SkuId`).
  - Le kernel partagé garantit une sémantique métier cohérente entre les services.

### Fiabilité des états internes (CQRS + Kernel)

- Écritures atomiques + outbox : les écritures d'état métier et l'ajout d'événements se réalisent dans la même transaction de base de données pour garantir l'atomicité.
- Durabilité de la publication : l'outbox garantit que les événements restent disponibles pour publication même si le service de diffusion échoue après le commit ; le service de lecture interroge l'outbox et traite les messages.
- Partitionnement et préservation de l'ordre : `OutboxPartitionedPoller` partitionne par `aggregateId` (ex. `partition = Math.floorMod(aggregateId.hashCode(), PARTITIONS)`) pour garantir l'ordre des événements d'un même agrégat.
- Idempotence des projections et contrôle de séquence : les `Projector` comparent `ev.sequence()` avec `current.get().getVersion()` pour ignorer les événements obsolètes (no-op) et éviter les doubles traitements.
- Gestion des erreurs et réessais : le service de lecture marque les entrées d'outbox en échec (`markFailed`) et planifie des réessais (ex. `blockedUntil`, `retryAfter`).

---

## Tâche 3 : Identifier les problèmes de qualité

### Aperçu des points observés (manuel, avant exécution de MegaLinter)

1. **TODOs non complétés & JavaDoc manquante** : plusieurs fichiers contiennent TODO (e.g., javadoc manquant pour `Product.java`, `EventLogJpaMapper.java`, `EventLogRepository`). Cela sera signalé par MegaLinter.
   - Fichiers avec `TODO`: `libs/kernel/*` (Product, ProductView), `libs/cqrs-support/*` (EventLogEntity, OutboxEntity), apps `ProductStreamResource` endpoints.

2. **Fonctionnalités non-implémentées (exercices)** : certaines API de streaming SSE (`ProductStreamResource`), ProductEventBroadcaster, et certains endpoints dans `store-back` sont marqués `TODO`.

3. **Tests** : couverture de tests limitée (vérifier avec `gradle test` et les rapports `jacoco`). Beaucoup de composants métier (Projectors, Repositories, Services) devraient avoir des tests unitaires et d’intégration.

4. **Validation & Input Checks** : Certaines validations d'API sont rudimentaires (ex: `ProductRpcResource.updateProduct` checks), mais du côté client certains inputs ne sont pas validés/détaillés.

5. **Gestion des exceptions et observabilité** :
  - `EventLogEntity` mentionne des TODOs concernant les métadonnées (correlation, causation, tenant, shardKey). Ces métadonnées sont importantes pour le débogage et l'observabilité.
  - Les erreurs sont parfois renvoyées de manière générique (ex : `IllegalArgumentException`) plutôt que via des exceptions métier explicites mappées en codes HTTP 4xx/5xx.

6. **Idempotence (inter-services)** : Pas d’indication de mécanisme d'idempotence pour les appels REST inter-services — il est recommandé d'ajouter des clés d'idempotence (`idempotency key`) si des appels sont ré-essayables.

7. **Configuration** : vérifier fichiers `.yaml` pour cohérence entre services (profiles, datasources, ports).

8. **Architecture & Design** :
   - Certaines parties fortes du modèle (ex: Event versioning) sont en place, mais il manque des tests sur compatibilité des événements / migration des payloads.
   - Vérifier l'utilisation d'un Outbox adapter/worker stable pour la publication réelle (vers Kafka ou autre), aujourd’hui on a la structure DB+poller.

### Suggestions d'améliorations (prioritaires)

1. **Ajouter des tests unitaires et d'intégration** : Projectors, repositories JPA, services de commande et poller d'outbox.
2. **Compléter / enlever les TODO** : Javadoc, commentaires et endpoints non implémentés (SSE streaming).
3. **Améliorer la gestion des erreurs** : standardiser les exceptions métier et techniques, et les mapper en réponses HTTP/codes de statut appropriés.
4. **Métadonnées sur EventLog** : implémenter `correlation`/`causation` pour le traçage distribué entre services.
5. **Linting & CI** : Configurer MegaLinter : activer les contrôles Java, désactiver certaines vérifications de type repository (leaks) si nécessaire, et appliquer des corrections automatiques (`APPLY_FIXES: none` / `all` selon l'étape du workflow).
6. **Observabilité** : ajouter des logs et des métriques pour le poller et les projections.
7. **Idempotence** : ajouter des mécanismes explicites d'idempotence pour les endpoints REST et les événements lorsqu'il y a des appels ré-essayables.
