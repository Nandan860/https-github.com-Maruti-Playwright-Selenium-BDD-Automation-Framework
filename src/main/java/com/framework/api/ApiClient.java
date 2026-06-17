package com.framework.api;

import com.framework.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Thin REST Assured wrapper.
 *
 * Each ApiClient instance is configured for one base URL.
 * For multi-service testing, create multiple instances.
 *
 * Usage:
 *   ApiClient api = new ApiClient("api.base.url");
 *   Response r = api.get("/accounts/123");
 *   r.then().statusCode(200);
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);
    private final RequestSpecification spec;

    public ApiClient(String baseUrlConfigKey) {
        String baseUrl = ConfigManager.get(baseUrlConfigKey, baseUrlConfigKey);
        log.info("ApiClient initialised → {}", baseUrl);

        spec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    public ApiClient withBearerToken(String token) {
        RestAssured.given(spec).header("Authorization", "Bearer " + token);
        return this;
    }

    // ── HTTP verbs ──────────────────────────────────────────────────────────

    public Response get(String path) {
        return RestAssured.given(spec).get(path);
    }

    public Response get(String path, Map<String, Object> queryParams) {
        return RestAssured.given(spec).queryParams(queryParams).get(path);
    }

    public Response post(String path, Object body) {
        return RestAssured.given(spec).body(body).post(path);
    }

    public Response put(String path, Object body) {
        return RestAssured.given(spec).body(body).put(path);
    }

    public Response patch(String path, Object body) {
        return RestAssured.given(spec).body(body).patch(path);
    }

    public Response delete(String path) {
        return RestAssured.given(spec).delete(path);
    }

    /** Multipart file upload */
    public Response uploadFile(String path, File file, String controlName) {
        return RestAssured.given(spec)
                .contentType("multipart/form-data")
                .multiPart(controlName, file)
                .post(path);
    }

    /** Download — returns raw bytes */
    public byte[] download(String path) {
        return RestAssured.given(spec).get(path).asByteArray();
    }

    // ── Status code assertions (readable in step defs) ──────────────────────

    public void assertStatus(Response response, int expectedStatus) {
        int actual = response.statusCode();
        if (actual != expectedStatus) {
            throw new AssertionError(
                "Expected HTTP " + expectedStatus + " but got " + actual
                + "\nBody: " + response.body().asString());
        }
        log.info("✓ Status {} confirmed for {}", expectedStatus, response.getHeader("Content-Type"));
    }
}
