package com.kaleido.fetch.service;

import com.kaleido.fetch.domain.Activity;
import com.kaleido.fetch.domain.ActivitySummary;
import com.kaleido.fetch.domain.ActivityVersion;
import com.kaleido.fetch.domain.PlateMap;
import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Experiment;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ActivityService<E> {

    private final KaptureClient<Experiment> experimentKaptureClient;
   
    @Value("${cabinet.endpoint}")
    private String cabinetURI;
   
    public ActivityService(KaptureClient<Experiment> experimentKaptureClient) {
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
    
    public ResponseEntity<String> saveNewActivityDraft(PlateMap plateMap) {
    	
    	log.info("Platemap data is ", plateMap);
    	String plateMapURI = cabinetURI + "plate-maps";
    	RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<PlateMap> entity = new HttpEntity<PlateMap>(plateMap,headers);
    	if(!findActivities(plateMap.getActivityName()).isEmpty()) {
    		return restTemplate.exchange(plateMapURI, HttpMethod.POST, entity, String.class);
    	}
    	else {
    		return new ResponseEntity<String>(HttpStatus.CONFLICT);
    	}
    	
    }

    public ResponseEntity<String> saveActivityDraft(PlateMap plateMap) {
    	
    	log.info("Platemap data is ", plateMap);
    	String plateMapURI = cabinetURI + "plate-maps";
    	
    	RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<PlateMap> entity = new HttpEntity<PlateMap>(plateMap,headers);
        return restTemplate.exchange(plateMapURI, HttpMethod.PUT, entity, String.class);
    }
    
    public ResponseEntity<PlateMap> saveCompletedActivity(PlateMap plateMap) {
    	
    	log.info("Platemap data is ", plateMap);
    	String plateMapURI = cabinetURI + "plate-maps";
    	
    	RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<PlateMap> entity = new HttpEntity<PlateMap>(plateMap,headers);
        return restTemplate.exchange(plateMapURI, HttpMethod.PUT, entity, PlateMap.class);
    }
    
    public ResponseEntity<PlateMap[]> getActivitiesPlatemap(PlateMap plateMap) {
    	
    	log.info("PlateMap data is ", plateMap);
        String plateMapURI = cabinetURI + "plate-maps/details";
    	
    	RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<PlateMap> entity = new HttpEntity<PlateMap>(plateMap,headers);
        return restTemplate.exchange(plateMapURI, HttpMethod.POST, entity, PlateMap[].class);
    }
    
    public ResponseEntity<PlateMap[]> getActivitiesList(String activityName) {
    	log.debug("Activity name is ", activityName);
        String plateMapURI = cabinetURI + "plate-maps/details";
    	
    	RestTemplate restTemplate = new RestTemplate();
    	HttpHeaders headers = new HttpHeaders();
    	PlateMap plateMap = new PlateMap();
    	plateMap.setActivityName(activityName);
    	plateMap.setStatus("COMPLETED");
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<PlateMap> entity = new HttpEntity<PlateMap>(plateMap,headers);
        return restTemplate.exchange(plateMapURI, HttpMethod.POST, entity, PlateMap[].class);
    	
    }
    
    public List<ActivitySummary> getActivitySummaryList(String searchTerm) {
  	    log.debug("ActivitySummary name is ", searchTerm);
  	    List<ActivitySummary> activitySummaryList = this.findActivities(searchTerm).stream()
  	        .map(this::buildActivitySummary)
  	        .collect(Collectors.toList());
  	    activitySummaryList.forEach(action ->{
  		    action.getVersions().addAll(getPlateMapSummaryFromCabinet(action.getName()).stream()
  		        .map(this::buildActivityVersion)
  		        .collect(Collectors.toList()));
  	    });
        return activitySummaryList;
    }
    
   private List<Experiment> searchExperiment(String searchTerm) {
        final var mediaResponse = experimentKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");
        return mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null ?
                new ArrayList<>(mediaResponse.getBody()) : //map the responses to a Component
                Collections.emptyList();
    }

    private Activity buildActivity(Experiment experiment) {
        if(experiment == null) {
            return null;
        }

        return Activity.builder()
                .id(experiment.getId())
                .name(experiment.getName())
                .description(experiment.getDescription())
                .build();
    }
   
    private List<PlateMap> getPlateMapSummaryFromCabinet(String activityName) {
        RestTemplate restTemplate = new RestTemplate();
        String cabinetplateInfoURI = cabinetURI + "plate-map-summary/"+activityName;
        ResponseEntity<List<PlateMap>> plateMapResponse =restTemplate.exchange(cabinetplateInfoURI,
                            HttpMethod.GET, null, new ParameterizedTypeReference<List<PlateMap>>() {
                    });
   		List<PlateMap> plateMapList = plateMapResponse.getBody();
   		return plateMapList;
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
    
    private ActivityVersion buildActivityVersion(PlateMap plateMap) {
        return ActivityVersion.builder()
               .id(plateMap.getId())
               .status(plateMap.getStatus())
               .timestamp(plateMap.getLastModified())
               .build();
    }
     
}
