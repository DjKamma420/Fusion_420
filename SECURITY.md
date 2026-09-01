# Security

## Supported Versions

| Version | Security updates |
|---|---|
| `main` | Yes |
| Released versions | According to the current release policy |

## Reporting a Security Issue

Do not publish sensitive security issues in a public GitHub issue.
Use GitHub's private vulnerability reporting feature when it is enabled for this repository.

Fusion 420 does not require Minecraft login credentials, session tokens, passwords, or private API keys.

## Security Principles

- No credentials in the repository.
- No intentional storage of personal data in source code, configuration, or logs.
- External requests use timeouts and response validation.
- Incoming recipe data is validated before use.
- Release artifacts are built and validated before publication.
- User-facing documentation and security guidance are kept in English.
