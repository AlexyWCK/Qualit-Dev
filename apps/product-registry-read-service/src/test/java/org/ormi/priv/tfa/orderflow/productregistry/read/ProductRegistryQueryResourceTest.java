package org.ormi.priv.tfa.orderflow.productregistry.read;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("Tests d'intégration ProductRegistryQueryResource")
class ProductRegistryQueryResourceTest {

    private static final String BASE_URL = "/api/products";

    @Nested
    @DisplayName("GET /api/products - Recherche paginée")
    class SearchProductsTests {

        @Test
        @DisplayName("Doit retourner 200 avec tous les produits")
        void shouldReturnAllProducts() {
            // When & Then
            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get(BASE_URL)
            .then()
                .statusCode(200)
                .body("products", notNullValue())
                .body("page", equalTo(0))
                .body("size", equalTo(10));
        }

        @Test
        @DisplayName("Doit filtrer par SKU")
        void shouldFilterBySkuId() {
            // When & Then
            given()
                .queryParam("sku", "SKU-00001")
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get(BASE_URL)
            .then()
                .statusCode(200)
                .body("products", notNullValue());
        }

        @Test
        @DisplayName("Doit retourner une liste vide si pas de correspondance")
        void shouldReturnEmptyListIfNoMatch() {
            // When & Then
            given()
                .queryParam("sku", "NONEXISTENT-SKU")
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get(BASE_URL)
            .then()
                .statusCode(200)
                .body("products", hasSize(0))
                .body("total", equalTo(0));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - Recherche par ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Doit retourner 200 si le produit existe")
        void shouldReturnProductIfExists() {
            // Given - supposer qu'un produit avec cet ID existe
            String productId = "00000000-0000-0000-0000-000000000001";

            // When & Then
            given()
            .when()
                .get(BASE_URL + "/" + productId)
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue());
        }

        @Test
        @DisplayName("Doit retourner 404 si le produit n'existe pas")
        void shouldReturn404IfProductNotFound() {
            // Given
            String nonexistentId = "00000000-0000-0000-0000-000000000099";

            // When & Then
            given()
            .when()
                .get(BASE_URL + "/" + nonexistentId)
            .then()
                .statusCode(404);
        }
    }
}
