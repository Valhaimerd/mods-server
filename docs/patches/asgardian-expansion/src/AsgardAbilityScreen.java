package com.emandev.bifrost;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** A keyboard/mouse scroll selector; opening this screen never activates a skill. */
public final class AsgardAbilityScreen extends Screen {
    private final String kind;
    private final List<String> descriptions;
    private final String[] names;
    private int selected;
    private int detailScroll;
    private int panelX, panelY, panelW, panelH, listW;

    public AsgardAbilityScreen(String kind) {
        super(Component.translatable("screen.bifrost.abilities"));
        this.kind = kind;
        this.descriptions = AsgardAbilities.describe(kind);
        this.names = switch (kind) {
            case "hofund" -> new String[]{"Bifrost Travel", "Realm Shift", "Hilt Bash and Counter", "Berserk Slashes"};
            case "mjolnir" -> new String[]{"Bifrost Travel", "Shard Swarm", "Shield Strike", "Lightning Vortex"};
            case "stormbreaker" -> new String[]{"Bifrost Travel", "Wakanda Ground Slam", "Multi-Target Ricochet"};
            default -> new String[0];
        };
    }
    @Override protected void init() {
        panelW = Math.max(240, Math.min(640, width - 20));
        panelH = Math.max(180, Math.min(330, height - 20));
        panelX = (width - panelW) / 2; panelY = (height - panelH) / 2;
        listW = Math.min(220, Math.max(100, panelW / 3));
        for (int i = 0; i < names.length; i++) {
            final int choice = i;
            addRenderableWidget(Button.builder(Component.literal(names[i]), b -> select(choice))
                .bounds(panelX + 8, panelY + 32 + i * 27, listW - 12, 24).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.bifrost.activate"), b -> activate())
            .bounds(panelX + panelW - 160, panelY + panelH - 28, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
            .bounds(panelX + panelW - 80, panelY + panelH - 28, 72, 20).build());
    }
    private void select(int choice) { selected = Math.floorMod(choice, Math.max(1, names.length)); detailScroll = 0; }
    private void activate() {
        if (names.length == 0) return;
        if (minecraft.player == null || !AsgardWeapons.kind(minecraft.player.getMainHandItem()).equals(kind)) {
            if (minecraft.player != null) minecraft.player.sendOverlayMessage(Component.literal("Keep the selected weapon in your main hand."));
            onClose(); return;
        }
        AsgardNetwork.send(0, selected);
        onClose();
    }
    @Override public void tick() {
        if (minecraft.player == null || !AsgardWeapons.kind(minecraft.player.getMainHandItem()).equals(kind)) onClose();
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(KeyEvent key) {
        if (key.key() == 264) { select(selected + 1); return true; }
        if (key.key() == 265) { select(selected - 1); return true; }
        if (key.key() == 257 || key.key() == 335) { activate(); return true; }
        return super.keyPressed(key);
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (vertical == 0) return false;
        if (x > panelX + listW) {
            int max = Math.max(0, detailLines().size() - visibleLines());
            detailScroll = Math.max(0, Math.min(max, detailScroll + (vertical > 0 ? -1 : 1)));
        } else select(selected + (vertical > 0 ? -1 : 1));
        return true;
    }
    private int visibleLines() { return Math.max(1, (panelH - 76) / 10); }
    private List<FormattedCharSequence> detailLines() {
        String text = selected < descriptions.size() ? descriptions.get(selected) : "Unavailable.";
        return font.split(Component.literal(text), Math.max(80, panelW - listW - 28));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF018202A);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFFD7AD55);
        graphics.centeredText(font, title.copy().append(" - " + titleCase(kind)), width / 2, panelY + 12, 0xFFFFDC8C);
        if (names.length > 0) graphics.fill(panelX + 3, panelY + 32 + selected * 27, panelX + 6, panelY + 56 + selected * 27, 0xFFFFDC8C);
        List<FormattedCharSequence> lines = detailLines();
        int visible = visibleLines();
        for (int i = 0; i < visible && detailScroll + i < lines.size(); i++)
            graphics.text(font, lines.get(detailScroll + i), panelX + listW + 8, panelY + 34 + i * 10, 0xFFE7EBEF);
        if (lines.size() > visible) graphics.text(font, Component.literal("Scroll details"), panelX + listW + 8, panelY + panelH - 43, 0xFFABB5C0);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
    private static String titleCase(String text) { return text.isEmpty() ? "" : Character.toUpperCase(text.charAt(0)) + text.substring(1); }
}
