Feature: Data Export

  Scenario: Export results to TXT file
    Given crawled data is available
    When a user exports results to a TXT file
    Then the system should write data to "results.txt"

  Scenario: Export logs to TXT file
    Given crawling logs are available
    When a user exports logs to a TXT file
    Then the system should write logs to "log.txt"
