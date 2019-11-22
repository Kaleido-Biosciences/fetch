package com.kaleido.jarvis.resource;

import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
@Slf4j
public class ActivityResource {

    private KaptureClient<Experiment> experimentKaptureClient;

    public ActivityResource(KaptureClient<Experiment> experimentKaptureClient) {
        this.experimentKaptureClient = experimentKaptureClient;
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<List<Experiment>> searchComponents(@PathVariable String searchTerm) {
        log.debug("call to /search/{}", searchTerm);

        final String originalSearchTerm = searchTerm;

        //add asterisks to the searchTerm if they are not already there
        if(! searchTerm.startsWith("*")){
            searchTerm = "*"+searchTerm;
        }
        if(! searchTerm.endsWith("*")){
            searchTerm = searchTerm+"*";
        }

        return null;
    }
}
