package com.ddoddii.resume.repository;

import com.ddoddii.resume.model.question.TechQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechQuestionRepository extends JpaRepository<TechQuestion, Long> {
    TechQuestion findTechQuestionById(long id);

    List<TechQuestion> findTechQuestionByPosition(String position);
}
