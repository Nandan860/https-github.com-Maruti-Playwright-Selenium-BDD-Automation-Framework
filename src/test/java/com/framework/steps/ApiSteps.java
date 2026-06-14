package com.framework.steps;

import com.framework.api.ApiClient;
import com.framework.db.DBClient;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {

    private final ApiClient api = new ApiClient("api.base.url");
    private Response lastResponse;

    // ── Request steps ───────────────────────────────────────────────────────

    @When("the user sends a GET request to {string}")
    public void get(String path) {
        lastResponse = api.get(path);
    }

    @When("the user sends a POST request to {string} with body:")
    public void post(String path, String body) {
        lastResponse = api.post(path, body);
    }

    @When("the user sends a DELETE request to {string}")
    public void delete(String path) {
        lastResponse = api.delete(path);
    }

    // ── Response assertions ─────────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void assertStatus(int status) {
        api.assertStatus(lastResponse, status);
    }

    @Then("the response body should contain {string}")
    public void assertBodyContains(String text) {
        assertThat(lastResponse.body().asString()).contains(text);
    }

    @Then("the response field {string} should equal {string}")
    public void assertField(String jsonPath, String expected) {
        String actual = lastResponse.jsonPath().getString(jsonPath);
        assertThat(actual)
                .as("JSON path: " + jsonPath)
                .isEqualToIgnoringCase(expected);
    }

    @Then("the response header {string} should be {string}")
    public void assertHeader(String header, String expected) {
        assertThat(lastResponse.header(header)).isEqualToIgnoringCase(expected);
    }

    // ── API → DB cross-validation ───────────────────────────────────────────

    @Then("the API response should match the database record for id {string}")
    public void validateApiAgainstDb(String id) {
        String apiStatus = lastResponse.jsonPath().getString("status");
        String apiAmount = lastResponse.jsonPath().getString("amount");

        List<Map<String, Object>> rows = DBClient.target()
                .query("SELECT status, amount FROM transactions WHERE id = ?", id);

        assertThat(rows).as("DB record for id " + id).isNotEmpty();
        Map<String, Object> dbRow = rows.get(0);

        assertThat(apiStatus)
                .as("status: API vs DB")
                .isEqualToIgnoringCase(String.valueOf(dbRow.get("status")));
        assertThat(apiAmount)
                .as("amount: API vs DB")
                .isEqualTo(String.valueOf(dbRow.get("amount")));
    }

    // ── File upload via API ─────────────────────────────────────────────────

    @When("the user uploads file {string} via API to {string}")
    public void uploadFileViaApi(String fileName, String path) {
        java.io.File file = new java.io.File("src/test/resources/testdata/" + fileName);
        assertThat(file).exists();
        lastResponse = api.uploadFile(path, file, "file");
    }
}