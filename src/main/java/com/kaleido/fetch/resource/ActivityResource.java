package com.kaleido.fetch.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaleido.cabinetclient.domain.CabinetPlateMap;
import com.kaleido.cabinetclient.domain.enumeration.Status;
import com.kaleido.fetch.domain.Activity;
import com.kaleido.fetch.domain.ActivitySummary;
import com.kaleido.fetch.domain.dto.AtlasPlateDTO;
import com.kaleido.fetch.domain.enumeration.PlateMapStatus;
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
    public ResponseEntity<ResponseEntity<List<Experiment>>> searchActivities(@PathVariable String searchTerm) {
        return ResponseEntity.ok(activityService.responseSearch(searchTerm));
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

    @GetMapping("/completed/{id}")
    public ResponseEntity<Activity> getActivity(@PathVariable String id) {
        return ResponseEntity.ok(activityService.getActivity(Long.valueOf(id)));
    }

    @GetMapping("/completed/{id}/{version}")
    public ResponseEntity<Activity> getActivityByVersion(@PathVariable String id, @PathVariable String version) {
        return ResponseEntity.ok(activityService.getActivity(Long.valueOf(id)));
    }

    @ApiOperation(value = "Saves current copy of platemap data")
    @PostMapping("/save")
    public ResponseEntity<AtlasPlateDTO> saveActivityDraft(@RequestBody AtlasPlateDTO atlasPlateDTO) throws JsonProcessingException {
        return  activityService.saveActivity(atlasPlateDTO, Status.DRAFT);
    }

    @ApiOperation(value = "Saves a snapshot of the passed activity, and creates a version which can be retrieved at a later time.")
    @PostMapping("/release")
    public ResponseEntity<AtlasPlateDTO> saveCompletedActivity(@RequestBody AtlasPlateDTO atlasPlateDTO) throws JsonProcessingException {
        return  activityService.saveActivity(atlasPlateDTO, Status.COMPLETED);
    }

    @ApiOperation(value = "Retrieves platemap data for the provided Activity name")
    @GetMapping("/retrieve-platemap/{activityName}")
    public ResponseEntity<ResponseEntity<CabinetPlateMap>> getActivitiesPlatemap(@PathVariable String activityName) throws JsonProcessingException {
        return activityService.getPlatesByActivityName(activityName);
    }

    @ApiOperation(value = "Retrieves the list of completed activity for the given activityName")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = CabinetPlateMap[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/cabinet/completedlist/{activityName}")
    public ResponseEntity<ResponseEntity<Activity>> getCompletedActivity(@PathVariable String activityName) {
        return ResponseEntity.ok(activityService.getCompletedPayloadList(activityName));
    }

    @ApiOperation(value = "Retrieves the list of draft activity for the given activityName")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = CabinetPlateMap[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })

    @GetMapping("/cabinet/draft/{activityName}")
    public ResponseEntity<ResponseEntity<Activity>> getDraftActivity(@PathVariable String activityName) {
        return ResponseEntity.ok(activityService.getDraftPayload(activityName));
    }

    @ApiOperation(value = "Retrieves a specific completed payload based on checksum")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = CabinetPlateMap[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/cabinet/completed/{checksum}")
    public ResponseEntity<ResponseEntity<Activity>> getSpecificCompletedActivity(@PathVariable String checksum) {
        return ResponseEntity.ok(activityService.getSpecificCompletedPayload(checksum));
    }

    @ApiOperation(value = "Retrieves the activity summary details by given activity name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Activity.class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/activitySummary/{activityName}")
    public ResponseEntity<List<ActivitySummary>> getActivitySummary(@PathVariable String activityName) {
         return ResponseEntity.ok(activityService.getActivitySummaryList(activityName));
    }

    @ApiOperation(value = "Retrieves the activity details based on given name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Experiment.class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/{name}")
    public ResponseEntity<ResponseEntity<Experiment>> getActivityId(@PathVariable String name) {
         return ResponseEntity.ok(activityService.retrieveActivityByName(name));
    }
}
