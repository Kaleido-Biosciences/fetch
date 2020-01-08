package com.kaleido.fetch.service;

import com.kaleido.fetch.domain.Activity;
import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ActivityService {

    private final KaptureClient<Experiment> experimentKaptureClient;

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
}
