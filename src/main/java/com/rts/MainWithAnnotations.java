package com.rts;

import com.rts.engine.ComparaisonEngine;
import com.rts.model.*;

import java.nio.file.Path;
import java.util.List;

public class MainWithAnnotations {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RTS-BDD — Sélection par scan d'annotations ===\n");

        ComparaisonEngine engine = new ComparaisonEngine(
                Path.of("src/test/resources/features"),   // .feature files
                Path.of("src/test/java")                  // step defs + unit tests
        );

        System.out.println("\nScénarios chargés : " + engine.getNombreScenarios());

        // Vérifier ce que le scan a trouvé
        System.out.println("\nDétail du mapping par scénario :");
        System.out.println("─────────────────────────────────────────");
        for (Scenario s : engine.getExistingScenarios()) {
            String at = s.getAcceptanceTestId() != null ? s.getAcceptanceTestId() : "(aucun)";
            int nbUT = s.getUnitTestIds().size();
            System.out.printf("  %-45s → AT: %-18s → UT: %d%n", s.getName(), at, nbUT);
            for (String ut : s.getUnitTestIds()) {
                System.out.println("        " + ut);
            }
        }

        // ──────────────────────────────────────────────────────
        //  2. Nouveau scénario (l'exigence du sprint)
        // ──────────────────────────────────────────────────────

        Scenario nouveau = new Scenario("Import URLs from file", List.of(
                new Step("Given", "a file \"url.txt\" with one URL per line"),
                new Step("When", "a user imports URLs from the file as the seed"),
                new Step("Then", "the system should read \"url.txt\"")));

        System.out.println("\n─────────────────────────────────────────");
        System.out.println("Nouveau scénario : " + nouveau.getName());
        for (Step step : nouveau.getSteps()) {
            System.out.println("  " + step.getKeyword() + " " + step.getText());
        }

        // ──────────────────────────────────────────────────────
        //  3. Sélection complète : comparaison → AT → UT
        // ──────────────────────────────────────────────────────

        RTSResult result = engine.selectionnerTestsKneePoint(nouveau, 3);
        System.out.println();

        result.afficherRapport();

        // ──────────────────────────────────────────────────────
        //  4. Commande Maven pour le CI
        // ──────────────────────────────────────────────────────

        List<String> tests = result.getTousLesTests();
        if (!tests.isEmpty()) {
            String filtre = String.join(",", tests);
            System.out.println("\nCommande Maven :");
            System.out.println("  mvn test -Dtest=\"" + filtre + "\"");
        } else {
            System.out.println("\nAucun test sélectionné — le mapping est peut-être incomplet.");
            System.out.println("Vérifiez que vos *Steps.java utilisent les annotations @Given/@When/@Then");
            System.out.println("et que vos tests unitaires suivent la convention *Test.java.");
        }
    }
}
