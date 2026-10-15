package io.github.httpsluch.timecapsule.platform;

import java.nio.file.Path;

public final class Platform {
    private Platform() {
    }

    public static Path configDir() {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?} elif forge {
        /*return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?} else {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?}
    }
}
