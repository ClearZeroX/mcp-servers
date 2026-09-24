package com.opay.occ.mcp.businessdiagnostic.tools;
import com.opay.occ.mcp.businessdiagnostic.core.ProfileStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool definitions and dispatch metadata.
 */
public final class ToolRegistry {
    private ToolRegistry() {
    }

    public static List<Map<String, Object>> listTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(tool(
                "list_profiles",
                "List configured diagnostic data source profiles.",
                objectSchema(Collections.<String, Object>emptyMap(), Collections.<String>emptyList())
        ));
        tools.add(tool(
                "get_schema",
                "Get MySQL table columns and indexes.",
                objectSchema(
                        props(
                                prop("profile", "string", "Profile name"),
                                prop("table", "string", "Table name")
                        ),
                        Arrays.asList("profile", "table")
                )
        ));
        tools.add(tool(
                "query_mysql",
                "Run a safe, read-only MySQL SELECT query with pagination and IN batching.",
                mysqlQuerySchema(true)
        ));
        tools.add(tool(
                "count_mysql",
                "Count MySQL rows for a safe, structured where clause.",
                objectSchema(
                        props(
                                prop("profile", "string", "Profile name"),
                                prop("table", "string", "Table name"),
                                prop("where", "array", "Structured where conditions")
                        ),
                        Arrays.asList("profile", "table")
                )
        ));
        tools.add(tool(
                "explain_mysql",
                "Explain a safe, read-only MySQL SELECT query.",
                mysqlQuerySchema(false)
        ));
        tools.add(tool(
                "query_mongo",
                "Run a safe, read-only Mongo find query.",
                mongoQuerySchema()
        ));
        tools.add(tool(
                "count_mongo",
                "Count Mongo documents for a safe filter.",
                objectSchema(
                        props(
                                prop("profile", "string", "Profile name"),
                                prop("collection", "string", "Collection name"),
                                prop("filter", "object", "Mongo filter object")
                        ),
                        Arrays.asList("profile", "collection")
                )
        ));
        tools.add(tool(
                "explain_mongo",
                "Explain a safe, read-only Mongo find query.",
                mongoQuerySchema()
        ));
        return tools;
    }

    public static Map<String, Object> call(String name, Map<String, Object> args, ProfileStore profiles) {
        return Tools.call(name, args == null ? Collections.<String, Object>emptyMap() : args, profiles);
    }

    private static Map<String, Object> mysqlQuerySchema(boolean includeCursor) {
        Map<String, Object> properties = props(
                prop("profile", "string", "Profile name"),
                prop("table", "string", "Table name"),
                prop("columns", "array", "Columns to select; empty means all columns"),
                prop("where", "array", "Structured where conditions"),
                prop("orderBy", "array", "Order by fields and directions"),
                prop("limit", "integer", "Maximum rows to return, from 1 to 1000"),
                prop("offset", "integer", "Offset for pagination, from 0 to 10000")
        );
        if (includeCursor) {
            properties.put("cursor", schema("string", "Opaque keyset pagination cursor"));
        }
        return objectSchema(properties, Arrays.asList("profile", "table"));
    }

    private static Map<String, Object> mongoQuerySchema() {
        Map<String, Object> properties = props(
                prop("profile", "string", "Profile name"),
                prop("collection", "string", "Collection name"),
                prop("filter", "object", "Mongo filter object"),
                prop("projection", "object", "Mongo projection object"),
                prop("sort", "object", "Mongo sort object"),
                prop("limit", "integer", "Maximum documents to return, from 1 to 1000"),
                prop("skip", "integer", "Number of documents to skip, from 0 to 10000")
        );
        return objectSchema(properties, Arrays.asList("profile", "collection"));
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> inputSchema) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("inputSchema", inputSchema);
        return result;
    }

    private static Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }

    private static Map<String, Object> schema(String type, String description) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("description", description);
        return result;
    }

    private static Map<String, Object> props(Map.Entry<String, Object>... entries) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static Map.Entry<String, Object> prop(String name, String type, String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", type);
        schema.put("description", description);
        return new java.util.AbstractMap.SimpleEntry<>(name, schema);
    }
}
