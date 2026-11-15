# Security Policy

## Supported Versions

| Version | Supported |
| ------- | --------- |
| 1.0.x   | Yes       |

## Reporting a Vulnerability

If you discover a security vulnerability, save-data tampering issue, or accidental credential exposure in **Mansion Mystery: The Last Secret**, please report it privately via GitHub Security Advisories on `12av21/mansion-mystery` rather than opening a public issue.

Please include:
- A clear description of the issue and affected component.
- Steps to reproduce or verify the problem.
- Any relevant Android version or device details.

## Secret & Signing Key Hygiene

- **Never commit** `.jks`, `.keystore`, `*.keystore.base64`, `.env`, `local.properties`, or `google-services.json` files.
- Release signing in CI uses encrypted **GitHub Actions Secrets** (`ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) decoded only in ephemeral runner memory and never printed to workflow logs.
- Rotate any keystore or API secret immediately if accidentally committed to git history.
