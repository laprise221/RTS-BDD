package com.rts.comparaison;

import com.rts.model.ComparaisonResult;
import com.rts.model.Scenario;
import com.rts.model.ScenarioSimilaire;
import com.rts.model.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparateurScenarioTest {

    private ComparateurScenario comparateur;
    private Scenario snew;
    private List<Scenario> existing;

    @BeforeEach
    void setUp() {
        comparateur = new ComparateurScenario();

        // Nouveau scénario (inspiré du papier)
        snew = new Scenario("Import URLs from file", List.of(
            new Step("Given", "a file url.txt with one URL per line"),
            new Step("When", "a user imports URLs from the file"),
            new Step("Then", "the system should read url.txt")
        ));

        // Scénarios existants
        existing = List.of(
            new Scenario("Import task configurations", List.of(
                new Step("Given", "a TXT file task configuration.txt"),
                new Step("When", "a user imports task configurations from the file"),
                new Step("Then", "the system should read task configuration.txt")
            )),
            new Scenario("Import proxy servers", List.of(
                new Step("Given", "a TXT file ip.txt with one IP per line"),
                new Step("When", "a user imports proxy servers from the file"),
                new Step("Then", "the system should read ip.txt")
            )),
            new Scenario("Crawl data from URL", List.of(
                new Step("Given", "a URL www.example.com"),
                new Step("When", "a user crawls data from the URL"),
                new Step("Then", "the system should use www.example.com as the seed")
            ))
        );
    }

    @Test
    void testRankingNonVide() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        assertFalse(result.getRanking().isEmpty());
    }

    @Test
    void testRankingContientTousLesScenarios() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        assertEquals(existing.size(), result.getRanking().size());
    }

    @Test
    void testRankingTrieParScoreDecroissant() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        List<ScenarioSimilaire> ranking = result.getRanking();
        for (int i = 0; i < ranking.size() - 1; i++) {
            assertTrue(ranking.get(i).getScoreSimilarite() >= ranking.get(i + 1).getScoreSimilarite());
        }
    }

    @Test
    void testScoreEntre0Et1() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        for (ScenarioSimilaire s : result.getRanking()) {
            assertTrue(s.getScoreSimilarite() >= 0.0 && s.getScoreSimilarite() <= 1.0);
        }
    }

    @Test
    void testGetTopN() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        List<ScenarioSimilaire> top2 = result.getTopN(2);
        assertEquals(2, top2.size());
    }

    @Test
    void testScenarioLePlusSimilairePartageDesMotsCles() {
        ComparaisonResult result = comparateur.comparer(snew, existing);
        ScenarioSimilaire premier = result.getRanking().get(0);
        ScenarioSimilaire dernier = result.getRanking().get(result.getRanking().size() - 1);
        assertTrue(premier.getScoreSimilarite() > dernier.getScoreSimilarite());
    }
}
