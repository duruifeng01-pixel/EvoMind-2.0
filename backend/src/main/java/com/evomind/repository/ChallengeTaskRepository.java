package com.evomind.repository;

import com.evomind.entity.ChallengeTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChallengeTaskRepository extends JpaRepository<ChallengeTask, Long> {

    Optional<ChallengeTask> findByDateKey(String dateKey);

    default Optional<ChallengeTask> findToday() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return findByDateKey(today);
    }
}
