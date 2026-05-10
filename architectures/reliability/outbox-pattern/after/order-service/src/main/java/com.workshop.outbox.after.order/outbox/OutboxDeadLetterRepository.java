package com.workshop.outbox.after.order.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxDeadLetterRepository extends JpaRepository<OutboxDeadLetter, String> {}
