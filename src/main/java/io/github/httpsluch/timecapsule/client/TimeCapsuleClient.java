package io.github.httpsluch.timecapsule.client;

import io.github.httpsluch.timecapsule.TimeCapsuleContent;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
//?}
//? if forge {
/*import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
*///?}
//? if neoforge {
/*import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
*///?}

/** Client-only setup: connects the capsule menu to its screen. */
public class TimeCapsuleClient /*? if fabric {*/ implements ClientModInitializer /*?}*/ {
    //? if fabric {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new);
    }
    //?}

    //? if forge {
    /*public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new));
    }
    *///?}

    //? if neoforge {
    /*public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(TimeCapsuleContent.MENU.get(), CapsuleScreen::new);
    }
    *///?}
}
