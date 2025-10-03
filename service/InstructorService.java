package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.CourseResponseDTO;
import com.example.FeedbackSystem.DTO.InstructorResponseDTO;
import com.example.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.example.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.repository.CourseRepository;
import com.example.FeedbackSystem.repository.InstructorRepository;
import com.example.FeedbackSystem.specification.CourseSpecification;
import com.example.FeedbackSystem.specification.InstructorSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorService {

    @Autowired
    InstructorRepository instructorRepo;

    @Autowired
    CourseRepository courseRepo;


    public List<Instructor> getAllInstructor() {
        return instructorRepo.findAll();
    }

    public Instructor getInstructorById(int instructorId) {
        return instructorRepo.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found."));
    }

    public InstructorResponseDTO getInstructorByIdDTO(int instructorId){
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found."));
        return new InstructorResponseDTO(instructor.getInstructorId(),
                instructor.getInstructorName(),
                instructor.getCourses().stream()
                        .map(course ->  {
                            return new CourseResponseDTO(course.getCourseId(),
                                            course.getCourseName(),
                                            course.getCourseDescription(),
                                            course.getInstructor().getInstructorName());
                                }
                        )
                        .toList());
    }

    public Instructor addInstructor(Instructor instructor) {
        return instructorRepo.save(instructor);
    }

    public Instructor updateInstructorById(int instructorId, Instructor instructor){

        Instructor instructor1 = getInstructorById(instructorId);

        instructor1.setInstructorName(instructor.getInstructorName());
        instructor1.setCourses(instructor.getCourses());

        return instructorRepo.save(instructor1);
    }


    public void deleteInstructorById(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        if(instructor == null){
            throw new ResourceNotFoundException("Instructor not found!");
        }
        instructorRepo.delete(instructor);
    }

    public Instructor assignCourseToInstructor(int instructorId, int courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
        Instructor instructor = getInstructorById(instructorId);

        if(course.getInstructor() != null){
            throw new BadRequestException(HttpStatus.BAD_REQUEST+
                    " Course already assigned to an instructor "+course.getInstructor().getInstructorName());
        }

        instructor.addCourse(course);

        return instructorRepo.save(instructor);
    }

    public void unassignCourseFromInstructor(int instructorId, int courseId) {
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getInstructor() != null && course.getInstructor().getInstructorId() == instructorId) {
            instructor.removeCourse(course);
            courseRepo.save(course);
        } else {
            throw new RuntimeException("This course is not assigned to this instructor");
        }
    }

    public List<Instructor> getUnassignedInstructors(){
//        List<Instructor> instructors = getAllInstructor();
//        return instructors.stream()
//                .filter(instructor -> instructor.getCourses().isEmpty())
//                .collect(Collectors.toList());
        if(instructorRepo.findUnassignedInstructors().isEmpty()){
            throw new ResourceNotFoundException("Every instructors are assigned to courses");
        }
        return instructorRepo.findUnassignedInstructors();
    }

    public List<Course> viewAssignedCourse(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        if(instructor.getCourses().isEmpty()){
            throw new ResourceNotFoundException("No courses assigned to this instructor.");
        }
        return instructor.getCourses();
    }

    public List<FeedbacksByInstructor> getAllFeedbacksByInstructor(){
        return instructorRepo.getAllFeedbacksByInstructor();
    }

    public List<TopRatedInstructorsDTO> getAllTopRatedInstructors() {
        return instructorRepo.findTopRatedInstructor();
    }

    public List<Instructor> searchInstructor(Integer instructorId,
                                                String instructorName,
                                                String courseName
    ){
        Specification<Instructor> specification = Specification.allOf(
                InstructorSpecification.hasInstructorId(instructorId),
                InstructorSpecification.hasInstructorName(instructorName),
                InstructorSpecification.byAssignedCourseName(courseName)
        );
        return instructorRepo.findAll(specification);
    }
}

