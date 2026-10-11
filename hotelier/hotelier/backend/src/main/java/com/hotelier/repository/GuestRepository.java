package com.hotelier.repository;

import com.hotelier.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for guests. JpaRepository already provides save, findAll, findById, count... */
public interface GuestRepository extends JpaRepository<Guest, Long> {
}
