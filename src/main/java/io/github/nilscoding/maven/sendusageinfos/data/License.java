package io.github.nilscoding.maven.sendusageinfos.data;

import java.io.Serializable;

/**
 * License information.
 * @author NilsCoding
 */
public class License implements Serializable {
    private static final long serialVersionUID = -4703130343551325886L;

    /**
     * Name.
     */
    protected String name;
    /**
     * URL.
     */
    protected String url;

    /**
     * Creates a new instance.
     */
    public License() {
    }

    /**
     * Creates an instance from Maven license data.
     * @param mavenLicense Maven license data
     * @return instance or null on error
     */
    public static License fromMavenLicense(org.apache.maven.model.License mavenLicense) {
        if (mavenLicense == null) {
            return null;
        }
        License license = new License();
        license.setName(mavenLicense.getName());
        license.setUrl(mavenLicense.getUrl());
        return license;
    }

    /**
     * Returns the name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the URL.
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL.
     * @param url URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
