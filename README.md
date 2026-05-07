# TP Qualité et Test — Calculatrice TVA

> Projet d'examen **Qualité et Test** — automatisation des tests d'une calculatrice de TVA en ligne avec **Selenium + TestNG + Excel DataProvider + ExtentReports**.

**Binôme :** Mohamed Dhia **SELMI** & Fedi **TRABELSI**
**Établissement :** ISI — Institut Supérieur d'Informatique, Université de Tunis El Manar
**Site testé :** [ma-calculatrice.fr/calculer-tva-ttc-ht-convertir](https://www.ma-calculatrice.fr/calculer-tva-ttc-ht-convertir)

---

## Sommaire

- [Objectif](#objectif)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation & exécution](#installation--exécution)
- [Données de test](#données-de-test)
- [Règles de validation](#règles-de-validation)
- [Rapports](#rapports)
- [Exécution parallèle](#exécution-parallèle)
- [Résultats](#résultats)
- [Auteurs](#auteurs)

---

## Objectif

Automatiser la validation fonctionnelle des trois modes de calcul de la calculatrice TVA :

| Méthode de test         | Entrée fournie | Sorties attendues |
|-------------------------|----------------|-------------------|
| `HT_test_validator`     | Montant **HT** + taux | TVA, TTC |
| `TVA_test_validator`    | Montant **TVA** + taux | HT, TTC |
| `TTC_test_validator`    | Montant **TTC** + taux | HT, TVA |

Chaque méthode est paramétrée via un **Excel DataProvider** et exécutée en **parallèle** sur 3 threads.

---

## Stack technique

| Outil                | Version  | Usage                                       |
|----------------------|----------|---------------------------------------------|
| Java                 | 17       | Langage                                     |
| Maven                | 3.x      | Build & dépendances                         |
| Selenium WebDriver   | 4.21.0   | Pilotage navigateur (Page Object Model)     |
| TestNG               | 7.10.2   | Framework de tests + DataProvider + parallel|
| Apache POI           | 5.2.5    | Lecture/écriture Excel & génération .docx   |
| ExtentReports        | 5.1.2    | Rapport HTML + screenshots                  |
| WebDriverManager     | 5.8.0    | Gestion auto du ChromeDriver                |

---

## Architecture

```
TP_Final/
├── pom.xml
├── testng.xml
├── test-data/
│   └── tva_test_data.xlsx        ← généré automatiquement (3 feuilles : HT/TVA/TTC)
├── reports/
│   └── run_<timestamp>/          ← rapport ExtentReports + screenshots
└── src/
    ├── main/java/com/firstproject/
    │   ├── models/
    │   │   └── TestRow.java                ← POJO d'une ligne de test
    │   ├── pages/
    │   │   └── CalculatricePage.java       ← Page Object (sélecteurs + actions)
    │   ├── dataproviders/
    │   │   └── TVADataProvider.java        ← @DataProvider + validation
    │   └── utils/
    │       ├── ExcelDataGenerator.java     ← création de l'Excel
    │       ├── ExcelReader.java            ← lecture des feuilles
    │       ├── DriverManager.java          ← ThreadLocal<WebDriver>
    │       ├── ReportManager.java          ← ExtentReports + screenshots
    │       └── DocxReportGenerator.java    ← génération du compte rendu .docx
    └── test/java/com/firstproject/tests/
        └── TestCaseFile.java               ← 3 méthodes @Test
```

### Patterns appliqués

- **Page Object Model** — un seul fichier (`CalculatricePage`) encapsule sélecteurs et interactions UI.
- **DataProvider Excel** — découplage code/données : modifier les cas de test ne touche pas au Java.
- **ThreadLocal WebDriver** — chaque thread reçoit sa propre instance Chrome (compatibilité parallel).
- **Validation upstream** — les lignes invalides (`valide=false`) sont rejetées par la fonction de validation, sans solliciter l'UI.

---

## Prérequis

- **JDK 17+** (`java -version`)
- **Maven 3.6+** (`mvn -v`)
- **Google Chrome** installé (le ChromeDriver est téléchargé automatiquement par WebDriverManager)
- Connexion internet (premier run)

---

## Installation & exécution

```bash
# 1. Cloner le dépôt
git clone https://github.com/<your-user>/<your-repo>.git
cd <your-repo>/TP_Final

# 2. (Optionnel) Générer le fichier Excel de test si absent
mvn -q exec:java -Dexec.mainClass="com.firstproject.utils.ExcelDataGenerator"

# 3. Lancer la suite complète (21 tests, 3 threads)
mvn test
```

### Commandes utiles

| But                                     | Commande                                                                  |
|-----------------------------------------|---------------------------------------------------------------------------|
| Compiler sans exécuter                  | `mvn compile`                                                             |
| Exécuter une seule méthode              | `mvn test -Dtest=TestCaseFile#HT_test_validator`                          |
| Régénérer l'Excel                       | Supprimer `test-data/tva_test_data.xlsx` puis `mvn test`                  |
| Générer le compte rendu .docx           | `mvn -q exec:java -Dexec.mainClass="com.firstproject.utils.DocxReportGenerator"` |

---

## Données de test

Le fichier `test-data/tva_test_data.xlsx` contient **3 feuilles** (HT, TVA, TTC) avec **7 cas chacune** = **21 cas au total**, mélangeant cas valides et invalides.

### Feuille HT — exemple

| HT   | Type de taux | Taux | Attendu TVA | Attendu TTC | Valide | Remarque              |
|------|--------------|------|-------------|-------------|--------|-----------------------|
| 100  | France       | 20.0 | 20.00       | 120.00      | TRUE   | Taux France valide    |
| 200  | France       | 10.0 | 20.00       | 220.00      | TRUE   |                       |
| 50   | France       | 5.5  | 2.75        | 52.75       | TRUE   |                       |
| 150  | France       | 105  | -           | -           | FALSE  | Taux > 100%, invalide |
| -80  | France       | 20.0 | -           | -           | FALSE  | HT négatif            |
| 120  | autre        | 7.0  | 8.40        | 128.40      | TRUE   | Taux personnalisé     |
| 90   | France       | 2.1  | 1.89        | 91.89       | TRUE   |                       |

> Les feuilles **TVA** et **TTC** suivent la même structure adaptée à leur sens de calcul.

---

## Règles de validation

La fonction `TVADataProvider.isRowDataValid(...)` applique les règles suivantes :

- **Valeur d'entrée** : strictement positive
- **Type de taux** :
  - `France` → taux ∈ `{ 2.1, 5.5, 10.0, 20.0 }`
  - `autre`  → taux ∈ `]0 ; 100[`
- Toute ligne marquée `valide=false` **doit** être rejetée par cette fonction (cohérence vérifiée par chaque test).

Pour les lignes invalides, l'UI **n'est volontairement pas sollicitée** : la validation amont est la garantie attendue, conformément à la consigne du sujet.

---

## Rapports

### ExtentReports HTML

Après chaque exécution, un rapport est créé sous :

```
TP_Final/reports/run_<timestamp>/index.html
```

Il contient :
- Le tableau de bord (passed / failed / skipped, durée)
- La liste des 21 cas avec logs détaillés (entrées, sorties réelles, sorties attendues)
- Les **screenshots automatiques** des cas en échec
- La timeline d'exécution

> Ouvrir le rapport : `start TP_Final/reports/run_<timestamp>/index.html` (Windows)


---

## Exécution parallèle

Configuration dans `testng.xml` :

```xml
<suite name="TVA_Calculator_Suite" parallel="methods" thread-count="3">
    <test name="TVA_Tests">
        <classes>
            <class name="com.firstproject.tests.TestCaseFile"/>
        </classes>
    </test>
</suite>
```

- **`parallel="methods"`** : chaque `@Test` peut tourner dans son propre thread.
- **`thread-count="3"`** : 3 instances Chrome simultanées.
- **`DriverManager`** utilise un `ThreadLocal<WebDriver>` pour garantir l'isolation.

---

## Résultats

```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: ~47 s
```

| Statistique          | Valeur |
|----------------------|--------|
| Tests exécutés       | 21     |
| Réussis              | 21     |
| Échoués              | 0      |
| Threads parallèles   | 3      |
| Durée totale         | ~47 s  |

---

## Auteurs

- **Mohamed Dhia SELMI** — ISI
- **Fedi TRABELSI** — ISI

Projet réalisé dans le cadre du module **Qualité et Test**, année universitaire 2025-2026.

---

## Licence

Projet académique — usage pédagogique uniquement.
