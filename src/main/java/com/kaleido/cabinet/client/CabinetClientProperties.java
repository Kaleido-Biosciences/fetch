package com.kaleido.cabinet.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Holds properties from externalized configuration such as environment variables or {@code application.properties} files.
 * Any variable beginning with {@code cabinet.client} or for environment variables {@code CABINET_CLIENT} and matching
 * a field of this class will over-ride the default value. For example, {@code cabinet.client.password=secret} will set
 * the value of the password used by the client.
 * Default values are set to interact with the {@code dev} profile of Cabinet and are suitable for development and testing
 * purposes.
 */
@ConfigurationProperties("cabinet.client")
@EnableRetry
public class CabinetClientProperties {

    private String username = "admin";
    private String password = "admin";
    // This link can be changed to localhost:8080/api/ if fetch is not deployed as a docker
    //private String base = "http://host.docker.internal:8080/api/";
    private String base = "http://localhost:8080/api/";

    private String searchPathComponent = "_search";

    private String cabinetPlatemapEndpoint = "plate-maps";

    private long retryInterval = 5000L;
    private double retryMultiplier = 2.0D;
    private long maxRetryInterval = 15000L;
    private int maxRequestAttempts = 3;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getSearchPathComponent() {
        return searchPathComponent;
    }

    public void setSearchPathComponent(String searchPathComponent) {
        this.searchPathComponent = searchPathComponent;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }

    public long getMaxRetryInterval() {
        return maxRetryInterval;
    }

    public void setMaxRetryInterval(long maxRetryInterval) {
        this.maxRetryInterval = maxRetryInterval;
    }

    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }

    public void setMaxRequestAttempts(int maxRequestAttempts) {
        this.maxRequestAttempts = maxRequestAttempts;
    }

    public String getCabinetPlatemapEndpoint() {
        return cabinetPlatemapEndpoint;
    }

    public void setAssayReadoutDTOEndpoint(String cabinetPlatemapEndpoint) {
        this.cabinetPlatemapEndpoint = cabinetPlatemapEndpoint;
    }
}
