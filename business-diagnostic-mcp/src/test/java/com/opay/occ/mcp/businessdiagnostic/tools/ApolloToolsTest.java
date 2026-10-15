package com.opay.occ.mcp.businessdiagnostic.tools;

import com.opay.occ.mcp.businessdiagnostic.core.LogEnv;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApolloToolsTest {

    private LogEnv env() {
        return LogEnv.builder("idn-test")
                .mysql("MOCK_HOST", 3306, "whatsapp_crm", "MOCK_USER", "PWD_ENV")
                .apollo("whatsapp-crm", "http://10.99.1.187:8080",
                        Arrays.asList("application", "llm.yml", "datasource.yml"), "KEY_ENV")
                .build();
    }

    @Test
    void shouldUseAllConfiguredNamespacesWhenBlank() {
        List<String> namespaces = ApolloTools.resolveNamespaces(env(), null);
        assertThat(namespaces).containsExactly("application", "llm.yml", "datasource.yml");
    }

    @Test
    void shouldUseRequestedNamespaceOnly() {
        assertThat(ApolloTools.resolveNamespaces(env(), " llm.yml "))
                .containsExactly("llm.yml");
    }

    @Test
    void shouldMatchKeyCaseInsensitively() {
        assertThat(ApolloTools.matchesKey("test.log.db.enabled", "LOG.DB")).isTrue();
        assertThat(ApolloTools.matchesKey("test.log.db.enabled", null)).isTrue();
        assertThat(ApolloTools.matchesKey("test.log.db.enabled", "rabbitmq")).isFalse();
    }
}
