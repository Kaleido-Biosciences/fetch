package com.kaleido.fetch.resource;

import com.kaleido.fetch.service.ActivityService;
import com.kaleido.kaptureclient.domain.Experiment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
