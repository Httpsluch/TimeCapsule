# Time Capsule

Seal items and a note in a capsule that only opens after a set time.

This project uses [Stonecraft](https://stonecraft.meza.gg) to build for Fabric, Forge, and NeoForge. It includes:

- a shared mod entrypoint across the selected loaders;

- an access widener that Stonecraft converts for loaders that require it;

- artifact publishing for Modrinth and CurseForge;




## Targets

| Minecraft | Fabric | Forge | NeoForge |
| --- | --- | --- | --- |
| 1.20.1 | yes | yes | (Forge jar) |
| 1.21.1 | yes | yes | yes |
| 26.1.2 | yes | yes | yes |
| 26.2 | yes | yes | yes |
| 26.3 | yes | yes | yes |

Loader and API versions live in `versions/dependencies/<minecraft>.properties`, together with the Minecraft range each jar declares.

## Requirements

- JDK 25


The Gradle wrapper downloads the required Gradle distribution automatically.

## Build and verify

Run the available commands from the project root:

```shell
./gradlew buildAndCollect

```

`buildAndCollect` builds every configured Minecraft-version and loader pair, then collects the resulting JARs under `build/libs`.



## Publishing configuration

Stonecraft's `publishMods` task performs network publication only when the `DO_PUBLISH` environment variable is `true`. Otherwise it prints the publication payload as a dry run. Modrinth and CurseForge are configured independently: a platform is skipped unless every required value for it is present, and an incomplete platform does not prevent publishing to the other one. Set the variables for the platform you want to publish to in the shell that runs Gradle, and treat both token values as secrets.

```shell
./gradlew publishMods
```

| Name | Purpose |
| --- | --- |
| `DO_PUBLISH` | Enables network publication when set to `true` |
| `RELEASE_TYPE` | Selects `stable`, `beta`, or `alpha` publication metadata |
| `MODRINTH_ID` | Modrinth project ID |
| `MODRINTH_TOKEN` | Modrinth API token |
| `CURSEFORGE_ID` | CurseForge project ID |
| `CURSEFORGE_SLUG` | CurseForge project slug |
| `CURSEFORGE_TOKEN` | CurseForge API token |

For example, this PowerShell session publishes a stable release to Modrinth:

```powershell
$env:DO_PUBLISH = "true"
$env:RELEASE_TYPE = "stable"
$env:MODRINTH_ID = "your-project-id"
$env:MODRINTH_TOKEN = "your-api-token"
./gradlew publishMods
```

Use `CURSEFORGE_ID`, `CURSEFORGE_SLUG`, and `CURSEFORGE_TOKEN` instead to publish only to CurseForge, or set both complete platform groups to publish to both.




## Access widening

Access wideners come in two files because 26.1+ ships unobfuscated:

- `src/main/resources/timecapsule-obfuscated.accesswidener` for 1.20.1 and 1.21.1 (`named` namespace)
- `src/main/resources/timecapsule-unobfuscated.accesswidener` for 26.x (`official` namespace)

Keep both at the resources root: Forge's conversion to access transformers only finds them there.


