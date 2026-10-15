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

// Data pack formats changed in 1.21 (singular folder names) and 1.21.2 (string recipe ingredients),
// so each era ships its own copy from src/main/data-eras/<era>.
val dataEra =
    when {
        stonecutter.current.parsed >= "1.21.2" -> "1.21.2"
        stonecutter.current.parsed >= "1.21" -> "1.21"
        else -> "1.20"
    }
extensions.getByType<SourceSetContainer>().named("main") {
    resources.srcDir(rootProject.file("src/main/data-eras/$dataEra"))
}

// Ship only the access widener for this jar's era.
tasks.named<ProcessResources>("processResources") {
    exclude(
        if (stonecutter.current.parsed >= "26.1") {
            "timecapsule-obfuscated.accesswidener"
        } else {
            "timecapsule-unobfuscated.accesswidener"
        },
    )
}

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
            // Only the Forge jars ship pack.mcmeta; its format fields changed in 1.20.2 and again in 1.21.9.
            "packMcmetaFormats" to project.mod.prop("pack_mcmeta_formats"),
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
        // The block and its screen must exist on both sides.
        environment.set(CLIENT_AND_SERVER)
    }

    curseforge {
        client = true
        server = true
        if (mod.isFabric) requires("fabric-api")
    }
}

