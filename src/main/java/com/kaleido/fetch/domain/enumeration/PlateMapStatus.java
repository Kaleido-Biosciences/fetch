package com.kaleido.fetch.domain.enumeration;


public enum PlateMapStatus {
    DRAFT("Draft"), COMPLETED("Completed");

    private final String plateMapStatusDescription;

    private PlateMapStatus(String value) {
        plateMapStatusDescription = value;
    }

    public String getPlateMapStatusDescription() {
        return plateMapStatusDescription;
    }
}
