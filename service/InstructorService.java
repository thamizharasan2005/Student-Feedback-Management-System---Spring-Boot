package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.FeedbacksByInstructor;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.repository.CourseRepository;
import com.example.FeedbackSystem.repository.InstructorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        instructorRepo.delete(instructor);
    }

    public Instructor assignCourseToInstructor(int instructorId, int courseId){
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found."));
        Instructor instructor = getInstructorById(instructorId);

        if(course.getInstructor() != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Course already assigned to an instructor "+course.getInstructor().getInstructorName());
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
            course.setInstructor(null);
            courseRepo.save(course);
        } else {
            throw new RuntimeException("Course is not assigned to this instructor");
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
}

