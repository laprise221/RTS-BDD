package com.rts.parser;

import com.rts.model.Feature;
import com.rts.model.Scenario;
import com.rts.model.Step;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parse les fichiers .feature Gherkin en objets Feature / Scenario / Step.
 *
 * Syntaxe supportée :
 *   Feature: ...
 *   Scenario: ...
 *     Given ...
 *     When ...
 *     Then ...
 *     And ...
 *     But ...
 */
public class GherkinParser {

    /**
     * Parse un seul fichier .feature.
     */
    public Feature parseFile(Path featureFile) throws IOException {
        List<String> lines = Files.readAllLines(featureFile);
        String filePath = featureFile.toString();
        String featureName = "";
        List<Scenario> scenarios = new ArrayList<>();

        String currentScenarioName = null;
        List<Step> currentSteps = new ArrayList<>();
        String lastKeyword = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("Feature:")) {
                featureName = line.substring(8).trim();
                continue;
            }

            if (line.startsWith("Scenario:") || line.startsWith("Scenario Outline:")) {
                // Flush le scénario précédent
                if (currentScenarioName != null && !currentSteps.isEmpty()) {
                    scenarios.add(new Scenario(currentScenarioName, new ArrayList<>(currentSteps)));
                }
                currentScenarioName = line.replaceFirst("Scenario(\\s+Outline)?:\\s*", "").trim();
                currentSteps.clear();
                lastKeyword = null;
                continue;
            }

            if (line.startsWith("Given ")) {
                lastKeyword = "Given";
                currentSteps.add(new Step("Given", line.substring(6).trim()));
            } else if (line.startsWith("When ")) {
                lastKeyword = "When";
                currentSteps.add(new Step("When", line.substring(5).trim()));
            } else if (line.startsWith("Then ")) {
                lastKeyword = "Then";
                currentSteps.add(new Step("Then", line.substring(5).trim()));
            } else if (line.startsWith("And ")) {
                String keyword = lastKeyword != null ? lastKeyword : "And";
                currentSteps.add(new Step(keyword, line.substring(4).trim()));
            } else if (line.startsWith("But ")) {
                String keyword = lastKeyword != null ? lastKeyword : "But";
                currentSteps.add(new Step(keyword, line.substring(4).trim()));
            }
        }

        // Flush le dernier scénario
        if (currentScenarioName != null && !currentSteps.isEmpty()) {
            scenarios.add(new Scenario(currentScenarioName, new ArrayList<>(currentSteps)));
        }

        return new Feature(featureName, filePath, scenarios);
    }

    /**
     * Parse tous les fichiers .feature d'un répertoire (récursif).
     */
    public List<Feature> parseDirectory(Path directory) throws IOException {
        List<Feature> features = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> featureFiles = paths
                    .filter(p -> p.toString().endsWith(".feature"))
                    .sorted()
                    .collect(Collectors.toList());
            for (Path f : featureFiles) {
                features.add(parseFile(f));
            }
        }
        return features;
    }

    /**
     * Extrait tous les scénarios de toutes les features (mise à plat).
     */
    public List<Scenario> parseAllScenarios(Path directory) throws IOException {
        return parseDirectory(directory).stream()
                .flatMap(f -> f.getScenarios().stream())
                .collect(Collectors.toList());
    }
}
