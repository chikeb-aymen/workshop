package com.workshop.outbox.after.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {}
