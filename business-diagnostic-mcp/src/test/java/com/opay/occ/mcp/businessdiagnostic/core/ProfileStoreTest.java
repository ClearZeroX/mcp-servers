package com.opay.occ.mcp.businessdiagnostic.core;
import com.opay.occ.mcp.businessdiagnostic.core.Profile;
import com.opay.occ.mcp.businessdiagnostic.core.ProfileStore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldLoadMysqlAndMongoProfiles() throws Exception {
        Path file = tempDir.resolve("profiles.json");
        String json = "{\n"
                + "  \"profiles\": {\n"
                + "    \"mysql-test\": {\n"
                + "      \"type\": \"mysql\",\n"
                + "      \"host\": \"127.0.0.1\",\n"
                + "      \"port\": 3307,\n"
                + "      \"database\": \"crm\",\n"
                + "      \"user\": \"readonly\",\n"
                + "      \"passwordEnv\": \"MYSQL_TEST_PASSWORD\",\n"
                + "      \"allowedTables\": [\"task\", \"task_sub\"]\n"
                + "    },\n"
                + "    \"mongo-test\": {\n"
                + "      \"type\": \"mongo\",\n"
                + "      \"uriEnv\": \"MONGO_TEST_URI\",\n"
                + "      \"database\": \"metrics\",\n"
                + "      \"allowedCollections\": [\"task_metrics\"]\n"
                + "    }\n"
                + "  }\n"
                + "}";
        java.nio.file.Files.write(file, json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ProfileStore store = ProfileStore.load(file);
        Map<String, Profile> profiles = store.getProfiles();

        assertThat(profiles).containsKeys("mysql-test", "mongo-test");
        assertThat(store.require("mysql-test").isTableAllowed("task")).isTrue();
        assertThat(store.require("mysql-test").isTableAllowed("user")).isFalse();
        assertThat(store.require("mongo-test").isCollectionAllowed("task_metrics")).isTrue();
        assertThat(store.require("mysql-test").isSensitiveField("password")).isTrue();
    }

    @Test
    void shouldRejectInvalidJson() {
        Path file = Paths.get("/not-exists/profiles.json");
        assertThatThrownBy(() -> ProfileStore.load(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }
}
