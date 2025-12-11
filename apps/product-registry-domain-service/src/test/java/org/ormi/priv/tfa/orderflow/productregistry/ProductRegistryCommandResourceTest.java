package org.ormi.priv.tfa.orderflow.productregistry;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;

@QuarkusTest
@DisplayName("Tests d'intégration ProductRegistryCommandResource")
class ProductRegistryCommandResourceTest {

    private static final String BASE_URL = "/api/products";

    @Nested
    @DisplayName("POST /api/products/register - Enregistrement")
    class RegisterProductTests {

        @Test
        @DisplayName("Doit créer un produit avec des données valides")
        void shouldRegisterValidProduct() {
            // Given
            RegisterProductCommandDto command = new RegisterProductCommandDto(
                "SKU-00001",
                "Laptop",
                "High-performance laptop"
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(command)
            .when()
                .post(BASE_URL + "/register")
            .then()
                .statusCode(201)
                .header("Location", notNullValue());
        }

        @Test
        @DisplayName("Doit retourner 400 si le SKU est manquant")
        void shouldReturn400WhenSkuIsMissing() {
            // Given
            RegisterProductCommandDto command = new RegisterProductCommandDto(
                null,
                "Laptop",
                "Description"
            );

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body(command)
            .when()
                .post(BASE_URL + "/register")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Doit retourner 400 si le body est null")
        void shouldReturn400WhenBodyIsNull() {
            // When & Then
            given()
                .contentType(ContentType.JSON)
            .when()
                .post(BASE_URL + "/register")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{id}/name - Mise à jour du nom")
    class UpdateProductNameTests {

        @Test
        @DisplayName("Doit mettre à jour le nom d'un produit existant")
        void shouldUpdateProductName() {
            // Given - créer un produit d'abord
            RegisterProductCommandDto createCmd = new RegisterProductCommandDto(
                "SKU-00002",
                "Original Name",
                "Description"
            );
            String productId = given()
                .contentType(ContentType.JSON)
                .body(createCmd)
            .when()
                .post(BASE_URL + "/register")
            .then()
                .statusCode(201)
                .extract()
                .path("id");

            // When & Then - mettre à jour le nom
            UpdateProductNameParamsDto updateCmd = new UpdateProductNameParamsDto("Updated Name");
            given()
                .contentType(ContentType.JSON)
                .body(updateCmd)
            .when()
                .patch(BASE_URL + "/" + productId + "/name")
            .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Doit retourner 400 si le body est invalide")
        void shouldReturn400WhenBodyIsInvalid() {
            // Given
            String productId = "00000000-0000-0000-0000-000000000001";

            // When & Then
            given()
                .contentType(ContentType.JSON)
                .body("{\"invalidField\": \"value\"}")
            .when()
                .patch(BASE_URL + "/" + productId + "/name")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id} - Suppression")
    class RetireProductTests {

        @Test
        @DisplayName("Doit retirer un produit existant")
        void shouldRetireExistingProduct() {
            // Given - créer un produit
            RegisterProductCommandDto createCmd = new RegisterProductCommandDto(
                "SKU-00003",
                "Product to Retire",
                "Description"
            );
            String productId = given()
                .contentType(ContentType.JSON)
                .body(createCmd)
            .when()
                .post(BASE_URL + "/register")
            .then()
                .statusCode(201)
                .extract()
                .path("id");

            // When & Then - supprimer
            given()
            .when()
                .delete(BASE_URL + "/" + productId)
            .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Doit retourner 404 si le produit n'existe pas")
        void shouldReturn404IfProductNotFound() {
            // When & Then
            given()
            .when()
                .delete(BASE_URL + "/00000000-0000-0000-0000-000000000099")
            .then()
                .statusCode(404);
        }
    }
}
