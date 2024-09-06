package com.ddoddii.resume.model.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tech_question")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TechQuestion extends BaseQuestionEntity {
    @Column(name = "topic")
    private String topic;

    @Column(name = "position")
    private String position;

    @Column(name = "example_answer", length = 2000)
    private String exampleAnswer;
}
