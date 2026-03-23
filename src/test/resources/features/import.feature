Feature: File Import
  As a user of the X-crawler system
  I want to import data from files

  Scenario: Import task configurations from file
    Given a TXT file "task_configuration.txt"
    When a user imports task configurations from the file
    Then the system should read "task_configuration.txt"

  Scenario: Import proxy servers from file
    Given a TXT file "ip.txt" with one IP per line
    When a user imports proxy servers from the file
    Then the system should read "ip.txt"

  Scenario: Import URL list from CSV
    Given a CSV file "urls.csv" with URLs in the first column
    When a user imports URLs from the CSV file
    Then the system should read URLs from "urls.csv"
