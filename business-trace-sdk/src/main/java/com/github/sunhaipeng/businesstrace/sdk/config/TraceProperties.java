package com.github.sunhaipeng.businesstrace.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "business.trace")
public class TraceProperties {
    private boolean enabled = true;
    private String serviceName;
    private Elasticsearch elasticsearch = new Elasticsearch();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public static class Elasticsearch {
        private String uri = "http://localhost:9200";
        private String index = "otel-traces";

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }
    }
}