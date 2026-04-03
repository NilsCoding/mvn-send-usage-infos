package io.github.nilscoding.maven.sendusageinfos.sink;

import io.github.nilscoding.maven.sendusageinfos.SettingsWrapper;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import org.apache.maven.plugin.logging.Log;

/**
 * Interface for data sinks.
 *
 * @author NilsCoding
 */
public interface IDataSink {

    /**
     * Checks if this sink will write to the given location.
     *
     * @param location location
     * @return true if sink will write, false otherwise
     */
    boolean willWriteTo(String location);

    /**
     * Sends the data using this sink.
     *
     * @param usageData  usage data to send
     * @param parameters parameters
     * @param settings   settings
     * @param log        logging instance
     */
    void sendData(UsageData usageData, SinkParameters parameters, SettingsWrapper settings, Log log);
}
