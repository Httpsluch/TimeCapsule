@AGENTS.md

# Time Capsule — notes for Claude Code

One Stonecraft (Stonecutter + Architectury Loom) workspace builds 14 jars: Fabric and Forge on 1.20.1, and Fabric, Forge and NeoForge on 1.21.1, 26.1.2, 26.2 and 26.3. The README's Targets table is the source of truth; per-version loader pins and declared Minecraft ranges live in `versions/dependencies/<minecraft>.properties`.

## Build and run

- All targets: `./gradlew chiseledBuild`. One target: `./gradlew :26.3-fabric:build`. Jars land in `versions/<target>/build/libs/`.
- Dev client (no Minecraft account needed): `./gradlew :1.21.1-fabric:runClient`.
- `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 25. 26.x needs it, and Gradle picks the installed Temurin 25 even when `JAVA_HOME` points elsewhere.
- The first build of a new target downloads and decompiles Minecraft (several minutes). Later builds take about 1–2 minutes.

## Writing version-specific code

- Shared source in `src/main/java` uses Stonecutter comments: `//? if fabric {`, `//? if forge {`, `//? if neoforge {`, `//? if >=26.1 {`.
- Never hand-edit the `stonecutter active` line in `stonecutter.gradle.kts`; switch versions with Stonecutter's tasks.
- 26.1+ is unobfuscated: names follow Mojang mappings everywhere. Two access wideners split by era sit at the resources root; see the README.

## Where things live

- `TimeCapsuleContent` registers everything through a per-loader `platform/*Registrar`; `TimeCapsule` is each loader's entrypoint.
- `block/CapsuleBlockEntity` holds the items, owner and timer; `menu/CapsuleMenu` and `client/CapsuleScreen` are the setup screen.
- Breaking keeps everything (shulker-style): `CapsuleBlock#getDrops` returns `CapsuleBlockEntity#toItem()`, which stores the block entity data on the item; vanilla `BlockItem` restores it on placement. `block/CapsuleItem` shows the open date/time in its tooltip; in-game time comes from `TimeCapsule.clientWorldTime`, which the client entrypoint sets. Screen buttons use the vanilla menu-button packet (ids in `CapsuleBlockEntity.BUTTON_*`).
- Data files (recipe, loot table, tags) live in `src/main/data-eras/<era>` because their format changed in 1.21 and 1.21.2. Assets in `src/main/resources` are shared.
- Settings: `config/timecapsule.properties`, read by `TimeCapsuleConfig` at startup.

## Design rules (the mod must run well on low-end PCs)

- No per-tick work: block entities must not tick. Check the unlock time only when a player interacts.
- Prefer data-driven content (JSON, tags) and plain JSON block and item models over custom renderers.
- Every visual or optional feature gets a config toggle.
