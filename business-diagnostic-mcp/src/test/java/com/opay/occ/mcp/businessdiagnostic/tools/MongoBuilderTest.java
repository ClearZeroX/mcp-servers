package com.opay.occ.mcp.businessdiagnostic.tools;
import com.opay.occ.mcp.businessdiagnostic.tools.MongoBuilder;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoBuilderTest {
    @Test
    void shouldBuildFilterAndSort() {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("status", "FAILED");
        Map<String, Object> sort = new LinkedHashMap<>();
        sort.put("createTime", -1);

        assertThat(MongoBuilder.buildFilter(filter).get("status")).isEqualTo("FAILED");
        assertThat(MongoBuilder.buildSort(sort).getInteger("createTime")).isEqualTo(-1);
    }

    @Test
    void shouldRejectUnsupportedOperators() {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("$where", "return true");

        assertThatThrownBy(() -> MongoBuilder.buildFilter(filter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported Mongo operator");
    }

    @Test
    void shouldRejectOversizedInArray() {
        List<Object> ids = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            ids.add(String.valueOf(i));
        }
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("$in", ids);
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("_id", value);

        assertThatThrownBy(() -> MongoBuilder.buildFilter(filter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supports at most 500");
    }
}
