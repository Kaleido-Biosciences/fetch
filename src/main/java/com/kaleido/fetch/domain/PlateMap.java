package com.kaleido.fetch.domain;

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
public class PlateMap {
    private Long id;

    @ApiModelProperty(value = "Date time of when the activity was last modified.")
    private ZonedDateTime lastModified;

    @ApiModelProperty(value = "Plate map data associated with the activity. Note: This string is currently LZW encoded as the plain text is quite large")
    private String data;

    @ApiModelProperty(value = "Status of activity, DRAFT or COMPLETED")
    private String status;

    @ApiModelProperty(value = "The name of activity, use for grouping on")
    private String activityName;

    @ApiModelProperty("The checksum is used when saving a new draft, as the last checksum has to be passed\nand match the most recent timestamp. Otherwise it is considered attempting to save a stale draft")
    private String checksum;

    @ApiModelProperty(value = "The number of plates in the platemap")
    private int numPlates;

}
