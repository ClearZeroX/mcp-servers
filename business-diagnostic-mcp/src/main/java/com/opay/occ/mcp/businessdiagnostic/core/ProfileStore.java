package com.opay.occ.mcp.businessdiagnostic.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and validates diagnostic data source profiles.
 */
public final class ProfileStore {
    private static final String CONFIG_ENV = "BUSINESS_DIAGNOSTIC_CONFIG";
    private static final List<String> DEFAULT_SENSITIVE_FIELDS = Arrays.asList(
            "password", "secret", "token", "phone", "mobile", "email", "id_card", "bank_card"
    );

    private final Map<String, Profile> profiles;

    private ProfileStore(Map<String, Profile> profiles) {
        this.profiles = Collections.unmodifiableMap(profiles);
    }

    public static ProfileStore loadDefault() {
        Path path = defaultConfigPath();
        if (!Files.exists(path)) {
            return new ProfileStore(Collections.<String, Profile>emptyMap());
        }
        return load(path);
    }

    public static ProfileStore load(Path path) {
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Profile config file does not exist: " + path);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(path.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read profile config: " + path, e);
        }

        if (!root.isObject() || !root.has("profiles") || !root.get("profiles").isObject()) {
            throw new IllegalArgumentException("Profile config must contain a profiles object");
        }

        Map<String, Profile> result = new LinkedHashMap<>();
        JsonNode profilesNode = root.get("profiles");
        profilesNode.fields().forEachRemaining(entry -> {
            String name = entry.getKey();
            JsonNode node = entry.getValue();
            if (!node.isObject()) {
                throw new IllegalArgumentException("Profile must be an object: " + name);
            }
            result.put(name, parseProfile(name, node));
        });

        return new ProfileStore(result);
    }

    private static Profile parseProfile(String name, JsonNode node) {
        String typeText = textOrNull(node, "type");
        if (typeText == null) {
            throw new IllegalArgumentException("Profile is missing type: " + name);
        }

        Profile.Type type;
        if ("mysql".equalsIgnoreCase(typeText)) {
            type = Profile.Type.MYSQL;
        } else if ("mongo".equalsIgnoreCase(typeText)) {
            type = Profile.Type.MONGO;
        } else {
            throw new IllegalArgumentException("Unsupported profile type: " + typeText);
        }

        Profile.Builder builder = Profile.builder(name, type)
                .allowedTables(stringList(node, "allowedTables"))
                .allowedCollections(stringList(node, "allowedCollections"))
                .sensitiveFields(stringList(node, "sensitiveFields"));

        if (builderIsUsingDefaults(node, "sensitiveFields")) {
            builder.sensitiveFields(DEFAULT_SENSITIVE_FIELDS);
        }

        if (type == Profile.Type.MYSQL) {
            String host = textOrNull(node, "host");
            String database = textOrNull(node, "database");
            String user = textOrNull(node, "user");
            String passwordEnv = textOrNull(node, "passwordEnv");
            if (host == null || database == null || user == null || passwordEnv == null) {
                throw new IllegalArgumentException("MySQL profile requires host, database, user, and passwordEnv: " + name);
            }
            int port = node.has("port") && node.get("port").canConvertToInt() ? node.get("port").asInt() : 3306;
            builder.host(host).port(port).database(database).user(user).passwordEnv(passwordEnv);
        } else {
            String uriEnv = textOrNull(node, "uriEnv");
            String database = textOrNull(node, "database");
            if (uriEnv == null || database == null) {
                throw new IllegalArgumentException("Mongo profile requires uriEnv and database: " + name);
            }
            builder.uriEnv(uriEnv).database(database);
        }

        return builder.build();
    }

    private static boolean builderIsUsingDefaults(JsonNode node, String field) {
        return !node.has(field) || node.get(field).isNull();
    }

    private static String textOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (!value.isTextual()) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return value.asText();
    }

    private static List<String> stringList(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return Collections.emptyList();
        }
        JsonNode value = node.get(field);
        if (!value.isArray()) {
            throw new IllegalArgumentException(field + " must be an array");
        }
        List<String> result = new ArrayList<>();
        for (JsonNode item : value) {
            if (!item.isTextual()) {
                throw new IllegalArgumentException(field + " must contain strings");
            }
            result.add(item.asText());
        }
        return result;
    }

    private static Path defaultConfigPath() {
        String configured = System.getenv(CONFIG_ENV);
        if (configured != null && !configured.trim().isEmpty()) {
            return Paths.get(configured.trim());
        }
        Path local = Paths.get(".", "profiles.json").toAbsolutePath().normalize();
        if (Files.exists(local)) {
            return local;
        }
        return Paths.get(System.getProperty("user.home"), ".business-diagnostic", "profiles.json");
    }

    public Map<String, Profile> getProfiles() {
        return profiles;
    }

    public Profile require(String name) {
        Profile profile = profiles.get(name);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown profile: " + name);
        }
        return profile;
    }
}
