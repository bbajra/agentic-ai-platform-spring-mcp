# System Overview

## Purpose
The Agentic AI Platform provides a lightweight AI gateway that routes user requests to a configured LLM provider and exposes introspection endpoints for demos and interviews.

## Components
- AI Gateway Service: Spring Boot API that accepts chat requests and orchestrates tool calls.
- Runbook Search Tool: Local lookup over markdown runbooks for grounded answers.
- Observability: Actuator info/health endpoints plus build and git metadata.

## Key Flows
1. Client sends POST /api/chat with a prompt.
2. The gateway decides whether to call tools (for example, searchRunbooks).
3. The model responds using tool citations when available.

