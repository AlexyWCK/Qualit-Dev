package org.ormi.priv.tfa.orderflow.kernel.product.persistence;

import java.util.Optional;

import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

/**
 * Repository d'accès à la persistance des aggregates Product.
 * Interface de contrat pour sauvegarder et récupérer des produits.
 *
 * @author Order Flow Team
 */
public interface ProductRepository {
    /**
     * Sauvegarde (ou met à jour) un produit en persistance.
     *
     * @param product le produit à sauvegarder (ne doit pas être null)
     */
    void save(Product product);

    /**
     * Récupère un produit par son identifiant.
     *
     * @param id l'identifiant du produit (ne doit pas être null)
     * @return Optional contenant le produit si trouvé, Optional.empty() sinon
     */
    Optional<Product> findById(ProductId id);

    /**
     * Vérifie si un produit avec le SKU spécifié existe.
     *
     * @param skuId le SKU à rechercher (ne doit pas être null)
     * @return true si un produit avec ce SKU existe, false sinon
     */
    boolean existsBySkuId(SkuId skuId);
}
