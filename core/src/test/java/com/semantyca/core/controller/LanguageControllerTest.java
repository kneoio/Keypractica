package com.semantyca.core.controller;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.service.LanguageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class LanguageControllerTest {

    @InjectMock
    LanguageService myService;


    @Test
    public void testCountUsers() {
        Mockito.when(myService.update("ddd", new LanguageDTO())).thenReturn(Uni.createFrom().item(1));
        given()
                .when().get("/users/count")
                .then()
                .statusCode(200)
                .body(is("1"));
    }
}
