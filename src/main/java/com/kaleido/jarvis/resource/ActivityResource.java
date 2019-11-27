package com.kaleido.jarvis.resource;

import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/activities")
@Slf4j
public class ActivityResource {

    private KaptureClient<Experiment> experimentKaptureClient;

    public ActivityResource(KaptureClient<Experiment> experimentKaptureClient) {
        this.experimentKaptureClient = experimentKaptureClient;
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<List<Experiment>> searchExperiments(@PathVariable String searchTerm) {
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
        List<Experiment> sortedExactMatches = matches.get(true).stream().collect(Collectors.toList());

        log.debug("Sorted exact matches: {}", sortedExactMatches);


        //combine the exact and non-exact matches
        List<Experiment> results = Stream.of(sortedExactMatches, sortedNonExactMatches)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //return the results
        log.debug("Combined results: {}", results);
        return ResponseEntity.ok(results);
    }


    private List<Experiment> searchExperiment(String searchTerm) {
        final var mediaResponse = experimentKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");
        if (mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null) {
            //map the responses to a Component
            return mediaResponse.getBody().stream()
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }
}
