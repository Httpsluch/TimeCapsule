import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

// 26.1+ ships unobfuscated, so its access widener uses "official" names; older versions use "named".
// Both files stay at the resources root: Forge's AW-to-AT conversion only finds them there.
val awFile =
    project.rootProject.layout.projectDirectory.file(
        if (stonecutter.current.parsed >= "26.1") {
            "src/main/resources/timecapsule-unobfuscated.accesswidener"
        } else {
            "src/main/resources/timecapsule-obfuscated.accesswidener"
        },
    )

modSettings {
    accessWidenerLocation = awFile

    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    variableReplacements =
        mapOf(
            "minecraftVersionVirtual" to stonecutter.current.version,
            "fabricMinecraftRange" to project.mod.prop("fabric_mc_range"),
            "forgeMinecraftRange" to project.mod.prop("forge_mc_range"),
            "forgeLoaderVersion" to
                if (project.mod.isForge) {
                    project.mod
                        .prop("forge_version")
                        .substringAfter("-")
                        .substringBefore(".")
                } else {
                    ""
                },
        )
}


publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
        environment.set(CLIENT_OR_SERVER_PREFERS_BOTH)
    }

    curseforge {
        client = true
        server = true
        if (mod.isFabric) requires("fabric-api")
    }
}

