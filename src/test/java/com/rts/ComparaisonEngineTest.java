package com.rts;

import com.rts.engine.ComparaisonEngine;
import com.rts.model.ComparaisonResult;
import com.rts.model.Scenario;
import com.rts.model.ScenarioSimilaire;
import com.rts.model.Step;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparaisonEngineTest {

    private static ComparaisonEngine engine;
    private static Scenario sImportConfig;
    private static Scenario sImportProxy;
    private static Scenario sCrawlUrl;
    private static Scenario sExportTxt;
    private static Scenario sConfigDepth;

    @BeforeAll
    static void setup() {
        sImportConfig = scenario("Import task configurations from file",
                step("Given", "a TXT file \"task_configuration.txt\""),
                step("When", "a user imports task configurations from the file"),
                step("Then", "the system should read \"task_configuration.txt\""));

        sImportProxy = scenario("Import proxy servers from file",
                step("Given", "a TXT file \"ip.txt\" with one IP per line"),
                step("When", "a user imports proxy servers from the file"),
                step("Then", "the system should read \"ip.txt\""));

        sCrawlUrl = scenario("Crawl data from URL",
                step("Given", "a URL \"www.example.com\""),
                step("When", "a user crawls data from the URL"),
                step("Then", "the system should use \"www.example.com\" as the seed"));

        sExportTxt = scenario("Export results to TXT file",
                step("Given", "crawled data is available"),
                step("When", "a user exports results to a TXT file"),
                step("Then", "the system should write data to \"results.txt\""));

        sConfigDepth = scenario("Configure crawl depth",
                step("Given", "the crawl settings page is displayed"),
                step("When", "a user sets the crawl depth to 3"),
                step("Then", "the system should limit crawling to 3 levels deep"));

        engine = new ComparaisonEngine(
                List.of(sImportConfig, sImportProxy, sCrawlUrl, sExportTxt, sConfigDepth));
    }

    @Test
    void testRankingReturnsAllScenarios() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        ComparaisonResult result = engine.comparer(nouveau);

        assertEquals(5, result.getRanking().size(),
                "Le ranking doit contenir tous les scénarios existants");
    }

    @Test
    void testImportUrlMostSimilarToImportScenarios() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        ComparaisonResult result = engine.comparer(nouveau);
        List<ScenarioSimilaire> top3 = result.getTopN(3);

        // Les deux scénarios d'import doivent être dans le top 3
        List<String> topNames = top3.stream()
                .map(ss -> ss.getScenario().getName())
                .toList();

        assertTrue(topNames.contains("Import task configurations from file"),
                "Import config doit être dans le top 3");
        assertTrue(topNames.contains("Import proxy servers from file"),
                "Import proxy doit être dans le top 3");
    }

    @Test
    void testSimilarityScoresDescending() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        List<ScenarioSimilaire> ranking = engine.comparer(nouveau).getRanking();

        for (int i = 1; i < ranking.size(); i++) {
            assertTrue(
                    ranking.get(i - 1).getScoreSimilarite()
                            >= ranking.get(i).getScoreSimilarite(),
                    "Les scores doivent être triés par ordre décroissant");
        }
    }

    @Test
    void testSimilarityScoresBetweenZeroAndOne() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        for (ScenarioSimilaire ss : engine.comparer(nouveau).getRanking()) {
            assertTrue(ss.getScoreSimilarite() >= 0.0 && ss.getScoreSimilarite() <= 1.0,
                    "Cosine similarity doit être entre 0 et 1, mais vaut "
                            + ss.getScoreSimilarite());
        }
    }

    @Test
    void testConfigScenarioLeastSimilarToImport() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        List<ScenarioSimilaire> ranking = engine.comparer(nouveau).getRanking();
        ScenarioSimilaire last = ranking.get(ranking.size() - 1);

        assertEquals("Configure crawl depth", last.getScenario().getName(),
                "Configure crawl depth ne partage aucun terme d'import, "
                        + "il doit être le moins similaire");
    }

    @Test
    void testTopNReturnsCorrectCount() {
        Scenario nouveau = scenario("Test",
                step("Given", "something"),
                step("When", "action"),
                step("Then", "result"));

        assertEquals(2, engine.comparerTopN(nouveau, 2).size());
        assertEquals(5, engine.comparerTopN(nouveau, 10).size(),
                "TopN avec n > total doit retourner tous les scénarios");
    }

    @Test
    void testSeuilFiltering() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        // Seuil très haut : peu de résultats
        List<ScenarioSimilaire> strict = engine.comparerParSeuil(nouveau, 0.30);
        // Seuil très bas : beaucoup de résultats
        List<ScenarioSimilaire> permissif = engine.comparerParSeuil(nouveau, 0.01);

        assertTrue(strict.size() <= permissif.size(),
                "Un seuil plus strict doit donner moins de résultats");

        // Tous les résultats stricts doivent respecter le seuil
        for (ScenarioSimilaire ss : strict) {
            assertTrue(ss.getScoreSimilarite() >= 0.30);
        }
    }

    @Test
    void testKneePointReturnsAtLeastMinimum() {
        Scenario nouveau = scenario("Import URLs from file",
                step("Given", "a file \"url.txt\" with one URL per line"),
                step("When", "a user imports URLs from the file"),
                step("Then", "the system should read \"url.txt\""));

        List<ScenarioSimilaire> selection = engine.comparerKneePoint(nouveau, 2);
        assertTrue(selection.size() >= 2,
                "Knee point doit retourner au moins minSelection scénarios");
    }

    @Test
    void testIdenticalScenarioHasHighSimilarity() {
        // Un scénario identique à un existant doit avoir un score très élevé
        Scenario copie = scenario("Import task configurations from file",
                step("Given", "a TXT file \"task_configuration.txt\""),
                step("When", "a user imports task configurations from the file"),
                step("Then", "the system should read \"task_configuration.txt\""));

        ScenarioSimilaire top = engine.comparer(copie).getRanking().get(0);
        assertTrue(top.getScoreSimilarite() > 0.9,
                "Un scénario identique doit avoir un score > 0.9, mais vaut "
                        + top.getScoreSimilarite());
    }

    // ── Helpers ──────────────────────────────────────────────

    private static Scenario scenario(String name, Step... steps) {
        return new Scenario(name, List.of(steps));
    }

    private static Step step(String keyword, String text) {
        return new Step(keyword, text);
    }
}
