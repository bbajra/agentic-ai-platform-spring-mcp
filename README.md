# agentic-ai-platform-spring-mcp
Agentic AI platform with Spring MCP protocol


# 🧠 Agentic AI Platform with Spring Boot & MCP

A production-grade, extensible **Agentic AI platform** built using **Spring Boot**, **Spring AI**, and **Model Context Protocol (MCP)**.

Goal of this project is to learn and implement the AI principles and Model Context Protocol.

This project demonstrates how to safely build AI copilots that can reason, call tools, and operate in regulated enterprise environments such as healthcare — while remaining flexible for other industries.

---

## 🚀 Key Capabilities
- Agentic AI with tool-based reasoning
- Secure MCP tool orchestration
- Healthcare (FHIR) + generic industry support
- Spring Boot + Java enterprise patterns
- Observability, security, and extensibility

---

## 🏗️ High-Level Architecture
- **AI Gateway Service**
    - Hosts the LLM-based agent
    - Orchestrates MCP tool calls
- **MCP Tool Servers**
    - Runbooks / Knowledge (generic)
      - Runbook: a step-by-step operational guide that documents how the system works,
        how to troubleshoot it, and how to handle common tasks or incidents.
    - SQL Analytics (safe templates)
    - FHIR Validation (healthcare)
- **Web Client**
    - Next.js + TypeScript
    - Chat UI with tool execution trace

📄 See: `docs/architecture.md`

---

## 🛡️ Safety & Governance
- Tool allowlists
- Read-only SQL templates
- JWT authentication
- PHI / PII redaction
- Audit logs for all agent actions

---

## 🧪 Testing Strategy
- Unit tests (agent logic)
- Contract tests (MCP tools)
- Integration tests (Testcontainers)
- CI with GitHub Actions

---

## ▶️ Running Locally
set -a; source ./.env; set +a; mvn -f apps/ai-gateway-service/pom.xml spring-boot:run

```bash
POST http://localhost:8080/api/chat
```

### Local free model (Ollama)
Install and run Ollama, then pull a model:
brew install ollama
ollama serve


```bash
ollama run llama3.1:8b
```

Start the app with the Ollama profile:

```bash
mvn -f apps/ai-gateway-service/pom.xml spring-boot:run -Dspring-boot.run.profiles=ollama
```
