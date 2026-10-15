package io.github.httpsluch.timecapsule.menu;

import io.github.httpsluch.timecapsule.TimeCapsuleContent;
import io.github.httpsluch.timecapsule.block.CapsuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The capsule's screen: 9 capsule slots, the player's inventory, and the setup controls.
 * Buttons travel over the vanilla menu-button packet, so no loader networking is needed.
 */
public class CapsuleMenu extends AbstractContainerMenu {
    public static final int CAPSULE_SLOTS_Y = 20;
    public static final int INVENTORY_Y = 126;

    private final ContainerData data;
    private final CapsuleBlockEntity capsule; // null on the client

    /** Client-side constructor; the server fills in the real values. */
    public CapsuleMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CapsuleBlockEntity.SIZE),
            new SimpleContainerData(CapsuleBlockEntity.DATA_COUNT), null);
    }

    public CapsuleMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, CapsuleBlockEntity capsule) {
        super(TimeCapsuleContent.MENU.get(), containerId);
        this.data = data;
        this.capsule = capsule;

        for (int i = 0; i < CapsuleBlockEntity.SIZE; i++) {
            addSlot(new CapsuleSlot(container, i, 8 + i * 18, CAPSULE_SLOTS_Y));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, INVENTORY_Y + 58));
        }
        addDataSlots(data);
        if (capsule != null) {
            capsule.startOpen(playerInventory.player);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (capsule != null) {
            capsule.stopOpen(player);
        }
    }

    public int getState() {
        return data.get(CapsuleBlockEntity.DATA_STATE);
    }

    public int getMode() {
        return data.get(CapsuleBlockEntity.DATA_MODE);
    }

    public int getDays() {
        return data.get(CapsuleBlockEntity.DATA_DAYS);
    }

    public boolean canEdit() {
        return data.get(CapsuleBlockEntity.DATA_CAN_EDIT) == 1;
    }

    public boolean isSetup() {
        return getState() == CapsuleBlockEntity.STATE_SETUP;
    }

    /** Items can move in and out unless the capsule is sealed. */
    public boolean isUnlocked() {
        return getState() != CapsuleBlockEntity.STATE_SEALED;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return capsule != null && player instanceof ServerPlayer serverPlayer && capsule.handleButton(serverPlayer, id);
    }

    @Override
    public boolean stillValid(Player player) {
        if (capsule == null) {
            return true;
        }
        BlockPos pos = capsule.getBlockPos();
        return !capsule.isRemoved() && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !isUnlocked()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int capsuleEnd = CapsuleBlockEntity.SIZE;
        if (index < capsuleEnd) {
            if (!moveItemStackTo(stack, capsuleEnd, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, capsuleEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private final class CapsuleSlot extends Slot {
        CapsuleSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        /** Capsules can't go inside capsules, which would allow endless nesting (as with shulker boxes). */
        @Override
        public boolean mayPlace(ItemStack stack) {
            return isUnlocked() && !(stack.getItem() instanceof io.github.httpsluch.timecapsule.block.CapsuleItem);
        }

        @Override
        public boolean mayPickup(Player player) {
            return isUnlocked();
        }
    }
}
