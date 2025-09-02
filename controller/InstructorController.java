package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.FeedbacksByInstructor;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.service.InstructorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructor")
public class InstructorController {

    @Autowired
    InstructorService instructorService;

    @GetMapping("/all")
    public ResponseEntity<List<Instructor>> getAllInstructor(){
        return new ResponseEntity<>(instructorService.getAllInstructor(), HttpStatus.OK);
    }

    @GetMapping("/id/{instructorId}")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable int instructorId){
        return new ResponseEntity<>(instructorService.getInstructorById(instructorId), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Instructor> addInstructor(@RequestBody Instructor instructor){
        return new ResponseEntity<>(instructorService.addInstructor(instructor), HttpStatus.CREATED);
    }

    @PutMapping("/update/{instructorId}")
    public ResponseEntity<?> updateInstructorById(@PathVariable int instructorId, @RequestBody Instructor instructor){
        return new ResponseEntity<>(instructorService.updateInstructorById(instructorId, instructor), HttpStatus.OK);
    }

    @DeleteMapping("delete/{instructorId}")
    public ResponseEntity<?> deleteInstructorById(@PathVariable int instructorId){
        try{
            instructorService.deleteInstructorById(instructorId);
            return ResponseEntity.ok().body("Instructor with ID: "+instructorId+" deleted successfully");
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    // Assigning and unassigning course to instructor
    @PutMapping("/{instructorId}/assign-course/{courseId}")
    public ResponseEntity<?> assignCourse(@PathVariable int instructorId,
                                                   @PathVariable int courseId){
        try {
            return ResponseEntity.ok(instructorService.assignCourseToInstructor(instructorId, courseId));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{instructorId}/unassign-course/{courseId}")
    public ResponseEntity<String> unassignCourseFromInstructor(@PathVariable int instructorId, @PathVariable int courseId) {
        instructorService.unassignCourseFromInstructor(instructorId, courseId);
        return ResponseEntity.ok("Course unassigned from instructor");
    }

    @GetMapping("/all/unassigned-instructor")
    public ResponseEntity<?> getAllUnassignedInstructors(){
        try {
            return ResponseEntity.ok(instructorService.getUnassignedInstructors());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/courses-assigned/{instructorId}")
    public ResponseEntity<List<?>> viewAssignedCourses(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.viewAssignedCourse(instructorId));

    }

    @GetMapping("/all-feedbacks/instructor")
    public ResponseEntity<List<?>> getAllFeedbacksByInstructor(){
        try {
            return ResponseEntity.ok(instructorService.getAllFeedbacksByInstructor());
        }
        catch (Exception e){
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
