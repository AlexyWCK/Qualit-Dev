package org.ormi.priv.tfa.orderflow.store.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * DTO pour les opérations de mise à jour de produit. Contient les opérations patch à appliquer.
 */

public record UpdateProductDto(String id, UpdateOperation[] operations) {

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateNameOperation.class, name = "UpdateProductName"),
        @JsonSubTypes.Type(value = UpdateDescriptionOperation.class, name = "UpdateProductDescription")
    })
    public interface UpdateOperation {
        UpdateProductOperationType type();
    }

    @JsonTypeName("UpdateProductName")
    public record UpdateNameOperation(UpdateProductOperationType type, UpdateNameOperationPayload payload) implements UpdateOperation {
        public record UpdateNameOperationPayload(String name) {}
    }

    @JsonTypeName("UpdateProductDescription")
    public record UpdateDescriptionOperation(UpdateProductOperationType type, UpdateDescriptionOperationPayload payload) implements UpdateOperation {
        public record UpdateDescriptionOperationPayload(String description) {}
    }

    public enum UpdateProductOperationType {
        @JsonProperty("UpdateProductName")
        UPDATE_NAME,
        @JsonProperty("UpdateProductDescription")
        UPDATE_DESCRIPTION;
    }
}
