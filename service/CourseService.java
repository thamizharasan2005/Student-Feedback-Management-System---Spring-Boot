package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.UserResponseDTO;
import com.example.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.example.FeedbackSystem.DTO.CourseResponseDTO;
import com.example.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.CourseRepository;
import com.example.FeedbackSystem.repository.InstructorRepository;
import com.example.FeedbackSystem.specification.CourseSpecification;
import com.example.FeedbackSystem.specification.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    CourseRepository courseRepo;

    @Autowired
    InstructorRepository instructorRepo;

    @Autowired
    InstructorService instructorService;

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    public List<CourseResponseDTO> getAllCoursesDTO() {
        List<Course> courses = courseRepo.findAll();
        return courses.stream()
                .map(this::convertToCourseResponseDTO)
                .collect(Collectors.toList());
    }

    public CourseResponseDTO convertToCourseResponseDTO(Course course){
        Instructor instructor = null;
        String instructorName = null;

        if(course.getInstructor() != null){
            instructor = instructorService.getInstructorById(course.getInstructor().getInstructorId());
        }
        if(instructor != null){
            instructorName = instructor.getInstructorName();
        }

        return new CourseResponseDTO(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDescription(),
                instructorName
        );
    }

    public Course getCourseById(int courseId) {
        return courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
    }

    public Course addCourse(Course course) {
        return courseRepo.save(course);
    }

//    public Course addCourseDTO(CourseDTO courseDTO){
//        Course course = new Course();
//        course.setCourseName(courseDTO.getCourseName());
//        course.setCourseDescription(courseDTO.getCourseDescription());
//        course.setInstructor(courseDTO.getInstructor());
//        return course;
//    }

    public Course updateCourseById(int courseId, Course course){
        Course course1 = getCourseById(courseId);

        course1.setCourseName(course.getCourseName());
        course1.setInstructor(course.getInstructor());
        course1.setFeedbacks(course.getFeedbacks());

        return courseRepo.save(course1);
    }

    // @SqlDelete Soft Delete the record instead of physically deleting it
    public void deleteCourseById(int courseId) {
        if(getCourseById(courseId) == null){
            throw new ResourceNotFoundException("User not Found");
        }
        courseRepo.deleteById(courseId);
    }

    public List<CourseResponseDTO> findAllSoftDeletedCourses(){
        List<Course> deletedCourses = courseRepo.findAllDeletedCourses();
                // this won't work because the '@Where clause' in Course entity global filters every query with a query that defined in that clause
                // so .findAll() won't get the soft deleted courses
//                findAll().stream()
//                .filter(course -> course.isDeleted())
//                .toList();
        if(deletedCourses.isEmpty()){
            throw new ResourceNotFoundException("No courses deleted.");
        }
        return deletedCourses.stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }

    // To restore the deleted records
    public CourseResponseDTO restoreCourse(int courseId){
        Course course = courseRepo.restoreCourseById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setDeleted(false);
        courseRepo.save(course);
        return convertToCourseResponseDTO(course);

    }

    @Transactional
    public void deleteCoursePermanently(int courseId){
        if(getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found");
        }
        courseRepo.deletePermanently(courseId);
    }

    public Course assignInstructorToCourse(int courseId, int instructorId){
        Course course = getCourseById(courseId);
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with ID: "+instructorId+" not found." ));

        course.setInstructor(instructor);
        return courseRepo.save(course);
    }

    public Course unassignInstructorToCourse(int courseId){
        Course course = getCourseById(courseId);
        if(course.getInstructor() != null) {
            course.setInstructor(null);
        }
        return courseRepo.save(course);
    }


    public List<Course> searchCourseByName(String courseName){
        try {
            return courseRepo.findByCourseNameContainingIgnoreCase(courseName);
        } catch (Exception e) {
            throw new ResourceNotFoundException("No matches found!");
        }
    }

    public List<CourseResponseDTO> getCoursesForInstructor(int instructorId){
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with ID: "+instructorId+" not found." ));

        List<CourseResponseDTO> courseList = instructor.getCourses().stream()
                .map(this::convertToCourseResponseDTO)
                .collect(Collectors.toList());

        if(courseList.isEmpty()){
            throw new ResourceNotFoundException("No courses available for instructor");
        }
        return courseList;
    }

    public List<CourseFeedbackCountDTO> getFeedbackCountAndAvg(){
        return courseRepo.countFeedbacksAndAvgRatePerCourse();
    }


    public List<CourseResponseDTO> getSortedCourses(String sortBy) {
        if(!sortBy.equals("ASC".toLowerCase()) || !sortBy.equals("DESC".toLowerCase())){
            throw new BadRequestException("Provide correct sorting order ASC or DESC");
        }

        Sort sort = sortBy.equalsIgnoreCase("desc") ? Sort.by("courseName").descending() :
                Sort.by("courseName").ascending();
        // findAll method accept Sort as a parameter
        return courseRepo.findAll(sort).stream()
                .map(this::convertToCourseResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PopularCourseDTO> getPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findPopularCourses(pageable);
    }

    // Slice<T> is useful for infinite scrolling or “Load More” UI. & Doesn't count data
    public Slice<PopularCourseDTO> getUnPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findUnPopularCourses(pageable);
    }

    public List<CourseResponseDTO> searchCourse(Integer courseId,
                                                Integer instructorId,
                                                String courseName,
                                                String instructorName
    ){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.hasCourseId(courseId),
                CourseSpecification.hasInstructorId(instructorId),
                CourseSpecification.hasCourseName(courseName),
                CourseSpecification.hasInstructorName(instructorName)
        );
        return courseRepo.findAll(specification).stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }

    public List<CourseResponseDTO> hasEnrollmentsGreaterThan(int minEnrollments) {
        return courseRepo.hasEnrollmentGreaterThan(minEnrollments).stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesWithoutFeedback(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesWithoutFeedback()
        );
        return courseRepo.findAll(specification).stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesNotAssigned(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesNotAssigned()
        );
        return courseRepo.findAll(specification).stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesLessThanAvgRating(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.greaterThanAvgRatingCourses()
        );
        return courseRepo.findAll(specification).stream()
                .map(this::convertToCourseResponseDTO)
                .toList();
    }
}
