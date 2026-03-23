package com.rts.model;

public class ScenarioSimilaire {
    private Scenario scenario;
    private double scoreSimilarite;  // score cosinus entre 0 et 1

    public ScenarioSimilaire(Scenario scenario, double scoreSimilarite) {
        this.scenario = scenario;
        this.scoreSimilarite = scoreSimilarite;
    }

    public Scenario getScenario() { return scenario; }
    public double getScoreSimilarite() { return scoreSimilarite; }
}
