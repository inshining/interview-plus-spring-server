package com.ddoddii.resume.model;

import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.model.question.PersonalQuestion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
@ManyToOne : 자식 엔티티(FK를 들고있는 쪽)
@ManyToOne(optional = false) : FK 에 null 값을 허용하지 않는다.
@OneToMany : 부모 엔티티(참조 당하는 쪽)
Interview(다) - Resume(1) : 양방향
Interview(다) - User(1) : 양방향
 */

@Entity
@Table(name = "interview")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Interview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_round")
    private InterviewRound interviewRound;

    @Column(name = "company_id")
    private int companyId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_id")
    private int jobId;

    @Column(name = "department_id")
    private int departmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonalQuestion> personalQuestions;


    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> introduceEval;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> personalEval;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> techEval;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> behaviorEval;
}
