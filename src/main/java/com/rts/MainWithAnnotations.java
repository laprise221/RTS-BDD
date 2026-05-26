package com.rts;

import com.rts.engine.ComparaisonEngine;
import com.rts.model.*;
import com.rts.parser.GherkinParser;

import java.nio.file.Path;
import java.util.List;

public class MainWithAnnotations {

    private static final String USAGE = """
            Usage:
              mvn exec:java -Dexec.mainClass="com.rts.MainWithAnnotations" \\
                -Drts.features=<chemin/vers/features> \\
                -Drts.testSource=<chemin/vers/src/test/java> \\
                -Drts.newScenario=<chemin/vers/new_requirement.feature> \\
                [-Drts.minSelection=3]

            Paramètres :
              rts.features      Répertoire contenant les fichiers .feature existants
              rts.testSource    Répertoire racine des sources de test Java (*Steps.java, *Test.java)
              rts.newScenario   Fichier .feature décrivant la nouvelle exigence
              rts.minSelection  Nombre minimum de scénarios à sélectionner (défaut: 3)
            """;

    public static void main(String[] args) throws Exception {
        System.out.println("=== RTS-BDD — Sélection de tests de régression ===\n");

        String featuresStr   = System.getProperty("rts.features");
        String testSourceStr = System.getProperty("rts.testSource");
        String newScenStr    = System.getProperty("rts.newScenario");
        int minSelection     = Integer.parseInt(System.getProperty("rts.minSelection", "3"));

        if (featuresStr == null || testSourceStr == null || newScenStr == null) {
            System.err.println("[ERREUR] Paramètres manquants.\n");
            System.err.println(USAGE);
            System.exit(1);
        }

        Path featuresDir   = Path.of(featuresStr);
        Path testSourceDir = Path.of(testSourceStr);
        Path newScenFile   = Path.of(newScenStr);

        // ── 1. Charger les scénarios existants + mapping AT/UT ──
        ComparaisonEngine engine = new ComparaisonEngine(featuresDir, testSourceDir);
        System.out.println("Scénarios existants chargés : " + engine.getNombreScenarios());

        // ── 2. Afficher le mapping détecté ──────────────────────
        System.out.println("\nMapping AT/UT par scénario :");
        System.out.println("─────────────────────────────────────────");
        for (Scenario s : engine.getExistingScenarios()) {
            String at  = s.getAcceptanceTestId() != null ? s.getAcceptanceTestId() : "(aucun)";
            int    nbUT = s.getUnitTestIds().size();
            System.out.printf("  %-50s → AT: %-20s → UT: %d%n", s.getName(), at, nbUT);
            for (String ut : s.getUnitTestIds()) {
                System.out.println("        " + ut);
            }
        }

        // ── 3. Charger le(s) nouveau(x) scénario(s) ────────────
        GherkinParser parser = new GherkinParser();
        List<Scenario> nouveaux = parser.parseFile(newScenFile).getScenarios();

        if (nouveaux.isEmpty()) {
            System.err.println("\n[ERREUR] Aucun scénario trouvé dans : " + newScenFile);
            System.exit(1);
        }

        // ── 4. Sélection RTS pour chaque nouveau scénario ───────
        for (Scenario nouveau : nouveaux) {
            System.out.println("\n─────────────────────────────────────────");
            System.out.println("Nouveau scénario : " + nouveau.getName());
            for (Step step : nouveau.getSteps()) {
                System.out.println("  " + step.getKeyword() + " " + step.getText());
            }

            RTSResult result = engine.selectionnerTestsKneePoint(nouveau, minSelection);
            System.out.println();
            result.afficherRapport();

            List<String> tests = result.getTousLesTests();
            if (!tests.isEmpty()) {
                System.out.println("\nCommande Maven :");
                System.out.println("  mvn test -Dtest=\"" + String.join(",", tests) + "\"");
            } else {
                System.out.println("\nAucun test mappé — vérifiez que vos *Steps.java utilisent");
                System.out.println("les annotations @Given/@When/@Then et que vos tests unitaires");
                System.out.println("sont nommés XxxTest.java ou TestXxx.java.");
            }
        }
    }
}
