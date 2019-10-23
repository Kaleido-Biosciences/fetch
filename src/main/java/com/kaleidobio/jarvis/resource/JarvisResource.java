package com.kaleidobio.jarvis.resource;

import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.*;
import com.kaleidobio.jarvis.domain.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/jarvis")
public class JarvisResource {

    private KaptureClient<Media> mediaKaptureClient;
    private KaptureClient<Batch> batchKaptureClient;
    private KaptureClient<Community> communityKaptureClient;
    private KaptureClient<Supplement> supplementKaptureClient;

    public static final String microLiter = "\u00B5L";
    public static final String microGram = "\u00B5g";

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
        //add asterisks to the searchTerm if they are not already there
        if(! searchTerm.startsWith("*")){
            searchTerm = "*"+searchTerm;
        }
        if(! searchTerm.endsWith("*")){
            searchTerm = searchTerm+"*";
        }



        //search a bunch of endpoints
        //todo these can all be called asynchronously and the results collected together
        List<Component> mediaComponents = searchMedia(searchTerm);
        List<Component> batchComponents = searchBatch(searchTerm);
        List<Component> communityComponents = searchCommunity(searchTerm);
        List<Component> supplementComponents = searchSupplement(searchTerm);

        //combine the results
        List<Component> results = Stream.of(mediaComponents, batchComponents, communityComponents, supplementComponents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //return the results
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
                            .altName(batch.getAliases()
                                    .stream()
                                    .map(BatchAlias::getAlias)
                                    .collect(Collectors.joining(",")))
                            //todo determine what fields should be in the tool tip
                            .toolTip(Map.of(
                                    "Notebook", batch.getNotebook(),
                                    "Glycan Composition", batch.getGlycanComposition(),
                                    "Mw", batch.getMw(),
                                    "AveDP", batch.getAveDP(),
                                    "Concepts", batch.getConcepts().stream().map(ChemicalConcept::getConceptId)
                                    )
                            )
                            .allowedUnits(List.of("%", "mL", microLiter)) //todo confirm these units
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
                                    "BSI Name", community.getBsiName(),
                                    "Description", community.getDescription()
                                    //todo more mapping??
                            ))
                            .allowedUnits(List.of("%", "mL", microLiter)) //todo confirm these units
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
                    .map(supplement -> {
                        return Component.builder()
                            .id(supplement.getId())
                            .classification("Supplement")
                            .classificationSymbol("S")
                            .name(supplement.getName())
                            .altName("")
                            .allowedUnits(List.of(microGram,"mg","g")) //todo check these
                            .toolTip(Map.of("Supplement class", supplement.getClassification(), "", supplement.getDescription())) //todo confirm this
                            .build();

                    })
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
                    .map(media -> {
                        Component component = new Component();
                        //todo more mapping required maybe in a mapper class
                        return component;
                    })
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }
}
