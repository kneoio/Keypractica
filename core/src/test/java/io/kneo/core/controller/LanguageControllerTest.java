package io.kneo.core.controller;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.service.LanguageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;


@QuarkusTest
public class LanguageControllerTest {

    @InjectMock
    LanguageService languageService;

    @Test
    public void testGetLanguages() {
        // Arrange
        List<LanguageDTO> languages = Arrays.asList(
                new LanguageDTO(LanguageCode.BUL, new HashMap<>()),
                new LanguageDTO(LanguageCode.SPA, new HashMap<>()));
        Mockito.when(languageService.getAll(0,0)).thenReturn(Uni.createFrom().item(languages));

        // Act
        given()
                .when().get("/languages")
                .then()
                .statusCode(200)
                .body("size()", is(equalTo(1)));
    }
}
