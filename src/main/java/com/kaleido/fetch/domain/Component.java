package com.kaleido.fetch.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component {

    private Long id;

    @ApiModelProperty(value = "Text to display as the name of the component")
    private String name;

    @ApiModelProperty(value = "Text to be shown after the Display name to give context")
    private String altName;

    @ApiModelProperty(value = "The classification of the component, for example: Compound or Community or Strain")
    private String classification;

    @ApiModelProperty(value = "Characters that will be shown next to the name in an icon. Components with the same classification symbol can be considered the same type of component")
    private String classificationSymbol;

    @ApiModelProperty(value = "Ordered List of fields to display in a tool tip on hover of the component")
    private Map<String, Optional<?>> toolTip;

    @ApiModelProperty(value = "List of units the component can be measured in")
    private List<String> allowedUnits;

}
