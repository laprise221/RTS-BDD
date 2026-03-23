package com.rts.sample.steps;

import com.rts.sample.service.FileWriter;
import com.rts.sample.service.LogFormatter;
import com.rts.sample.service.ResultFormatter;

public class ExportSteps {

    @Given("crawled data is available")
    public void crawledDataAvailable() {}

    @Given("crawling logs are available")
    public void logsAvailable() {}

    @When("a user exports results to a TXT file")
    public void exportResults() {
        FileWriter writer = new FileWriter();
        ResultFormatter formatter = new ResultFormatter();
    }

    @When("a user exports logs to a TXT file")
    public void exportLogs() {
        FileWriter writer = new FileWriter();
        LogFormatter formatter = new LogFormatter();
    }

    @Then("the system should write data to {string}")
    public void writeData(String filename) {}

    @Then("the system should write logs to {string}")
    public void writeLogs(String filename) {}

    @interface Given { String value(); }
    @interface When { String value(); }
    @interface Then { String value(); }
}
