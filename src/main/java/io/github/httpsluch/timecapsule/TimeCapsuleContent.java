package io.github.httpsluch.timecapsule;

import io.github.httpsluch.timecapsule.block.CapsuleBlock;
import io.github.httpsluch.timecapsule.block.CapsuleBlockEntity;
import io.github.httpsluch.timecapsule.block.CapsuleItem;
import io.github.httpsluch.timecapsule.menu.CapsuleMenu;
import io.github.httpsluch.timecapsule.platform.Registrar;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Everything the mod registers. Each loader supplies its own {@link Registrar}. */
public final class TimeCapsuleContent {
    public static Supplier<CapsuleBlock> BLOCK;
    public static Supplier<BlockItem> ITEM;
    public static Supplier<BlockEntityType<CapsuleBlockEntity>> BLOCK_ENTITY;
    public static Supplier<MenuType<CapsuleMenu>> MENU;

    private TimeCapsuleContent() {
    }

    public static void register(Registrar registrar) {
        BLOCK = registrar.register(BuiltInRegistries.BLOCK, "time_capsule", key -> new CapsuleBlock(blockProperties(key)));
        ITEM = registrar.register(BuiltInRegistries.ITEM, "time_capsule", key -> new CapsuleItem(BLOCK.get(), itemProperties(key)));
        BLOCK_ENTITY = registrar.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "time_capsule", key -> blockEntityType());
        MENU = registrar.register(BuiltInRegistries.MENU, "time_capsule", key -> new MenuType<CapsuleMenu>(CapsuleMenu::new, FeatureFlags.VANILLA_SET));
    }

    private static BlockBehaviour.Properties blockProperties(ResourceKey<Block> key) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5F, 1200.0F)
            .sound(SoundType.WOOD)
            .noOcclusion();
        //? if >=26.1 {
        properties = properties.setId(key);
        //?}
        return properties;
    }

    private static Item.Properties itemProperties(ResourceKey<Item> key) {
        Item.Properties properties = new Item.Properties();
        //? if >=26.1 {
        properties = properties.setId(key).useBlockDescriptionPrefix();
        //?}
        return properties;
    }

    private static BlockEntityType<CapsuleBlockEntity> blockEntityType() {
        //? if >=26.1 {
        return new BlockEntityType<>(CapsuleBlockEntity::new, java.util.Set.of(BLOCK.get()));
        //?} else {
        /*return BlockEntityType.Builder.of(CapsuleBlockEntity::new, BLOCK.get()).build(null);
        *///?}
    }
}
