package com.rts.model;

import java.util.List;

public class ComparaisonResult {
    // liste triée par score de similarité décroissant
    private List<ScenarioSimilaire> ranking;

    public ComparaisonResult(List<ScenarioSimilaire> ranking) {
        this.ranking = ranking;
    }

    public List<ScenarioSimilaire> getRanking() { return ranking; }

    // retourne les N scénarios les plus similaires
    public List<ScenarioSimilaire> getTopN(int n) {
        return ranking.subList(0, Math.min(n, ranking.size()));
    }
}
