package com.ddoddii.resume.repository;

import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUser(User user);

    Interview findInterviewById(long interviewId);
}
