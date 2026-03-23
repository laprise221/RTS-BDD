package com.rts;

import com.rts.engine.ComparaisonEngine;
import com.rts.model.ComparaisonResult;
import com.rts.model.Scenario;
import com.rts.model.ScenarioSimilaire;
import com.rts.model.Step;
import com.rts.parser.GherkinParser;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RTS-BDD — Sélection de tests de régression ===\n");

        // ── 1. Charger les scénarios existants ──────────────
        // Mode A : depuis un dossier de .feature
        // ComparaisonEngine engine = new ComparaisonEngine(
        //     Path.of("src/test/resources/features"));

        // Mode B : scénarios en dur (démo)
        List<Scenario> existants = List.of(
            scenario("Import task configurations from file",
                step("Given", "a TXT file \"task_configuration.txt\""),
                step("When", "a user imports task configurations from the file"),
                step("Then", "the system should read \"task_configuration.txt\"")),

            scenario("Import proxy servers from file",
                step("Given", "a TXT file \"ip.txt\" with one IP per line"),
                step("When", "a user imports proxy servers from the file"),
                step("Then", "the system should read \"ip.txt\"")),

            scenario("Crawl data from URL",
                step("Given", "a URL \"www.example.com\""),
                step("When", "a user crawls data from the URL"),
                step("Then", "the system should use \"www.example.com\" as the seed")),

            scenario("Export results to TXT file",
                step("Given", "crawled data is available"),
                step("When", "a user exports results to a TXT file"),
                step("Then", "the system should write data to \"results.txt\"")),

            scenario("Export logs to TXT file",
                step("Given", "crawling logs are available"),
                step("When", "a user exports logs to a TXT file"),
                step("Then", "the system should write logs to \"log.txt\"")),

            scenario("Configure crawl depth",
                step("Given", "the crawl settings page is displayed"),
                step("When", "a user sets the crawl depth to 3"),
                step("Then", "the system should limit crawling to 3 levels deep")),

            scenario("Parse HTML page content",
                step("Given", "a URL has been crawled successfully"),
                step("When", "the system parses the HTML content"),
                step("Then", "the data should be extracted according to the rules")),

            scenario("Import URL list from CSV",
                step("Given", "a CSV file \"urls.csv\" with URLs in the first column"),
                step("When", "a user imports URLs from the CSV file"),
                step("Then", "the system should read URLs from \"urls.csv\"")),

            scenario("Validate URL format",
                step("Given", "a user enters a URL"),
                step("When", "the system validates the URL format"),
                step("Then", "the system should accept valid URLs and reject invalid ones"))
        );

        ComparaisonEngine engine = new ComparaisonEngine(existants);
        System.out.println("Scénarios existants chargés : " + engine.getNombreScenarios());

        // ── 2. Nouveau scénario (la nouvelle exigence) ──────
        Scenario nouveau = scenario("Import URLs from file",
            step("Given", "a file \"url.txt\" with one URL per line"),
            step("When", "a user imports URLs from the file as the seed"),
            step("Then", "the system should read \"url.txt\""));

        System.out.println("Nouveau scénario : " + nouveau.getName());
        System.out.println();

        // ── 3. Comparer ─────────────────────────────────────
        ComparaisonResult result = engine.comparer(nouveau);

        System.out.println("Ranking de similarité :");
        System.out.println("─────────────────────────────────────────");
        List<ScenarioSimilaire> ranking = result.getRanking();
        for (int i = 0; i < ranking.size(); i++) {
            ScenarioSimilaire ss = ranking.get(i);
            System.out.printf("  %2d. [%.4f] %s%n",
                    i + 1, ss.getScoreSimilarite(), ss.getScenario().getName());
        }

        // ── 4. Sélection automatique (knee point) ───────────
        System.out.println();
        List<ScenarioSimilaire> selectionnes = engine.comparerKneePoint(nouveau, 3);
        System.out.println("Scénarios sélectionnés (knee point, min=3) :");
        for (ScenarioSimilaire ss : selectionnes) {
            System.out.printf("  ✔ [%.4f] %s%n",
                    ss.getScoreSimilarite(), ss.getScenario().getName());
        }
        System.out.println("\n→ " + selectionnes.size() + " scénario(s) à retester sur "
                + engine.getNombreScenarios() + " existants");
    }


    // ── Helpers ──────────────────────────────────────────────

    private static Scenario scenario(String name, Step... steps) {
        return new Scenario(name, List.of(steps));
    }

    private static Step step(String keyword, String text) {
        return new Step(keyword, text);
    }
}

