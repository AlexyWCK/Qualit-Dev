package org.ormi.priv.tfa.orderflow.kernel.product;

import java.time.Instant;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductDescriptionUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductNameUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRegistered;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRetired;

/**
 * Classe abstraite enveloppe spécialisée pour les événements ProductEventV1.
 * Encapsule les événements avec métadonnées (séquence, timestamp).
 *
 * @param <E> le type d'événement ProductEventV1 encapsulé
 * @author Order Flow Team
 */
public abstract class ProductEventV1Envelope<E extends ProductEventV1> extends EventEnvelope<E> {

    public ProductEventV1Envelope(E event, Long sequence, Instant timestamp) {
		super(event, sequence, timestamp);
	}

	public static class ProductRegisteredEnvelope extends ProductEventV1Envelope<ProductRegistered> {
        public ProductRegisteredEnvelope(ProductRegistered event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    public static class ProductRetiredEnvelope extends ProductEventV1Envelope<ProductRetired> {
        public ProductRetiredEnvelope(ProductRetired event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    public static class ProductNameUpdatedEnvelope extends ProductEventV1Envelope<ProductNameUpdated> {
        public ProductNameUpdatedEnvelope(ProductNameUpdated event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    public static class ProductDescriptionUpdatedEnvelope extends ProductEventV1Envelope<ProductDescriptionUpdated> {
        public ProductDescriptionUpdatedEnvelope(ProductDescriptionUpdated event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }
}
