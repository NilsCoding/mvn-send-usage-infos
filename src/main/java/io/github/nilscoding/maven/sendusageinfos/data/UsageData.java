package io.github.nilscoding.maven.sendusageinfos.data;

import java.io.Serializable;
import java.util.List;

/**
 * Usage data.
 * @author NilsCoding
 */
public class UsageData implements Serializable {
    private static final long serialVersionUID = -5548464498473724433L;

    /**
     * Project artifact.
     */
    protected Artifact projectArtifact;
    /**
     * Referenced artifacts.
     */
    protected List<Artifact> referencedArtifacts;

    /**
     * Creates a new instance.
     */
    public UsageData() {
    }

    /**
     * Returns the project artifact data.
     * @return project artifact data
     */
    public Artifact getProjectArtifact() {
        return projectArtifact;
    }

    /**
     * Sets the project artifact data.
     * @param projectArtifact project artifact data to set
     */
    public void setProjectArtifact(Artifact projectArtifact) {
        this.projectArtifact = projectArtifact;
    }

    /**
     * Returns the referenced project's artifacts data.
     * @return referenced project's artifacts data
     */
    public List<Artifact> getReferencedArtifacts() {
        return referencedArtifacts;
    }

    /**
     * Sets the referenced project's artifacts data.
     * @param referencedArtifacts referenced project's artifacts data to set
     */
    public void setReferencedArtifacts(List<Artifact> referencedArtifacts) {
        this.referencedArtifacts = referencedArtifacts;
    }
}
