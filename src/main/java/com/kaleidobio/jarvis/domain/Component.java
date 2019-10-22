package com.kaleidobio.jarvis.domain;

import lombok.Data;

import java.util.List;

@Data
public class Component {
    private Long id;
    private String name;
    private String altName;
    private String classification;
    private String classificationSymbol;
    private List<String> toolTip;
    private List<String> allowedUnits;
}
