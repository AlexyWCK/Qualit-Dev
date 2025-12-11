package org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read;

import java.time.Instant;

/**
 * DTO pour un élément du stream de produits exposé aux clients. Contient les données d'événement.
 */
public record ProductStreamElementDto(
    String type,
    String productId,
    Instant occuredAt
) {
}
