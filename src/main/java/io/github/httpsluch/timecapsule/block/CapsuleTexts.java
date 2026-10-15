package io.github.httpsluch.timecapsule.block;

import net.minecraft.network.chat.Component;

/** Player-facing wording shared by the block, its messages and the setup screen. */
public final class CapsuleTexts {
    private CapsuleTexts() {
    }

    public static Component mode(int mode) {
        return Component.translatable(mode == CapsuleBlockEntity.MODE_IN_GAME
            ? "gui.timecapsule.mode.in_game"
            : "gui.timecapsule.mode.real_world");
    }

    public static Component days(int days) {
        return days == 1
            ? Component.translatable("gui.timecapsule.days.one")
            : Component.translatable("gui.timecapsule.days.many", days);
    }

    public static Component timeLeft(long days, long hours, long minutes) {
        if (days == 0 && hours == 0 && minutes == 0) {
            return Component.translatable("time.timecapsule.under_minute");
        }
        return Component.translatable("time.timecapsule.dhm", days, hours, minutes);
    }
}
