package com.kaleido.fetch.resource;

import com.kaleido.fetch.domain.Component;
import com.kaleido.fetch.service.FetchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("Fetch Operations")
@RestController
@RequestMapping("/")
public class FetchResource {

    private final FetchService fetchService;

    public FetchResource(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @ApiOperation(value = "Return if it is alive")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class)
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Alive");
    }

    @ApiOperation(value = "Finds Components by text string to match on")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Component[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @GetMapping("/components/search/{searchTerm}")
    public ResponseEntity<List<Component>> searchComponents(@PathVariable String searchTerm) {
        return ResponseEntity.ok(fetchService.findComponents(searchTerm));
    }

    @ApiOperation(value = "Finds Components by classification and id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Component[].class),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    @PostMapping("/components/find")
    public ResponseEntity<List<Component>> getComponentsByClassificationAndId(@RequestBody List<Component> searchComponents) {
        return ResponseEntity.ok(fetchService.getComponentsByClassificationAndId(searchComponents));
    }

}
