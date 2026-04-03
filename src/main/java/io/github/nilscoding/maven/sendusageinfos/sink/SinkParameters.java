package io.github.nilscoding.maven.sendusageinfos.sink;

/**
 * Sink parameters.
 *
 * @author NilsCoding
 */
public class SinkParameters {
    /**
     * URL.
     */
    private String url;
    /**
     * HTTP method.
     */
    private String httpMethod;
    /**
     * Auth header.
     */
    private String authHeader;
    /**
     * Pretty printing flag.
     */
    private boolean prettyPrinting;
    /**
     * Custom parameter.
     */
    private String customParameter;

    /**
     * Creates a new instance.
     */
    public SinkParameters() {
    }

    /**
     * Returns the URL.
     *
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL.
     *
     * @param url URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the HTTP method.
     *
     * @return HTTP method
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the HTTP method.
     *
     * @param httpMethod HTTP method to set
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Returns the auth header.
     *
     * @return auth header
     */
    public String getAuthHeader() {
        return authHeader;
    }

    /**
     * Sets the auth header.
     *
     * @param authHeader auth header to set
     */
    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    /**
     * Returns the pretty printing flag.
     *
     * @return pretty printing flag
     */
    public boolean isPrettyPrinting() {
        return prettyPrinting;
    }

    /**
     * Sets the pretty printing flag.
     *
     * @param prettyPrinting pretty printing flag to set
     */
    public void setPrettyPrinting(boolean prettyPrinting) {
        this.prettyPrinting = prettyPrinting;
    }

    /**
     * Returns the custom parameter.
     *
     * @return custom parameter
     */
    public String getCustomParameter() {
        return customParameter;
    }

    /**
     * Sets the custom parameter.
     *
     * @param customParameter custom parameter to set
     */
    public void setCustomParameter(String customParameter) {
        this.customParameter = customParameter;
    }
}
