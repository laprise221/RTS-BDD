package com.rts.sample.steps;

import com.rts.sample.service.ConfigParser;
import com.rts.sample.service.FileImporter;
import com.rts.sample.service.TxtFileReader;

// Ce fichier simule une vraie step definition Cucumber.
// Le scan d'annotations va :
//   1. Lire les @Given/@When/@Then pour matcher avec les scénarios
//   2. Extraire les imports (FileImporter, TxtFileReader, ConfigParser)
//   3. Chercher les *Test.java correspondants

public class ImportSteps {

    @Given("a TXT file {string}")
    public void aTxtFile(String filename) {
        TxtFileReader reader = new TxtFileReader();
        reader.load(filename);
    }

    @Given("a CSV file {string} with URLs in the first column")
    public void aCsvFileWithUrls(String filename) {
        FileImporter importer = new FileImporter();
        importer.loadCsv(filename);
    }

    @When("a user imports task configurations from the file")
    public void importTaskConfigurations() {
        ConfigParser parser = new ConfigParser();
        parser.parse();
    }

    @When("a user imports proxy servers from the file")
    public void importProxyServers() {
        FileImporter importer = new FileImporter();
        importer.importProxies();
    }

    @When("a user imports URLs from the CSV file")
    public void importUrlsFromCsv() {
        FileImporter importer = new FileImporter();
        importer.importUrls();
    }

    @Then("the system should read {string}")
    public void systemShouldRead(String filename) {
        // vérification
    }

    @Then("the system should read URLs from {string}")
    public void systemShouldReadUrls(String filename) {
        // vérification
    }

    // Annotations factices pour que le scan les détecte
    @interface Given { String value(); }
    @interface When { String value(); }
    @interface Then { String value(); }
}
