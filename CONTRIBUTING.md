# Contributing to Time Capsule

Thanks for helping! Bug reports, ideas, translations and code are all welcome. Please follow the [code of conduct](CODE_OF_CONDUCT.md).

## Report a bug or suggest an idea

Open an [issue](https://github.com/Httpsluch/TimeCapsule/issues/new/choose) and pick a template. If you found a way to open capsules early or duplicate items on a server, report it privately instead; see the [security policy](SECURITY.md).

## Translate the mod

1. Copy `src/main/resources/assets/timecapsule/lang/en_us.json` to your language's file, for example `de_de.json` or `pt_br.json`.
2. Translate the values, not the keys, and keep every `%s` placeholder.
3. Open a pull request.

## Work on the code

### What you need

- JDK 25. The Gradle wrapper downloads the right Gradle version.
- Optionally IntelliJ IDEA Community. Open the folder as a Gradle project.

### Build and run

```shell
./gradlew chiseledBuild
./gradlew :26.3-fabric:build
./gradlew :1.21.1-fabric:runClient
```

The first builds all 14 jars into `versions/<target>/build/libs`, the second builds one target, and the third starts a dev game (no Minecraft account needed). The first build of a target downloads and decompiles Minecraft, which takes several minutes.

### How the code is organized

One source tree builds every Minecraft version and loader through [Stonecutter](https://stonecutter.kikugie.dev/wiki/). Lines that differ between versions or loaders sit in comments such as `//? if fabric {` or `//? if >=26.1 {`. Don't hand-edit the `stonecutter active` line in `stonecutter.gradle.kts`.

- `TimeCapsuleContent` registers everything, through a registrar per loader in `platform/`.
- `block/` holds the capsule block, its block entity (items, owner and timer) and its item.
- `menu/` and `client/` hold the setup screen and the lid renderer.
- `src/main/data-eras/` holds the recipe, loot table and tags, split by data format.

### Design rules

The mod must stay light on low-end PCs.

- No per-tick work: block entities don't tick, and timers are checked only when a player interacts.
- Prefer JSON models and data files over custom rendering.
- Give every visual or optional feature a setting in `TimeCapsuleConfig`.

### Pull requests

1. Fork the repository and branch from `main`.
2. Keep each pull request to one change. Write commit messages as one short plain sentence, like "Add a comparator output".
3. Make sure `./gradlew chiseledBuild` passes. If you touched version-specific code, test in game on one older version (1.20.1 or 1.21.1) and one 26.x version.
4. Add a line to `CHANGELOG.md` for anything players will notice.
5. Fill in the pull request template, including whether AI tools wrote any part of the change. That's allowed, but the project has to disclose it on Modrinth.

By contributing, you agree that your work is released under the [MIT License](LICENSE).
