package io.github.nilscoding.maven.sendusageinfos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mojo to send usage infos (metadata and licenses) from referenced artifacts to a URL endpoint or save them to a file.
 * @author NilsCoding
 */
@Mojo(
        name = "send-usage-infos",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class SendUsageInfoMojo extends AbstractMojo {

    /**
     * URL method (optional).
     */
    @Parameter(property = "urlMethod", defaultValue = "POST")
    private String urlMethod;
    /**
     * URL location.
     */
    @Parameter(property = "urlLocation", required = true)
    private String urlLocation;
    /**
     * URL authentication header value (optional).
     */
    @Parameter(property = "urlAuthHeaderValue")
    private String urlAuthHeader;
    /**
     * Flag for pretty-printing.
     */
    @Parameter(property = "prettyPrint")
    private Boolean prettyPrint;

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Maven project builder.
     */
    @Inject
    private ProjectBuilder mavenProjectBuilder;

    /**
     * Creates a new instance.
     */
    public SendUsageInfoMojo() {
    }

    /**
     * Executes the Maven Mojo.
     * @throws MojoExecutionException Mojo execution exception
     * @throws MojoFailureException   Mojo failure exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("SendUsageInfo Mojo at work...");

        UsageData usageData = new UsageData();

        String tmpLocation = this.urlLocation;
        tmpLocation = (tmpLocation != null) ? tmpLocation.trim() : "";
        if (tmpLocation.isEmpty()) {
            return;
        }
        // split by space / line breaks
        String[] tmpLocations = tmpLocation.split("[\n|\r]");
        Set<String> locations = new LinkedHashSet<>();
        for (String oneTmpLocation : tmpLocations) {
            if (!oneTmpLocation.trim().isEmpty()) {
                oneTmpLocation = oneTmpLocation.trim();
                locations.add(oneTmpLocation);
            }
        }

        if (locations.isEmpty()) {
            log.warn("unsupported location, not sending any data");
            return;
        }

        // assign project artifact's data
        io.github.nilscoding.maven.sendusageinfos.data.Artifact projectArtifact
                = io.github.nilscoding.maven.sendusageinfos.data.Artifact.fromMavenProject(this.project);
        usageData.setProjectArtifact(projectArtifact);

        // assign data of referenced artifacts
        Set<Artifact> mavenRefArtifacts = this.project.getArtifacts();
        if ((mavenRefArtifacts != null) && (mavenRefArtifacts.isEmpty() == false)) {
            List<io.github.nilscoding.maven.sendusageinfos.data.Artifact> refArtifacts = new LinkedList<>();
            for (final Artifact oneRefArtifact : mavenRefArtifacts) {
                io.github.nilscoding.maven.sendusageinfos.data.Artifact refArtifact =
                        io.github.nilscoding.maven.sendusageinfos.data.Artifact.fromMavenArtifact(oneRefArtifact);
                MavenProject refProject = this.resolveProjectViaRepo(oneRefArtifact);
                if (refProject != null) {
                    List<License> mavenRefLicenses = refProject.getLicenses();
                    if ((mavenRefLicenses != null) && (mavenRefLicenses.isEmpty() == false)) {
                        List<io.github.nilscoding.maven.sendusageinfos.data.License> refLicenses = new LinkedList<>();
                        for (final License oneMavenRefLicense : mavenRefLicenses) {
                            io.github.nilscoding.maven.sendusageinfos.data.License oneRefLicense =
                                    io.github.nilscoding.maven.sendusageinfos.data.License.fromMavenLicense(oneMavenRefLicense);
                            if (oneRefLicense != null) {
                                refLicenses.add(oneRefLicense);
                            }
                        }
                        refArtifact.setLicenses(refLicenses);
                    }
                    refArtifact.setName(refProject.getName());
                    refArtifact.setDescription(refProject.getDescription());
                    refArtifact.setWebsiteUrl(refProject.getUrl());
                    refArtifacts.add(refArtifact);
                }
            }
            if (refArtifacts.isEmpty() == false) {
                usageData.setReferencedArtifacts(refArtifacts);
            }
        }

        // send data

        for (String oneLocation : locations) {
            if (oneLocation.startsWith("http://") || oneLocation.startsWith("https://")) {
                this.sendDataViaWeb(usageData, oneLocation, log);
            } else if (oneLocation.startsWith("@logging")) {
                this.sendDataToLog(usageData, oneLocation, log);
            } else {
                this.sendDataToFile(usageData, oneLocation, log);
            }
        }
    }

    /**
     * Resolves a Maven project via Artifact info.
     * @param artifact artifact used for resolving
     * @return Maven project
     */
    protected MavenProject resolveProjectViaRepo(Artifact artifact) {
        if (artifact == null) {
            return null;
        }
        try {
            ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
            projectBuildingRequest.setLocalRepository(this.session.getLocalRepository());
            projectBuildingRequest.setRepositorySession(this.session.getRepositorySession());
            ProjectBuildingResult pbRes = this.mavenProjectBuilder.build(artifact, projectBuildingRequest);
            return pbRes.getProject();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sends the data via HTTP/HTTPS.
     * @param usageData usage data to send
     * @param sendToUrl url to send data to
     * @param log       logging
     */
    protected void sendDataViaWeb(UsageData usageData, String sendToUrl, Log log) {
        if (usageData == null) {
            return;
        }
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            // pretty-printing for web requests is disabled by default
            if ((this.prettyPrint != null) && (this.prettyPrint == true)) {
                gsonBuilder.setPrettyPrinting();
            }
            Gson gson = gsonBuilder.create();
            String bodyContent = gson.toJson(usageData);

            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .hostnameVerifier((hostname, sslSession) -> true)
                    .build();
            Request.Builder reqBuilder = new Request.Builder()
                    .url(sendToUrl);
            if ((this.urlAuthHeader != null) && (this.urlAuthHeader.isEmpty() == false)) {
                reqBuilder.addHeader("Authorization", this.urlAuthHeader);
            }
            RequestBody reqBody = RequestBody.create(bodyContent, MediaType.get("application/json"));
            reqBuilder.method(this.urlMethod, reqBody);
            Request req = reqBuilder.build();
            log.info("sending usage info data to '" + sendToUrl + "' ...");
            Call call = client.newCall(req);
            try (Response resp = call.execute()) {
                int respCode = resp.code();
                log.info("data sent (http " + respCode + ")");
            }
        } catch (Exception ex) {
            log.error("exception sending data via web: " + ex);
        }
    }

    /**
     * Sends the data to local file.
     * @param usageData    usage data to send
     * @param fileLocation file location to write to
     * @param log          logging
     */
    protected void sendDataToFile(UsageData usageData, String fileLocation, Log log) {
        if (usageData == null) {
            return;
        }
        try {
            String location = fileLocation;
            if (location.startsWith("file://")) {
                location = location.substring("file://".length());
            }
            File f = new File(location);
            if (f.isDirectory()) {
                if (!location.endsWith(File.separator)) {
                    location = location + File.separator;
                }
                location = location + "usage-infos.json";
                f = new File(location);
            }
            if (f.isFile()) {
                log.warn("file '" + location + "' exists and will not be overwritten");
            } else {
                GsonBuilder gsonBuilder = new GsonBuilder();
                // pretty-printing for web requests is enabled by default
                if ((this.prettyPrint == null) || (this.prettyPrint == true)) {
                    gsonBuilder.setPrettyPrinting();
                }
                Gson gson = gsonBuilder.create();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8)) {
                    log.info("writing usage info data to '" + location + "' ...");
                    gson.toJson(usageData, writer);
                    writer.flush();
                    log.info("data has been written to file");
                } catch (IOException ioEx) {
                    log.error("exception sending data to file: " + ioEx);
                }
            }
        } catch (Exception ex) {
            log.error("exception sending data to file: " + ex);
        }
    }

    /**
     * Displays the data in build log.
     * @param usageData usage data to display
     * @param logInfo   log info
     * @param log       logging
     */
    protected void sendDataToLog(UsageData usageData, String logInfo, Log log) {
        if (usageData == null) {
            return;
        }
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            // pretty-printing is enabled by default for log output
            boolean usePrettyPrint = (this.prettyPrint == null) || (this.prettyPrint);
            if (logInfo.contains("(single-line)")) {
                usePrettyPrint = false;
            }
            if (usePrettyPrint) {
                gsonBuilder.setPrettyPrinting();
            }
            Consumer<String> logTarget = log::info;
            if (logInfo != null) {
                if (logInfo.startsWith("@logging:debug")) {
                    logTarget = log::debug;
                } else if (logInfo.startsWith("@logging:warn")) {
                    logTarget = log::warn;
                } else if (logInfo.startsWith("@logging:error")) {
                    logTarget = log::error;
                }
            }
            Writer sw = new LineBasedWriter(logTarget);
            Gson gson = gsonBuilder.create();
            gson.toJson(usageData, sw);
            sw.flush();
        } catch (Exception ex) {
            log.error("exception sending data to log: " + ex);
        }
    }

    /**
     * Line-based writer, forwarding each line to the given consumer.
     */
    private static class LineBasedWriter extends Writer {
        /**
         * Temporary line buffer.
         */
        private final StringBuilder buffer = new StringBuilder();
        /**
         * Target consumer to write to.
         */
        private final Consumer<String> consumer;

        /**
         * Creates a new instance with given consumer.
         * @param consumer consumer
         */
        LineBasedWriter(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            for (int i = off; i < off + len; i++) {
                char c = cbuf[i];
                if (c == '\n' || c == '\r') {
                    this.writeBufferToConsumer();
                } else {
                    buffer.append(c);
                }
            }
        }

        @Override
        public void flush() {
            this.writeBufferToConsumer();
        }

        @Override
        public void close() {
            this.writeBufferToConsumer();
        }

        private void writeBufferToConsumer() {
            if (buffer.length() > 0) {
                this.consumer.accept(buffer.toString());
                buffer.setLength(0);
            }
        }
    }

}
