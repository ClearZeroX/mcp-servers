package com.opay.occ.mcp.businessdiagnostic.tools;
import com.opay.occ.mcp.businessdiagnostic.tools.ToolRegistry;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {
    @Test
    void shouldExposeReadonlyDiagnosticTools() {
        List<Map<String, Object>> tools = ToolRegistry.listTools();
        assertThat(tools)
                .extracting(tool -> tool.get("name"))
                .containsExactly(
                        "list_profiles", "get_schema", "query_mysql", "count_mysql", "explain_mysql",
                        "query_mongo", "count_mongo", "explain_mongo"
                );
    }
}
