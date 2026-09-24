# Business Diagnostic MCP

A standalone, read-only MySQL and Mongo diagnostic MCP server.

## Current transport

- Local `stdio`
- Newline-delimited JSON-RPC 2.0 over stdin/stdout

The transport layer is isolated under:

```text
src/main/java/com/opay/occ/mcp/businessdiagnostic/transport/stdio/
```

Future HTTP, SSE, or remote deployment transports can be added as sibling packages without changing the core tool implementations.

## Project layout

```text
business-diagnostic-mcp/
├── .codex-plugin/
│   └── plugin.json
├── .mcp.json
├── profiles.example.json
├── scripts/
│   └── run.sh
├── skills/
│   └── business-data-diagnostics/
│       └── SKILL.md
└── src/
    ├── main/java/com/opay/occ/mcp/businessdiagnostic/
    │   ├── core/
    │   │   ├── Audit.java
    │   │   ├── Json.java
    │   │   ├── Masking.java
    │   │   ├── Profile.java
    │   │   └── ProfileStore.java
    │   ├── tools/
    │   │   ├── MongoBuilder.java
    │   │   ├── MongoTools.java
    │   │   ├── MySqlTools.java
    │   │   ├── SqlBuilder.java
    │   │   ├── ToolRegistry.java
    │   │   └── Tools.java
    │   └── transport/
    │       └── stdio/
    │           └── McpServer.java
    └── test/java/com/opay/occ/mcp/businessdiagnostic/
        ├── core/
        └── tools/
```

## Build

```bash
mvn test
mvn package -DskipTests
```

The executable jar is generated at:

```text
target/business-diagnostic-mcp.jar
```

## Configure

1. Copy `profiles.example.json` to `profiles.json`.
2. Edit the profile names, hosts, databases, allowlists, and environment variable names.
3. Export the referenced environment variables before starting Codex or the server.

Example:

```bash
export BUSINESS_DIAGNOSTIC_TEST_MYSQL_PASSWORD='...'
export BUSINESS_DIAGNOSTIC_TEST_MONGO_URI='mongodb://...'
```

The server also supports:

```bash
export BUSINESS_DIAGNOSTIC_CONFIG='/absolute/path/to/profiles.json'
```

## Run manually

```bash
scripts/run.sh
```

The process reads JSON-RPC requests from stdin and writes responses to stdout.

## Tools

- `list_profiles`
- `get_schema`
- `query_mysql`
- `count_mysql`
- `explain_mysql`
- `query_mongo`
- `count_mongo`
- `explain_mongo`

## Safety

- The database account should still be read-only when possible.
- The server does not accept raw SQL.
- Only generated `SELECT`, `COUNT`, and `EXPLAIN` statements are executed.
- MySQL connections are explicitly set to read-only sessions.
- Passwords and Mongo URIs are read from environment variables, not from the profile file.
- Tables and collections must be explicitly allowlisted.
- Sensitive fields are masked before results are returned.
- MySQL `IN` predicates are split into batches of 500 values.
- Mongo `$in` and `$nin` arrays are limited to 500 values.
