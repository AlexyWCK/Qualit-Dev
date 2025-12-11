package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

/**
 * Interface scellée pour les commandes de produit. Représente les actions métier possibles.
 */
public sealed interface ProductCommand {
    public record RegisterProductCommand(
            String name,
            String description,
            SkuId skuId) implements ProductCommand {
    }

    public record RetireProductCommand(ProductId productId) implements ProductCommand {
    }

    public record UpdateProductNameCommand(ProductId productId, String newName) implements ProductCommand {
    }

    public record UpdateProductDescriptionCommand(ProductId productId, String newDescription) implements ProductCommand {
    }
}
