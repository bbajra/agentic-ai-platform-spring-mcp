
---

# Sequence Diagram (docs/sequence-diagram.md)

```markdown
## Agent Execution Flow

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant Agent as AI Gateway
    participant Tool as MCP Tool Server
    participant DB

    User->>UI: Ask Question
    UI->>Agent: POST /api/chat
    Agent->>Agent: Reason + Plan
    Agent->>Tool: MCP Tool Call
    Tool->>DB: Query / Validate
    DB-->>Tool: Result
    Tool-->>Agent: Structured Output
    Agent->>UI: Final Answer + Tool Trace
