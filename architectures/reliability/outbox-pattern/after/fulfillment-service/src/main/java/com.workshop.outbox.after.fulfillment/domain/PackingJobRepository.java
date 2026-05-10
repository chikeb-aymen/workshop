package com.workshop.outbox.after.fulfillment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PackingJobRepository extends JpaRepository<PackingJob, Long> {}
