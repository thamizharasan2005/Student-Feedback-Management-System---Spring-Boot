package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.example.FeedbackSystem.DTO.FeedbackDTO;
import com.example.FeedbackSystem.DTO.FeedbackResponseDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Feedback;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.EnrollmentRepository;
import com.example.FeedbackSystem.repository.FeedbackRepository;
import com.example.FeedbackSystem.specification.FeedbackSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    FeedbackRepository feedbackRepo;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    EnrollmentRepository enrollmentRepo;

    public List<FeedbackResponseDTO> getFeedbacks() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        return feedbacks.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Feedback getFeedbackById(int feedbackId) {
        return feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("No Feedback"));
    }

    public Feedback convertToFeedbackFromDTO(FeedbackDTO feedbackDTO, Feedback feedback){
        //converting FeedbackDTO into Feedback
        int studentId = feedbackDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int courseId = feedbackDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);
        //mapping fields
        feedback.setCourseRating(feedbackDTO.getCourseRating());
        feedback.setInstructorRating(feedbackDTO.getInstructorRating());
        feedback.setCourseComment(feedbackDTO.getCourseComment());
        feedback.setInstructorComment(feedbackDTO.getInstructorComment());
        feedback.setAnonymous(feedbackDTO.isAnonymous());
        feedback.setStudent(student);
        feedback.setCourse(course);
        feedback.setSubmittedAt(LocalDate.now());

        return feedback;
    }

    public FeedbackResponseDTO convertToResponseDTO(@Valid Feedback feedback){
        return new FeedbackResponseDTO(
                feedback.getFeedbackId(),
                feedback.getCourseRating(),
                feedback.getInstructorRating(),
                feedback.getCourseComment(),
                feedback.getInstructorComment(),
                feedback.isAnonymous(),
                feedback.isAnonymous() ? "Anonymous" : feedback.getStudent().getUsername(),
                feedback.getCourse().getCourseName(),
                feedback.getCourse().getInstructor().getInstructorName(),
                feedback.getSubmittedAt()
        );
    }

    public FeedbackResponseDTO submitFeedback(@Valid FeedbackDTO feedbackDTO) {
        //fetch student
        int studentId = feedbackDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int todayFeedbackCount = feedbackRepo.countTodayFeedbacks(studentId);
        if(todayFeedbackCount >= 10){
            throw new BadRequestException("You can submit up to 10 feedbacks per day.");
        }

        if(!student.getRole().toString().equals("STUDENT")){
            throw new BadRequestException("Only students can submit feedbacks.");
        }
        //fetch course
        int courseId = feedbackDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);
        //check whether student enrolled or not
        if(!enrollmentRepo.existsByCourseAndStudent(course, student)){
            throw new BadRequestException("Student "+student.getUsername()+" is not enrolled in this course "+course.getCourseName()+".");
        }
        Feedback feedback = new Feedback();
        //convert the FeedbackDTO into Feedback
        convertToFeedbackFromDTO(feedbackDTO, feedback);
        //save it on the database
        feedbackRepo.save(feedback);
        //returning response as FeedbackResponseDTO
        return convertToResponseDTO(feedback);
        //same procedure for updating feedback
    }


    public FeedbackResponseDTO editFeedback(int feedbackDTOId, @Valid FeedbackDTO feedbackDTO) {
        if(!feedbackRepo.existsById(feedbackDTOId)){
            throw new EntityNotFoundException("Feedback not found.");
        }

        int studentId = feedbackDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int courseId = feedbackDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);

        if(!enrollmentRepo.existsByCourseAndStudent(course, student)){
            throw new BadRequestException("Student "+student.getUsername()+" is not enrolled in this course "+course.getCourseName()+".");
        }

        Feedback feedback = getFeedbackById(feedbackDTOId);
        Feedback editedFeedback = convertToFeedbackFromDTO(feedbackDTO, feedback);
        feedbackRepo.save(editedFeedback);
        return convertToResponseDTO(editedFeedback);
    }


    public void deleteFeedbackById(int feedbackId) {
        if(feedbackRepo.existsById(feedbackId)) {
            feedbackRepo.delete(getFeedbackById(feedbackId));
            return;
        }
        throw new ResourceNotFoundException("Feedback not found.");
    }

    //Sorting Method
    public Sort sortingFunction(String sort){
        String[] sortParam = sort.split(","); //Split the string by ',' store them in an array
        String sortBy = sortParam[0];   // sorting order is 0th value which is field name
        Sort.Direction direction = Sort.Direction.fromString(sortParam.length > 1 ? sortParam[1] : "ASC"); //sorting direction is 1st value
        return Sort.by(direction, sortBy);
    }

    public Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId, int page, int size, String sort){
        Sort sorting = sortingFunction(sort);

        if(courseService.getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found!");
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Feedback> feedbacks = feedbackRepo.findByCourse_CourseId(courseId, pageable);

        return feedbacks.map(this::convertToResponseDTO);
    }


    public List<FeedbackResponseDTO> getFilteredFeedback(int courseId, int minRating, int maxRating,
                                                         LocalDate fromDate, LocalDate toDate, boolean anonymous) {
        // parameters can be null
        List<Feedback> feedbacks = feedbackRepo.filterFeedback(
                courseId, minRating, maxRating, fromDate, toDate, anonymous
        );

        // It converts the feedbacks into FeedBackResponseDTO
        return  feedbacks.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<FeedbackResponseDTO> getFeedbackByUserId(int userId, int pageNo,
                                                         int size, String sortString){

        Sort sorting = sortingFunction(sortString);
        Pageable pageable = PageRequest.of(pageNo, size, sorting);
        Page<Feedback> feedbackPages = feedbackRepo.findByStudentUserId(userId, pageable);
        return feedbackPages.map(this::convertToResponseDTO);
    }

    public Double getAverageRatingForCourse(int courseId){
        Double avgRating = feedbackRepo.findAverageRatingByCourseId(courseId);
        if(avgRating == null) {
            throw new EntityNotFoundException("No feedbacks found for this course.");
        }
        return avgRating;
    }

    public Double getAverageRatingForInstructor(int instructorId){
        Double avgRating = feedbackRepo.findAverageRatingByInstructorId(instructorId);
        if(avgRating == null) {
            throw new EntityNotFoundException("No feedbacks found for this instructor.");
        }
        return avgRating;
    }

    public List<CourseFeedbackSummary> getCourseSummary(){
        return feedbackRepo.findCourseSummaries();
    }

    public List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int studentId, int courseId) {
        List<Feedback> feedbacks = feedbackRepo.findByStudent_UserIdAndCourse_CourseId(studentId, courseId);
        if(feedbacks.isEmpty()){
            throw new RuntimeException("No feedbacks found!");
        }
        return feedbacks.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId){
        LocalDate date = LocalDate.now().minusDays(7);
        return feedbackRepo.getRecentFeedbacksByCourseId(courseId, date).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<FeedbackResponseDTO> searchFeedback(Integer courseId, Integer studentId,
                                                    Integer minRating, String keyword,
                                                    String studentName, Boolean anonymous,
                                                    LocalDate fromDate, LocalDate toDate
                                                    ){
        Specification<Feedback> specification = Specification.allOf(
                FeedbackSpecification.hasCourseId(courseId),
                FeedbackSpecification.hasStudentId(studentId),
                FeedbackSpecification.courseRatingGreaterThan(minRating),
                FeedbackSpecification.containsKeyword(keyword),
                FeedbackSpecification.hasStudentName(studentName),
                FeedbackSpecification.anonymousFeedbacks(anonymous),
                FeedbackSpecification.feedbackSubmittedBetween(fromDate, toDate)
                );
        return feedbackRepo.findAll(specification).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

}
