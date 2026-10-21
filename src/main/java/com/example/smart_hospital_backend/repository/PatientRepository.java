package com.example.smart_hospital_backend.repository;

import com.example.smart_hospital_backend.entity.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Patient p where p.id = :patientId")
    Optional<Patient> findByIdForUpdate(@Param("patientId") Long patientId);

    @Query("""
            select p from Patient p
            where (:patientId is not null and p.id = :patientId)
               or (:term is not null and (
                    lower(p.name) like lower(concat('%', :term, '%'))
                    or lower(p.email) like lower(concat('%', :term, '%'))
                    or lower(p.phone) like lower(concat('%', :term, '%'))))
            """)
    Page<Patient> searchForReception(@Param("patientId") Long patientId,
            @Param("term") String term, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Patient> findByEmail(String email);
}
