package com.rts.model;

import java.util.ArrayList;
import java.util.List;

public class RTSResult {
    private Scenario nouveauScenario;
    private List<ScenarioSimilaire> scenariosSimilaires;
    private List<String> acceptanceTests;
    private List<String> unitTests;

    public RTSResult(Scenario nouveauScenario, List<ScenarioSimilaire> scenariosSimilaires) {
        this.nouveauScenario = nouveauScenario;
        this.scenariosSimilaires = scenariosSimilaires;
        this.acceptanceTests = new ArrayList<String>();
        this.unitTests = new ArrayList<>();

        for (ScenarioSimilaire ss : scenariosSimilaires) {
            Scenario s = ss.getScenario();

            if (s.getAcceptanceTestId() != null && !acceptanceTests.contains(s.getAcceptanceTestId())) {
                acceptanceTests.add(s.getAcceptanceTestId());
            }

            for(String ut : s.getUnitTestIds()) {
                if(!unitTests.contains(ut)) {
                    unitTests.addAll(s.getUnitTestIds());
                }


            }
        }

    }
    public List<String> getTousLesTests() {
        List<String> tous = new ArrayList<>(acceptanceTests);
        tous.addAll(unitTests);
        return tous;
    }


    public void afficherRapport() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  SÉLECTION RTS pour : " + nouveauScenario.getName());
        System.out.println("═══════════════════════════════════════════════════");

        System.out.println("\nScénarios similaires sélectionnés : "
                + scenariosSimilaires.size());
        for (ScenarioSimilaire ss : scenariosSimilaires) {
            System.out.printf("    [%.4f] %s%n",
                    ss.getScoreSimilarite(), ss.getScenario().getName());
        }

        System.out.println("\nTests d'acceptation à ré-exécuter (AT_rts) : "
                + acceptanceTests.size());
        for (String at : acceptanceTests) {
            System.out.println("    - " + at);
        }

        System.out.println("\nTests unitaires à ré-exécuter (UT_rts) : "
                + unitTests.size());
        for (String ut : unitTests) {
            System.out.println("    - " + ut);
        }

        System.out.println("\n→ Total : " + getTousLesTests().size() + " tests à lancer");
        System.out.println("═══════════════════════════════════════════════════");
    }

}

