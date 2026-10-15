package io.github.httpsluch.timecapsule.block;

import io.github.httpsluch.timecapsule.TimeCapsuleConfig;
import io.github.httpsluch.timecapsule.TimeCapsuleContent;
import io.github.httpsluch.timecapsule.menu.CapsuleMenu;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds a capsule's items, owner and timer. It never ticks: the remaining time is worked out
 * only when a player interacts with the capsule or has its screen open.
 * <p>
 * Deliberately not a {@code Container}, so hoppers and pipes can't pull items out of a sealed capsule.
 */
public class CapsuleBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SIZE = 9;

    public static final int STATE_SETUP = 0;
    public static final int STATE_SEALED = 1;
    public static final int STATE_OPENED = 2;

    public static final int MODE_REAL_WORLD = 0;
    public static final int MODE_IN_GAME = 1;

    // Indices of the values synced to the setup screen.
    public static final int DATA_STATE = 0;
    public static final int DATA_MODE = 1;
    public static final int DATA_DAYS = 2;
    public static final int DATA_LEFT_DAYS = 3;
    public static final int DATA_LEFT_HOURS = 4;
    public static final int DATA_LEFT_MINUTES = 5;
    public static final int DATA_CAN_EDIT = 6;
    public static final int DATA_COUNT = 7;

    // Menu button ids, sent with the vanilla menu-button packet.
    public static final int BUTTON_TOGGLE_MODE = 0;
    public static final int BUTTON_DAYS_MINUS_10 = 1;
    public static final int BUTTON_DAYS_MINUS_1 = 2;
    public static final int BUTTON_DAYS_PLUS_1 = 3;
    public static final int BUTTON_DAYS_PLUS_10 = 4;
    public static final int BUTTON_SEAL = 5;

    private static final long MILLIS_PER_DAY = 86_400_000L;
    private static final long MILLIS_PER_HOUR = 3_600_000L;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long TICKS_PER_DAY = 24_000L;
    private static final long TICKS_PER_HOUR = 1_000L;

    private final SimpleContainer items = new SimpleContainer(SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            CapsuleBlockEntity.this.setChanged();
        }
    };
    private int state = STATE_SETUP;
    private int mode = TimeCapsuleConfig.defaultInGame ? MODE_IN_GAME : MODE_REAL_WORLD;
    private int days = TimeCapsuleConfig.defaultDays;
    /** Epoch millis for real-world capsules, overworld clock ticks for in-game ones. */
    private long sealedAt;
    private long unlockAt;
    private UUID owner;
    private String ownerName = "";
    /**
     * Players with the screen open; drives the lid and the open/close sounds. Not saved. Kept by id, not as a
     * count, so a player who switches to spectator while looking inside still closes the lid.
     */
    private final Set<UUID> viewers = new HashSet<>();

    public CapsuleBlockEntity(BlockPos pos, BlockState blockState) {
        super(TimeCapsuleContent.BLOCK_ENTITY.get(), pos, blockState);
    }

    // ---- Interaction -------------------------------------------------------------------------

    /**
     * Placing a new capsule makes the placer its owner and opens the setup screen right away.
     * A capsule placed back down from its item keeps its owner and timer; only its look is restored.
     */
    public void onPlaced(ServerPlayer player) {
        if (state != STATE_SETUP) {
            setSealedState(state == STATE_SEALED);
            return;
        }
        owner = player.getUUID();
        ownerName = player.getName().getString();
        setChanged();
        player.openMenu(this);
    }

    public void onUse(ServerPlayer player) {
        switch (state) {
            case STATE_SETUP -> {
                if (canEdit(player)) {
                    player.openMenu(this);
                } else {
                    level.playSound(null, worldPosition, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    overlay(player, Component.translatable("message.timecapsule.not_owner", ownerName));
                }
            }
            case STATE_SEALED -> {
                long[] left = remaining();
                if (left[3] > 0) {
                    level.playSound(null, worldPosition, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    String key = mode == MODE_IN_GAME ? "message.timecapsule.locked_in_game" : "message.timecapsule.locked";
                    overlay(player, Component.translatable(key, CapsuleTexts.timeLeft(left[0], left[1], left[2])));
                } else {
                    open(player);
                    player.openMenu(this);
                }
            }
            default -> player.openMenu(this);
        }
    }

    /** Handles the setup screen's buttons. Returns true when something changed. */
    public boolean handleButton(ServerPlayer player, int button) {
        if (!canEdit(player)) {
            return false;
        }
        switch (button) {
            case BUTTON_TOGGLE_MODE -> mode = mode == MODE_REAL_WORLD ? MODE_IN_GAME : MODE_REAL_WORLD;
            case BUTTON_DAYS_MINUS_10 -> days = clampDays(days - 10);
            case BUTTON_DAYS_MINUS_1 -> days = clampDays(days - 1);
            case BUTTON_DAYS_PLUS_1 -> days = clampDays(days + 1);
            case BUTTON_DAYS_PLUS_10 -> days = clampDays(days + 10);
            case BUTTON_SEAL -> seal(player);
            default -> {
                return false;
            }
        }
        setChanged();
        return true;
    }

    public boolean canEdit(Player player) {
        return state == STATE_SETUP && (owner == null || owner.equals(player.getUUID()) || player.isCreative());
    }

    private void seal(ServerPlayer player) {
        state = STATE_SEALED;
        if (mode == MODE_REAL_WORLD) {
            sealedAt = System.currentTimeMillis();
            unlockAt = sealedAt + days * MILLIS_PER_DAY;
        } else {
            sealedAt = overworldTime();
            unlockAt = sealedAt + days * TICKS_PER_DAY;
        }
        setSealedState(true);
        // A clunk and a lock click, distinct from the chest sounds.
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.7F, 0.8F);
        level.playSound(null, worldPosition, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.closeContainer();
        overlay(player, Component.translatable("message.timecapsule.sealed", CapsuleTexts.days(days), CapsuleTexts.mode(mode)));
    }

    private void open(ServerPlayer player) {
        state = STATE_OPENED;
        setSealedState(false);
        setChanged();
        level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        MinecraftServer server = level.getServer();
        if (TimeCapsuleConfig.announceOpen && server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.translatable("message.timecapsule.announce",
                player.getDisplayName(), ownerName.isEmpty() ? "?" : ownerName,
                CapsuleTexts.days(days), CapsuleTexts.mode(mode)), false);
        }
    }

    // ---- Lid ---------------------------------------------------------------------------------

    public void startOpen(Player player) {
        // Spectators look inside unseen, like with vanilla chests.
        if (level == null || isRemoved() || player.isSpectator()) {
            return;
        }
        if (viewers.add(player.getUUID()) && viewers.size() == 1) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.1F);
            moveLid();
        }
    }

    public void stopOpen(Player player) {
        // A capsule broken while open must not touch the world again: setBlock would put the block back.
        if (level == null || isRemoved()) {
            return;
        }
        if (viewers.remove(player.getUUID()) && viewers.isEmpty()) {
            if (state != STATE_SEALED) {
                level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.1F);
            }
            moveLid();
        }
    }

    public boolean isOpenByPlayers() {
        return !viewers.isEmpty();
    }

    /** Puts the lid half way and lets CapsuleBlock#tick finish the move two ticks later. */
    private void moveLid() {
        BlockState current = getBlockState();
        if (TimeCapsuleConfig.animateLid && current.hasProperty(CapsuleBlock.LID) && level.getBlockEntity(worldPosition) == this) {
            level.setBlock(worldPosition, current.setValue(CapsuleBlock.LID, 1), Block.UPDATE_ALL);
            level.scheduleTick(worldPosition, current.getBlock(), 2);
        }
    }

    private void setSealedState(boolean sealed) {
        BlockState current = getBlockState();
        if (level != null && current.hasProperty(CapsuleBlock.SEALED) && current.getValue(CapsuleBlock.SEALED) != sealed) {
            level.setBlock(worldPosition, current.setValue(CapsuleBlock.SEALED, sealed), Block.UPDATE_ALL);
        }
    }

    // ---- Time --------------------------------------------------------------------------------

    /** {days, hours, minutes, raw units left}; units are millis or ticks depending on the mode. */
    private long[] remaining() {
        if (mode == MODE_REAL_WORLD) {
            // A clock set backwards never shows more than the full timer.
            long left = Math.max(0L, unlockAt - Math.max(System.currentTimeMillis(), sealedAt));
            return new long[] {left / MILLIS_PER_DAY, left % MILLIS_PER_DAY / MILLIS_PER_HOUR, left % MILLIS_PER_HOUR / MILLIS_PER_MINUTE, left};
        }
        // /time set can move the clock backwards; never report more than the full timer.
        long now = Math.max(overworldTime(), sealedAt);
        long left = Math.max(0L, unlockAt - now);
        return new long[] {left / TICKS_PER_DAY, left % TICKS_PER_DAY / TICKS_PER_HOUR, left % TICKS_PER_HOUR * 60 / TICKS_PER_HOUR, left};
    }

    private long overworldTime() {
        MinecraftServer server = level == null ? null : level.getServer();
        if (server == null) {
            return sealedAt;
        }
        //? if >=26.1 {
        return server.overworld().getOverworldClockTime();
        //?} else {
        /*return server.overworld().getDayTime();
        *///?}
    }

    private static int clampDays(int value) {
        return Math.max(1, Math.min(TimeCapsuleConfig.maxDays, value));
    }

    // ---- Menu --------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.timecapsule.time_capsule");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CapsuleMenu(containerId, inventory, items, dataFor(player), this);
    }

    /** Values the screen shows, computed fresh each time the server syncs an open menu. */
    private ContainerData dataFor(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_STATE -> state;
                    case DATA_MODE -> mode;
                    case DATA_DAYS -> days;
                    case DATA_LEFT_DAYS -> state == STATE_SEALED ? (int) Math.min(Short.MAX_VALUE, remaining()[0]) : 0;
                    case DATA_LEFT_HOURS -> state == STATE_SEALED ? (int) remaining()[1] : 0;
                    case DATA_LEFT_MINUTES -> state == STATE_SEALED ? (int) remaining()[2] : 0;
                    case DATA_CAN_EDIT -> canEdit(player) ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    // ---- Breaking ----------------------------------------------------------------------------

    /** Whether the dropped item must carry this capsule's data (a fresh, empty capsule drops plain). */
    public boolean hasDataToKeep() {
        return state != STATE_SETUP || !items.isEmpty();
    }

    /**
     * The capsule as an item that keeps its items, owner and timer, like a shulker box.
     * A sealed capsule therefore stays sealed wherever it's carried.
     */
    public ItemStack toItem() {
        ItemStack stack = new ItemStack(TimeCapsuleContent.ITEM.get());
        if (!hasDataToKeep() || level == null) {
            return stack;
        }
        //? if >=26.1 {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput
            .createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        saveCustomOnly(output);
        net.minecraft.world.item.BlockItem.setBlockEntityData(stack, getType(), output);
        //?} elif >=1.21 {
        /*net.minecraft.world.item.BlockItem.setBlockEntityData(stack, getType(), saveCustomOnly(level.registryAccess()));
        *///?} else {
        /*net.minecraft.world.item.BlockItem.setBlockEntityData(stack, getType(), saveWithoutMetadata());
        *///?}
        return stack;
    }

    private static void overlay(ServerPlayer player, Component message) {
        //? if >=26.1 {
        player.sendOverlayMessage(message);
        //?} else {
        /*player.displayClientMessage(message, true);
        *///?}
    }

    // ---- Saving ------------------------------------------------------------------------------

    private NonNullList<ItemStack> snapshot() {
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) {
            list.set(i, items.getItem(i));
        }
        return list;
    }

    private void restore(NonNullList<ItemStack> list) {
        for (int i = 0; i < SIZE; i++) {
            items.setItem(i, list.get(i));
        }
    }

    private static UUID parseUuid(String value) {
        try {
            return value.isEmpty() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //? if >=26.1 {
    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, snapshot());
        output.putInt("State", state);
        output.putInt("Mode", mode);
        output.putInt("Days", days);
        output.putLong("SealedAt", sealedAt);
        output.putLong("UnlockAt", unlockAt);
        output.putString("Owner", owner == null ? "" : owner.toString());
        output.putString("OwnerName", ownerName);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, list);
        restore(list);
        state = input.getIntOr("State", STATE_SETUP);
        mode = input.getIntOr("Mode", MODE_REAL_WORLD);
        days = input.getIntOr("Days", TimeCapsuleConfig.defaultDays);
        sealedAt = input.getLongOr("SealedAt", 0L);
        unlockAt = input.getLongOr("UnlockAt", 0L);
        owner = parseUuid(input.getStringOr("Owner", ""));
        ownerName = input.getStringOr("OwnerName", "");
    }
    //?}

    //? if >=1.21 && <26.1 {
    /*@Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, snapshot(), registries);
        writeFields(tag);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list, registries);
        restore(list);
        readFields(tag);
    }
    *///?}

    //? if <1.21 {
    /*@Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, snapshot());
        writeFields(tag);
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list);
        restore(list);
        readFields(tag);
    }
    *///?}

    //? if <26.1 {
    /*private void writeFields(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("State", state);
        tag.putInt("Mode", mode);
        tag.putInt("Days", days);
        tag.putLong("SealedAt", sealedAt);
        tag.putLong("UnlockAt", unlockAt);
        tag.putString("Owner", owner == null ? "" : owner.toString());
        tag.putString("OwnerName", ownerName);
    }

    private void readFields(net.minecraft.nbt.CompoundTag tag) {
        state = tag.getInt("State");
        mode = tag.getInt("Mode");
        days = tag.contains("Days") ? tag.getInt("Days") : TimeCapsuleConfig.defaultDays;
        sealedAt = tag.getLong("SealedAt");
        unlockAt = tag.getLong("UnlockAt");
        owner = parseUuid(tag.getString("Owner"));
        ownerName = tag.getString("OwnerName");
    }
    *///?}
}
