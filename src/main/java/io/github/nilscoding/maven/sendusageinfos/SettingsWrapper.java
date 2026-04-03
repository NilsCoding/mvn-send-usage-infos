package io.github.nilscoding.maven.sendusageinfos;

import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import java.util.List;
import java.util.Map;

/**
 * Settings wrapper to allow some read access to Maven settings.
 *
 * @author NilsCoding
 */
public final class SettingsWrapper {
    /**
     * Maven settings.
     */
    private final Settings mavenSettings;

    /**
     * Creates a new wrapping instance.
     *
     * @param mavenSettings maven settings
     */
    public SettingsWrapper(Settings mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    /**
     * Returns the servers.
     *
     * @return servers
     */
    public List<Server> getServer() {
        if (this.mavenSettings == null) {
            return null;
        }
        return this.mavenSettings.getServers();
    }

    /**
     * Returns the active profiles names.
     *
     * @return active profiles names
     */
    public List<String> getActiveProfiles() {
        if (this.mavenSettings == null) {
            return null;
        }
        return this.mavenSettings.getActiveProfiles();
    }

    /**
     * Returns all profiles.
     *
     * @return all profiles
     */
    public List<Profile> getProfiles() {
        if (this.mavenSettings == null) {
            return null;
        }
        return this.mavenSettings.getProfiles();
    }

    /**
     * Returns a map with all profiles.
     *
     * @return map with all profiles
     */
    public Map<String, Profile> getProfilesAsMap() {
        if (this.mavenSettings == null) {
            return null;
        }
        return this.mavenSettings.getProfilesAsMap();
    }

}
