package io.github.httpsluch.timecapsule.block;

import io.github.httpsluch.timecapsule.TimeCapsule;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * The capsule as an item. A broken capsule keeps its contents, owner and timer in the item (like a
 * shulker box), and the tooltip says when it opens without revealing what's inside.
 */
public class CapsuleItem extends BlockItem {
    private static final DateTimeFormatter OPENS_AT =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    public CapsuleItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    //? if >=26.1 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        describe(stack, tooltip);
    }
    //?} elif >=1.21 {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        describe(stack, tooltip::add);
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        describe(stack, tooltip::add);
    }
    *///?}

    private static void describe(ItemStack stack, Consumer<Component> tooltip) {
        CompoundTag tag = capsuleData(stack);
        if (tag == null) {
            return;
        }
        int state = readInt(tag, "State", CapsuleBlockEntity.STATE_SETUP);
        String owner = readString(tag, "OwnerName");
        if (state == CapsuleBlockEntity.STATE_SETUP) {
            tooltip.accept(Component.translatable("tooltip.timecapsule.not_sealed").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!owner.isEmpty()) {
            tooltip.accept(Component.translatable("tooltip.timecapsule.sealed_by", owner).withStyle(ChatFormatting.GRAY));
        }
        if (state == CapsuleBlockEntity.STATE_OPENED) {
            tooltip.accept(Component.translatable("tooltip.timecapsule.opened").withStyle(ChatFormatting.GRAY));
            return;
        }

        int mode = readInt(tag, "Mode", CapsuleBlockEntity.MODE_REAL_WORLD);
        long sealedAt = readLong(tag, "SealedAt");
        long unlockAt = readLong(tag, "UnlockAt");
        if (mode == CapsuleBlockEntity.MODE_REAL_WORLD) {
            if (System.currentTimeMillis() >= unlockAt) {
                tooltip.accept(Component.translatable("tooltip.timecapsule.ready").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.accept(Component.translatable("tooltip.timecapsule.opens_at", OPENS_AT.format(Instant.ofEpochMilli(unlockAt))).withStyle(ChatFormatting.GOLD));
            }
        } else {
            long now = TimeCapsule.clientWorldTime == null ? Long.MIN_VALUE : TimeCapsule.clientWorldTime.getAsLong();
            if (now == Long.MIN_VALUE) {
                tooltip.accept(Component.translatable("tooltip.timecapsule.opens_after", CapsuleTexts.days(readInt(tag, "Days", 1))).withStyle(ChatFormatting.GOLD));
            } else {
                long left = Math.max(0L, unlockAt - Math.max(now, sealedAt));
                if (left == 0) {
                    tooltip.accept(Component.translatable("tooltip.timecapsule.ready").withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.accept(Component.translatable("tooltip.timecapsule.opens_in_game",
                        CapsuleTexts.timeLeft(left / 24_000L, left % 24_000L / 1_000L, left % 1_000L * 60 / 1_000L)).withStyle(ChatFormatting.GOLD));
                }
            }
        }
        tooltip.accept(Component.translatable("tooltip.timecapsule.secret").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /** The capsule's saved block entity data, or null for a plain (never placed or empty) capsule. */
    private static CompoundTag capsuleData(ItemStack stack) {
        //? if >=26.1 {
        var data = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? null : data.copyTagWithoutId();
        //?} elif >=1.21 {
        /*var data = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? null : data.copyTag();
        *///?} else {
        /*return BlockItem.getBlockEntityData(stack);
        *///?}
    }

    private static int readInt(CompoundTag tag, String key, int fallback) {
        //? if >=26.1 {
        return tag.getIntOr(key, fallback);
        //?} else {
        /*return tag.contains(key) ? tag.getInt(key) : fallback;
        *///?}
    }

    private static long readLong(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getLongOr(key, 0L);
        //?} else {
        /*return tag.getLong(key);
        *///?}
    }

    private static String readString(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getStringOr(key, "");
        //?} else {
        /*return tag.getString(key);
        *///?}
    }
}
