package io.kneo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthenticationTest {

    @Test
    public void testSecuredEndpoint() {
        given()
                .when().get("/users")
                .then()
                .statusCode(401);
    }


}
