# RTS-BDD — Sélection de Tests de Régression pour projets BDD

Outil Java qui, face à une **nouvelle exigence** décrite en Gherkin, identifie automatiquement les scénarios BDD existants les plus similaires et génère la commande Maven pour ne rejouer que les tests impactés.

---

## Contexte

Dans un projet BDD, chaque nouvelle fonctionnalité implique de rejouer l'intégralité de la suite de tests, ce qui peut être coûteux. RTS-BDD applique des techniques de **traitement automatique du langage naturel (TF-IDF + similarité cosinus)** sur les textes des scénarios Gherkin pour sélectionner uniquement les tests pertinents.

---

## Architecture

```
src/main/java/com/rts/
├── Main.java                          # Point d'entrée démo (scénarios en dur)
├── MainWithAnnotations.java           # Point d'entrée production (CLI)
├── parser/
│   └── GherkinParser.java             # Parsing .feature → Feature / Scenario / Step
├── comparaison/
│   └── ComparateurScenario.java       # Vectorisation TF-IDF + similarité cosinus
├── engine/
│   └── ComparaisonEngine.java         # Orchestrateur : parse → compare → sélectionne
├── RTSengine/
│   └── TestMappingLoader.java         # Liaison scénario ↔ tests d'acceptance ↔ tests unitaires
└── model/
    ├── Feature.java
    ├── Scenario.java
    ├── Step.java
    ├── ComparaisonResult.java
    ├── ScenarioSimilaire.java
    └── RTSResult.java
```

---

## Pipeline de sélection

```
Fichiers .feature existants
        │
        ▼
  GherkinParser          → Feature / Scenario / Step
        │
        ▼
TestMappingLoader        → liaison Scénario ↔ AT (Steps.java) ↔ UT (*Test.java)
        │
        ▼
ComparateurScenario      → TF-IDF + cosinus  (Apache Lucene EnglishAnalyzer)
        │
        ▼
ComparaisonEngine        → ranking + stratégie de sélection
        │
        ▼
RTSResult                → rapport + commande mvn test
```

### Stratégies de sélection disponibles

| Méthode | Description |
|---|---|
| `comparerTopN(scenario, n)` | Retourne les N scénarios les plus proches |
| `comparerParSeuil(scenario, seuil)` | Retourne tous les scénarios au-dessus d'un score |
| `comparerKneePoint(scenario, min)` | Détection automatique du coude (plus grand saut de score) |

---

## Prérequis

- Java 17+
- Maven 3.6+

---

## Compilation

```bash
mvn clean compile
```

---

## Utilisation

### Mode démo (scénarios en dur)

Lance une comparaison sur des scénarios prédéfinis :

```bash
mvn exec:java -Dexec.mainClass="com.rts.Main"
```

### Mode production (fichiers .feature réels)

```bash
mvn exec:java \
  -Drts.features=<chemin/vers/features> \
  -Drts.testSource=<chemin/vers/src/test/java> \
  -Drts.newScenario=<chemin/vers/new_requirement.feature> \
  [-Drts.minSelection=3]
```

**Paramètres :**

| Paramètre | Description | Obligatoire |
|---|---|---|
| `rts.features` | Répertoire contenant les `.feature` existants | Oui |
| `rts.testSource` | Racine des sources de test Java | Oui |
| `rts.newScenario` | Fichier `.feature` décrivant la nouvelle exigence | Oui |
| `rts.minSelection` | Nombre minimum de scénarios à retourner (défaut : 3) | Non |

**Exemple :**

```bash
mvn exec:java \
  -Drts.features=src/test/resources/features \
  -Drts.testSource=src/test/java \
  -Drts.newScenario=new_requirement.feature \
  -Drts.minSelection=3
```

**Sortie attendue :**

```
=== RTS-BDD — Sélection de tests de régression ===

Scénarios existants chargés : 9

Mapping AT/UT par scénario :
─────────────────────────────────────────
  Import task configurations from file   → AT: ImportSteps   → UT: 2

─────────────────────────────────────────
Nouveau scénario : Import URLs from file
  Given a file "url.txt" with one URL per line
  When  a user imports URLs from the file as the seed
  Then  the system should read "url.txt"

Scénarios sélectionnés (knee point, min=3) :
  ✔ [0.8321] Import proxy servers from file
  ✔ [0.7654] Import URL list from CSV
  ✔ [0.6102] Import task configurations from file

Commande Maven :
  mvn test -Dtest="ImportSteps,TxtFileReaderTest,FileImporterTest"
```

---

## Mapping AT / UT

`TestMappingLoader` parcourt automatiquement les sources de test pour établir les correspondances :

1. **Acceptance Tests** : fichiers `*Steps.java` / `*StepDefs.java` contenant les annotations `@Given`, `@When`, `@Then`
2. **Tests unitaires** : fichiers `*Test.java` dont les imports référencent les mêmes classes de production que les Steps

Ce mapping permet à la commande générée de cibler à la fois les tests d'acceptance Cucumber et les tests unitaires JUnit associés.

---

## Algorithme NLP

La similarité entre deux scénarios est calculée ainsi :

1. **Extraction du texte** : nom du scénario + texte de chaque étape
2. **Prétraitement** (via Apache Lucene `EnglishAnalyzer`) : tokenisation, suppression des stop-words, stemming
3. **Vectorisation TF-IDF** : TF local au scénario × IDF sur l'ensemble du corpus
4. **Similarité cosinus** entre le vecteur du nouveau scénario et chaque vecteur existant
5. **Knee point** : détection du plus grand écart entre deux scores consécutifs dans le ranking pour couper automatiquement la liste

---

## Tests

```bash
mvn test
```

---

## Dépendances

| Bibliothèque | Version | Usage |
|---|---|---|
| JUnit Jupiter | 5.10.2 | Tests unitaires |
| Apache Lucene (`lucene-analyzers-common`) | 8.11.3 | Tokenisation NLP (EnglishAnalyzer) |

---

## Structure des fichiers .feature attendus

```gherkin
Feature: Nom de la fonctionnalité

  Scenario: Nom du scénario
    Given une précondition
    When  une action
    Then  un résultat attendu
```

Les mots-clés supportés sont : `Feature`, `Scenario`, `Scenario Outline`, `Given`, `When`, `Then`, `And`, `But`.
