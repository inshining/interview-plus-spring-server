package com.ddoddii.resume.model;

import com.ddoddii.resume.model.eunm.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Evaluation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Evaluation 이 Interview의 FK를 가지고 있다 = 연관관계의 주인이다
    @JoinColumn(name = "interview_id")
    private Long interviewId;

    @ManyToOne
    @JoinColumn(name = "interview")
    private Interview interview;

    @Column(name = "question_id")
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    @Column(name = "asked_question", columnDefinition = "TEXT")
    private String askedQuestion;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "gpt_evaluation", columnDefinition = "TEXT")
    private String gptEvaluation;

    private boolean done;
}
