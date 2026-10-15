package io.github.httpsluch.timecapsule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*//*?}*/

/*? if neoforge {*/
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
*//*?}*/

/*? if neoforge {*/
/*@Mod(TimeCapsule.MOD_ID)
*//*?}*/
/*? if forge {*/
/*@Mod(TimeCapsule.MOD_ID)
*//*?}*/
public class TimeCapsule /*? if fabric {*/ implements ModInitializer /*?}*/ {
    public static final String MOD_ID = "timecapsule";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /*? if forge {*/
    /*public TimeCapsule(FMLJavaModLoadingContext context) {
        LOGGER.info("Hello Forge world!");
    }
    *//*?}*/

    /*? if neoforge {*/
    /*public TimeCapsule(IEventBus modEventBus) {
        LOGGER.info("Hello NeoForge world!");
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
    /*?}*/
}
