package com.ddoddii.resume.repository;

import com.ddoddii.resume.model.Evaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByInterviewId(long interviewId);
}
