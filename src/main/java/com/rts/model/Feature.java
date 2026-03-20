package com.rts.model;

import java.util.List;

public class Feature {
    private String name;               // phrase qui décrit le feature
    private String filePath;           // chemin du feature
    private List<Scenario> scenarios;  // tous les scénarios du fichier

    public Feature(String name, String filePath, List<Scenario> scenarios) {
        this.name = name;
        this.filePath = filePath;
        this.scenarios = scenarios;
    }

    public String getName() { return name; }
    public String getFilePath() { return filePath; }
    public List<Scenario> getScenarios() { return scenarios; }
}
