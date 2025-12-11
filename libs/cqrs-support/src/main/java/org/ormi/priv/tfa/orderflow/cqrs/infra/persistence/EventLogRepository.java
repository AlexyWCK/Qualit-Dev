package org.ormi.priv.tfa.orderflow.cqrs.infra.persistence;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;

/**
 * Repository d'accès à la persistance du journal des événements.
 * Enregistre tous les événements de domaine pour la traçabilité et la reconstruction d'état.
 *
 * @author Order Flow Team
 */
public interface EventLogRepository {
    /**
     * Ajoute (append) un événement au journal des événements.
     * Opération atomique : l'événement doit être enregistré exactement une fois.
     *
     * @param eventLog l'enveloppe d'événement à enregistrer (ne doit pas être null)
     * @return l'entité EventLogEntity persistée (avec id généré)
     */
    EventLogEntity append(EventEnvelope<?> eventLog);
}
