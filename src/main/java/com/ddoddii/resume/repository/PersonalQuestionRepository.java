package com.ddoddii.resume.repository;

import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.question.PersonalQuestion;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalQuestionRepository extends
        JpaRepository<com.ddoddii.resume.model.question.PersonalQuestion, Long> {

    PersonalQuestion findPersonalQuestionById(long questionId);

    boolean existsByInterview(Interview interview);
}
