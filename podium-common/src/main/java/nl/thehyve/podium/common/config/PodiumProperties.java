/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(
    prefix = "podium",
    ignoreUnknownFields = false
)
public class PodiumProperties {
    private final Async async = new Async();
    private final Http http = new Http();
    private final Cache cache = new Cache();
    private final Mail mail = new Mail();
    private final Security security = new Security();
    private final Swagger swagger = new Swagger();
    private final Metrics metrics = new Metrics();
    private final CorsConfiguration cors = new CorsConfiguration();
    private final Gateway gateway = new Gateway();
    private final Ribbon ribbon = new Ribbon();
    private final Logging logging = new Logging();

    private final Registry registry = new Registry();

    private final Files files = new Files();

    private final Access access = new Access();

    public PodiumProperties() {
    }

    public Async getAsync() {
        return this.async;
    }

    public Http getHttp() {
        return this.http;
    }

    public Cache getCache() {
        return this.cache;
    }

    public Mail getMail() {
        return this.mail;
    }

    public Registry getRegistry() { return registry; }

    public Security getSecurity() {
        return this.security;
    }

    public Swagger getSwagger() {
        return this.swagger;
    }

    public Metrics getMetrics() {
        return this.metrics;
    }

    public CorsConfiguration getCors() {
        return this.cors;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public Ribbon getRibbon() {
        return this.ribbon;
    }

    public Logging getLogging() {
        return this.logging;
    }

    public Files getFiles() {return this.files; }

    public Access getAccess() { return access; }

    public static class Ribbon {
        private String[] displayOnActiveProfiles;

        public Ribbon() { }

        public String[] getDisplayOnActiveProfiles() {
            return this.displayOnActiveProfiles;
        }

        public void setDisplayOnActiveProfiles(String[] displayOnActiveProfiles) {
            this.displayOnActiveProfiles = displayOnActiveProfiles;
        }
    }

    public static class Gateway {
        private final Gateway.RateLimiting rateLimiting = new Gateway.RateLimiting();
        private Map<String, List<String>> authorizedMicroservicesEndpoints = new LinkedHashMap();

        public Gateway() {
        }

        public Gateway.RateLimiting getRateLimiting() {
            return this.rateLimiting;
        }

        public Map<String, List<String>> getAuthorizedMicroservicesEndpoints() {
            return this.authorizedMicroservicesEndpoints;
        }

        public void setAuthorizedMicroservicesEndpoints(Map<String, List<String>> authorizedMicroservicesEndpoints) {
            this.authorizedMicroservicesEndpoints = authorizedMicroservicesEndpoints;
        }

        public static class RateLimiting {
            private boolean enabled = false;
            private long limit = 100000L;

            public RateLimiting() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public long getLimit() {
                return this.limit;
            }

            public void setLimit(long limit) {
                this.limit = limit;
            }
        }
    }

    public static class Logging {
        private final Logging.Logstash logstash = new Logging.Logstash();
        private final Logging.SpectatorMetrics spectatorMetrics = new Logging.SpectatorMetrics();

        public Logging() {
        }

        public Logging.Logstash getLogstash() {
            return this.logstash;
        }

        public Logging.SpectatorMetrics getSpectatorMetrics() {
            return this.spectatorMetrics;
        }

        public static class SpectatorMetrics {
            private boolean enabled = false;

            public SpectatorMetrics() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static class Logstash {
            private boolean enabled = false;
            private String host = "localhost";
            private int port = 5000;
            private int queueSize = 512;

            public Logstash() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return this.host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return this.port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public int getQueueSize() {
                return this.queueSize;
            }

            public void setQueueSize(int queueSize) {
                this.queueSize = queueSize;
            }
        }
    }

    public static class Metrics {
        private final Metrics.Jmx jmx = new Metrics.Jmx();
        private final Metrics.Graphite graphite = new Metrics.Graphite();
        private final Metrics.Prometheus prometheus = new Metrics.Prometheus();
        private final Metrics.Logs logs = new Metrics.Logs();

        public Metrics() {
        }

        public Metrics.Jmx getJmx() {
            return this.jmx;
        }

        public Metrics.Graphite getGraphite() {
            return this.graphite;
        }

        public Metrics.Prometheus getPrometheus() {
            return this.prometheus;
        }

        public Metrics.Logs getLogs() {
            return this.logs;
        }

        public static class Logs {
            private boolean enabled = false;
            private long reportFrequency = 60L;

            public Logs() {
            }

            public long getReportFrequency() {
                return this.reportFrequency;
            }

            public void setReportFrequency(int reportFrequency) {
                this.reportFrequency = (long)reportFrequency;
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static class Prometheus {
            private boolean enabled = false;
            private String endpoint = "/prometheusMetrics";

            public Prometheus() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getEndpoint() {
                return this.endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }
        }

        public static class Graphite {
            private boolean enabled = false;
            private String host = "localhost";
            private int port = 2003;
            private String prefix = "podiumApplication";

            public Graphite() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return this.host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return this.port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getPrefix() {
                return this.prefix;
            }

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }
        }

        public static class Jmx {
            private boolean enabled = true;

            public Jmx() {
            }

            public boolean isEnabled() {
                return this.enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }

    public static class Swagger {
        private String title = "Application API";
        private String description = "API documentation";
        private String version = "0.0.1";
        private String termsOfServiceUrl;
        private String contactName;
        private String contactUrl;
        private String contactEmail;
        private String license;
        private String licenseUrl;
        private String defaultIncludePattern = "/api/.*";
        private String host;
        private String[] protocols;

        public Swagger() {
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTermsOfServiceUrl() {
            return this.termsOfServiceUrl;
        }

        public void setTermsOfServiceUrl(String termsOfServiceUrl) {
            this.termsOfServiceUrl = termsOfServiceUrl;
        }

        public String getContactName() {
            return this.contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactUrl() {
            return this.contactUrl;
        }

        public void setContactUrl(String contactUrl) {
            this.contactUrl = contactUrl;
        }

        public String getContactEmail() {
            return this.contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }

        public String getLicense() {
            return this.license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getLicenseUrl() {
            return this.licenseUrl;
        }

        public void setLicenseUrl(String licenseUrl) {
            this.licenseUrl = licenseUrl;
        }

        public String getDefaultIncludePattern() {
            return this.defaultIncludePattern;
        }

        public void setDefaultIncludePattern(String defaultIncludePattern) {
            this.defaultIncludePattern = defaultIncludePattern;
        }

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public String[] getProtocols() {
            return protocols;
        }

        public void setProtocols(final String[] protocols) {
            this.protocols = protocols;
        }
    }

    public static class Security {
        private final Security.RememberMe rememberMe = new Security.RememberMe();
        private final Security.ClientAuthorization clientAuthorization = new Security.ClientAuthorization();
        private final Security.Authentication authentication = new Security.Authentication();

        public Security() {
        }

        public Security.RememberMe getRememberMe() {
            return this.rememberMe;
        }

        public Security.ClientAuthorization getClientAuthorization() {
            return this.clientAuthorization;
        }

        public Security.Authentication getAuthentication() {
            return this.authentication;
        }

        public static class RememberMe {
            @NotNull
            private String key;

            public RememberMe() {
            }

            public String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }
        }

        public static class Authentication {
            private final Security.Authentication.Oauth oauth = new Security.Authentication.Oauth();
            private final Security.Authentication.Jwt jwt = new Security.Authentication.Jwt();

            public Authentication() {
            }

            public Security.Authentication.Oauth getOauth() {
                return this.oauth;
            }

            public Security.Authentication.Jwt getJwt() {
                return this.jwt;
            }

            public static class Jwt {
                private String secret;
                private long tokenValidityInSeconds = 1800L;
                private long tokenValidityInSecondsForRememberMe = 2592000L;

                public Jwt() {
                }

                public String getSecret() {
                    return this.secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }

                public long getTokenValidityInSeconds() {
                    return this.tokenValidityInSeconds;
                }

                public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
                    this.tokenValidityInSeconds = tokenValidityInSeconds;
                }

                public long getTokenValidityInSecondsForRememberMe() {
                    return this.tokenValidityInSecondsForRememberMe;
                }

                public void setTokenValidityInSecondsForRememberMe(long tokenValidityInSecondsForRememberMe) {
                    this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;
                }
            }

            public static class Oauth {
                private String clientId;
                private String clientSecret;
                private int tokenValidityInSeconds = 1800;

                public Oauth() {
                }

                public String getClientId() {
                    return this.clientId;
                }

                public void setClientId(String clientId) {
                    this.clientId = clientId;
                }

                public String getClientSecret() {
                    return this.clientSecret;
                }

                public void setClientSecret(String clientSecret) {
                    this.clientSecret = clientSecret;
                }

                public int getTokenValidityInSeconds() {
                    return this.tokenValidityInSeconds;
                }

                public void setTokenValidityInSeconds(int tokenValidityInSeconds) {
                    this.tokenValidityInSeconds = tokenValidityInSeconds;
                }
            }
        }

        public static class ClientAuthorization {
            private String accessTokenUri;
            private String tokenServiceId;
            private String clientId;
            private String clientSecret;

            public ClientAuthorization() {
            }

            public String getAccessTokenUri() {
                return this.accessTokenUri;
            }

            public void setAccessTokenUri(String accessTokenUri) {
                this.accessTokenUri = accessTokenUri;
            }

            public String getTokenServiceId() {
                return this.tokenServiceId;
            }

            public void setTokenServiceId(String tokenServiceId) {
                this.tokenServiceId = tokenServiceId;
            }

            public String getClientId() {
                return this.clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return this.clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }
        }
    }

    public static class Mail {
        private String from = "";
        private String baseUrl = "";

        public Mail() {
        }

        public String getFrom() {
            return this.from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getBaseUrl() {
            return this.baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Cache {
        private final Cache.Ehcache ehcache = new Cache.Ehcache();
        private final Cache.Hazelcast hazelcast = new Cache.Hazelcast();

        public Cache() {
        }

        public Cache.Ehcache getEhcache() {
            return this.ehcache;
        }

        public Cache.Hazelcast getHazelcast() {
            return this.hazelcast;
        }

        public static class Hazelcast {
            private int timeToLiveSeconds = 3600;
            private int backupCount = 1;

            public Hazelcast() {
            }

            public int getTimeToLiveSeconds() {
                return this.timeToLiveSeconds;
            }

            public void setTimeToLiveSeconds(int timeToLiveSeconds) {
                this.timeToLiveSeconds = timeToLiveSeconds;
            }

            public int getBackupCount() {
                return this.backupCount;
            }

            public void setBackupCount(int backupCount) {
                this.backupCount = backupCount;
            }
        }

        public static class Ehcache {
            private String maxBytesLocalHeap = "16M";

            public Ehcache() {
            }

            public String getMaxBytesLocalHeap() {
                return this.maxBytesLocalHeap;
            }

            public void setMaxBytesLocalHeap(String maxBytesLocalHeap) {
                this.maxBytesLocalHeap = maxBytesLocalHeap;
            }
        }
    }

    public static class Http {
        private final Http.Cache cache = new Http.Cache();

        public Http() {
        }

        public Http.Cache getCache() {
            return this.cache;
        }

        public static class Cache {
            private int timeToLiveInDays = 1461;

            public Cache() {
            }

            public int getTimeToLiveInDays() {
                return this.timeToLiveInDays;
            }

            public void setTimeToLiveInDays(int timeToLiveInDays) {
                this.timeToLiveInDays = timeToLiveInDays;
            }
        }
    }

    public static class Async {
        private int corePoolSize = 2;
        private int maxPoolSize = 50;
        private int queueCapacity = 10000;

        public Async() {
        }

        public int getCorePoolSize() {
            return this.corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return this.maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return this.queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    private static class Registry {

        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Files {
        private String uploadDir = "/tmp/podium_data";

        public Files(){

        }

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }
    }

    public static class Access {
        private List<String> externalRequest;

        public List<String> getExternalRequest() {
            return externalRequest;
        }

        public void setExternalRequest(List<String> externalRequest) {
            this.externalRequest = externalRequest;
        }
    }
}
