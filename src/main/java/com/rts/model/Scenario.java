package com.rts.model;

import java.util.List;

public class Scenario {
    private String name;        // phrase qui décrit le scenario
    private List<Step> steps;   // liste des étapes

    public Scenario(String name, List<Step> steps) {
        this.name = name;
        this.steps = steps;
    }

    public String getName() { return name; }
    public List<Step> getSteps() { return steps; }
}
