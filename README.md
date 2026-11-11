# MCP Server/Client with Spring AI

This project demonstrates an end-to-end **Model Context Protocol (MCP)** setup using Spring AI, with:

- an MCP server over SSE (`sse-mcp-server`)
- an AI client application (`mcp-client`) that connects to MCP tools
- a Python MCP server (`python-mcp-server`) started via stdio
- an Angular chat UI (`chat-frontend`)

It is designed as a practical playground for building AI apps that can call structured external tools.

## Architecture

The runtime flow is:

1. User sends a message from UI (Angular or Thymeleaf page).
2. `mcp-client` receives the message and sends it to Anthropic via Spring AI.
3. Spring AI can call MCP tools exposed by:
   - `sse-mcp-server` (SSE transport, port `8877`)
   - `python-mcp-server` (stdio transport, started by `uv`)
   - filesystem MCP server (`@modelcontextprotocol/server-filesystem`)
4. Tool results are returned to the model and then to the user.

## Modules

| Module | Purpose | Default Port |
| --- | --- | --- |
| `mcp-client` | Spring Boot AI client + API + server-rendered chat page | `8080` |
| `sse-mcp-server` | Spring Boot MCP server exposing stock/company tools | `8877` |
| `python-mcp-server` | Python MCP server exposing employee info tool | stdio (no HTTP port) |
| `chat-frontend` | Angular UI consuming `mcp-client` REST endpoint | `4200` |

## Features

### `mcp-client`
- Uses `spring-ai-starter-mcp-client` and `spring-ai-starter-model-anthropic`
- Registers tool callbacks automatically through MCP client integrations
- Keeps short message history with `MessageWindowChatMemory`
- Exposes:
  - `GET /` (Thymeleaf chat UI)
  - `POST /chat/send` (form field: `message`)
  - `GET /api/chat?query=...`
- CORS is configured to allow `http://localhost:4200`

### `sse-mcp-server`
Exposes tool methods from `StockExchangeService`:
- `getCompanyByName(companyName)`
- `getAllCompanies()`
- `getStockByCompany(companyName)`

Configured MCP server settings:
- `spring.ai.mcp.server.type=sync`
- SSE endpoint: `/sse`
- message endpoint: `/mcp/message`

### `python-mcp-server`
Provides one MCP tool:
- `get_info_about(name)` returning first name, last name, salary, and email

### `chat-frontend`
- Angular 19 app with simple chat experience
- Sends user messages to `http://localhost:8080/chat/send`
- Renders assistant messages with Markdown (`marked`)

## Prerequisites

- Java 21
- Maven (or use the included Maven Wrapper in each Spring Boot module)
- Node.js + npm (for `chat-frontend`)
- Python 3.10+
- `uv` (required by `mcp-client` stdio MCP configuration)
- Anthropic API key

## Configuration

### 1. Anthropic API key

Set your API key before starting `mcp-client`:

```bash
export CLAUDE_API_KEY=your_key_here
```

### 2. MCP server config paths

`mcp-client/src/main/resources/mcp-servers.json` currently contains absolute paths.  
If your local directory is different, update these paths:

- filesystem server root path
- Python server script path

## Running the project

Start services in this order.

### 1) Start the SSE MCP server

```bash
cd sse-mcp-server
./mvnw spring-boot:run
```

The server starts on `http://localhost:8877` and exposes `/sse`.

### 2) Start the Spring AI client

```bash
cd mcp-client
./mvnw spring-boot:run
```

Client app runs on `http://localhost:8080`.

### 3) (Optional) Start Angular frontend

```bash
cd chat-frontend
npm install
npm start
```

UI runs on `http://localhost:4200`.

## Usage

You can use either UI:

- **Thymeleaf UI**: `http://localhost:8080/`
- **Angular UI**: `http://localhost:4200/`

Or call the API directly:

```bash
curl -X POST http://localhost:8080/chat/send \
  -F "message=Give me stock info for OCI"
```

```bash
curl "http://localhost:8080/api/chat?query=List all companies"
```

## Build and test

Run tests per Spring Boot module:

```bash
cd sse-mcp-server && ./mvnw test
cd ../mcp-client && ./mvnw test
```

Build Angular app:

```bash
cd chat-frontend
npm run build
```

## API and endpoints summary

| Service | Endpoint | Description |
| --- | --- | --- |
| `mcp-client` | `GET /` | Server-rendered chat page |
| `mcp-client` | `POST /chat/send` | Chat request (`message` form field) |
| `mcp-client` | `GET /api/chat?query=...` | Chat API endpoint |
| `sse-mcp-server` | `GET /sse` | MCP SSE stream endpoint |
| `sse-mcp-server` | `POST /mcp/message` | MCP message endpoint |

## Notes

- The root `pom.xml` is minimal and not configured as a full multi-module Maven aggregator.
- `mcp-client` logs MCP internals at TRACE level for easier debugging.
- If tool calls fail, first verify:
  1. `sse-mcp-server` is running on `8877`
  2. `CLAUDE_API_KEY` is exported
  3. `mcp-servers.json` paths match your machine
