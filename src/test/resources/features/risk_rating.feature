# =============================================================================
# Feature: Risk Rating Application
# Tags:     @ui  @risk
# Note:     Scenario Outline uses an inline Examples table.
#           The step def can also be backed by ExcelReader for larger datasets.
# =============================================================================
@ui @risk
Feature: Risk Rating Application

  Background:
    Given the user navigates to the banking portal
    And the user logs in with username "testuser@bank.com" and password "Pass@1234"
    And the user navigates to the risk rating screen

  # ---------------------------------------------------------------------------
  # Smoke — one representative happy path
  # ---------------------------------------------------------------------------
  @smoke
  Scenario: Risk rating calculated as LOW for strong financial profile
    When the user enters customer id "CUST001", income "120000", score "780", employment "Permanent"
    Then the risk rating should be "LOW"

  # ---------------------------------------------------------------------------
  # Data-driven via Scenario Outline
  # ---------------------------------------------------------------------------
  @regression
  Scenario Outline: Risk rating calculation across customer profiles
    When the user enters customer id "<customerId>", income "<income>", score "<creditScore>", employment "<employment>"
    Then the risk rating should be "<expectedRating>"

    Examples:
      | customerId | income  | creditScore | employment  | expectedRating |
      | CUST001    | 120000  | 780         | Permanent   | LOW            |
      | CUST002    | 60000   | 650         | Permanent   | MEDIUM         |
      | CUST003    | 30000   | 580         | Contract    | HIGH           |
      | CUST004    | 15000   | 490         | Self        | VERY HIGH      |
      | CUST005    | 200000  | 820         | Permanent   | LOW            |
      | CUST006    | 45000   | 610         | Part-time   | MEDIUM         |
      | CUST007    | 8000    | 400         | Unemployed  | VERY HIGH      |

  # ---------------------------------------------------------------------------
  # Excel-backed data-driven test (step def reads from Excel at runtime)
  # ---------------------------------------------------------------------------
  @regression
  Scenario: Risk rating validated against Excel test data
    When the risk rating is calculated for all customers in the Excel sheet "RiskRating"
    Then all risk rating results should match the expected values from the sheet

  # ---------------------------------------------------------------------------
  # Edge cases
  # ---------------------------------------------------------------------------
  @regression
  Scenario: Risk rating form rejects missing credit score
    When the user enters customer id "CUST999", income "50000", score "", employment "Permanent"
    Then the form validation error should show "Credit score is required"

  @regression
  Scenario: Risk rating form rejects negative income
    When the user enters customer id "CUST999", income "-1000", score "700", employment "Permanent"
    Then the form validation error should show "Income must be a positive value"
