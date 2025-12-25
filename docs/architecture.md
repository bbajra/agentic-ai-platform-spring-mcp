
---

# Architecture Explanation (docs/architecture.md)

```markdown
## Design Goals
- Safety over raw LLM power
- Clear separation of concerns
- Industry-agnostic extensibility
- Enterprise observability

## Why MCP?
- Explicit contracts between agent and tools
- Prevents arbitrary code execution
- Enables independent scaling and ownership

## Why Spring AI?
- Spring-native integration
- Provider abstraction (OpenAI, Azure, Bedrock)
- Testability and DI support
