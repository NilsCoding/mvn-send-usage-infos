package io.github.nilscoding.maven.sendusageinfos.data;

import org.apache.maven.project.MavenProject;
import java.io.Serializable;
import java.util.List;

/**
 * Artifact information.
 * @author NilsCoding
 */
public class Artifact implements Serializable {
    private static final long serialVersionUID = 5497108326195918954L;

    /**
     * Group ID.
     */
    protected String groupId;
    /**
     * Artifact ID.
     */
    protected String artifactId;
    /**
     * Version.
     */
    protected String version;
    /**
     * Scope.
     */
    protected String scope;
    /**
     * Packaging.
     */
    protected String packaging;
    /**
     * List with licenses.
     */
    protected List<License> licenses;
    /**
     * Name.
     */
    protected String name;
    /**
     * Description.
     */
    protected String description;
    /**
     * Website URL.
     */
    protected String websiteUrl;
    /**
     * Dependency trail.
     */
    protected List<String> dependencyTrail;

    /**
     * Creates a new instance.
     */
    public Artifact() {
    }

    /**
     * Creates an instance from given Maven project data.
     * @param mavenProject Maven project data
     * @return instance or null on error
     */
    public static Artifact fromMavenProject(MavenProject mavenProject) {
        if (mavenProject == null) {
            return null;
        }
        Artifact artifact = new Artifact();
        artifact.setGroupId(mavenProject.getGroupId());
        artifact.setArtifactId(mavenProject.getArtifactId());
        artifact.setVersion(mavenProject.getVersion());
        artifact.setPackaging(mavenProject.getPackaging());
        artifact.setName(mavenProject.getName());
        artifact.setDescription(mavenProject.getDescription());
        artifact.setWebsiteUrl(mavenProject.getUrl());
        return artifact;
    }

    /**
     * Creates an instance from given Maven artifact data.
     * @param mavenArtifact Maven artifact data
     * @return instance or null on error
     */
    public static Artifact fromMavenArtifact(org.apache.maven.artifact.Artifact mavenArtifact) {
        if (mavenArtifact == null) {
            return null;
        }
        Artifact artifact = new Artifact();
        artifact.setGroupId(mavenArtifact.getGroupId());
        artifact.setArtifactId(mavenArtifact.getArtifactId());
        artifact.setVersion(mavenArtifact.getVersion());
        artifact.setScope(mavenArtifact.getScope());
        artifact.setPackaging(mavenArtifact.getType());
        artifact.setDependencyTrail(mavenArtifact.getDependencyTrail());
        return artifact;
    }

    /**
     * Returns the artifact's coordinates string (in GATV format, with type).
     * @return artifact's coordinates string
     */
    public String getGatv() {
        return this.groupId + ":" + this.artifactId + ":" + this.packaging + ":" + this.version;
    }

    /**
     * Checks if this artifact is a direct dependency of the given other artifact (identified by its gatv).
     * @param otherGatv other gatv to check for
     * @return true if this artifact is a direct dependency of the given other artifact, false in all other cases
     */
    public boolean isDirectDependencyOf(String otherGatv) {
        if ((otherGatv == null) || (otherGatv.isEmpty())) {
            return false;
        }
        if ((this.dependencyTrail == null) || (this.dependencyTrail.size() != 2)) {
            return false;
        }
        return (otherGatv.equals(this.dependencyTrail.get(0)));
    }

    /**
     * Returns the Group ID.
     * @return Group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the Group ID.
     * @param groupId Group ID to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the Artifact ID.
     * @return Artifact ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the Artifact ID.
     * @param artifactId Artifact ID to set
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Returns the version.
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     * @param version version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the list with licenses.
     * @return licenses
     */
    public List<License> getLicenses() {
        return licenses;
    }

    /**
     * Sets the list with licenses.
     * @param licenses list with licenses to set
     */
    public void setLicenses(List<License> licenses) {
        this.licenses = licenses;
    }

    /**
     * Returns the scope.
     * @return scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope.
     * @param scope scope to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Returns the packaging.
     * @return packaging
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * Sets the packaging.
     * @param packaging packaging to set
     */
    public void setPackaging(String packaging) {
        this.packaging = packaging;
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
     * Returns the description.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the website URL.
     * @return website URL
     */
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * Sets the website URL.
     * @param websiteUrl website URL to set
     */
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    /**
     * Returns the dependency trail, if any.
     * @return dependency trail
     */
    public List<String> getDependencyTrail() {
        return dependencyTrail;
    }

    /**
     * Sets the dependency trail.
     * @param dependencyTrail dependency trail to set
     */
    public void setDependencyTrail(List<String> dependencyTrail) {
        this.dependencyTrail = dependencyTrail;
    }
}
