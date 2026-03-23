package com.rts.model;

import java.util.ArrayList;
import java.util.List;

public class Scenario {
    private String name;        // phrase qui décrit le scenario
    private List<Step> steps;   // liste des étapes
    private String acceptanceTestId;          // nom de la step definition (1:1)
    private List<String> unitTestIds;         // noms des tests unitaires (1:N)

    public Scenario(String name, List<Step> steps) {
        this.name = name;
        this.steps = steps;
        this.unitTestIds = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Step> getSteps() { return steps; }

    public String getAcceptanceTestId() {
        return acceptanceTestId;
    }

    public void setAcceptanceTestId(String acceptanceTestId) {
        this.acceptanceTestId = acceptanceTestId;
    }

    public List<String> getUnitTestIds() {
        return unitTestIds;
    }

    public void setUnitTestIds(List<String> unitTestIds) {
        this.unitTestIds = unitTestIds;
    }

    public void addUnitTestId(String unitTestId){
        this.unitTestIds.add(unitTestId);
    }
}
