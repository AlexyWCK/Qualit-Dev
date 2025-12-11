package org.ormi.priv.tfa.orderflow.cqrs.infra.persistence;

import java.util.List;

import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;

/**
 * Repository d'accès à la boîte de sortie (outbox).
 * Gère la persistance et le traitement des messages en attente de publication.
 *
 * @author Order Flow Team
 */
public interface OutboxRepository {
    /**
     * Publie une entité OutboxEntity (ajoute au journal outbox).
     *
     * @param entity l'entité à publier (ne doit pas être null)
     */
    void publish(OutboxEntity entity);

    /**
     * Récupère les messages prêts à être traités par type d'agrégat.
     *
     * @param aggregateType type d'agrégat (ne doit pas être null)
     * @param limit nombre maximum de messages à récupérer
     * @param maxRetries nombre maximum de tentatives autorisées
     * @return liste des messages prêts à être envoyés
     */
    List<OutboxEntity> fetchReadyByAggregateTypeOrderByAggregateVersion(String aggregateType, int limit, int maxRetries);

    /**
     * Supprime une entité OutboxEntity (message traité avec succès).
     *
     * @param entity l'entité à supprimer (ne doit pas être null)
     */
    void delete(OutboxEntity entity);

    /**
     * Marque une entité comme échouée (sans reprogrammation).
     *
     * @param entity l'entité (ne doit pas être null)
     * @param err message d'erreur (ne doit pas être null)
     */
    void markFailed(OutboxEntity entity, String err);

    /**
     * Marque une entité comme échouée avec reprogrammation.
     *
     * @param entity l'entité (ne doit pas être null)
     * @param err message d'erreur (ne doit pas être null)
     * @param retryAfter secondes à attendre avant la prochaine tentative
     */
    void markFailed(OutboxEntity entity, String err, int retryAfter);
}
