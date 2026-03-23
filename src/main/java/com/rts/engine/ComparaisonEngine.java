package com.rts.engine;

import com.rts.RTSengine.TestMappingLoader;
import com.rts.comparaison.ComparateurScenario;
import com.rts.model.*;
import com.rts.parser.GherkinParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Moteur de comparaison des scénarios BDD.
 *
 * Orchestre le pipeline complet :
 *   1. Parsing des .feature → Feature / Scenario / Step  (GherkinParser)
 *   2. Comparaison TF-IDF + cosinus                      (ComparateurScenario)
 *   3. Stratégies de sélection (top-N, seuil, knee point)
 */
public class ComparaisonEngine {

    private final List<Feature> features;
    private final List<Scenario> existingScenarios;
    private final ComparateurScenario comparateur;

    /**
     * Construit le moteur à partir d'un répertoire de fichiers .feature.
     */
    public ComparaisonEngine(Path featuresDirectory) throws IOException {
        GherkinParser parser = new GherkinParser();
        this.features = parser.parseDirectory(featuresDirectory);
        this.existingScenarios = features.stream()
                .flatMap(f -> f.getScenarios().stream())
                .collect(Collectors.toList());
        this.comparateur = new ComparateurScenario();
    }

    public ComparaisonEngine(Path featuresDirectory, Path testSourceRoot) throws IOException {

        GherkinParser parser = new GherkinParser();
        this.features = parser.parseDirectory(featuresDirectory);
        this.existingScenarios = features.stream()
                .flatMap(f -> f.getScenarios().stream())
                .collect(Collectors.toList());

        this.comparateur = new ComparateurScenario();

        //Mapping Tests <-> scenarios avec scan d'annotations
        TestMappingLoader.scannerAnnotations(testSourceRoot,existingScenarios);

    }
    /**
     * Construit le moteur à partir d'une liste de scénarios déjà parsés.
     */
    public ComparaisonEngine(List<Scenario> scenarios) {
        this.features = Collections.emptyList();
        this.existingScenarios = new ArrayList<>(scenarios);
        this.comparateur = new ComparateurScenario();
    }

    /**
     * Compare un nouveau scénario à tous les scénarios existants.
     * Délègue le calcul TF-IDF + cosinus au ComparateurScenario.
     */
    public ComparaisonResult comparer(Scenario nouveauScenario) {
        return comparateur.comparer(nouveauScenario, existingScenarios);
    }

    /**
     * Compare et retourne les top-N scénarios les plus similaires.
     */
    public List<ScenarioSimilaire> comparerTopN(Scenario nouveauScenario, int n) {
        return comparer(nouveauScenario).getTopN(n);
    }

    /**
     * Compare et retourne les scénarios au-dessus d'un seuil de similarité.
     */
    public List<ScenarioSimilaire> comparerParSeuil(Scenario nouveauScenario, double seuil) {
        return comparer(nouveauScenario).getRanking().stream()
                .filter(s -> s.getScoreSimilarite() >= seuil)
                .collect(Collectors.toList());
    }

    /**
     * Compare avec détection automatique du coude (knee point).
     * Trouve le plus grand saut entre deux scores consécutifs
     * dans le ranking trié.
     *
     * @param minSelection plancher : nombre minimum de scénarios à retourner
     */
    public List<ScenarioSimilaire> comparerKneePoint(Scenario nouveauScenario, int minSelection) {
        List<ScenarioSimilaire> ranking = comparer(nouveauScenario).getRanking();

        int kneeIndex = ranking.size();
        double maxDrop = 0;
        for (int i = 1; i < ranking.size(); i++) {
            double drop = ranking.get(i - 1).getScoreSimilarite()
                        - ranking.get(i).getScoreSimilarite();
            if (drop > maxDrop) {
                maxDrop = drop;
                kneeIndex = i;
            }
        }

        int cutoff = Math.max(kneeIndex, minSelection);
        cutoff = Math.min(cutoff, ranking.size());
        return ranking.subList(0, cutoff);
    }

    public RTSResult selectionnerTests(Scenario nouveauScenario,int n){
        List<ScenarioSimilaire> ss = comparerTopN(nouveauScenario,n);
        return new RTSResult(nouveauScenario, ss);
    }

    public RTSResult selectionnerTestsKneePoint(Scenario nouveauScenario,int min){
        List<ScenarioSimilaire> ss = comparerKneePoint(nouveauScenario,min);
        return new RTSResult(nouveauScenario, ss);
    }

    // ── Accesseurs ───────────────────────────────────────────

    public List<Feature> getFeatures() { return features; }
    public List<Scenario> getExistingScenarios() { return existingScenarios; }
    public int getNombreScenarios() { return existingScenarios.size(); }
}
