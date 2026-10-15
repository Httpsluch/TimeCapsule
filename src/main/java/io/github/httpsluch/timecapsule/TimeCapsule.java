package io.github.httpsluch.timecapsule;

import io.github.httpsluch.timecapsule.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if fabric {
import io.github.httpsluch.timecapsule.platform.FabricRegistrar;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.CreativeModeTabs;
//?}

//? if forge {
/*import io.github.httpsluch.timecapsule.client.TimeCapsuleClient;
import io.github.httpsluch.timecapsule.platform.ForgeRegistrar;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
*///?}

//? if neoforge {
/*import io.github.httpsluch.timecapsule.client.TimeCapsuleClient;
import io.github.httpsluch.timecapsule.platform.NeoForgeRegistrar;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
*///?}

//? if forgeLike {
/*@Mod(TimeCapsule.MOD_ID)
*///?}
public class TimeCapsule /*? if fabric {*/ implements ModInitializer /*?}*/ {
    public static final String MOD_ID = "timecapsule";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if >=26.1 {
    public static net.minecraft.resources.Identifier id(String path) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    //?} elif >=1.21 {
    /*public static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
    *///?} else {
    /*public static net.minecraft.resources.ResourceLocation id(String path) {
        return new net.minecraft.resources.ResourceLocation(MOD_ID, path);
    }
    *///?}

    //? if fabric {
    @Override
    public void onInitialize() {
        TimeCapsuleConfig.load(Platform.configDir());
        TimeCapsuleContent.register(new FabricRegistrar());
        //? if >=26.1 {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(output -> output.accept(TimeCapsuleContent.ITEM.get()));
        //?} else {
        /*net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(entries -> entries.accept(TimeCapsuleContent.ITEM.get()));
        *///?}
    }
    //?}

    //? if forge && <26.1 {
    /*public TimeCapsule(FMLJavaModLoadingContext context) {
        TimeCapsuleConfig.load(Platform.configDir());
        ForgeRegistrar registrar = new ForgeRegistrar();
        TimeCapsuleContent.register(registrar);
        var modBus = context.getModEventBus();
        registrar.registerAll(modBus);
        modBus.addListener(TimeCapsule::onBuildCreativeTabs);
        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(TimeCapsuleClient::onClientSetup);
        }
    }
    *///?}

    //? if forge && >=26.1 {
    /*public TimeCapsule(FMLJavaModLoadingContext context) {
        TimeCapsuleConfig.load(Platform.configDir());
        ForgeRegistrar registrar = new ForgeRegistrar();
        TimeCapsuleContent.register(registrar);
        var modBus = context.getModBusGroup();
        registrar.registerAll(modBus);
        BuildCreativeModeTabContentsEvent.BUS.addListener(TimeCapsule::onBuildCreativeTabs);
        if (FMLEnvironment.dist.isClient()) {
            FMLClientSetupEvent.getBus(modBus).addListener(TimeCapsuleClient::onClientSetup);
        }
    }
    *///?}

    //? if forge {
    /*private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TimeCapsuleContent.ITEM);
        }
    }
    *///?}

    //? if neoforge {
    /*public TimeCapsule(IEventBus modEventBus) {
        TimeCapsuleConfig.load(Platform.configDir());
        NeoForgeRegistrar registrar = new NeoForgeRegistrar();
        TimeCapsuleContent.register(registrar);
        registrar.registerAll(modEventBus);
        modEventBus.addListener(TimeCapsule::onBuildCreativeTabs);
        if (isClient()) {
            modEventBus.addListener(TimeCapsuleClient::onRegisterScreens);
        }
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TimeCapsuleContent.ITEM.get());
        }
    }
    *///?}

    //? if neoforge && >=1.21.9 {
    /*private static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }
    *///?}

    //? if neoforge && <1.21.9 {
    /*private static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }
    *///?}
}
