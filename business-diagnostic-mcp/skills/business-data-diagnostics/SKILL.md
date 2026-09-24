---
name: business-data-diagnostics
description: Use the business-diagnostic-mcp tools to inspect read-only MySQL and Mongo business data, explain schemas, and diagnose records with safe pagination and masking.
---

# Business Data Diagnostics

Use this skill when the user asks to inspect business data, diagnose a record, compare tables, or explain a MySQL/Mongo schema through the local MCP server.

## Workflow

1. Call `list_profiles` first to see the available data sources.
2. For MySQL questions, call `get_schema` before querying if the table shape is unknown.
3. Prefer `count_mysql` or `count_mongo` before large queries.
4. Use `query_mysql` or `query_mongo` with a bounded `limit` and a stable `orderBy`.
5. Use `cursor` for MySQL keyset pagination when you need the next page.
6. Use `explain_mysql` or `explain_mongo` when a query is slow or unexpectedly returns many rows.
7. Summarize findings for the user; show raw rows only when explicitly requested.

## Safety

- The tools are read-only; never attempt to mutate data.
- Do not ask the user for database passwords in chat.
- Credentials come from the profile's environment variables.
- Respect the profile allowlists for tables and collections.
- Keep result sets small and use pagination.
- Masked fields must not be unmasked or reconstructed.

## MySQL examples

List profiles:

```json
{}
```

Get a schema:

```json
{
  "profile": "test-mysql",
  "table": "message_template"
}
```

Query with pagination:

```json
{
  "profile": "test-mysql",
  "table": "task",
  "columns": ["id", "status", "create_time"],
  "where": [
    { "field": "status", "op": "eq", "value": "FAILED" }
  ],
  "orderBy": [
    { "field": "id", "direction": "desc" }
  ],
  "limit": 100,
  "offset": 0
}
```

Continue with the returned `nextCursor`.

## Mongo examples

Query documents:

```json
{
  "profile": "test-mongo",
  "collection": "task_metrics",
  "filter": { "status": "FAILED" },
  "sort": { "createTime": -1 },
  "limit": 100,
  "skip": 0
}
```

Count documents:

```json
{
  "profile": "test-mongo",
  "collection": "task_metrics",
  "filter": { "status": "FAILED" }
}
```
