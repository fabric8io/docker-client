package io.fabric8.docker.client;

import io.fabric8.docker.client.utils.Utils;
import io.sundr.builder.annotations.Buildable;

public class Config {

    public static String HTTP_PROTOCOL_PREFIX = "http://";
    public static String HTTPS_PROTOCOL_PREFIX = "https://";

    private boolean trustCerts;
    private String masterUrl;
    private String caCertFile;
    private String caCertData;
    private String clientCertFile;
    private String clientCertData;
    private String clientKeyFile;
    private String clientKeyData;
    private String clientKeyAlgo = "RSA";
    private String clientKeyPassphrase = "changeit";
    private String username;
    private String password;
    private String oauthToken;
    private int watchReconnectInterval = 1000;
    private int watchReconnectLimit = -1;
    private int connectionTimeout = 10 * 1000;
    private int requestTimeout = 10 * 1000;
    private String httpProxy;
    private String httpsProxy;
    private String[] noProxy;

    @Buildable
    public Config(boolean trustCerts, String masterUrl, String caCertFile, String caCertData, String clientCertFile, String clientCertData, String clientKeyFile, String clientKeyData, String clientKeyAlgo, String clientKeyPassphrase, String username, String password, String oauthToken, int watchReconnectInterval, int watchReconnectLimit, int connectionTimeout, int requestTimeout, String httpProxy, String httpsProxy, String[] noProxy) {
        this.trustCerts = trustCerts;
        this.masterUrl = masterUrl;
        this.caCertFile = caCertFile;
        this.caCertData = caCertData;
        this.clientCertFile = clientCertFile;
        this.clientCertData = clientCertData;
        this.clientKeyFile = clientKeyFile;
        this.clientKeyData = clientKeyData;
        this.clientKeyAlgo = clientKeyAlgo;
        this.clientKeyPassphrase = clientKeyPassphrase;
        this.username = username;
        this.password = password;
        this.oauthToken = oauthToken;
        this.watchReconnectInterval = watchReconnectInterval;
        this.watchReconnectLimit = watchReconnectLimit;
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.noProxy = noProxy;

        if (masterUrl == null) {
            this.masterUrl = Utils.getSystemPropertyOrEnvVar("docker.host");
        }
    }

    public boolean isTrustCerts() {
        return trustCerts;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public String getCaCertData() {
        return caCertData;
    }

    public String getClientCertFile() {
        return clientCertFile;
    }

    public String getClientCertData() {
        return clientCertData;
    }

    public String getClientKeyFile() {
        return clientKeyFile;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public String getClientKeyAlgo() {
        return clientKeyAlgo;
    }

    public String getClientKeyPassphrase() {
        return clientKeyPassphrase;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public int getWatchReconnectInterval() {
        return watchReconnectInterval;
    }

    public int getWatchReconnectLimit() {
        return watchReconnectLimit;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public String[] getNoProxy() {
        return noProxy;
    }
}
