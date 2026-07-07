---
name: skill-creator
description: "Trigger: create skill, new skill, skill for project. Create project-specific skills following LLM-first style guide and AlphaKids conventions."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- User asks to create a new skill for the project
- A reusable pattern emerges that AI needs guidance on
- Project-specific conventions differ from generic best practices

Do NOT create a skill when the pattern is trivial, one-off, or already covered by an existing skill.

## Hard Rules

- Read `references/project-context.md` before creating any skill.
- Read `references/skill-style-guide.md` for formatting and structure rules.
- Check existing skills in `.claude/skills/` to avoid duplicates.
- A skill is a runtime instruction contract for an LLM, not human documentation.
- `description` MUST be one line, quoted, YAML-safe, trigger words first, <=250 chars.
- Frontmatter MUST include `name`, `description`, `license`, `metadata.author`, `metadata.version`.
- Body target: 180–450 tokens, recommended max 700, hard max 1000.
- Move examples, schemas, edge cases to `assets/` or `references/`.
- Use imperative instructions, not tutorials.
- Do NOT add a `Keywords` section.
- All skill content (body, references, assets) MUST be in English.

## Decision Gates

| Need | Action |
|---|---|
| Code templates, schemas, fixtures | Put in `assets/` |
| Conceptual detail, edge cases | Put in `references/` |
| Long explanation in `SKILL.md` | Move to a supporting file |
| Multiple meaningful paths | Add a compact decision table |
| Skill already exists | Update existing skill instead |

## Execution Steps

1. Read `references/project-context.md` for stack and module conventions.
2. Read `references/skill-style-guide.md` for structure and formatting rules.
3. Confirm the skill does not already exist in `.claude/skills/`.
4. Create directory: `.claude/skills/{skill-name}/`
5. Create `SKILL.md` with sections in this order:
   - Frontmatter (name, description, license, metadata)
   - Activation Contract
   - Hard Rules
   - Decision Gates
   - Execution Steps
   - Output Contract
   - References
6. Add supporting files in `assets/` or `references/` as needed.
7. Commit the skill with message: `feat: add {skill-name} skill`

## Output Contract

Return:
- Files created or modified.
- Whether the style guide was followed.
- Any supporting files added under `assets/` or `references/`.

## References

- `references/project-context.md` — AlphaKids stack, module structure, and conventions.
- `references/skill-style-guide.md` — LLM-first skill formatting and quality rules.
