package com.opay.occ.mcp.businessdiagnostic.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable log diagnostic environment: MySQL connection plus Apollo coordinates.
 *
 * <p>Secrets are never stored in the profile file; they are resolved from
 * environment variables at call time.</p>
 */
public final class LogEnv {

    private final String name;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUser;
    private final String mysqlPasswordEnv;
    private final String apolloAppId;
    private final String apolloMeta;
    private final List<String> apolloNamespaces;
    private final String apolloAccessKeyEnv;

    private LogEnv(Builder builder) {
        this.name = builder.name;
        this.mysqlHost = builder.mysqlHost;
        this.mysqlPort = builder.mysqlPort;
        this.mysqlDatabase = builder.mysqlDatabase;
        this.mysqlUser = builder.mysqlUser;
        this.mysqlPasswordEnv = builder.mysqlPasswordEnv;
        this.apolloAppId = builder.apolloAppId;
        this.apolloMeta = builder.apolloMeta;
        this.apolloNamespaces = Collections.unmodifiableList(new ArrayList<>(builder.apolloNamespaces));
        this.apolloAccessKeyEnv = builder.apolloAccessKeyEnv;
    }

    public String getName() {
        return name;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public String getMysqlPasswordEnv() {
        return mysqlPasswordEnv;
    }

    /** Resolves the MySQL password from its environment variable, or null when unset. */
    public String getMysqlPassword() {
        if (mysqlPasswordEnv == null || mysqlPasswordEnv.isEmpty()) {
            return null;
        }
        String value = System.getenv(mysqlPasswordEnv);
        return (value == null || value.isEmpty()) ? null : value;
    }

    public String getApolloAppId() {
        return apolloAppId;
    }

    public String getApolloMeta() {
        return apolloMeta;
    }

    public List<String> getApolloNamespaces() {
        return apolloNamespaces;
    }

    public String getApolloAccessKeyEnv() {
        return apolloAccessKeyEnv;
    }

    /** Resolves the Apollo access key secret, or null when not configured or unset. */
    public String getApolloAccessKey() {
        if (apolloAccessKeyEnv == null || apolloAccessKeyEnv.isEmpty()) {
            return null;
        }
        String value = System.getenv(apolloAccessKeyEnv);
        return (value == null || value.isEmpty()) ? null : value;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String mysqlHost;
        private int mysqlPort = 3306;
        private String mysqlDatabase;
        private String mysqlUser;
        private String mysqlPasswordEnv;
        private String apolloAppId;
        private String apolloMeta;
        private List<String> apolloNamespaces = Collections.emptyList();
        private String apolloAccessKeyEnv;

        private Builder(String name) {
            this.name = name;
        }

        public Builder mysql(String host, int port, String database, String user, String passwordEnv) {
            this.mysqlHost = host;
            this.mysqlPort = port;
            this.mysqlDatabase = database;
            this.mysqlUser = user;
            this.mysqlPasswordEnv = passwordEnv;
            return this;
        }

        public Builder apollo(String appId, String meta, List<String> namespaces, String accessKeyEnv) {
            this.apolloAppId = appId;
            this.apolloMeta = meta;
            this.apolloNamespaces = namespaces == null ? Collections.<String>emptyList() : namespaces;
            this.apolloAccessKeyEnv = accessKeyEnv;
            return this;
        }

        public LogEnv build() {
            return new LogEnv(this);
        }
    }
}
