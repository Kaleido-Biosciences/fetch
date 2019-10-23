package com.kaleido.jarvis.domain;

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
    private String name;
    private String altName;
    private String classification;
    private String classificationSymbol;
    private Map<String, Optional<?>> toolTip;
    private List<String> allowedUnits;
}
