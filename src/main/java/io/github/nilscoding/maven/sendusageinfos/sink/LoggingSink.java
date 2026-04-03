package io.github.nilscoding.maven.sendusageinfos.sink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.nilscoding.maven.sendusageinfos.SettingsWrapper;
import io.github.nilscoding.maven.sendusageinfos.data.UsageData;
import org.apache.maven.plugin.logging.Log;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Logging sink.
 *
 * @author NilsCoding
 */
public final class LoggingSink implements IDataSink {
    @Override
    public boolean willWriteTo(String location) {
        if (location == null) {
            return false;
        }
        return (location.startsWith("@logging"));
    }

    @Override
    public void sendData(UsageData usageData, SinkParameters parameters, SettingsWrapper settings, Log log) {
        if ((usageData == null) || (settings == null) || (log == null)) {
            return;
        }
        boolean prettyPrinting = parameters.isPrettyPrinting();
        String logInfo = parameters.getUrl();
        if ((logInfo == null) || (logInfo.isEmpty())) {
            return;
        }
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            // pretty-printing is enabled by default for log output
            if (logInfo.contains("(single-line)")) {
                prettyPrinting = false;
            }
            if (prettyPrinting) {
                gsonBuilder.setPrettyPrinting();
            }
            Consumer<String> logTarget = log::info;
            if (logInfo.startsWith("@logging:debug")) {
                logTarget = log::debug;
            } else if (logInfo.startsWith("@logging:warn")) {
                logTarget = log::warn;
            } else if (logInfo.startsWith("@logging:error")) {
                logTarget = log::error;
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
         *
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
