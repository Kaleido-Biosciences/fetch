package com.kaleidobio.jarvis.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
public class Component {
    private Long id;
    private String name;
    private String altName;
    private String classification;
    private String classificationSymbol;
    private Map<String, ?> toolTip;
    private List<String> allowedUnits;
}
