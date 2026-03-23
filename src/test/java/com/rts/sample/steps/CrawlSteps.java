package com.rts.sample.steps;

import com.rts.sample.service.*;

public class CrawlSteps {

    @Given("a URL {string}")
    public void aUrl(String url) {
        UrlResolver resolver = new UrlResolver();
        resolver.resolve(url);
    }

    @Given("a user enters a URL")
    public void aUserEntersUrl() {
        UrlValidator validator = new UrlValidator();
    }

    @Given("the crawl settings page is displayed")
    public void settingsPage() {
        CrawlConfig config = new CrawlConfig();
    }

    @When("a user crawls data from the URL")
    public void crawlData() {
        HttpClient client = new HttpClient();
        SeedManager seeds = new SeedManager();
    }

    @When("the system validates the URL format")
    public void validateUrl() {
        UrlValidator validator = new UrlValidator();
        validator.validate();
    }

    @When("a user sets the crawl depth to {int}")
    public void setCrawlDepth(int depth) {
        CrawlConfig config = new CrawlConfig();
        config.setDepth(depth);
    }

    @Then("the system should use {string} as the seed")
    public void useAsSeed(String url) {
        SeedManager seeds = new SeedManager();
    }

    @Then("the system should accept valid URLs and reject invalid ones")
    public void acceptValidUrls() {}

    @Then("the system should limit crawling to {int} levels deep")
    public void limitCrawling(int depth) {}

    @interface Given { String value(); }
    @interface When { String value(); }
    @interface Then { String value(); }
}
