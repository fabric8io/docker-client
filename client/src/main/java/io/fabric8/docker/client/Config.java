/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.AuthConfigBuilder;
import io.fabric8.docker.api.model.DockerConfig;
import io.fabric8.docker.api.model.Doneable;
import io.fabric8.docker.client.utils.SSLUtils;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.client.utils.Utils;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.fabric8.docker.client.utils.Utils.getSystemPropertyOrEnvVar;
import static io.fabric8.docker.client.utils.Utils.hasSystemPropertyOrEnvVar;
import static io.fabric8.docker.client.utils.Utils.isNullOrEmpty;

@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = false, builderPackage = "io.fabric8.docker.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static final String UTF_8 = "UTF-8";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final MapType AUTHCONFIG_TYPE = JSON_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, AuthConfig.class);
    private static final String USERNAME_LABEL = "username";
    private static final String PASSWORD_LABEL = "password";
    private static final Pattern AUTH_PATTERN = Pattern.compile("(?<username>[^ :]+):(?<password>[^ ]+)");


    public static final String DEFAULT_INDEX="docker.io";
    public static final String DEFAULT_INDEX_SERVER="https://index.docker.io/v1/";
    public static final String DOCKER_AUTH_FALLBACK_KEY= "docker.auth.fallback.key";
    public static final String DOCKER_AUTH_DOCKERCFG_ENABLED = "docker.auth.dockercfg.enabled";
    public static final String DOCKER_DOCKERCFG_FILE = "docker.auth.dockercfg.path";

    public static final String DOCKER_HOST = "docker.host";

    public static final String KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY = "kubernetes.auth.tryServiceAccount";
    public static final String KUBERNETES_SERVICE_ACCOUNT_USER = "serviceaccount";
    public static final String KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    public static final String DOCKER_REQUEST_TIMEOUT_SYSTEM_PROPERTY = "docker.request.timeout";
    public static final String DOCKER_CONNECTION_TIMEOUT_SYSTEM_PROPERTY = "docker.connection.timeout";
    public static final String DOCKER_BUILD_TIMEOUT_SYSTEM_PROPERTY = "docker.build.timeout";
    public static final String DOCKER_PUSH_TIMEOUT_SYSTEM_PROPERTY = "docker.push.timeout";
    public static final String DOCKER_SEARCH_TIMEOUT_SYSTEM_PROPERTY = "docker.search.timeout";

    public static final String DOCKER_CERT_PATH_SYSTEM_PROPERTY = "docker.cert.path";
    public static final String DOCKER_TLS_VERIFY_PROPERTY = "docker.tls.verify";

    public static final String DOCKER_HTTP_PROXY = "http.proxy";
    public static final String DOCKER_HTTPS_PROXY = "https.proxy";
    public static final String DOCKER_ALL_PROXY = "all.proxy";
    public static final String DOCKER_NO_PROXY = "no.proxy";

    public static String TCP_PROTOCOL_PREFIX = "tcp://";
    public static String HTTP_PROTOCOL_PREFIX = "http://";
    public static String HTTPS_PROTOCOL_PREFIX = "https://";

    public static final String USE_JNR_UNIX_SOCKET = "docker.useJnrUnixSocket";

    private boolean trustCerts = true;
    private String dockerUrl;
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
    private int imageBuildTimeout = 10 * 60 * 1000;
    private int imagePushTimeout = 10 * 60 * 1000;
    private int imagePullTimeout = 10 * 60 * 1000;
    private int imageSearchTimeout = 3 * 60 * 1000;
    private int connectionTimeout = 10 * 1000;
    private int requestTimeout = 30 * 1000;
    private String httpProxy;
    private String httpsProxy;
    private String[] noProxy;
    private Map<String, AuthConfig> authConfigs = new HashMap<>();

    public Config() {
        tryDockerConfig(this);
        //In case of Kubernetes / Openshift let's try to read the authconfig from service account token
        tryServiceAccount(this);
    }

    public Config(boolean trustCerts, String dockerUrl, String caCertFile, String caCertData, String clientCertFile, String clientCertData, String clientKeyFile, String clientKeyData, String clientKeyAlgo, String clientKeyPassphrase, String username, String password, String oauthToken, int imageBuildTimeout, int imagePushTimeout, int imagePullTimeout, int imageSearchTimeout, int connectionTimeout, int requestTimeout, String httpProxy, String httpsProxy, String[] noProxy, Map<String, AuthConfig> authConfigs) {
        this();
        this.trustCerts = trustCerts;
        this.dockerUrl = dockerUrl;
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
        this.imageBuildTimeout = imageBuildTimeout;
        this.imagePushTimeout = imagePushTimeout;
        this.imagePullTimeout = imagePullTimeout;
        this.imageSearchTimeout = imageSearchTimeout;
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.noProxy = noProxy;

        if (authConfigs != null && !authConfigs.isEmpty()) {
            this.authConfigs = authConfigs;
        } else {
            this.authConfigs = new HashMap<>();
        }

        if (isNullOrEmpty(dockerUrl)) {
            this.dockerUrl = getSystemPropertyOrEnvVar(DOCKER_HOST, "unix:///var/run/docker.sock");
        }

        String dockerCertPath = getSystemPropertyOrEnvVar(DOCKER_CERT_PATH_SYSTEM_PROPERTY, new File(getHomeDir(), ".docker").getAbsolutePath());

        if (isNullOrEmpty(caCertData) && isNullOrEmpty(caCertFile)) {
            File caCertCandidate = new File(dockerCertPath, "ca.pem");
            if (caCertCandidate.exists()) {
                this.caCertFile = caCertCandidate.getAbsolutePath();
            }
        }

        if (isNullOrEmpty(clientCertData) && isNullOrEmpty(clientCertFile)) {
            File clientCertCandidate = new File(dockerCertPath, "cert.pem");
            if (clientCertCandidate.exists()) {
                this.clientCertFile = clientCertCandidate.getAbsolutePath();
            }
        }

        if (isNullOrEmpty(clientKeyData) && isNullOrEmpty(clientKeyFile)) {
            File clientKeyCandidate = new File(dockerCertPath, "key.pem");
            if (clientKeyCandidate.exists()) {
                this.clientKeyFile = clientKeyCandidate.getAbsolutePath();
            }
        }

        this.trustCerts |= !hasSystemPropertyOrEnvVar(DOCKER_TLS_VERIFY_PROPERTY);

        if (this.dockerUrl != null && this.dockerUrl.startsWith(TCP_PROTOCOL_PREFIX)) {
            if (SSLUtils.isHttpsAvailable(this)) {
                this.dockerUrl = URLUtils.withProtocol(this.dockerUrl, HTTPS_PROTOCOL_PREFIX);
            } else {
                this.dockerUrl = URLUtils.withProtocol(this.dockerUrl, HTTP_PROTOCOL_PREFIX);
            }
        }
    }

    private String getHomeDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("win")) {
            String userProfile = System.getenv("USERPROFILE");
            if (!userProfile.isEmpty()) {
                File f = new File(userProfile);
                if (f.exists() && f.isDirectory()) {
                    return userProfile;
                }
            }
        }
        String home = System.getenv("HOME");
        if (!home.isEmpty()) {
            File f = new File(home);
            if (f.exists() && f.isDirectory()) {
                return home;
            }
        }

        //Fall back to user.home should never really get here
        return System.getProperty("user.home", ".");
    }

    private boolean tryDockerConfig(Config config) {
        if (Utils.getSystemPropertyOrEnvVar(DOCKER_AUTH_DOCKERCFG_ENABLED, true)) {
            String dockerConfig = Utils.getSystemPropertyOrEnvVar(DOCKER_DOCKERCFG_FILE, Paths.get(System.getProperty("user.home", "."), ".docker", "config.json").toFile().getAbsolutePath());
            File dockerConfigFile = new File((dockerConfig));
            boolean dockerConfigExists = Files.isRegularFile(dockerConfigFile.toPath());
            if (dockerConfigExists) {
                try {
                    DockerConfig cfg = JSON_MAPPER.readValue(dockerConfigFile, DockerConfig.class);
                    for (Map.Entry<String, AuthConfig> entry: cfg.getAuths().entrySet()) {
                        String serverAddress = entry.getKey();
                        AuthConfig authConfig = entry.getValue();
                        if (authConfig.getAuth() != null) {
                            String auth = new String(Base64.decodeBase64(authConfig.getAuth().getBytes(UTF_8)), UTF_8);
                            Matcher m = AUTH_PATTERN.matcher(auth);
                            if (m.matches()) {
                                String username = m.group(USERNAME_LABEL);
                                String password = m.group(PASSWORD_LABEL);
                                String email = authConfig.getEmail();
                                config.getAuthConfigs().put(serverAddress,
                                        new AuthConfigBuilder()
                                                .withUsername(username)
                                                .withPassword(password)
                                                .withEmail(email)
                                                .withServeraddress(serverAddress).build());
                            } else {
                                config.getAuthConfigs().put(serverAddress, authConfig);
                            }
                        }
                    }
                    return !cfg.getAuths().isEmpty();
                } catch (IOException e) {
                    LOGGER.error("Could not load docker config file from {}", dockerConfig, e);
                }
            }
        }
        return false;
    }

    private boolean tryServiceAccount(Config config) {
        if (Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, true)) {
            try {
                String token = new String(Files.readAllBytes(new File(KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH).toPath()));
                if (token != null) {
                    config.getAuthConfigs().put(DOCKER_AUTH_FALLBACK_KEY, new AuthConfigBuilder()
                            .withUsername(KUBERNETES_SERVICE_ACCOUNT_USER)
                            .withPassword(token).build());
                    return true;
                }
            } catch (IOException e) {
                // No service account token available...
            }
        }
        return false;
    }

    public boolean isTrustCerts() {
        return trustCerts;
    }

    public void setTrustCerts(boolean trustCerts) {
        this.trustCerts = trustCerts;
    }

    public String getDockerUrl() {
        return dockerUrl;
    }

    public void setDockerUrl(String dockerUrl) {
        this.dockerUrl = dockerUrl;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public void setCaCertFile(String caCertFile) {
        this.caCertFile = caCertFile;
    }

    public String getCaCertData() {
        return caCertData;
    }

    public void setCaCertData(String caCertData) {
        this.caCertData = caCertData;
    }

    public String getClientCertFile() {
        return clientCertFile;
    }

    public void setClientCertFile(String clientCertFile) {
        this.clientCertFile = clientCertFile;
    }

    public String getClientCertData() {
        return clientCertData;
    }

    public void setClientCertData(String clientCertData) {
        this.clientCertData = clientCertData;
    }

    public String getClientKeyFile() {
        return clientKeyFile;
    }

    public void setClientKeyFile(String clientKeyFile) {
        this.clientKeyFile = clientKeyFile;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public void setClientKeyData(String clientKeyData) {
        this.clientKeyData = clientKeyData;
    }

    public String getClientKeyAlgo() {
        return clientKeyAlgo;
    }

    public void setClientKeyAlgo(String clientKeyAlgo) {
        this.clientKeyAlgo = clientKeyAlgo;
    }

    public String getClientKeyPassphrase() {
        return clientKeyPassphrase;
    }

    public void setClientKeyPassphrase(String clientKeyPassphrase) {
        this.clientKeyPassphrase = clientKeyPassphrase;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public int getImageBuildTimeout() {
        return imageBuildTimeout;
    }

    public void setImageBuildTimeout(int imageBuildTimeout) {
        this.imageBuildTimeout = imageBuildTimeout;
    }

    public int getImagePushTimeout() {
        return imagePushTimeout;
    }

    public void setImagePushTimeout(int imagePushTimeout) {
        this.imagePushTimeout = imagePushTimeout;
    }

    public int getImagePullTimeout() {
        return imagePullTimeout;
    }

    public void setImagePullTimeout(int imagePullTimeout) {
        this.imagePullTimeout = imagePullTimeout;
    }

    public int getImageSearchTimeout() {
        return imageSearchTimeout;
    }

    public void setImageSearchTimeout(int imageSearchTimeout) {
        this.imageSearchTimeout = imageSearchTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public void setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
    }

    public String[] getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(String[] noProxy) {
        this.noProxy = noProxy;
    }

    public Map<String, AuthConfig> getAuthConfigs() {
        return authConfigs;
    }

    public void setAuthConfigs(Map<String, AuthConfig> authConfigs) {
        this.authConfigs = authConfigs;
    }
}
