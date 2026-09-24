package com.opay.occ.mcp.businessdiagnostic.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Immutable data source profile.
 */
public final class Profile {
    public enum Type {
        MYSQL,
        MONGO
    }

    private final String name;
    private final Type type;
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String passwordEnv;
    private final String uriEnv;
    private final Set<String> allowedTables;
    private final Set<String> allowedCollections;
    private final Set<String> sensitiveFields;

    private Profile(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.user = builder.user;
        this.passwordEnv = builder.passwordEnv;
        this.uriEnv = builder.uriEnv;
        this.allowedTables = Collections.unmodifiableSet(new HashSet<>(builder.allowedTables));
        this.allowedCollections = Collections.unmodifiableSet(new HashSet<>(builder.allowedCollections));
        this.sensitiveFields = Collections.unmodifiableSet(new HashSet<>(builder.sensitiveFields));
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPasswordEnv() {
        return passwordEnv;
    }

    public String getPassword() {
        if (passwordEnv == null || passwordEnv.isEmpty()) {
            return null;
        }
        return System.getenv(passwordEnv);
    }

    public String getUri() {
        if (uriEnv == null || uriEnv.isEmpty()) {
            return null;
        }
        return System.getenv(uriEnv);
    }

    public boolean isTableAllowed(String table) {
        return allowedTables.contains(table);
    }

    public boolean isCollectionAllowed(String collection) {
        return allowedCollections.contains(collection);
    }

    public boolean isSensitiveField(String field) {
        return sensitiveFields.contains(field);
    }

    public Set<String> getAllowedTables() {
        return allowedTables;
    }

    public Set<String> getAllowedCollections() {
        return allowedCollections;
    }

    public Set<String> getSensitiveFields() {
        return sensitiveFields;
    }

    public static Builder builder(String name, Type type) {
        return new Builder(name, type);
    }

    public static final class Builder {
        private final String name;
        private final Type type;
        private String host;
        private int port;
        private String database;
        private String user;
        private String passwordEnv;
        private String uriEnv;
        private List<String> allowedTables = Collections.emptyList();
        private List<String> allowedCollections = Collections.emptyList();
        private List<String> sensitiveFields = Collections.emptyList();

        private Builder(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder passwordEnv(String passwordEnv) {
            this.passwordEnv = passwordEnv;
            return this;
        }

        public Builder uriEnv(String uriEnv) {
            this.uriEnv = uriEnv;
            return this;
        }

        public Builder allowedTables(List<String> allowedTables) {
            this.allowedTables = allowedTables == null ? Collections.<String>emptyList() : allowedTables;
            return this;
        }

        public Builder allowedCollections(List<String> allowedCollections) {
            this.allowedCollections = allowedCollections == null ? Collections.<String>emptyList() : allowedCollections;
            return this;
        }

        public Builder sensitiveFields(List<String> sensitiveFields) {
            this.sensitiveFields = sensitiveFields == null ? Collections.<String>emptyList() : sensitiveFields;
            return this;
        }

        public Profile build() {
            return new Profile(this);
        }
    }
}
