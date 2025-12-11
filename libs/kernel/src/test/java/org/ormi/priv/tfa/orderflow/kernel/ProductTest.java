package org.ormi.priv.tfa.orderflow.kernel;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

@DisplayName("Tests unitaires pour Product")
class ProductTest {

    private SkuId validSkuId;
    private String validName;
    private String validDescription;

    @BeforeEach
    void setUp() {
        validSkuId = new SkuId("SKU-00001");
        validName = "Laptop";
        validDescription = "High-performance laptop";
    }

    @Nested
    @DisplayName("Création de produit")
    class CreateProductTests {

        @Test
        @DisplayName("Doit créer un produit valide")
        void shouldCreateValidProduct() {
            // When
            Product product = Product.create(validName, validDescription, validSkuId);

            // Then
            assertNotNull(product);
            assertEquals(validName, product.getName());
            assertEquals(validDescription, product.getDescription());
            assertEquals(validSkuId, product.getSkuId());
            assertEquals(ProductLifecycle.ACTIVE, product.getStatus());
            assertEquals(1L, product.getVersion());
        }

        @Test
        @DisplayName("Doit jeter une exception si le nom est null")
        void shouldThrowExceptionIfNameIsNull() {
            assertThrows(ConstraintViolationException.class, () ->
                Product.create(null, validDescription, validSkuId)
            );
        }

        @Test
        @DisplayName("Doit jeter une exception si le nom est vide")
        void shouldThrowExceptionIfNameIsEmpty() {
            assertThrows(ConstraintViolationException.class, () ->
                Product.create("", validDescription, validSkuId)
            );
        }

        @Test
        @DisplayName("Doit jeter une exception si la description est null")
        void shouldThrowExceptionIfDescriptionIsNull() {
            assertThrows(ConstraintViolationException.class, () ->
                Product.create(validName, null, validSkuId)
            );
        }

        @Test
        @DisplayName("Doit jeter une exception si le skuId est null")
        void shouldThrowExceptionIfSkuIdIsNull() {
            assertThrows(ConstraintViolationException.class, () ->
                Product.create(validName, validDescription, null)
            );
        }
    }

    @Nested
    @DisplayName("Mise à jour du nom")
    class UpdateNameTests {

        private Product product;

        @BeforeEach
        void setUp() {
            product = Product.create(validName, validDescription, validSkuId);
        }

        @Test
        @DisplayName("Doit mettre à jour le nom d'un produit actif")
        void shouldUpdateNameOfActiveProduct() {
            // Given
            String newName = "Gaming Laptop";

            // When
            var event = product.updateName(newName);

            // Then
            assertNotNull(event);
            assertEquals(newName, product.getName());
            assertEquals(2L, product.getVersion());
        }

        @Test
        @DisplayName("Doit jeter une exception si on met à jour un produit retiré")
        void shouldThrowExceptionWhenUpdatingRetiredProduct() {
            // Given
            product.retire();

            // Then
            assertThrows(IllegalStateException.class, () ->
                product.updateName("New Name")
            );
        }

        @Test
        @DisplayName("Doit jeter une exception si le nouveau nom est vide")
        void shouldThrowExceptionIfNewNameIsEmpty() {
            assertThrows(ConstraintViolationException.class, () ->
                product.updateName("")
            );
        }
    }

    @Nested
    @DisplayName("Mise à jour de la description")
    class UpdateDescriptionTests {

        private Product product;

        @BeforeEach
        void setUp() {
            product = Product.create(validName, validDescription, validSkuId);
        }

        @Test
        @DisplayName("Doit mettre à jour la description d'un produit actif")
        void shouldUpdateDescriptionOfActiveProduct() {
            // Given
            String newDescription = "Ultra-high-performance laptop";

            // When
            var event = product.updateDescription(newDescription);

            // Then
            assertNotNull(event);
            assertEquals(newDescription, product.getDescription());
            assertEquals(2L, product.getVersion());
        }

        @Test
        @DisplayName("Doit jeter une exception si on met à jour un produit retiré")
        void shouldThrowExceptionWhenUpdatingRetiredProduct() {
            // Given
            product.retire();

            // Then
            assertThrows(IllegalStateException.class, () ->
                product.updateDescription("New Description")
            );
        }
    }

    @Nested
    @DisplayName("Retrait du produit")
    class RetireProductTests {

        private Product product;

        @BeforeEach
        void setUp() {
            product = Product.create(validName, validDescription, validSkuId);
        }

        @Test
        @DisplayName("Doit retirer un produit actif")
        void shouldRetireActiveProduct() {
            // When
            var event = product.retire();

            // Then
            assertNotNull(event);
            assertEquals(ProductLifecycle.RETIRED, product.getStatus());
            assertEquals(2L, product.getVersion());
        }

        @Test
        @DisplayName("Doit jeter une exception si on retire un produit déjà retiré")
        void shouldThrowExceptionWhenRetiringRetiredProduct() {
            // Given
            product.retire();

            // Then
            assertThrows(IllegalStateException.class, () ->
                product.retire()
            );
        }
    }
}
