# =============================================================================
# Feature: Login — Banking Portal Authentication
# Tags:     @ui  @login  @smoke (smoke = critical path, runs on every commit)
# =============================================================================
@ui @login
Feature: Login — Banking Portal Authentication

  Background:
    Given the user navigates to the banking portal

  # ---------------------------------------------------------------------------
  # Happy path — must be tagged @smoke so it runs on every CI push
  # ---------------------------------------------------------------------------
  @smoke
  Scenario: Successful login with valid credentials
    When the user logs in with username "testuser@bank.com" and password "Pass@1234"
    Then the user should be redirected to the dashboard
    And the welcome message should contain "Welcome"

  # ---------------------------------------------------------------------------
  # Negative scenarios
  # ---------------------------------------------------------------------------
  @regression
  Scenario: Login fails with invalid password
    When the user logs in with username "testuser@bank.com" and password "wrongpass"
    Then the login should fail with error "Invalid credentials"

  @regression
  Scenario: Login fails with empty username
    When the user logs in with username "" and password "Pass@1234"
    Then the login should fail with error "Username is required"

  @regression
  Scenario: Login fails with empty password
    When the user logs in with username "testuser@bank.com" and password ""
    Then the login should fail with error "Password is required"

  @regression
  Scenario: Account is locked after 3 consecutive failed login attempts
    When the user attempts to login with wrong password 3 times
    Then the account should be locked
    And the error message should contain "Account locked"

  # ---------------------------------------------------------------------------
  # Data-driven — multiple user types
  # ---------------------------------------------------------------------------
  @regression
  Scenario Outline: Login validation across multiple user roles
    When the user logs in with username "<username>" and password "<password>"
    Then the login result should be "<expectedOutcome>"

    Examples:
      | username              | password    | expectedOutcome |
      | admin@bank.com        | Admin@123   | success         |
      | manager@bank.com      | Mgr@456     | success         |
      | readonly@bank.com     | Read@789    | success         |
      | blocked@bank.com      | Block@321   | locked          |
      | notexist@bank.com     | Any@pass1   | invalid         |
