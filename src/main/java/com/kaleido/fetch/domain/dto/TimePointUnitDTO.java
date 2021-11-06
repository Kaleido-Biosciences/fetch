package com.kaleido.fetch.domain.dto;

import com.kaleido.fetch.domain.ActivityVersion;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;


public class TimePointUnitDTO {
    private Long id;
    private String abbreviation;
    private String name;

    public TimePointUnitDTO() {
    }

    public TimePointUnitDTO(Long id, String abbreviation, String name) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimePointUnitDTO Id(Long id) {
        this.id = id;
        return this;
    }

    public TimePointUnitDTO Abbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        return this;
    }

    public TimePointUnitDTO Name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "TimePointUnitDTO{" +
            "id=" + id +
            ", abbreviation='" + abbreviation + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
