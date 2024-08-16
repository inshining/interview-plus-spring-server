package com.ddoddii.resume.model.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "introduce_question")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class IntroduceQuestion extends BaseQuestionEntity {

    @Column(name = "criteria", length = 2000)
    private String criteria;
}
