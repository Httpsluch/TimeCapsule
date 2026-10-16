package io.github.httpsluch.timecapsule.client;

import io.github.httpsluch.timecapsule.TimeCapsule;
import io.github.httpsluch.timecapsule.TimeCapsuleContent;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//?}
//? if forge {
/*import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
*///?}
//? if neoforge {
/*import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
*///?}

/** Client-only setup: connects the capsule menu to its screen and the capsule to its lid renderer. */
public class TimeCapsuleClient /*? if fabric {*/ implements ClientModInitializer /*?}*/ {
    //? if fabric {
    @Override
    public void onInitializeClient() {
        TimeCapsule.clientWorldTime = TimeCapsuleClient::worldTime;
        MenuScreens.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new);
        BlockEntityRenderers.register(TimeCapsuleContent.BLOCK_ENTITY.get(), CapsuleLidRenderer::new);
    }
    //?}

    //? if forgeLike {
    /*public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TimeCapsuleContent.BLOCK_ENTITY.get(), CapsuleLidRenderer::new);
    }
    *///?}

    //? if forge {
    /*public static void onClientSetup(FMLClientSetupEvent event) {
        TimeCapsule.clientWorldTime = TimeCapsuleClient::worldTime;
        event.enqueueWork(() -> MenuScreens.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new));
    }
    *///?}

    //? if neoforge {
    /*public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        TimeCapsule.clientWorldTime = TimeCapsuleClient::worldTime;
        event.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new);
    }
    *///?}

    private static long worldTime() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            return Long.MIN_VALUE;
        }
        //? if >=26.1 {
        return level.getOverworldClockTime();
        //?} else {
        /*return level.getDayTime();
        *///?}
    }
}
