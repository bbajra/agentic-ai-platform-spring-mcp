# Project Spec — Agentic AI Platform with Spring Boot & MCP

## Goal
Build an enterprise-grade Agentic AI platform using Spring Boot, Spring AI, and Model Context Protocol (MCP).  
The platform must be safe, extensible, observable, and suitable for regulated domains such as healthcare while remaining industry-agnostic.

## Core Architecture
- AI Gateway Service (Spring Boot + Spring AI)
- MCP Tool Servers (Runbooks, SQL Analytics, FHIR Validation)
- Optional Web Client (Next.js + TypeScript + React)

## AI Gateway Responsibilities
- Expose POST /api/chat
- Use Spring AI with OpenAI provider
- Orchestrate MCP tool calls
- Enforce tool allowlist
- Return structured response:
  {
  "answer": string,
  "evidence": [],
  "toolCalls": []
  }

## MCP Tool Servers
### Runbook Tool (Generic)
- Index markdown files under /docs/runbooks
- Retrieve relevant sections
- Return citations

### SQL Tool (Generic)
- PostgreSQL read-only
- Only predefined query templates allowed
- No raw SQL execution

### FHIR Tool (Healthcare)
- Use HAPI FHIR
- Validate FHIR resources or bundles
- Summarize validation issues

## Security & Safety
- JWT authentication
- Tool-level authorization
- PHI / PII redaction
- Prompt injection protection
- Full audit log of agent actions

## Reliability & Observability
- Resilience4j timeouts and circuit breakers
- OpenTelemetry tracing
- Correlation IDs across agent and tools

## Non-Goals
- No auto-executing arbitrary code
- No direct database writes from LLM

## Technology Choices
- Java 17+
- Spring Boot 3.x
- Spring AI
- OpenAI (for demos)
- Docker & Docker Compose
