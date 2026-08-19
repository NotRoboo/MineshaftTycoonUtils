# Contributing to MineshaftTycoon Utils

Thanks for taking the time to contribute! This document covers how to set up the project, the conventions used in the codebase, and how to submit changes.

## Getting Started

### Prerequisites

- JDK 21
- An IDE with Fabric/Loom support (IntelliJ IDEA is recommended)
- Git

### Setup

1. Fork the repository and clone your fork:
   ```
   git clone https://github.com/<your-username>/MineshaftTycoonUtils.git
   cd MineshaftTycoonUtils
   ```
2. Import the project into your IDE as a Gradle project, or run:
   ```
   ./gradlew idea
   ```
   for IntelliJ project files.
3. Build the mod locally to confirm your environment is set up correctly:
   ```
   ./gradlew build
   ```
4. Run the client with the mod loaded for manual testing:
   ```
   ./gradlew runClient
   ```

## Code Style

- Match the existing style in the file you're editing rather than introducing a new one. This codebase generally favors:
    - `private static final` fields declared at the top of a class
    - Feature classes exposing a static `init()` method registered from `MineshaftTycoonUtils.java`
    - Config options grouped into per-feature `*Category` classes under `config/`, using MoulConfig annotations
    - Mixins prefixed with `mineshaftUtils$` or `mstu$` for injected members, to avoid collisions
- Keep features client-side only; this mod has no server component.
- Avoid adding commented-out code or leftover debug logging in submitted PRs.
- Use American English spelling in identifiers, config text, and comments.

## Submitting Changes

1. Create a branch off `main` with a descriptive name, e.g. `fix/warp-helper-retry` or `feature/timers-hud-order`.
2. Make your changes, keeping commits focused and readable.
3. Run a full build before opening a PR:
   ```
   ./gradlew build
   ```
4. Open a pull request against `main` using the PR template. Link any related issue.
5. The CI workflow will automatically build the mod and run tests on your PR — make sure it passes before requesting review.

## Reporting Bugs & Requesting Features

Please use the issue templates when opening a new issue. Include your Minecraft version, mod version, and (for bugs) steps to reproduce along with any relevant logs from `.minecraft/logs/latest.log`.

## Warp Helper & Pets Helper Changes

These features automate menu clicks against the live server, so changes to `WarpHelper.java` or `PetsHelper.java` should be tested carefully in-game before submitting, since incorrect click/slot logic can misbehave on the actual server.

## Questions

If anything here is unclear, feel free to open an issue with your question or ask in your pull request description.