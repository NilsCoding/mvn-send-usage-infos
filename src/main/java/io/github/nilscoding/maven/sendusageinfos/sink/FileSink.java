package io.github.nilscoding.maven.sendusageinfos.sink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.nilscoding.maven.sendusageinfos.SettingsWrapper;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import org.apache.maven.plugin.logging.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * File sink, also used as the fallback.
 *
 * @author NilsCoding
 */
public final class FileSink implements IDataSink {
    @Override
    public boolean willWriteTo(String location) {
        return true;
    }

    @Override
    public void sendData(UsageData usageData, SinkParameters parameters, SettingsWrapper settings, Log log) {
        if ((usageData == null) || (parameters == null) || (log == null)) {
            return;
        }
        boolean prettyPrinting = parameters.isPrettyPrinting();
        String location = parameters.getUrl();
        if ((location == null) || (location.isEmpty())) {
            return;
        }
        if (location.startsWith("@")) {
            // special location which should be a custom sink, so don't write to file
            log.warn("location '" + location + "' is not a file location, so data will not be written to file");
            return;
        }
        try {
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
                if (prettyPrinting) {
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
}
