package io.github.httpsluch.timecapsule.client;

import io.github.httpsluch.timecapsule.block.CapsuleBlockEntity;
import io.github.httpsluch.timecapsule.block.CapsuleTexts;
import io.github.httpsluch.timecapsule.menu.CapsuleMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Setup and contents screen. Drawn with plain filled rectangles instead of a texture, which keeps
 * it identical across versions and cheap to render.
 */
public class CapsuleScreen extends AbstractContainerScreen<CapsuleMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = CapsuleMenu.INVENTORY_Y + 82;

    private static final int BORDER = 0xFF000000;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SLOT_EDGE = 0xFF373737;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int TEXT = 0xFF404040;

    private final List<Button> controls = new ArrayList<>();
    private Button modeButton;

    public CapsuleScreen(CapsuleMenu menu, Inventory inventory, Component title) {
        //? if >=26.1 {
        super(menu, inventory, title, WIDTH, HEIGHT);
        //?} else {
        /*super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        *///?}
        this.inventoryLabelY = CapsuleMenu.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        controls.clear();
        int x = leftPos + 8;
        modeButton = addControl(x, 42, 160, Component.empty(), CapsuleBlockEntity.BUTTON_TOGGLE_MODE);
        addControl(x, 66, 24, Component.literal("<<"), CapsuleBlockEntity.BUTTON_DAYS_MINUS_10);
        addControl(x + 26, 66, 20, Component.literal("<"), CapsuleBlockEntity.BUTTON_DAYS_MINUS_1);
        addControl(x + 114, 66, 20, Component.literal(">"), CapsuleBlockEntity.BUTTON_DAYS_PLUS_1);
        addControl(x + 136, 66, 24, Component.literal(">>"), CapsuleBlockEntity.BUTTON_DAYS_PLUS_10);
        addControl(x, 90, 160, Component.translatable("gui.timecapsule.seal"), CapsuleBlockEntity.BUTTON_SEAL);
        updateControls();
    }

    private Button addControl(int x, int y, int width, Component label, int buttonId) {
        Button button = Button.builder(label, b -> press(buttonId)).bounds(x, topPos + y, width, 20).build();
        controls.add(button);
        return addRenderableWidget(button);
    }

    private void press(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private boolean editing() {
        return menu.canEdit() && menu.isSetup();
    }

    private void updateControls() {
        boolean editing = editing();
        for (Button button : controls) {
            button.visible = editing;
        }
        if (modeButton != null) {
            modeButton.setMessage(Component.translatable("gui.timecapsule.mode", CapsuleTexts.mode(menu.getMode())));
        }
    }

    /** Panel and slot frames, in screen coordinates. */
    private void drawBackground(Painter painter) {
        updateControls();
        int x = leftPos;
        int y = topPos;
        painter.fill(x, y, x + WIDTH, y + HEIGHT, BORDER);
        painter.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, HIGHLIGHT);
        painter.fill(x + 2, y + 2, x + WIDTH - 1, y + HEIGHT - 1, SHADOW);
        painter.fill(x + 2, y + 2, x + WIDTH - 2, y + HEIGHT - 2, PANEL);
        for (Slot slot : menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            painter.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_EDGE);
            painter.fill(sx, sy, sx + 17, sy + 17, HIGHLIGHT);
            painter.fill(sx, sy, sx + 16, sy + 16, SLOT);
        }
    }

    /** Text below the capsule slots, in coordinates relative to the panel. */
    private void drawStatus(Painter painter) {
        if (editing()) {
            Component days = CapsuleTexts.days(menu.getDays());
            painter.text(days, WIDTH / 2 - font.width(days) / 2, 72, TEXT);
            return;
        }
        Component headline = switch (menu.getState()) {
            case CapsuleBlockEntity.STATE_OPENED -> Component.translatable("gui.timecapsule.opened");
            case CapsuleBlockEntity.STATE_SEALED -> Component.translatable("gui.timecapsule.sealed");
            default -> Component.translatable("gui.timecapsule.waiting");
        };
        painter.text(headline, 8, 46, TEXT);
        painter.text(Component.translatable("gui.timecapsule.sealed_for", CapsuleTexts.days(menu.getDays())), 8, 60, TEXT);
        painter.text(Component.translatable("gui.timecapsule.mode", CapsuleTexts.mode(menu.getMode())), 8, 72, TEXT);
    }

    /** The two drawing calls this screen needs, so the layout code is the same on every version. */
    private interface Painter {
        void fill(int x1, int y1, int x2, int y2, int color);

        void text(Component text, int x, int y, int color);
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawBackground(painter(graphics));
    }

    @Override
    protected void extractLabels(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        drawStatus(painter(graphics));
    }

    private Painter painter(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        return new Painter() {
            @Override
            public void fill(int x1, int y1, int x2, int y2, int color) {
                graphics.fill(x1, y1, x2, y2, color);
            }

            @Override
            public void text(Component text, int x, int y, int color) {
                graphics.text(font, text, x, y, color, false);
            }
        };
    }
    //?}

    //? if <26.1 {
    /*@Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawBackground(painter(graphics));
    }

    @Override
    protected void renderLabels(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawStatus(painter(graphics));
    }

    private Painter painter(net.minecraft.client.gui.GuiGraphics graphics) {
        return new Painter() {
            @Override
            public void fill(int x1, int y1, int x2, int y2, int color) {
                graphics.fill(x1, y1, x2, y2, color);
            }

            @Override
            public void text(Component text, int x, int y, int color) {
                graphics.drawString(font, text, x, y, color, false);
            }
        };
    }
    *///?}

    //? if >=1.21 && <26.1 {
    /*@Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    *///?}

    //? if <1.21 {
    /*@Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    *///?}
}
