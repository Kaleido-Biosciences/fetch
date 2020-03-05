package com.kaleido.fetch.service;

import com.kaleido.fetch.domain.Component;
import com.kaleido.kaptureclient.client.KaptureClient;
import com.kaleido.kaptureclient.domain.Batch;
import com.kaleido.kaptureclient.domain.BatchAlias;
import com.kaleido.kaptureclient.domain.ChemicalConcept;
import com.kaleido.kaptureclient.domain.Community;
import com.kaleido.kaptureclient.domain.Experiment;
import com.kaleido.kaptureclient.domain.Media;
import com.kaleido.kaptureclient.domain.Platemap;
import com.kaleido.kaptureclient.domain.Supplement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class FetchService {

    private static final int URI_BATCH_SIZE = 50;
    private static final String MICRO_LITER = "\u00B5L";
    private static final String MICRO_GRAM = "\u00B5g";
    private KaptureClient < Media > mediaKaptureClient;
    private KaptureClient < Batch > batchKaptureClient;
    private KaptureClient < Community > communityKaptureClient;
    private KaptureClient < Supplement > supplementKaptureClient;
    private KaptureClient < Experiment > experimentKaptureClient;
    private KaptureClient < Platemap > plateMapKaptureClient;

    public FetchService(KaptureClient < Media > mediaKaptureClient, KaptureClient < Batch > batchKaptureClient, KaptureClient < Community > communityKaptureClient, KaptureClient < Supplement > supplementKaptureClient, KaptureClient < Experiment > experimentKaptureClient, KaptureClient < Platemap > plateMapKaptureClient) {
        this.mediaKaptureClient = mediaKaptureClient;
        this.batchKaptureClient = batchKaptureClient;
        this.communityKaptureClient = communityKaptureClient;
        this.supplementKaptureClient = supplementKaptureClient;
        this.experimentKaptureClient = experimentKaptureClient;
        this.plateMapKaptureClient = plateMapKaptureClient;
    }

    public List < Component > findComponents(String searchTerm) {
        log.debug("call to /components/search/{}", searchTerm);

        final String originalSearchTerm = searchTerm;

        //add asterisks to the searchTerm if they are not already there
        if (!searchTerm.startsWith("*")) {
            searchTerm = "*" + searchTerm;
        }

        if (!searchTerm.endsWith("*")) {
            searchTerm = searchTerm + "*";
        }

        //search a bunch of endpoints in parallel
        final String elasticSearchTerm = searchTerm;
        log.debug("Expanded search term to elastic search term {}", searchTerm);
        var mediaComponents = CompletableFuture.supplyAsync(() - > searchMedia(originalSearchTerm));
        var batchComponents = CompletableFuture.supplyAsync(() - > searchBatch(elasticSearchTerm));
        var communityComponents = CompletableFuture.supplyAsync(() - > searchCommunity(originalSearchTerm));
        var supplementComponents = CompletableFuture.supplyAsync(() - > searchSupplement(originalSearchTerm));

        var matches = Stream.of(mediaComponents, batchComponents, communityComponents, supplementComponents)
            //wait for all the futures to complete
            .map(CompletableFuture::join)
            //make a combined stream of components
            .flatMap(Collection::stream)
            //make a boolean make with two lists, those that have an exact match and those that don't
            .collect(Collectors.partitioningBy(component - > component.getName().equals(originalSearchTerm)));

        //sort the matches that are not exact
        List < Component > sortedNonExactMatches = matches.get(false).stream()
            .sorted(Comparator.comparing(Component::getName)
                .thenComparing(Component::getClassification))
            .collect(Collectors.toList());

        log.debug("Sorted non-exact matches {}", sortedNonExactMatches);

        //sort any exact matches by classification only
        List < Component > sortedExactMatches = matches.get(true).stream()
            .sorted(Comparator.comparing(Component::getClassification))
            .collect(Collectors.toList());

        log.debug("Sorted exact matches: {}", sortedExactMatches);

        //combine the exact and non-exact matches
        List < Component > results = Stream.of(sortedExactMatches, sortedNonExactMatches)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        //return the results
        log.debug("Combined results: {}", results);

        return results;
    }

    public List < Component > getComponentsByClassificationAndId(List < Component > searchComponents) {
        log.debug("call to /components/find : {}", searchComponents);

        var mediaComponents = CompletableFuture.supplyAsync(() - > searchMediaByIds(
            searchComponents.stream()
            .filter(searchComponent - > searchComponent.getClassification().equalsIgnoreCase("media"))
            .map(Component::getId)
            .collect(Collectors.toList())
        ));

        var batchComponents = CompletableFuture.supplyAsync(() - > searchBatchByIds(
            searchComponents.stream()
            .filter(searchComponent - > searchComponent.getClassification().equalsIgnoreCase("batch"))
            .map(Component::getId)
            .collect(Collectors.toList())
        ));

        var communityComponents = CompletableFuture.supplyAsync(() - > searchCommunityByIds(
            searchComponents.stream()
            .filter(searchComponent - > searchComponent.getClassification().equalsIgnoreCase("community"))
            .map(Component::getId)
            .collect(Collectors.toList())
        ));

        var supplementComponents = CompletableFuture.supplyAsync(() - > searchSupplementByIds(
            searchComponents.stream()
            .filter(searchComponent - > searchComponent.getClassification().equalsIgnoreCase("supplement"))
            .map(Component::getId)
            .collect(Collectors.toList())
        ));

        List < Component > results = Stream.of(mediaComponents, batchComponents, communityComponents, supplementComponents)
            //wait for all the futures to complete
            .map(CompletableFuture::join)
            //make a combined stream of components
            .flatMap(Collection::stream)
            //sort the matches
            .sorted(Comparator.comparing(Component::getName)
                .thenComparing(Component::getClassification))
            .collect(Collectors.toList());

        log.debug("Sorted matches {}", results);

        return results;
    }

    public List < String > getAllBarcodes(String activityName) {
        List < String > barcodeArray = new ArrayList < String > ();

        ResponseEntity < List < Experiment >> experimentList = experimentKaptureClient.findByFieldEquals("name", activityName);
        var expertimentId = experimentList.getBody().get(0).getId().toString();
        if (experimentList.getStatusCode().is2xxSuccessful() && experimentList.getBody() != null && experimentList.getBody().size() > 0) {
            var platmapsfromrepository = plateMapKaptureClient.findByFieldEquals("experimentId", expertimentId);
            if (platmapsfromrepository.getStatusCode().is2xxSuccessful() && platmapsfromrepository.getBody() != null) {
                platmapsfromrepository.getBody().stream()
                    .forEach(plate - > {
                        barcodeArray.add(plate.getBarcode());
                    });
            }
        }

        return barcodeArray;
    }

    private List < Component > searchMediaByIds(List < Long > mediaIds) {
        List < Component > mediaList = new ArrayList < > ();

        IntStream.range(0, (mediaIds.size() + URI_BATCH_SIZE - 1) / URI_BATCH_SIZE)
            .mapToObj(i - > mediaIds.subList(i * URI_BATCH_SIZE, Math.min(mediaIds.size(), (i + 1) * URI_BATCH_SIZE)))
            .map(dataBatch - > dataBatch.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","))
            ).forEach(queryString - > {
                final
                var mediaResponse = mediaKaptureClient.findByFieldWithOperator("id", queryString, "in");
                if (mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null) {
                    //map the responses to a Component
                    mediaList.addAll(mediaResponse.getBody().stream()
                        .map(this::buildMediaComponent)
                        .collect(Collectors.toList()));
                }
            });

        return mediaList;
    }

    private List < Component > searchBatchByIds(List < Long > batchIds) {
        List < Component > batchList = new ArrayList < > ();

        IntStream.range(0, (batchIds.size() + URI_BATCH_SIZE - 1) / URI_BATCH_SIZE)
            .mapToObj(i - > batchIds.subList(i * URI_BATCH_SIZE, Math.min(batchIds.size(), (i + 1) * URI_BATCH_SIZE)))
            .map(dataBatch - > dataBatch.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","))
            ).forEach(queryString - > {
                final
                var batchResponse = batchKaptureClient.findByFieldWithOperator("id", queryString, "in");
                if (batchResponse.getStatusCode().is2xxSuccessful() && batchResponse.getBody() != null) {
                    //map the responses to a Component
                    batchList.addAll(batchResponse.getBody().stream()
                        .map(this::buildBatchComponent)
                        .collect(Collectors.toList()));
                }
            });

        return batchList;
    }

    private List < Component > searchCommunityByIds(List < Long > communityIds) {
        List < Component > communityList = new ArrayList < > ();

        IntStream.range(0, (communityIds.size() + URI_BATCH_SIZE - 1) / URI_BATCH_SIZE)
            .mapToObj(i - > communityIds.subList(i * URI_BATCH_SIZE, Math.min(communityIds.size(), (i + 1) * URI_BATCH_SIZE)))
            .map(dataBatch - > dataBatch.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","))
            ).forEach(queryString - > {
                final
                var communityResponse = communityKaptureClient.findByFieldWithOperator("id", queryString, "in");
                if (communityResponse.getStatusCode().is2xxSuccessful() && communityResponse.getBody() != null) {
                    //map the responses to a Component
                    communityList.addAll(communityResponse.getBody().stream()
                        .map(this::buildCommunityComponent)
                        .collect(Collectors.toList()));
                }
            });

        return communityList;
    }

    private List < Component > searchSupplementByIds(List < Long > supplementIds) {
        List < Component > supplementList = new ArrayList < > ();

        IntStream.range(0, (supplementIds.size() + URI_BATCH_SIZE - 1) / URI_BATCH_SIZE)
            .mapToObj(i - > supplementIds.subList(i * URI_BATCH_SIZE, Math.min(supplementIds.size(), (i + 1) * URI_BATCH_SIZE)))
            .map(dataBatch - > dataBatch.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","))
            ).forEach(queryString - > {
                final
                var supplementResponse = supplementKaptureClient.findByFieldWithOperator("id", queryString, "in");
                if (supplementResponse.getStatusCode().is2xxSuccessful() && supplementResponse.getBody() != null) {
                    //map the responses to a Component
                    supplementList.addAll(supplementResponse.getBody().stream()
                        .map(this::buildSupplementComponent)
                        .collect(Collectors.toList()));
                }
            });

        return supplementList;
    }

    private List < Component > searchBatch(String searchTerm) {
        final
        var batchResponse = batchKaptureClient.search(searchTerm);

        return batchResponse.getStatusCode().is2xxSuccessful() && batchResponse.getBody() != null ?
            //map the responses to a Component
            batchResponse.getBody().stream()
            .map(this::buildBatchComponent)
            .collect(Collectors.toList()) :
            Collections.emptyList();
    }

    private List < Component > searchCommunity(String searchTerm) {
        final
        var communityResponse = communityKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");

        return communityResponse.getStatusCode().is2xxSuccessful() && communityResponse.getBody() != null ?
            //map the responses to a Component
            communityResponse.getBody().stream()
            .map(this::buildCommunityComponent)
            .collect(Collectors.toList()) :
            Collections.emptyList();
    }

    private List < Component > searchSupplement(String searchTerm) {
        final
        var supplementResponse = supplementKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");

        return supplementResponse.getStatusCode().is2xxSuccessful() && supplementResponse.getBody() != null ?
            //map the responses to a Component
            supplementResponse.getBody().stream()
            .map(this::buildSupplementComponent)
            .collect(Collectors.toList()) :
            Collections.emptyList();
    }

    private List < Component > searchMedia(String searchTerm) {
        final
        var mediaResponse = mediaKaptureClient.findByFieldWithOperator("name", searchTerm, "contains");

        return mediaResponse.getStatusCode().is2xxSuccessful() && mediaResponse.getBody() != null ?
            //map the responses to a Component
            mediaResponse.getBody().stream()
            .map(this::buildMediaComponent)
            .collect(Collectors.toList()) :
            Collections.emptyList();
    }

    private Component buildMediaComponent(Media media) {
        return Component.builder()
            .id(media.getId())
            .classification("Media")
            .classificationSymbol("M")
            .name(media.getName())
            .altName("")
            .allowedUnits(List.of(MICRO_LITER, "mL", "%"))
            .toolTip(Map.of(
                "Base Media", Optional.ofNullable(media.getBaseMedia() != null ? media.getBaseMedia().getName() : null),
                "Description", Optional.ofNullable(media.getDescription()),
                "pH", Optional.ofNullable(media.getPh())
            ))
            .build();
    }

    private Component buildBatchComponent(Batch batch) {
        return Component.builder()
            .classification("Compound")
            .classificationSymbol("Cpd")
            .id(batch.getId())
            .name(batch.getName())
            .altName(Optional.ofNullable(
                batch.getAliases() == null ? null :
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
                    batch.getConcepts() == null ? null :
                    batch.getConcepts().stream()
                    .map(ChemicalConcept::getConceptId)
                    .collect(Collectors.joining(",")))
            ))
            .allowedUnits(List.of(MICRO_LITER, "mL", "%"))
            .build();
    }

    private Component buildCommunityComponent(Community community) {
        return Component.builder()
            .classification("Community")
            .classificationSymbol("C")
            .name(community.getName())
            .altName(community.getAlias())
            .toolTip(Map.of(
                "BSI Name", Optional.ofNullable(community.getBsiName()),
                "Description", Optional.ofNullable(community.getDescription())
            ))
            .allowedUnits(List.of(MICRO_LITER, "mL", "%"))
            .build();
    }

    private Component buildSupplementComponent(Supplement supplement) {
        return Component.builder()
            .id(supplement.getId())
            .classification("Supplement")
            .classificationSymbol("S")
            .name(supplement.getName())
            .altName("")
            .allowedUnits(List.of(MICRO_GRAM, "mg", "g"))
            .toolTip(Map.of(
                "Supplement class", Optional.ofNullable(supplement.getClassification()),
                "Description", Optional.ofNullable(supplement.getDescription())
            ))
            .build();
    }

}