package com.example.smart_hospital_backend.repository;

import com.example.smart_hospital_backend.entity.QueueEntry;
import com.example.smart_hospital_backend.entity.QueueStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from QueueEntry q where q.id = :queueEntryId")
    Optional<QueueEntry> findByIdForUpdate(@Param("queueEntryId") Long queueEntryId);

    boolean existsByAppointment_Id(Long appointmentId);

    long countByDoctor_IdAndQueueDate(Long doctorId, LocalDate queueDate);

    List<QueueEntry> findByQueueDateAndStatusInOrderByCheckedInAtAscIdAsc(
            LocalDate queueDate, Collection<QueueStatus> statuses);

    List<QueueEntry> findByDoctor_IdAndQueueDateAndStatusInOrderByCheckedInAtAscIdAsc(
            Long doctorId, LocalDate queueDate, Collection<QueueStatus> statuses);

    Optional<QueueEntry> findFirstByAppointment_Patient_IdAndQueueDateAndStatusInOrderByCheckedInAtDesc(
            Long patientId, LocalDate queueDate, Collection<QueueStatus> statuses);

    @Query("""
        select count(q) from QueueEntry q
        where q.doctor.id = :doctorId and q.queueDate = :queueDate
          and q.status = :waiting
          and (q.checkedInAt < :checkedInAt or (q.checkedInAt = :checkedInAt and q.id < :entryId))
        """)
    long countWaitingAhead(@Param("doctorId") Long doctorId,
            @Param("queueDate") LocalDate queueDate,
            @Param("waiting") QueueStatus waiting,
            @Param("checkedInAt") java.time.LocalDateTime checkedInAt,
            @Param("entryId") Long entryId);
}
