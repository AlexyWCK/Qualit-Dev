
# TP Exercice 4 : Projection des événements dans des vues matérialisées

## Tâche 1 : Questions sur la base de code

- **Interface Projector :** applique un événement à une vue matérialisée et met à jour son état.

- **Type S :** représente l'état de la vue matérialisée (DTO ou entité).

- **Javadoc pour S :** S est le modèle de lecture mis à jour par les événements.

- **Pourquoi interface Projector :** permet plusieurs implémentations, facilite les tests et le découplage.

- **Rôle de ProjectionResult :** contient le résultat d’un événement (succès, échec, nouvel état).

- **Intérêt de la monade :** gère succès/erreur sans exceptions, facilite la composition et les tests.

## Tâche 2 : Outboxing

- **OutboxRepository :** stocke les événements à publier et suit leur état (PENDING, SENT, FAILED).

- **Garantie de livraison :** écrire l’état métier + outbox dans la même transaction ; le dispatcher lit ensuite et publie.

**Fonctionnement concret :**

- Commande -> écriture entités + insert outbox dans la même transaction.

- Transaction commitée.

- Dispatcher lit outbox PENDING -> publie au broker → update status ou retry.

- **Gestion des erreurs :** retries, backoff, marquage FAILED, éventuellement dead-letter table.

**Diagramme simplifié :**

```
Service -> DB (domain + outbox)
Outbox Dispatcher -> DB.outbox -> Broker -> Consumers
```

## Tâche 3 : Journal d'événements

- **Rôle :** archive append-only pour audit, replay et traçabilité.

- **EventLogRepository.append :** seule méthode pour garantir l’immuabilité.

- **Implications :**

	- On peut reconstruire les projections par replay.

	- Pas de suppression -> audit simple mais nécessité de snapshots.

	- Autres usages : audit, rapports, debugging, analytics.

## Tâche 4 : Limites de CQRS

- **Limites :** complexité, cohérence éventuelle, duplication de données, versioning des événements.

- **Limites compensées :** Outbox -> fiabilité, EventLog -> replay des projections.

- **Nouvelles limites :** synchronisation de plusieurs projections, tests plus nombreux, versioning.

- **Projections multiples :** un événement peut déclencher plusieurs projections, pas d’atomicité cross-projections -> incohérences temporaires.
- **Solutions :** idempotence, versioning, Sagas/orchestrateurs, monitoring, snapshots et compactage. 