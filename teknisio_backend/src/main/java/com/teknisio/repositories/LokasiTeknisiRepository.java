package com.teknisio.repositories;

import com.teknisio.model.entities.LokasiTeknisi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for persisting the last known GPS location of a technician
 * per service request.
 */
@Repository
public interface LokasiTeknisiRepository extends JpaRepository<LokasiTeknisi, UUID> {
}
