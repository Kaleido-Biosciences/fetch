package com.kaleido.fetch.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySummary {

    private Long id;

    @ApiModelProperty(value = "The friendly name of the activity.")
    private String name;

    @ApiModelProperty(value = "Description of the activity.")
    private String description;

    @ApiModelProperty(value = "Date time of when the activity was last modified.")
    private ZonedDateTime lastModified;

    @ApiModelProperty(value = "List of IDs for versions along with timestamps of when they were completed.")
    private List<ActivityVersion> versions;
    
    @ApiModelProperty(value = "The number of plates that are in the version.")
    private int numPlates;
    
}
