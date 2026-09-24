# MCP Servers

This directory stores standalone MCP server projects.

Current projects:

- `business-diagnostic-mcp`: read-only MySQL and Mongo business data diagnostics.

Each MCP is an independent project and may expose its own transport implementation. The current `business-diagnostic-mcp` uses local `stdio`; future projects or transport modes may add HTTP, SSE, or remote deployment without changing this directory layout.
