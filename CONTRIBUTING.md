# Contributing to Mansion Mystery: The Last Secret

Thank you for contributing to **Mansion Mystery: The Last Secret**!

## Development Workflow

1. Fork the repository and create a feature branch from `main` (`feat/<short-name>` or `fix/<short-name>`).
2. Keep changes modular and adhere to Material 3 + Jetpack Compose best practices.
3. Never commit personal keystores (`.jks`, `.keystore`), `.env`, or `local.properties`.
4. Verify your changes locally before opening a pull request:
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:assembleDebug
   ```
5. Open a Pull Request using the repository PR template and describe how you tested your changes.
