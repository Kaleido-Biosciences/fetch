package com.kaleido.fetch.resource;

import com.kaleido.fetch.domain.Activity;
import com.kaleido.fetch.service.ActivityService;
import com.kaleido.kaptureclient.domain.Experiment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("Activities Operations")
@RestController
@RequestMapping("/activities")
public class ActivityResource {

    private final ActivityService activityService;

    public ActivityResource(ActivityService activityService) {
        this.activityService = activityService;
    }

    @ApiOperation(value = "Finds Activities by text string to match on")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Experiment[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<List<Experiment>> searchActivities(@PathVariable String searchTerm) {
        return ResponseEntity.ok(activityService.findActivities(searchTerm));
    }

    @ApiOperation(value = "Retrieves the list of completed versions for the given activity id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Experiment[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/completed/search/{id}")
    public ResponseEntity<List<Experiment>> getCompletedVersions(@PathVariable String id) {
        return ResponseEntity.ok(activityService.findActivities(id));
    }

    @ApiOperation(value = "Retrieves the version of the activity for the given id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Activity.class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/completed/{id}/{version}")
    public ResponseEntity<Activity> getActivity(@PathVariable String id, @PathVariable String version) {
        return ResponseEntity.ok(activityService.getActivity(Long.valueOf(id)));
    }
    
    @ApiOperation(value = "Saves the state of the activity that is passed to it.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation, returns the most recent activity with an updated lastModified time", response = Activity.class),
            @ApiResponse(code = 400, message = "Activity was not most recent activity, and returns the most up to date activity", response = Activity.class)
    })
    @PostMapping("/save")
    public ResponseEntity<ResponseEntity<Activity>> saveActivity(@RequestBody Activity activity) {
        //TODO: Add call to the service to save the working draft of the activity.
        return ResponseEntity.ok(activityService.saveActivityDraft(activity));
    }

    @ApiOperation(value = "Saves a snapshot of the passed activity, and creates a version which can be retrieved at a later time.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Activity.class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @PostMapping("/save/completed")
    public ResponseEntity<Activity> saveCompletedActivity(@RequestBody Activity activity) {
        //TODO: Add the call to the service to save
        return null;
    }
}
