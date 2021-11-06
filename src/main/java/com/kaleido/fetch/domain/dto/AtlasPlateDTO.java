package com.kaleido.fetch.domain.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AtlasPlateDTO {
    //private Long id;

    @ApiModelProperty(value = "The name of activity, use for grouping on")
    private String activityName;

    @ApiModelProperty(value = "The last modified datetime of the plate maps")
    private ZonedDateTime lastModified;

    @ApiModelProperty(value = "The components in the activity")
    private JsonNode components;

    @ApiModelProperty(value = "Plate map data associated with the activity. Note: This string is currently LZW encoded as the plain text is quite large")
    private JsonNode platemaps;
//
//    @JsonSetter("platemaps")
//    public void platemapsFromJsonNode(JsonNode jsonNode) {
//        platemaps = jsonNode.toPrettyString();
//    }
//
//    @JsonSetter("components")
//    public void componentsFromJsonNode(JsonNode jsonNode) {
//        components = jsonNode.toPrettyString();
//    }


}
