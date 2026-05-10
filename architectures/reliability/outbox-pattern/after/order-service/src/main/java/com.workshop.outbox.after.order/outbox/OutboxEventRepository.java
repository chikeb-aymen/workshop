package com.workshop.outbox.after.order.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    /**
     * Fetches in insertion order to preserve per-aggregate event ordering.
     * The composite index on (status, created_at) makes this fast even with a large backlog.
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingOrderedByCreatedAt();

    long countByStatus(String status);
}
