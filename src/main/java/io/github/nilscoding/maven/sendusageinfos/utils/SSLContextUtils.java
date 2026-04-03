package io.github.nilscoding.maven.sendusageinfos.utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;

/**
 * SSL context utility.
 *
 * @author NilsCoding
 */
public final class SSLContextUtils {

    private SSLContextUtils() {
    }

    /**
     * Acquires a new context instance with given protocol and uses the certificates of the given trust manager for init.
     *
     * @param protocol     protocol, e.g. "TLS" (default) or "SSL"
     * @param trustManager trust manager
     * @return SSLContext instance or null on error
     */
    public static SSLContext createContext(String protocol, X509TrustManager trustManager) {
        if (protocol == null) {
            protocol = "TLS";
        }
        try {
            SSLContext context = SSLContext.getInstance(protocol);
            context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            return context;
        } catch (Exception ex) {
            return null;
        }
    }
}
