package com.kaleido.fetch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaleido.cabinetclient.client.CabinetClient;
import com.kaleido.cabinetclient.domain.CabinetPlateMap;
import com.kaleido.cabinetclient.domain.enumeration.Status;
import com.kaleido.fetch.domain.Activity;
import com.kaleido.fetch.domain.ActivitySummary;
import com.kaleido.fetch.domain.ActivityVersion;
import com.kaleido.fetch.domain.dto.AtlasPlateDTO;
import com.kaleido.fetch.domain.enumeration.PlateMapStatus;
import com.kaleido.fetch.domain.mapper.AtlasPlateMapper;
import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Experiment;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ActivityService<E> {

    private final KaptureClient<Experiment> experimentKaptureClient;
    private final CabinetClient<CabinetPlateMap> plateMapCabinetClient;
    private final AtlasPlateMapper atlasPlateMapper;


    public ActivityService(KaptureClient<Experiment> experimentKaptureClient, CabinetClient<CabinetPlateMap> plateMapCabinetClient, AtlasPlateMapper atlasPlateMapper) {
        this.atlasPlateMapper = atlasPlateMapper;
        this.plateMapCabinetClient = plateMapCabinetClient;
        this.experimentKaptureClient = experimentKaptureClient;
    }

    public List<Experiment> findActivities(@PathVariable String searchTerm) {
        log.debug("call to /activities/search/{}", searchTerm);

        final String originalSearchTerm = searchTerm;

        log.debug("Expanded search term to elastic search term {}", searchTerm);
        var experimentComponents = CompletableFuture.supplyAsync(() -> searchExperiment(originalSearchTerm));

        var matches = Stream.of(experimentComponents)
            //wait for all the futures to complete
            .map(CompletableFuture::join)
            //make a combined stream of components
            .flatMap(Collection::stream)
            //make a boolean make with two lists, those that have an exact match and those that don't
            .collect(Collectors.partitioningBy(experiment -> experiment.getName().equals(originalSearchTerm)));

        //sort the matches that are not exact
        List<Experiment> sortedNonExactMatches = matches.get(false).stream()
            .sorted(Comparator.comparing(Experiment::getName))
            .collect(Collectors.toList());

        log.debug("Sorted non-exact matches {}", sortedNonExactMatches);

        //don't sort exact matches
        List<Experiment> sortedExactMatches = new ArrayList<>(matches.get(true));

        log.debug("Sorted exact matches: {}", sortedExactMatches);

        //combine the exact and non-exact matches
        List<Experiment> results = Stream.of(sortedExactMatches, sortedNonExactMatches)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        //return the results
        log.debug("Combined results: {}", results);

        return results;
    }

    public Activity getActivity(Long id) {
        experimentKaptureClient.find(id);
        return new Activity();
    }

    public ResponseEntity<AtlasPlateDTO> saveActivity(AtlasPlateDTO atlasPlateDTO, Status plateMapStatus) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CabinetPlateMap cabinetPlateMap = new CabinetPlateMap()
            .activityName(atlasPlateDTO.getActivityName())
            .data(mapper.writeValueAsString(atlasPlateDTO));

        cabinetPlateMap.setStatus(plateMapStatus);

        ResponseEntity<CabinetPlateMap> responseEntity = plateMapCabinetClient.save(cabinetPlateMap);

        atlasPlateDTO = atlasPlateMapper.mapCabinetPlateToAtlasPlateDTO(responseEntity.getBody());
        return ResponseEntity.ok(atlasPlateDTO);
    }

    public ResponseEntity<AtlasPlateDTO> getPlatesByActivityName(String activityName) throws JsonProcessingException {
        log.info("Activity name is ", activityName);
        ResponseEntity<CabinetPlateMap> responseEntity = plateMapCabinetClient.findOneByMethod("data", activityName);
        if (responseEntity.getStatusCodeValue() == 404) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            AtlasPlateDTO atlasPlateDTO = atlasPlateMapper.mapCabinetPlateToAtlasPlateDTO(responseEntity.getBody());
            return ResponseEntity.ok(atlasPlateDTO);
        }
    }

    public ResponseEntity<CabinetPlateMap[]> getCompletedPayloadList(String activityName) {
        log.info("Activity name is ", activityName);
        return null;
    }

    public ResponseEntity<CabinetPlateMap[]> getDraftPayload(String activityName) {
        log.info("Activity name is ", activityName);
        //  String plateMapURI = cabinetURI + "/plate-maps/data/draft/"+activityName;

        //This is created just to follow the cabinet client but it is not used
        CabinetPlateMap plateMap = new CabinetPlateMap();

        //return (ResponseEntity<CabinetPlateMap[]>) plateMapCabinetClient.cabinetPlatemap(plateMapURI, plateMap, HttpMethod.GET, CabinetPlateMap[].class);
        return null;
    }

    public ResponseEntity<CabinetPlateMap[]> getSpecificCompletedPayload(String checksum) {
        log.info("Checksum value is ", checksum);
        //String plateMapURI = cabinetURI + "/plate-maps/data/"+checksum;

        //This is created just to follow the cabinet client but it is not used
        CabinetPlateMap plateMap = new CabinetPlateMap();

//        return (ResponseEntity<CabinetPlateMap[]>) plateMapCabinetClient.cabinetPlatemap(plateMapURI, plateMap, HttpMethod.GET, CabinetPlateMap[].class);

        return null;
    }

    public List<ActivitySummary> getActivitySummaryList(String searchTerm) {
        log.debug("ActivitySummary name is ", searchTerm);
        List<ActivitySummary> activitySummaryList = this.findActivities(searchTerm).stream()
            .map(this::buildActivitySummary)
            .collect(Collectors.toList());
        activitySummaryList.forEach(action -> {
            action.getVersions().addAll(getPlateMapSummaryFromCabinet(action.getName()).stream()
                .map(this::buildActivityVersion)
                .collect(Collectors.toList()));
        });
        return activitySummaryList;
    }

    public ResponseEntity<List<Experiment>> responseSearch(String name) {
        HttpHeaders responseHeaders = new HttpHeaders();

        List<Experiment> response = (List<Experiment>) findActivities(name);

        return (ResponseEntity<List<Experiment>>) new ResponseEntity<List<Experiment>>(response, responseHeaders, HttpStatus.OK);
    }

    public ResponseEntity<E> retrieveActivityByName(String name) {
        HttpHeaders responseHeaders = new HttpHeaders();

        List<Experiment> response = (List<Experiment>) findActivities(name);

        if (response.isEmpty()) {
            return (ResponseEntity<E>) new ResponseEntity<String>("No results found", responseHeaders, HttpStatus.NOT_FOUND);
        }

        return (ResponseEntity<E>) new ResponseEntity<Experiment>(response.get(0), responseHeaders, HttpStatus.OK);
    }

    private List<Experiment> searchExperiment(String searchTerm) {
        final var mediaResponse = experimentKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");
        return mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null ?
            new ArrayList<>(mediaResponse.getBody()) : //map the responses to a Component
            Collections.emptyList();
    }

    private Activity buildActivity(Experiment experiment) {
        if (experiment == null) {
            return null;
        }

        return Activity.builder()
            .id(experiment.getId())
            .name(experiment.getName())
            .description(experiment.getDescription())
            .build();
    }

    private List<CabinetPlateMap> getPlateMapSummaryFromCabinet(String activityName) {
        RestTemplate restTemplate = new RestTemplate();
        // String cabinetplateInfoURI = cabinetURI + "plate-map-summary/"+activityName;
//        ResponseEntity<CabinetPlateMap[]> plateMapResponse = (ResponseEntity<CabinetPlateMap[]>)plateMapCabinetClient.cabinetPlatemap(cabinetplateInfoURI, null, HttpMethod.GET, CabinetPlateMap[].class);
//        return plateMapResponse.getStatusCode().is2xxSuccessful() && plateMapResponse.getBody() != null ?
//                Arrays.stream(plateMapResponse.getBody())
//                    .collect(Collectors.toList()) :
//                Collections.emptyList();

        return null;
    }

    private ActivitySummary buildActivitySummary(Experiment experment) {
        return ActivitySummary
            .builder()
            .name(experment.getName())
            .description(experment.getDescription())
            .id(experment.getId())
            .numPlates(experment.getNumberOfPlates())
            .versions(new ArrayList<ActivityVersion>())
            .build();
    }

    private ActivityVersion buildActivityVersion(CabinetPlateMap plateMap) {
        return ActivityVersion.builder()
            .id(plateMap.getId())
            .status(plateMap.getStatus().name())
            .numPlates(plateMap.getNumPlates())
            .timestamp(plateMap.getLastModified())
            .build();
    }

}
