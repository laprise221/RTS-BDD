Feature: Web Crawling

  Scenario: Crawl data from URL
    Given a URL "www.example.com"
    When a user crawls data from the URL
    Then the system should use "www.example.com" as the seed

  Scenario: Validate URL format
    Given a user enters a URL
    When the system validates the URL format
    Then the system should accept valid URLs and reject invalid ones

  Scenario: Configure crawl depth
    Given the crawl settings page is displayed
    When a user sets the crawl depth to 3
    Then the system should limit crawling to 3 levels deep
