package com.rts.RTSengine;

import com.rts.model.Scenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.lucene.index.IndexFileNames.stripExtension;

public class TestMappingLoader {
    // Capture @Given("..."), @When("..."), @Then("...")
    private static final Pattern STEP_ANNOTATION = Pattern.compile(
            "@(Given|When|Then|And|But)\\s*\\(\"(.+?)\"\\)");

    // Capture les imports : import com.example.service.Foo;
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "import\\s+([\\w.]+)\\s*;");

    // Capture new Foo() et foo.method()
    private static final Pattern CLASS_USAGE = Pattern.compile(
            "new\\s+(\\w+)\\s*\\(|\\b(\\w+)\\s*\\.\\s*\\w+\\s*\\(");


    public static void scannerAnnotations(Path testSourceRoot,
                                          List<Scenario> scenarios) throws IOException {

        //séparation des tests unitaires des tests d'acceptance

        //récupération des fichiers Steps
        List<Path> stepDefFiles;
        try (Stream<Path> walk = Files.walk(testSourceRoot)) {
            stepDefFiles = walk
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith("Steps.java")
                                || name.endsWith("StepDefs.java")
                                || name.endsWith("StepDefinitions.java");
                    })
                    .collect(Collectors.toList());
        }

        //récupération des tests unitaires
        List<Path> unitTestFiles;
        try (Stream<Path> walk = Files.walk(testSourceRoot)) {
            unitTestFiles = walk
                    .filter(p -> p.toString().endsWith("Test.java"))
                    .filter(p -> !p.toString().contains("Steps")) //On exclu les steps
                    .collect(Collectors.toList());
        }

        for(Path stepDefFile : stepDefFiles) {
            String content = Files.readString(stepDefFile);
            String stepDefName = stripExtension(stepDefFile.getFileName().toString());

            //récupération du contenu des annotations
            List<String> patterns = new ArrayList<>();
            Matcher annMatcher = STEP_ANNOTATION.matcher(content);
            while (annMatcher.find()) {
                patterns.add(annMatcher.group(2));
            }

            //Extraction des classes pour les UT
            Set<String>classesProduction =extraireClassesProduction(content);

            for (Scenario scenario : scenarios) {
                if (matchePattern(scenario, patterns)) {
                    scenario.setAcceptanceTestId(stepDefName);

                    for (Path utFile : unitTestFiles) {
                        String utName = stripExtension(utFile.getFileName().toString());
                        String classeTestee = utName.replaceAll("Test$", "");
                        if (classesProduction.contains(classeTestee)) {
                            scenario.addUnitTestId(utName);
                        }
                    }
                }
            }
        }


    }

    //permet d'extraire les dépendances du test d'acceptance pour pouvoir faire la liaison AT -> UT
    public  static Set<String> extraireClassesProduction(String source){
        Set<String> classes = new HashSet<>();

        //Capture des classes des imports
        Matcher importMatcher = IMPORT_PATTERN.matcher(source);
        while (importMatcher.find()) {
            String fqcn = importMatcher.group(1);
            //on saute les frameworks
            if (fqcn.contains(".test.") || fqcn.contains(".junit.")
                    || fqcn.contains(".cucumber.") || fqcn.startsWith("java.")
                    || fqcn.startsWith("javax.")) continue;
            //filtrage des package pour récupérer uniquement le nom de la classe
            classes.add(fqcn.substring(fqcn.lastIndexOf('.') + 1));
        }

        //capture des classes instanciées est appelées
        Matcher usageMatcher = CLASS_USAGE.matcher(source);
        while (usageMatcher.find()) {
            String name = usageMatcher.group(1) != null
                    ? usageMatcher.group(1) : usageMatcher.group(2);
            if (name != null && Character.isUpperCase(name.charAt(0))) {
                classes.add(name);
            }
        }

        return classes;
    }


    //permet de vérifier qu'un scénario correspond aux patterns d'une step def
    private static boolean matchePattern(Scenario scenario, List<String> patterns) {

        //extraction du texte du scenario
        StringBuilder sb = new StringBuilder(scenario.getName());
        for (var step : scenario.getSteps()) {
            sb.append(" ").append(step.getText());
        }
        String texteComplet = sb.toString();

        for (String pattern : patterns) {
            // Convertir les expressions Cucumber en regex
            String regex = pattern.toLowerCase()
                    .replaceAll("\\{string}", ".*")
                    .replaceAll("\\{int}", "\\\\d+")
                    .replaceAll("\\{float}", "[\\\\d.]+")
                    .replaceAll("\\{word}", "\\\\w+");
            try {
                if (texteComplet.matches(".*" + regex + ".*")) return true;
            } catch (Exception e) {
                // Si l'expression ne matche pas entièrement, on vérifie qu'elle match a 60%
                String simplifie = pattern.toLowerCase()
                        .replaceAll("\\{[^}]+}", " ")
                        .replaceAll("\\s+", " ").trim();
                String[] mots = simplifie.split("\\s+");
                int matchCount = 0;
                for (String mot : mots) {
                    if (mot.length() > 2 && texteComplet.contains(mot)) matchCount++;
                }
                if (mots.length > 0 && (double) matchCount / mots.length > 0.6)
                    return true;
            }
        }
        return false;
    }
}
