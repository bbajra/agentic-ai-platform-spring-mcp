
---

# Architecture Diagram (docs/architecture-diagram.md)

```markdown
## System Architecture (Mermaid)

```mermaid
flowchart LR
    UI[Next.js Web Client]

    AG[AI Gateway Service<br/>Spring Boot + Spring AI]

    UI -->|Chat Requests| AG

    AG -->|MCP| RB[MCP Runbook Server]
    AG -->|MCP| SQL[MCP SQL Server]
    AG -->|MCP| FHIR[MCP FHIR Server]

    SQL --> DB[(PostgreSQL)]
    FHIR --> HAPI[HAPI FHIR Engine]

    AG --> OBS[Observability<br/>OpenTelemetry]
