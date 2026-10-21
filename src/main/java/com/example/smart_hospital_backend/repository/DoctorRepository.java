package com.example.smart_hospital_backend.repository;

import com.example.smart_hospital_backend.entity.Doctor;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :doctorId")
    Optional<Doctor> findByIdForUpdate(@Param("doctorId") Long doctorId);

    Optional<Doctor> findByUserEmail(String email);

    boolean existsByUser_IdAndIdNot(Long userId, Long doctorId);

    boolean existsByUser_Id(Long userId);
}
