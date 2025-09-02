package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.CourseFeedbackCountDTO;
import com.example.FeedbackSystem.DTO.CourseResponseDTO;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    CourseService courseService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<Course>> getAllCourses(){
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public ResponseEntity<List<CourseResponseDTO>> getAllCoursesDTO(){
        return new ResponseEntity<>(courseService.getAllCoursesDTO(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable int courseId){
            return new ResponseEntity<>(courseService.getCourseById(courseId), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Course> addCourse(@RequestBody Course course){
        return new ResponseEntity<>(courseService.addCourse(course), HttpStatus.OK);
    }

    @PutMapping("/update/{courseId}")
    public ResponseEntity<?> updateCourseById(@PathVariable int courseId, @RequestBody Course course){
        return new ResponseEntity<>(courseService.updateCourseById(courseId, course), HttpStatus.OK);
    }

    @DeleteMapping("delete/{courseId}")
    public ResponseEntity<?> deleteCourseById(@PathVariable int courseId){
            courseService.deleteCourseById(courseId);
            return ResponseEntity.ok().body("Course soft deleted successfully");
    }

    @GetMapping("/all/deleted")
    public ResponseEntity<List<CourseResponseDTO>> AllDeletedCourses(){
        return ResponseEntity.ok(courseService.findAllSoftDeletedCourses());
    }

    @PutMapping("/restore/{courseId}")
    public ResponseEntity<CourseResponseDTO> restoreCourse(@PathVariable int courseId){
        return ResponseEntity.ok(courseService.restoreCourse(courseId));
    }

    @DeleteMapping("delete/permanent/{courseId}")
    public ResponseEntity<?> deleteCoursePermanentlyById(@PathVariable int courseId){
        courseService.deleteCoursePermanently(courseId);
        return ResponseEntity.ok().body("Course with ID: "+courseId+" deleted successfully");
    }

    @PutMapping("/assign/{courseId}/toInstructor/{instructorId}")
    public ResponseEntity<Course> assignCourse(@PathVariable int courseId,
                                               @PathVariable int instructorId){
        return ResponseEntity.ok(courseService.assignInstructorToCourse(courseId, instructorId));
    }

    @PutMapping("/{courseId}/unassign-instructor")
    public ResponseEntity<Course> unassignCourse(@PathVariable int courseId){
        return ResponseEntity.ok(courseService.unassignInstructorToCourse(courseId));
    }


    @GetMapping("/searchBy")
    public ResponseEntity<List<Course>> searchCourseByName(@RequestParam String courseName){
        return new ResponseEntity<>(courseService.searchCourseByName(courseName), HttpStatus.OK);
    }

    //get all courses that a single instructor teach
    @GetMapping("/all/instructor/Id")
    public ResponseEntity<?> getCoursesForInstructor(@RequestParam int instructorId){
        return ResponseEntity.ok(courseService.getCoursesForInstructor(instructorId));

    }

    @GetMapping("/feedback-status")
    public ResponseEntity<List<CourseFeedbackCountDTO>> getFeedbackCount(){
        return ResponseEntity.ok(courseService.getFeedbackCount());
    }

    @GetMapping("/sort-by")
    public ResponseEntity<List<CourseResponseDTO>> getSortedCourse(@RequestParam(defaultValue = "ASC") String sortBy){
        return ResponseEntity.ok(courseService.getSortedCourses(sortBy));
    }
}
