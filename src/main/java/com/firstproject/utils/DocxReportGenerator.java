package com.firstproject.utils;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class DocxReportGenerator {

    private static final String OUTPUT = "compte_rendu.docx";

    private static final String SCREENSHOT_DIR = "C:\\Users\\moham\\Pictures\\Screenshots\\";
    private static final String IMG_BUILD     = SCREENSHOT_DIR + "Capture d'écran 2026-05-05 234555.png";
    private static final String IMG_TESTS     = SCREENSHOT_DIR + "Capture d'écran 2026-05-05 234700.png";
    private static final String IMG_DASHBOARD = SCREENSHOT_DIR + "Capture d'écran 2026-05-05 234715.png";
    private static final String IMG_TIMELINE  = SCREENSHOT_DIR + "Capture d'écran 2026-05-05 234737.png";

    public static void main(String[] args) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {

            coverPage(doc);
            pageBreak(doc);

            heading(doc, "Sommaire", 1);
            bullet(doc, "1. Introduction et contexte");
            bullet(doc, "2. Objectifs du projet");
            bullet(doc, "3. Outils et technologies utilisés");
            bullet(doc, "4. Architecture du projet");
            bullet(doc, "5. Détails d'implémentation");
            bullet(doc, "6. Données de test et critères de validation");
            bullet(doc, "7. Exécution et résultats");
            bullet(doc, "8. Conclusion");
            pageBreak(doc);

            heading(doc, "1. Introduction et contexte", 1);
            paragraph(doc,
                    "Dans le cadre du module Qualité et Test, ce projet a pour objectif la mise en place " +
                    "d'une suite de tests automatisés afin de vérifier le bon fonctionnement d'un calculateur " +
                    "de TVA en ligne disponible à l'adresse :");
            paragraph(doc, "https://www.ma-calculatrice.fr/calculer-tva-ttc-ht-convertir");
            paragraph(doc,
                    "Le calculateur permet, à partir d'un montant et d'un taux de TVA, de calculer " +
                    "automatiquement les valeurs HT (Hors Taxes), TVA et TTC (Toutes Taxes Comprises). " +
                    "L'objectif des tests est de s'assurer que ces calculs sont corrects, quel que soit " +
                    "le type de saisie (HT, TVA ou TTC connu) et le taux appliqué (taux standard français " +
                    "ou taux personnalisé).");

            heading(doc, "2. Objectifs du projet", 1);
            paragraph(doc,
                    "Le projet vise à atteindre les objectifs suivants :");
            bullet(doc, "Automatiser entièrement la suite de tests via Selenium WebDriver, sans intervention manuelle.");
            bullet(doc, "Externaliser les jeux de données dans un fichier Excel afin de faciliter la maintenance.");
            bullet(doc, "Implémenter une fonction de validation garantissant la cohérence des données fournies.");
            bullet(doc, "Couvrir trois scénarios : HT fourni, TVA fournie, TTC fournie.");
            bullet(doc, "Configurer un parallélisme de 3 threads via TestNG.");
            bullet(doc, "Produire un rapport HTML lisible via ExtentReports avec captures d'écran en cas d'échec.");

            heading(doc, "3. Outils et technologies utilisés", 1);
            paragraph(doc, "La pile technique retenue pour ce projet est la suivante :");

            XWPFTable techTable = doc.createTable(7, 2);
            setTableHeader(techTable.getRow(0), "Outil / Bibliothèque", "Rôle");
            setTableRow(techTable.getRow(1), "Java 17", "Langage de programmation");
            setTableRow(techTable.getRow(2), "Maven", "Gestion des dépendances et du build");
            setTableRow(techTable.getRow(3), "Selenium WebDriver 4.21", "Automatisation du navigateur web");
            setTableRow(techTable.getRow(4), "TestNG 7.10", "Framework de tests, DataProvider et exécution parallèle");
            setTableRow(techTable.getRow(5), "Apache POI 5.2", "Lecture et écriture du fichier Excel (.xlsx)");
            setTableRow(techTable.getRow(6), "ExtentReports 5.1", "Génération du rapport HTML interactif");
            paragraph(doc, "");

            heading(doc, "4. Architecture du projet", 1);
            paragraph(doc,
                    "Le projet est organisé selon le pattern Page Object Model (POM) afin de séparer " +
                    "clairement la logique de test, la représentation des pages web et la gestion des données. " +
                    "L'arborescence est la suivante :");
            code(doc,
                    "TP_Final/\n" +
                    "├── pom.xml                              (dépendances Maven)\n" +
                    "├── testng.xml                           (suite TestNG, parallel=methods, thread-count=3)\n" +
                    "├── test-data/\n" +
                    "│   └── tva_test_data.xlsx               (jeux de données auto-générés)\n" +
                    "├── reports/                             (rapports ExtentReports horodatés)\n" +
                    "└── src/\n" +
                    "    ├── main/java/com/firstproject/\n" +
                    "    │   ├── models/TestRow.java          (POJO ligne Excel + enum Mode)\n" +
                    "    │   ├── pages/CalculatricePage.java  (Page Object Selenium)\n" +
                    "    │   ├── utils/\n" +
                    "    │   │   ├── DriverManager.java       (WebDriver thread-safe)\n" +
                    "    │   │   ├── ExcelReader.java         (lecture Excel)\n" +
                    "    │   │   ├── ExcelDataGenerator.java  (génération automatique de l'Excel)\n" +
                    "    │   │   └── ReportManager.java       (ExtentReports + screenshots)\n" +
                    "    │   └── dataproviders/\n" +
                    "    │       └── TVADataProvider.java     (3 DataProviders + validation)\n" +
                    "    └── test/java/com/firstproject/tests/\n" +
                    "        └── TestCaseFile.java            (HT/TVA/TTC_test_validator)\n");

            heading(doc, "5. Détails d'implémentation", 1);

            heading(doc, "5.1. Modèle de données (TestRow)", 2);
            paragraph(doc,
                    "La classe TestRow représente une ligne de l'Excel. Elle contient le mode " +
                    "(HT, TVA ou TTC), la valeur saisie, le type de taux (France ou autre), le taux, " +
                    "les deux valeurs attendues, l'indicateur de validité et la remarque associée.");

            heading(doc, "5.2. Génération et lecture du fichier Excel", 2);
            paragraph(doc,
                    "La classe ExcelDataGenerator crée automatiquement le fichier tva_test_data.xlsx " +
                    "s'il n'existe pas, avec trois feuilles : HT, TVA et TTC. Chaque feuille est alimentée " +
                    "à partir des tableaux fournis dans l'énoncé. La classe ExcelReader lit ensuite ces " +
                    "feuilles via Apache POI et retourne une liste de TestRow exploitable par les DataProviders.");

            heading(doc, "5.3. DataProvider et fonction de validation", 2);
            paragraph(doc,
                    "La classe TVADataProvider expose trois DataProviders TestNG (htData, tvaData, ttcData) " +
                    "qui chargent leur feuille respective. Avant de transmettre les données aux tests, une " +
                    "fonction isExcelValid() vérifie la cohérence du fichier en s'appuyant sur isRowDataValid() " +
                    "qui applique les règles métier suivantes :");
            bullet(doc, "Type \"France\" : le taux doit être l'une des valeurs 2.1, 5.5, 10 ou 20.");
            bullet(doc, "Type \"autre\" : le taux doit être strictement compris entre 0 et 100.");
            bullet(doc, "Tous les montants (entrée et valeurs attendues) doivent être strictement positifs.");
            paragraph(doc,
                    "Si le résultat calculé par cette fonction ne correspond pas à la colonne \"Valide\" " +
                    "du fichier Excel, une exception est levée pour signaler l'incohérence.");

            heading(doc, "5.4. Page Object Selenium (CalculatricePage)", 2);
            paragraph(doc,
                    "La classe CalculatricePage encapsule l'interaction avec le calculateur. Les sélecteurs " +
                    "ont été identifiés via l'inspecteur du navigateur :");
            bullet(doc, "inputHT, inputTVA, inputTTC : champs de saisie des montants.");
            bullet(doc, "selectTaux : liste déroulante des taux (valeurs décimales 0.2, 0.1, 0.055, 0.021, ou \"autre\").");
            bullet(doc, "autreTx : champ de saisie du taux personnalisé lorsque l'option \"Autre\" est choisie.");
            bullet(doc, "reset : bouton d'effacement du formulaire.");
            paragraph(doc,
                    "Le calculateur étant à recalcul automatique, aucune action de soumission n'est nécessaire : " +
                    "la perte de focus (touche TAB) suffit à déclencher le calcul.");

            heading(doc, "5.5. Cas de test (TestCaseFile)", 2);
            paragraph(doc,
                    "La classe TestCaseFile contient les trois fonctions de test annotées @Test :");
            bullet(doc, "HT_test_validator : reçoit HT et taux, vérifie la TVA et le TTC calculés.");
            bullet(doc, "TVA_test_validator : reçoit TVA et taux, vérifie le HT et le TTC calculés.");
            bullet(doc, "TTC_test_validator : reçoit TTC et taux, vérifie le HT et la TVA calculés.");
            paragraph(doc,
                    "Pour les lignes valides, le test ouvre le navigateur, saisit les valeurs et compare le " +
                    "résultat affiché à la valeur attendue (tolérance ±0.02€). Pour les lignes invalides, " +
                    "le test vérifie uniquement que la fonction de validation rejette correctement la ligne, " +
                    "sans solliciter l'interface (cas non saisissables dans le sélecteur ou montants négatifs).");

            heading(doc, "5.6. Reporting (ExtentReports)", 2);
            paragraph(doc,
                    "La classe ReportManager initialise un rapport ExtentReports unique par exécution, " +
                    "horodaté dans le dossier reports/run_<timestamp>/. Pour chaque test, sont enregistrés :");
            bullet(doc, "Le nom du cas de test (mode + ligne + valeur d'entrée + taux).");
            bullet(doc, "Les données d'entrée et les résultats attendus.");
            bullet(doc, "Les valeurs réellement renvoyées par le calculateur.");
            bullet(doc, "Le statut final (Pass / Fail / Skip).");
            bullet(doc, "Une capture d'écran horodatée en cas d'échec.");

            heading(doc, "5.7. Configuration TestNG (parallélisme)", 2);
            paragraph(doc,
                    "Le fichier testng.xml configure la suite avec parallel=\"methods\" et thread-count=\"3\", " +
                    "ce qui permet d'exécuter simultanément les trois fonctions de test dans trois instances " +
                    "indépendantes de Chrome. Le DriverManager utilise un ThreadLocal<WebDriver> pour garantir " +
                    "qu'aucune instance n'est partagée entre threads.");
            code(doc,
                    "<suite name=\"TVA_Calculator_Suite\" parallel=\"methods\" thread-count=\"3\">\n" +
                    "    <test name=\"TVA_Tests\">\n" +
                    "        <classes>\n" +
                    "            <class name=\"com.firstproject.tests.TestCaseFile\"/>\n" +
                    "        </classes>\n" +
                    "    </test>\n" +
                    "</suite>");

            pageBreak(doc);
            heading(doc, "6. Données de test et critères de validation", 1);

            heading(doc, "6.1. Tableau 1 — HT fourni (vérifier TVA et TTC)", 2);
            renderHtTable(doc);

            heading(doc, "6.2. Tableau 2 — TVA fournie (vérifier HT et TTC)", 2);
            renderTvaTable(doc);

            heading(doc, "6.3. Tableau 3 — TTC fourni (vérifier HT et TVA)", 2);
            renderTtcTable(doc);

            pageBreak(doc);
            heading(doc, "7. Exécution et résultats", 1);

            heading(doc, "7.1. Exécution de la suite Maven", 2);
            paragraph(doc,
                    "La commande mvn test lance la suite complète. Le résultat console montre que les " +
                    "21 tests (7 par mode HT/TVA/TTC) ont été exécutés avec succès en 47 secondes :");
            insertImage(doc, IMG_BUILD, 16);
            paragraph(doc, "Figure 1 : Sortie console de Maven — BUILD SUCCESS, 21 tests passés en 47.04 s");

            heading(doc, "7.2. Liste des cas de test (ExtentReports)", 2);
            paragraph(doc,
                    "Le rapport ExtentReports affiche chaque cas de test avec son timestamp, sa durée et " +
                    "son statut. La sélection d'un test fait apparaître les détails de l'exécution : " +
                    "données d'entrée, valeurs attendues, valeurs réelles et statut final.");
            insertImage(doc, IMG_TESTS, 16);
            paragraph(doc, "Figure 2 : Liste des cas de test exécutés et détail d'un test (TVA_test_validator)");

            heading(doc, "7.3. Tableau de bord global", 2);
            paragraph(doc,
                    "Le tableau de bord présente une vue synthétique de l'exécution avec les indicateurs " +
                    "principaux : début, fin, nombre de tests passés et échoués.");
            insertImage(doc, IMG_DASHBOARD, 16);
            paragraph(doc, "Figure 3 : Tableau de bord ExtentReports — 21 tests passés, 0 échec");

            heading(doc, "7.4. Timeline et environnement d'exécution", 2);
            paragraph(doc,
                    "La timeline visualise la durée et l'ordre d'exécution de chaque test. La section " +
                    "System/Environment précise le contexte d'exécution.");
            insertImage(doc, IMG_TIMELINE, 16);
            paragraph(doc, "Figure 4 : Timeline d'exécution et informations système");

            pageBreak(doc);
            heading(doc, "8. Conclusion", 1);
            paragraph(doc,
                    "Ce projet a permis de mettre en pratique une démarche de test automatisé complète et " +
                    "professionnelle. Le binôme a conçu une architecture modulaire reposant sur le pattern " +
                    "Page Object Model, externalisé les données de test dans un fichier Excel, mis en place " +
                    "une fonction de validation rigoureuse des entrées, et configuré un parallélisme à 3 threads " +
                    "via TestNG.");
            paragraph(doc,
                    "L'utilisation d'ExtentReports apporte une dimension professionnelle au reporting avec " +
                    "des captures d'écran automatiques en cas d'échec. La suite finale exécute 21 cas de test " +
                    "couvrant les trois modes de calcul (HT, TVA, TTC), avec des taux français standards et " +
                    "personnalisés, ainsi que des cas invalides (taux hors plage, montants négatifs).");
            paragraph(doc,
                    "L'ensemble s'exécute en moins d'une minute et produit un rapport HTML interactif " +
                    "directement consultable. Cette approche est représentative des bonnes pratiques de " +
                    "l'industrie en matière d'assurance qualité logicielle.");

            try (FileOutputStream out = new FileOutputStream(OUTPUT)) {
                doc.write(out);
            }
            System.out.println("Compte rendu généré : " + new File(OUTPUT).getAbsolutePath());
        }
    }

    private static void coverPage(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(2400);
        run(p, "Institut Supérieur Informatique (ISI)", 14, true);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        run(p, "Université de Tunis El Manar", 12, false);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(800);
        run(p, "Module : Qualité et Test", 14, true);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(1200);
        run(p, "COMPTE RENDU DU PROJET", 22, true);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(400);
        run(p, "Tests automatisés du calculateur de TVA", 18, true);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(200);
        run(p, "Java + Selenium + TestNG + ExtentReports", 13, false);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(1600);
        run(p, "Réalisé par :", 12, true);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        run(p, "Mohamed Dhia SELMI", 13, false);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        run(p, "Fedi TRABELSI", 13, false);

        p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(2400);
        run(p, "Année universitaire 2025 / 2026", 11, false);
    }

    private static void heading(XWPFDocument doc, String text, int level) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading" + level);
        int size = (level == 1) ? 18 : 14;
        run(p, text, size, true);
    }

    private static void paragraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);
        p.setSpacingAfter(120);
        run(p, text, 11, false);
    }

    private static void bullet(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(360);
        p.setSpacingAfter(60);
        XWPFRun r = p.createRun();
        r.setText("• " + text);
        r.setFontSize(11);
    }

    private static void code(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(120);
        p.setSpacingAfter(120);
        for (String line : text.split("\n", -1)) {
            XWPFRun r = p.createRun();
            r.setFontFamily("Consolas");
            r.setFontSize(9);
            r.setText(line);
            r.addBreak();
        }
    }

    private static void run(XWPFParagraph p, String text, int size, boolean bold) {
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(size);
        r.setBold(bold);
    }

    private static void pageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.createRun().addBreak(BreakType.PAGE);
    }

    private static void setTableHeader(XWPFTableRow row, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            XWPFTableCell c = (i < row.getTableCells().size()) ? row.getCell(i) : row.addNewTableCell();
            c.removeParagraph(0);
            XWPFParagraph p = c.addParagraph();
            XWPFRun r = p.createRun();
            r.setText(cells[i]);
            r.setBold(true);
            r.setFontSize(11);
            c.setColor("D9E2F3");
        }
    }

    private static void setTableRow(XWPFTableRow row, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            XWPFTableCell c = (i < row.getTableCells().size()) ? row.getCell(i) : row.addNewTableCell();
            c.removeParagraph(0);
            XWPFParagraph p = c.addParagraph();
            XWPFRun r = p.createRun();
            r.setText(cells[i]);
            r.setFontSize(10);
        }
    }

    private static void insertImage(XWPFDocument doc, String path, int widthCm) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            paragraph(doc, "[Image manquante : " + path + "]");
            return;
        }
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        try (FileInputStream fis = new FileInputStream(f)) {
            r.addPicture(fis,
                    XWPFDocument.PICTURE_TYPE_PNG,
                    f.getName(),
                    Units.toEMU(widthCm * 28.3464),
                    Units.toEMU(widthCm * 28.3464 * 0.55));
        } catch (Exception e) {
            throw new IOException("Erreur insertion image : " + path, e);
        }
    }

    private static void renderHtTable(XWPFDocument doc) {
        Object[][] rows = {
                { "HT (€)", "Type taux", "Taux (%)", "Attendu TVA (€)", "Attendu TTC (€)", "Valide", "Remarque" },
                { "100",  "France", "20",  "20.00", "120.00", "true",  "Taux France valide" },
                { "200",  "France", "10",  "20.00", "220.00", "true",  "" },
                { "50",   "France", "5.5", "2.75",  "52.75",  "true",  "" },
                { "150",  "France", "105", "-",     "-",      "false", "Taux > 100%, invalide" },
                { "-80",  "France", "20",  "-",     "-",      "false", "HT négatif" },
                { "120",  "autre",  "7",   "8.40",  "128.40", "true",  "Taux personnalisé" },
                { "90",   "France", "2.1", "1.89",  "91.89",  "true",  "" }
        };
        renderTable(doc, rows);
    }

    private static void renderTvaTable(XWPFDocument doc) {
        Object[][] rows = {
                { "TVA (€)", "Type taux", "Taux (%)", "Attendu HT (€)", "Attendu TTC (€)", "Valide", "Remarque" },
                { "20",   "France", "20",  "100.00", "120.00", "true",  "" },
                { "5.5",  "France", "5.5", "100.00", "105.50", "true",  "" },
                { "2.1",  "France", "2.1", "100.00", "102.10", "true",  "" },
                { "10",   "France", "0",   "-",      "-",      "false", "Taux zéro, invalide" },
                { "-15",  "France", "10",  "-",      "-",      "false", "TVA négative" },
                { "9",    "autre",  "6",   "150.00", "159.00", "true",  "Taux personnalisé" },
                { "18",   "France", "105", "-",      "-",      "false", "Taux > 100%" }
        };
        renderTable(doc, rows);
    }

    private static void renderTtcTable(XWPFDocument doc) {
        Object[][] rows = {
                { "TTC (€)", "Type taux", "Taux (%)", "Attendu HT (€)", "Attendu TVA (€)", "Valide", "Remarque" },
                { "120",    "France", "20",  "100.00", "20.00",  "true",  "" },
                { "105.5",  "France", "5.5", "100.00", "5.50",   "true",  "" },
                { "91.89",  "France", "2.1", "90.00",  "1.89",   "true",  "" },
                { "220",    "France", "110", "-",      "-",      "false", "Taux invalide (>100%)" },
                { "-200",   "France", "20",  "-",      "-",      "false", "TTC négatif" },
                { "150",    "autre",  "25",  "120.00", "30.00",  "true",  "Taux personnalisé" },
                { "100",    "France", "0",   "-",      "-",      "false", "Taux invalide (0%)" }
        };
        renderTable(doc, rows);
    }

    private static void renderTable(XWPFDocument doc, Object[][] data) {
        XWPFTable table = doc.createTable(data.length, data[0].length);
        table.setWidth("100%");
        for (int i = 0; i < data.length; i++) {
            String[] row = new String[data[i].length];
            for (int j = 0; j < data[i].length; j++) row[j] = String.valueOf(data[i][j]);
            if (i == 0) setTableHeader(table.getRow(i), row);
            else setTableRow(table.getRow(i), row);
        }
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(120);
    }
}
