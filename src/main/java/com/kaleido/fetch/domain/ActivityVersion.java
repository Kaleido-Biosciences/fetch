package com.kaleido.fetch.domain;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityVersion {
    private Long id;

    @ApiModelProperty(value = "Timestamp the activity was marked as completed/saved.")
    private ZonedDateTime timestamp;

    @ApiModelProperty(value = "The number of plates that are in the version.")
    private ZonedDateTime numPlates;
}
