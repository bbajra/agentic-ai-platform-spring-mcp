# AI Gateway Service

## Responsibilities
- Accept chat requests at /api/chat.
- Enrich responses with model metadata (modelUsed, latencyMs).
- Provide /api/meta and /actuator/info for runtime introspection.

## Configuration
- app.ai.provider-type: openai or ollama.
- app.ai.model: active model identifier (for example, llama3.1:8b).
- tools.allowlist: names of allowed tools.

## Tooling
The gateway can call the runbook search tool to ground answers in documentation. Tool citations are surfaced in the response evidence list.

