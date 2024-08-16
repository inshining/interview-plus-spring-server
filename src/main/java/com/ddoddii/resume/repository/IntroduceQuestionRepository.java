package com.ddoddii.resume.repository;

import com.ddoddii.resume.model.question.IntroduceQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntroduceQuestionRepository extends JpaRepository<IntroduceQuestion, Long> {
    List<IntroduceQuestion> findAll();

    IntroduceQuestion findIntroduceQuestionById(long questionId);
}
