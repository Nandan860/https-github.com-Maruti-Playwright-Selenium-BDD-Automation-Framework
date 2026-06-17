# =============================================================================
# Feature: Credit Card Payment Processing
# Tags:     @ui  @payments  @smoke
# Covers:   successful payment, API cross-validation, boundary amounts, declined
# =============================================================================
@ui @payments
Feature: Credit Card Payment Processing

  Background:
    Given the user navigates to the banking portal
    And the user logs in with username "testuser@bank.com" and password "Pass@1234"
    And the user navigates to the payment screen

  # ---------------------------------------------------------------------------
  # Core happy path — must run in smoke suite
  # ---------------------------------------------------------------------------
  @smoke
  Scenario: Successful credit card payment
    When the user submits a payment of "500.00" to "ACME Corp" using card "4111111111111111"
    Then the payment confirmation should be displayed
    And the transaction reference should not be empty
    And the transaction should be recorded in the API

  # ---------------------------------------------------------------------------
  # API cross-validation — UI data matches API response
  # ---------------------------------------------------------------------------
  @regression
  Scenario: Payment confirmation data matches API response
    When the user submits a payment of "250.00" to "Test Vendor" using card "4111111111111111"
    Then the payment confirmation should be displayed
    And the API status for the transaction should be "COMPLETED"
    And the API amount for the transaction should be "250.00"

  # ---------------------------------------------------------------------------
  # Boundary and validation scenarios
  # ---------------------------------------------------------------------------
  @regression
  Scenario: Payment fails for amount exceeding daily limit
    When the user submits a payment of "100000.00" to "ACME Corp" using card "4111111111111111"
    Then the payment should fail with error "Daily limit exceeded"

  @regression
  Scenario: Payment fails for expired card
    When the user submits a payment of "100.00" to "ACME Corp" using card "4000000000000069"
    Then the payment should fail with error "Card expired"

  @regression
  Scenario: Payment fails for declined card
    When the user submits a payment of "100.00" to "ACME Corp" using card "4000000000000002"
    Then the payment should fail with error "Card declined"

  @regression
  Scenario: Payment form rejects empty amount
    When the user submits a payment of "" to "ACME Corp" using card "4111111111111111"
    Then the form validation error should show "Amount is required"

  # ---------------------------------------------------------------------------
  # Data-driven — multiple payment amounts and vendors
  # ---------------------------------------------------------------------------
  @regression
  Scenario Outline: Payment processing across various amounts
    When the user submits a payment of "<amount>" to "<payee>" using card "4111111111111111"
    Then the payment result should be "<outcome>"

    Examples:
      | amount    | payee           | outcome  |
      | 1.00      | Min Vendor      | success  |
      | 500.00    | Mid Vendor      | success  |
      | 9999.99   | Max Vendor      | success  |
      | 0.00      | Zero Vendor     | rejected |
      | -100.00   | Negative Vendor | rejected |
