# TP Exercice 2 : Corriger les problèmes de qualité et introduire des tests

_Document de réponse, couvrant la correction des problèmes de qualité et l'implémentation de tests._

---

## Description rapide

Cet exercice se concentre sur l'amélioration de la qualité du code au sein du service `product-registry-domain-service` (côté métier OLTP). L'objectif est de compléter la Javadoc, d'exécuter MegaLinter pour identifier et corriger les défauts de qualité, puis d'ajouter des tests unitaires et d'intégration pour valider le comportement métier de l'application.

---

## Tâche 1 : Compléter les commentaires et la Javadoc

Complétez toute Javadoc manquante marquée `TODO: Complete Javadoc` dans le code.

**Format standard** :
- Décrire la responsabilité de la classe (une ligne).
- Documenter chaque méthode publique (paramètres, retour, exceptions).
- Ajouter @author, @version pour les classes principales.

**Fichiers à compléter** : tous les fichiers avec `TODO: Complete Javadoc` (Product, ProductEventV1, ProductId, SkuId, repositories, DTOs, services, ressources, etc.)

---

## Tâche 2 : Corriger les erreurs et les problèmes de qualité remontés par MegaLinter

### Installation et exécution

```bash
# Installer MegaLinter runner
pnpm install mega-linter-runner -D -w

# Générer la configuration de base
pnpm mega-linter-runner --install

# Exécuter MegaLinter sur l'espace de travail
pnpm mega-linter-runner -p $WORKSPACE_ROOT
```

### Configuration `.mega-linter.yml`

```yaml
APPLY_FIXES: none          # Changer à 'all' pour auto-correction en développement
CLEAR_REPORT_FOLDER: true
VALIDATE_ALL_CODEBASE: true

ENABLE:
  - JAVA

DISABLE:
  - REPOSITORY

FILTER_REGEX_EXCLUDE: '(build/|target/|\.gradle/|node_modules/)'
```

### Résultats et correction

Le rapport MegaLinter sera généré dans `megalinter-reports/`. Corrigez :
- **Javadoc manquante** : ajouter ou compléter les commentaires.
- **Violations de style Java** : indentation, nommage, longueur de ligne.
- **Code smells** : méthodes complexes, duplication, variables inutilisées.

Les corrections automatiques suggérées se trouvent dans `megalinter-reports/updated_sources/`.

---

## Tâche 3 : Tests unitaires pour Product

Créez `libs/kernel/src/test/java/org/ormi/.../ProductTest.java` avec des tests pour :
- **Création** : produit valide, entrées invalides (null, vide), validation des états
- **Mise à jour nom/description** : produit actif OK, produit retiré NOK, entrées invalides
- **Retrait** : produit actif OK, produit retiré déjà NOK

Structure : utiliser JUnit 5 avec `@Nested` pour organiser les groupes de tests.

**Exécution** :
```bash
gradle :libs:kernel:test
gradle test jacocoTestReport
```

---

## Tâche 4 : Tests d'intégration

---

## Tâche 3 : Tests unitaires pour Product

Créez `libs/kernel/src/test/java/org/ormi/.../ProductTest.java` avec des tests pour :
- **Création** : produit valide, entrées invalides (null, vide), validation des états
- **Mise à jour nom/description** : produit actif OK, produit retiré NOK, entrées invalides
- **Retrait** : produit actif OK, produit retiré déjà NOK

Structure : utiliser JUnit 5 avec `@Nested` pour organiser les groupes de tests.

**Exécution** :
```bash
gradle :libs:kernel:test
gradle test jacocoTestReport
```

---

## Tâche 4 : Tests d'intégration

### Tests pour ProductRegistryCommandResource

Créez `apps/product-registry-domain-service/src/test/.../ProductRegistryCommandResourceTest.java` pour tester :
- **POST /api/products/register** : créer avec données valides (201), invalides (400), body null (400)
- **PATCH /api/products/{id}/name** : mettre à jour OK (204), invalide (400), not found (404)
- **DELETE /api/products/{id}** : supprimer OK (204), not found (404)

### Tests pour ProductRegistryQueryResource

Créez `apps/product-registry-read-service/src/test/.../ProductRegistryQueryResourceTest.java` pour tester :
- **GET /api/products** : tous les produits (200), filtrer par SKU, liste vide
- **GET /api/products/{id}** : produit existant (200), non existant (404)

**Utiliser** : `@QuarkusTest`, RestAssured, `@Nested` pour l'organisation.

**Dépendances** (`build.gradle`) :
```gradle
testImplementation 'io.quarkus:quarkus-junit5'
testImplementation 'io.rest-assured:rest-assured'
testImplementation 'org.mockito:mockito-core'
testImplementation 'org.mockito:mockito-junit-jupiter'
```

```

---

## Tâche 5 : Questions

### Question 1 : Différence entre tests unitaires et tests d'intégration

**Tests unitaires** : testent une petite unité de code (fonction, méthode, classe) en isolation. Les dépendances externes sont généralement mockées. Ils sont rapides et spécifiques.

Exemple : tester `Product.create()` directement sans base de données.

**Tests d'intégration** : testent plusieurs composants ensemble pour vérifier qu'ils fonctionnent correctement en interaction. Ils utilisent de vraies dépendances (base de données, serveur HTTP, etc.). Ils sont plus lents mais valident le comportement réel.

Exemple : tester l'endpoint HTTP `/api/products` qui appelle le service métier, persiste en base, et crée des événements.

**Synthèse** : Les tests unitaires isolent la logique métier ; les tests d'intégration valident que tous les composants marchent ensemble.

### Question 2 : Est-il pertinent de couvrir 100% du code par des tests ?

**Non, pas systématiquement.** Voici pourquoi :

1. **Code trivial** : les getters/setters simples ne bénéficient pas beaucoup d'être testés.
2. **Coût/bénéfice** : certains chemins d'erreur rarissimes ou getters en cascade ne justifient pas le temps dédié.
3. **Coûts de maintenance** : plus de tests = plus de code à maintenir et à adapter lors de refactorisations.
4. **Rendement décroissant** : passer de 80% à 100% de couverture coûte bien plus cher que passer de 0% à 80%.

**Approche pragmatique** :
- Couvrir **la logique métier critique** (créations, transitions, validations).
- Couvrir **les cas d'erreur** fréquents et graves.
- Ne pas couvrir les **getters/setters triviaux**.
- Viser **70-80%** de couverture globale, puis raffiner selon les besoins.

### Question 3 : Avantages de l'architecture en couches (oignon) pour les tests

**Architecture en couches (oignon)** :
- **Couche métier (kernel)** : logique pur, sans dépendances externes.
- **Couche application** : orchestration des cas d'usage.
- **Couche infrastructure (JPA, REST)** : accès aux ressources externes.

**Avantages pour les tests** :

1. **Isolation du métier** : la logique métier (`Product`) peut être testée sans base de données, sans HTTP. Les tests sont rapides et déterministes.
   - Exemple : `ProductTest` n'a besoin que du constructeur et des méthodes.

2. **Tests réutilisables** : les tests du kernel peuvent être exécutés n'importe où, rapidement.

3. **Chaque couche a un type de test** :
   - Kernel → tests unitaires (rapides, aucune I/O).
   - Application → tests unitaires + mocking des repositories.
   - Infrastructure → tests d'intégration (avec vraie base de données/HTTP).

4. **Facilité à mocker** : les dépendances injectées (repositories, services) sont faciles à mocker pour isoler les tests.

**Exemple observé** : `ProductTest` teste uniquement `Product` sans `EventLogRepository`, sans base de données. Cela rend les tests :
- **Rapides** (< 1ms par test).
- **Isolés** (pas de dépendance externe).
- **Répétables** (pas d'état partagé).

### Question 4 : Nomenclature des packages

#### `infra` (Infrastructure)
Contient les implémentations techniques : accès à la base de données, mappers JPA, repositories concrètes.

```
infra/
  ├── jpa/
  ├── api/
  ├── rest/
  └── web/
```


#### `application`
Couche application : orchestration des cas d'usage, services de commande/requête.

```
application/
  ├── ProductCommand
  ├── RegisterProductService
  ├── UpdateProductService 
  └── RetireProductService  
```

