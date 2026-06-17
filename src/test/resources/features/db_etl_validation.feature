# =============================================================================
# Feature: ETL / Database Migration Validation
# Tags:     @db  @etl  |  @db  @accounts  |  @db  @transactions  |  @db  @integrity
# Purpose:  Validate data integrity after legacy → new platform migration.
#           Uses DBClient (HikariCP + PostgreSQL) for source vs target comparison.
# =============================================================================
@db @etl
Feature: ETL Migration — Data Integrity Validation

  # ---------------------------------------------------------------------------
  # Accounts table
  # ---------------------------------------------------------------------------
  @accounts @smoke
  Scenario: Accounts table record count matches after migration
    Given the ETL migration for table "accounts" has completed
    When the record count is compared between source and target for table "accounts"
    Then the record counts should match

  @accounts @regression
  Scenario: All account columns migrated correctly
    Given the ETL migration for table "accounts" has completed
    When the QA engineer compares table "accounts" columns "account_type,status,balance,currency" between source and target on key "account_id"
    Then no data discrepancies should be found

  @accounts @regression
  Scenario: Account balances match between source and target
    Then the sum of column "balance" in table "accounts" should match between source and target

  @accounts @regression
  Scenario: No accounts have null status after migration
    Given the ETL migration for table "accounts" has completed
    Then all records in table "accounts" should have non-null "status"

  # ---------------------------------------------------------------------------
  # Transactions table
  # ---------------------------------------------------------------------------
  @transactions @smoke
  Scenario: Transactions table record count matches after migration
    Given the ETL migration for table "transactions" has completed
    When the record count is compared between source and target for table "transactions"
    Then the record counts should match

  @transactions @regression
  Scenario: Chunked comparison of large transactions table
    Given the ETL migration for table "transactions" has completed
    When the QA engineer runs chunked comparison of table "transactions" on key "transaction_id" with chunk size 1000
    Then no data discrepancies should be found

  @transactions @regression
  Scenario: Transaction amounts and statuses migrated accurately
    Given the ETL migration for table "transactions" has completed
    When the QA engineer compares table "transactions" columns "amount,currency,status,transaction_date" between source and target on key "transaction_id"
    Then no data discrepancies should be found

  @transactions @regression
  Scenario: No transactions have null amount after migration
    Given the ETL migration for table "transactions" has completed
    Then all records in table "transactions" should have non-null "amount"

  # ---------------------------------------------------------------------------
  # Data integrity — cross-table referential checks
  # ---------------------------------------------------------------------------
  @integrity @regression
  Scenario: Target database has expected record counts after full migration
    Then the target database should have 5000 records in table "accounts"
    Then the target database should have 50000 records in table "transactions"

  @integrity @regression
  Scenario: Discrepancy threshold check — zero tolerance for financial data
    Given the ETL migration for table "accounts" has completed
    When the QA engineer compares table "accounts" between source and target on key "account_id"
    Then the number of discrepancies should not exceed 0
