package com.kaleido.jarvis.resource;

import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.*;
import com.kaleido.jarvis.domain.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/jarvis")
@Slf4j
public class JarvisResource {

    private KaptureClient<Media> mediaKaptureClient;
    private KaptureClient<Batch> batchKaptureClient;
    private KaptureClient<Community> communityKaptureClient;
    private KaptureClient<Supplement> supplementKaptureClient;

    private static final String microLiter = "\u00B5L";
    private static final String microGram = "\u00B5g";

    public JarvisResource(KaptureClient<Media> mediaKaptureClient, KaptureClient<Batch> batchKaptureClient, KaptureClient<Community> communityKaptureClient, KaptureClient<Supplement> supplementKaptureClient) {
        this.mediaKaptureClient = mediaKaptureClient;
        this.batchKaptureClient = batchKaptureClient;
        this.communityKaptureClient = communityKaptureClient;
        this.supplementKaptureClient = supplementKaptureClient;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("Alive");
    }

    @GetMapping("/components/search/{searchTerm}")
    public ResponseEntity<List<Component>> searchComponents(@PathVariable String searchTerm){
        log.debug("call to /components/search/{}", searchTerm);
        //add asterisks to the searchTerm if they are not already there
        if(! searchTerm.startsWith("*")){
            searchTerm = "*"+searchTerm;
        }
        if(! searchTerm.endsWith("*")){
            searchTerm = searchTerm+"*";
        }


        //search a bunch of endpoints in parallel
        final String finalSearchTerm = searchTerm;
        log.debug("expanded search term to {}", searchTerm);
        var mediaComponents = CompletableFuture.supplyAsync(() -> searchMedia(finalSearchTerm));
        var batchComponents = CompletableFuture.supplyAsync(() -> searchBatch(finalSearchTerm));
        var communityComponents = CompletableFuture.supplyAsync(() -> searchCommunity(finalSearchTerm));
        var supplementComponents = CompletableFuture.supplyAsync(() -> searchSupplement(finalSearchTerm));

        var results = Stream.of(mediaComponents, batchComponents, communityComponents, supplementComponents)
                            //wait for all the futures to complete
                            .map(CompletableFuture::join)
                            //make a combined stream of components
                            .flatMap(Collection::stream)
                            //sort by name and then by classification
                            .sorted(Comparator.comparing(Component::getName)
                                    .thenComparing(Component::getClassification))
                            .collect(Collectors.toList());

        //return the results
        log.debug("Combined results: {}", results);
        return ResponseEntity.ok(results);
    }

    private List<Component> searchBatch(String searchTerm) {
        final var batchResponse = batchKaptureClient.search(searchTerm);

        if(batchResponse.getStatusCode().is2xxSuccessful() && batchResponse.getBody() != null){
            //map the responses to a Component
            return batchResponse.getBody().stream()
                    .map(batch -> Component.builder()
                            .classification("Compound")
                            .classificationSymbol("Cpd")
                            .id(batch.getId())
                            .name(batch.getName())
                            .altName(Optional.ofNullable(
                                    batch.getAliases()
                                    .stream()
                                    .map(BatchAlias::getAlias)
                                    .collect(Collectors.joining(","))).orElse(""))
                            .toolTip(Map.of(
                                    "Notebook", Optional.ofNullable(batch.getNotebook()),
                                    "Glycan Composition", Optional.ofNullable(batch.getGlycanComposition()),
                                    "Mw", Optional.ofNullable(batch.getMw()),
                                    "AveDP", Optional.ofNullable(batch.getAveDP()),
                                    "Concepts", Optional.ofNullable(
                                            batch.getConcepts().stream()
                                                    .map(ChemicalConcept::getConceptId)
                                                    .collect(Collectors.joining(",")))
                                    )
                            )
                            .allowedUnits(List.of(microLiter, "mL", "%"))
                            .build())
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }

    private List<Component> searchCommunity(String searchTerm) {
        final var communityResponse = communityKaptureClient.search(searchTerm);

        if(communityResponse.getStatusCode().is2xxSuccessful() && communityResponse.getBody() != null){
            //map the responses to a Component
            return communityResponse.getBody().stream()
                    .map(community -> Component.builder()
                            .classification("Community")
                            .classificationSymbol("C")
                            .name(community.getName())
                            .altName(community.getAlias())
                            .toolTip(Map.of(
                                    "BSI Name", Optional.ofNullable(community.getBsiName()),
                                    "Description", Optional.ofNullable(community.getDescription())
                            ))
                            .allowedUnits(List.of(microLiter, "mL", "%"))
                            .build()
                    )
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }

    private List<Component> searchSupplement(String searchTerm) {
        final var supplementResponse = supplementKaptureClient.search(searchTerm);

        if(supplementResponse.getStatusCode().is2xxSuccessful() && supplementResponse.getBody() != null){
            //map the responses to a Component
            return supplementResponse.getBody().stream()
                    .map(supplement -> Component.builder()
                            .id(supplement.getId())
                            .classification("Supplement")
                            .classificationSymbol("S")
                            .name(supplement.getName())
                            .altName("")
                            .allowedUnits(List.of(microGram,"mg","g"))
                            .toolTip(Map.of(
                                    "Supplement class", Optional.ofNullable(supplement.getClassification()),
                                    "Description", Optional.ofNullable(supplement.getDescription())
                            ))
                            .build()
                    )
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }

    private List<Component> searchMedia(String searchTerm) {
        final var mediaResponse = mediaKaptureClient.search(searchTerm);
        if(mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null){
            //map the responses to a Component
            return mediaResponse.getBody().stream()
                    .map(media -> Component.builder()
                            .id(media.getId())
                            .classification("Media")
                            .classificationSymbol("M")
                            .name(media.getName())
                            .altName("")
                            .allowedUnits(List.of(microLiter, "mL", "%"))
                            .toolTip(Map.of(
                                    "Base Media", Optional.ofNullable(media.getBaseMedia() != null ? media.getBaseMedia().getName() : null),
                                    "Description", Optional.ofNullable(media.getDescription()),
                                    "pH", Optional.ofNullable(media.getPh())
                            ))
                            .build()
                    )
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }
}
