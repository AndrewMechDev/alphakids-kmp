# LLM-first Skill Style Guide

A skill is a **runtime instruction contract for an LLM**, not human-facing documentation.

## Required Structure

Every `SKILL.md` MUST use this order:

1. **Frontmatter** — metadata for discovery.
2. **Activation Contract** — when to load the skill.
3. **Hard Rules** — constraints the LLM MUST NOT violate.
4. **Decision Gates** — tables or bullets for branching choices.
5. **Execution Steps** — ordered workflow.
6. **Output Contract** — required artifacts or format.
7. **References** — local files only.

## Frontmatter Rules

- `description` MUST be one physical line, YAML-safe, quoted.
- Trigger words first: `"Trigger: ... . {What the skill does}."`
- `description` SHOULD be <=160 chars, MUST be <=250 chars.
- Required fields: `name`, `description`, `license`, `metadata.author`, `metadata.version`.

## Body Budget

- Target: **180–450 tokens**.
- Recommended max: **700 tokens**.
- Hard max: **1000 tokens**.
- Move examples, schemas, background into `assets/` or `references/`.

## Writing Rules

### DO

- Write imperative runtime instructions.
- Lead with activation trigger and hard constraints.
- Use compact tables for decision gates.
- Keep examples minimal and executable.
- Link to local supporting files.

### DON'T

- Explain history, motivation, or tutorial background.
- Duplicate long docs inside the skill.
- Add generic advice the LLM cannot execute.
- Use external URLs as primary references.
- Hide critical rules below examples.

## Directory Structure

```
.claude/skills/{skill-name}/
├── SKILL.md              # Required
├── assets/               # Optional — templates, schemas, examples
└── references/           # Optional — local docs, edge cases
```

## Quality Gates

- Frontmatter is complete, quoted, single-line, trigger-preserving.
- Required sections exist in expected order.
- Hard rules are testable or observable.
- Decision gates cover meaningful forks only.
- Output contract tells the LLM exactly what to return.
- References point to local files only.
