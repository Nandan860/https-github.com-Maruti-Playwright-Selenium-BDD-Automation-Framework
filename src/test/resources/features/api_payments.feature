# =============================================================================
# Feature: Payments API Validation
# Tags:     @api  @payments  @smoke
# =============================================================================
@api @payments
Feature: Payments API Validation

  # ---------------------------------------------------------------------------
  # GET endpoints
  # ---------------------------------------------------------------------------
  @smoke
  Scenario: GET payment by valid transaction ID returns 200
    When the user sends a GET request to "/api/v1/payments/TXN001"
    Then the response status should be 200
    And the response header "Content-Type" should be "application/json"
    And the response field "transactionId" should equal "TXN001"
    And the response field "status" should equal "COMPLETED"

  @regression
  Scenario: GET payment by non-existent ID returns 404
    When the user sends a GET request to "/api/v1/payments/TXNNOTEXIST"
    Then the response status should be 404
    And the response body should contain "Transaction not found"

  @regression
  Scenario: GET all payments returns paginated 200 response
    When the user sends a GET request to "/api/v1/payments"
    Then the response status should be 200
    And the response field "page" should equal "0"
    And the response body should contain "totalElements"

  # ---------------------------------------------------------------------------
  # POST endpoints
  # ---------------------------------------------------------------------------
  @smoke
  Scenario: POST valid payment request returns 201 Created
    When the user sends a POST request to "/api/v1/payments" with body:
      """
      {
        "cardNumber": "4111111111111111",
        "amount": 500.00,
        "currency": "GBP",
        "payee": "ACME Corp",
        "reference": "REF-SMOKE-001"
      }
      """
    Then the response status should be 201
    And the response body should contain "transactionId"
    And the response field "status" should equal "PENDING"

  @regression
  Scenario: POST payment with missing required field returns 400
    When the user sends a POST request to "/api/v1/payments" with body:
      """
      {
        "amount": 100.00,
        "payee": "ACME Corp"
      }
      """
    Then the response status should be 400
    And the response body should contain "cardNumber is required"

  @regression
  Scenario: POST payment with negative amount returns 422
    When the user sends a POST request to "/api/v1/payments" with body:
      """
      {
        "cardNumber": "4111111111111111",
        "amount": -50.00,
        "payee": "ACME Corp"
      }
      """
    Then the response status should be 422
    And the response body should contain "Amount must be positive"

  # ---------------------------------------------------------------------------
  # DELETE endpoint
  # ---------------------------------------------------------------------------
  @regression
  Scenario: DELETE pending payment returns 200
    When the user sends a DELETE request to "/api/v1/payments/TXN-PENDING-001"
    Then the response status should be 200

  @regression
  Scenario: DELETE completed payment returns 409 Conflict
    When the user sends a DELETE request to "/api/v1/payments/TXN001"
    Then the response status should be 409
    And the response body should contain "Cannot cancel a completed transaction"

  # ---------------------------------------------------------------------------
  # API File Upload
  # ---------------------------------------------------------------------------
  @regression @filetransfer
  Scenario: Upload bulk payments CSV via API returns 202 Accepted
    When the user uploads file "bulk-payments.csv" via API to "/api/v1/payments/bulk"
    Then the response status should be 202
    And the response body should contain "batchId"

  # ---------------------------------------------------------------------------
  # API vs DB cross-validation
  # ---------------------------------------------------------------------------
  @regression
  Scenario: API payment response matches database record
    When the user sends a GET request to "/api/v1/payments/TXN001"
    Then the response status should be 200
    And the API response should match the database record for id "TXN001"
