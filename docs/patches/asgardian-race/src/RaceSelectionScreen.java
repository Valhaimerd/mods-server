package com.eiden.iourusraces.client.gui;

import com.eiden.iourusraces.race.Race;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;

public class RaceSelectionScreen
extends Screen {
    private final List<Race> races = Arrays.stream(Race.values()).filter(race -> race != Race.NONE).toList();
    private int selectedIndex = 0;
    private Race hoveredRace = null;
    private int listX;
    private int listY;
    private int listW;
    private int buttonH;
    private int gap;
    private int cardX;
    private int cardY;
    private int cardW;
    private int cardH;

    public RaceSelectionScreen() {
        super((Component)Component.translatable((String)"screen.iourusraces.race_selection"));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.calcLayout();
        guiGraphics.fill(0, 0, this.width, this.height, -16448247);
        this.drawTitle(guiGraphics);
        this.drawRaceList(guiGraphics, mouseX, mouseY);
        this.drawRaceCard(guiGraphics, mouseX, mouseY);
        this.drawFooter(guiGraphics);
        this.drawRaceTooltip(guiGraphics, mouseX, mouseY);
    }

    private void calcLayout() {
        int usableHeight = this.height - 26;
        this.listW = Math.max(80, Math.min(140, this.width / 4));
        this.cardW = Math.min(426, this.width - this.listW - 40);
        this.cardH = Math.min(292, usableHeight - 20);
        this.gap = this.height < 360 ? 3 : 4;
        int available = usableHeight - 88;
        this.buttonH = Math.max(12, Math.min(27, (available - this.gap * (this.races.size() - 1)) / this.races.size()));
        int listHeight = this.buttonH * this.races.size() + this.gap * (this.races.size() - 1);
        this.listX = (this.width - this.listW - 16 - this.cardW) / 2;
        this.cardX = this.listX + this.listW + 16;
        this.cardY = (usableHeight - this.cardH) / 2;
        this.listY = Math.max(82, (usableHeight - listHeight) / 2 + 20);
    }

    private void drawTitle(GuiGraphicsExtractor guiGraphics) {
        int titleX = this.listX + this.listW / 2;
        int titleY = Math.max(this.cardY - 68, 16);
        guiGraphics.centeredText(this.font, (Component)Component.translatable((String)"screen.iourusraces.title"), titleX, titleY, -2709190);
        guiGraphics.fill(titleX - 100, titleY + 14, titleX + 100, titleY + 15, 1715218960);
        guiGraphics.centeredText(this.font, (Component)Component.translatable((String)"screen.iourusraces.subtitle"), titleX, titleY + 24, -4671288);
    }

    private void drawRaceList(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        this.hoveredRace = null;
        guiGraphics.text(this.font, (Component)Component.translatable((String)"screen.iourusraces.races"), this.listX, this.listY - 28, -2709190);
        for (int i = 0; i < this.races.size(); ++i) {
            boolean hovered;
            int y = this.listY + i * (this.buttonH + this.gap);
            boolean selected = i == this.selectedIndex;
            boolean bl = hovered = mouseX >= this.listX && mouseX <= this.listX + this.listW && mouseY >= y && mouseY <= y + this.buttonH;
            if (hovered) {
                this.hoveredRace = this.races.get(i);
            }
            int backgroundColor = selected ? -582338790 : (hovered ? -1441327326 : 1712328728);
            guiGraphics.fill(this.listX, y, this.listX + this.listW, y + this.buttonH, backgroundColor);
            if (selected) {
                guiGraphics.fill(this.listX, y, this.listX + 7, y + this.buttonH, -2709190);
                this.drawBorder(guiGraphics, this.listX, y, this.listW, this.buttonH, -2709190);
            } else {
                guiGraphics.fill(this.listX, y, this.listX + 4, y + this.buttonH, -2008402150);
            }
            guiGraphics.centeredText(this.font, this.raceName(this.races.get(i)), this.listX + this.listW / 2, y + this.buttonH / 2 - 4, selected ? -1 : -8947830);
        }
    }

    private void drawRaceCard(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Race race = this.races.get(this.selectedIndex);
        guiGraphics.fill(this.cardX + 5, this.cardY + 5, this.cardX + this.cardW + 5, this.cardY + this.cardH + 5, 0x66000000);
        guiGraphics.fill(this.cardX, this.cardY, this.cardX + this.cardW, this.cardY + this.cardH, -871888879);
        this.drawBorder(guiGraphics, this.cardX, this.cardY, this.cardW, this.cardH, -2709190);
        guiGraphics.text(this.font, this.raceName(race), this.cardX + 150, this.cardY + 10, -2709190);
        guiGraphics.fill(this.cardX + 34, this.cardY + 20, this.cardX + this.cardW - 34, this.cardY + 21, -2008402150);
        int dividerX = this.cardX + 132;
        this.renderPlayerPreview(guiGraphics, race, mouseX, mouseY);
        guiGraphics.fill(dividerX, this.cardY + 48, dividerX + 1, this.cardY + this.cardH - 52, 2001352474);
        int infoX = dividerX + 18;
        int valueX = infoX + 62;
        int y = this.cardY + 30;
        guiGraphics.text(this.font, (Component)Component.translatable((String)"screen.iourusraces.attributes"), infoX, y, -2709190);
        this.drawAttribute(guiGraphics, "screen.iourusraces.attribute.height", this.formatValue(race.getScale()), infoX, valueX, y += 22, -3618561);
        this.drawAttribute(guiGraphics, "screen.iourusraces.attribute.health", this.formatValue(race.getMaxHealth()), infoX, valueX, y += 18, -34953);
        this.drawAttribute(guiGraphics, "screen.iourusraces.attribute.speed", this.formatValue(race.getSpeedMultiplier()), infoX, valueX, y += 18, -11141291);
        guiGraphics.text(this.font, (Component)Component.translatable((String)"screen.iourusraces.traits"), infoX, y += 18, -2709190);
        y += 16;
        int traitMaxWidth = this.cardX + this.cardW - infoX - 18;
        for (int i = 1; i <= 3; ++i) {
            String key = this.raceKey(race, "trait." + i);
            MutableComponent trait = Component.translatable((String)key);
            int color = i == 3 && race != Race.HUMAN ? -43691 : -11141291;
            List<FormattedCharSequence> wrappedLines = this.font.split((FormattedText)trait, traitMaxWidth);
            for (FormattedCharSequence line : wrappedLines) {
                guiGraphics.text(this.font, line, infoX, y, color);
                y += 10;
            }
            y += 2;
            if (race == Race.HUMAN) break;
        }
        this.drawDescription(guiGraphics, race, dividerX);
    }

    private void drawAttribute(GuiGraphicsExtractor guiGraphics, String key, String value, int labelX, int valueX, int y, int color) {
        guiGraphics.text(this.font, (Component)Component.translatable((String)key), labelX, y, color);
        guiGraphics.text(this.font, value, valueX, y, color);
    }

    private void renderPlayerPreview(GuiGraphicsExtractor guiGraphics, Race race, int mouseX, int mouseY) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int renderScale = switch (race) {
            default -> throw new MatchException(null, null);
            case Race.DWARF -> 42;
            case Race.ELF -> 55;
            case Race.HUMAN -> 48;
            case Race.ORC -> 60;
            case Race.GOBLIN -> 40;
            case Race.MELFORK -> 48;
            case Race.DRACONIC -> 58;
            case Race.ASGARDIAN -> 50;
            case Race.NONE -> 48;
        };
        InventoryScreen.extractEntityInInventoryFollowsMouse((GuiGraphicsExtractor)guiGraphics, (int)(this.cardX + 20), (int)(this.cardY - 10), (int)(this.cardX + 120), (int)(this.cardY + this.cardH - 60), (int)renderScale, (float)0.0f, (float)mouseX, (float)mouseY, (LivingEntity)player);
    }

    private void drawDescription(GuiGraphicsExtractor guiGraphics, Race race, int dividerX) {
        int descY = this.cardY + this.cardH - 45;
        guiGraphics.fill(this.cardX + 18, descY - 6, dividerX - 18, descY + 14, 0x66000000);
        List<FormattedCharSequence> wrapped = this.font.split((FormattedText)Component.translatable((String)this.raceKey(race, "description")), 120);
        for (int i = 0; i < wrapped.size(); ++i) {
            guiGraphics.centeredText(this.font, (FormattedCharSequence)wrapped.get(i), (this.cardX + dividerX) / 2, descY + i * 10, -4671288);
        }
    }

    private void drawFooter(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.fill(0, this.height - 26, this.width, this.height, -872415232);
        guiGraphics.centeredText(this.font, (Component)Component.translatable((String)"screen.iourusraces.footer"), this.width / 2, this.height - 14, -8947830);
    }

    private void drawBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private Component raceName(Race race) {
        return Component.translatable((String)("race.iourusraces." + race.name().toLowerCase(Locale.ROOT)));
    }

    private String raceKey(Race race, String suffix) {
        return "race.iourusraces." + race.name().toLowerCase(Locale.ROOT) + "." + suffix;
    }

    private List<ClientTooltipComponent> getRaceTooltip(Race race) {
        ArrayList<ClientTooltipComponent> result = new ArrayList<ClientTooltipComponent>();
        List<FormattedCharSequence> lines = this.font.split((FormattedText)Component.translatable((String)this.raceKey(race, "tooltip")), 180);
        for (FormattedCharSequence line : lines) {
            result.add(ClientTooltipComponent.create((FormattedCharSequence)line));
        }
        return result;
    }

    private void drawRaceTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredRace == null) {
            return;
        }
        guiGraphics.tooltip(this.font, this.getRaceTooltip(this.hoveredRace), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case 87:
            case 265: {
                this.selectedIndex = (this.selectedIndex - 1 + this.races.size()) % this.races.size();
                return true;
            }
            case 83:
            case 264: {
                this.selectedIndex = (this.selectedIndex + 1) % this.races.size();
                return true;
            }
            case 257:
            case 335: {
                this.confirmRace();
                return true;
            }
            case 256: {
                Minecraft.getInstance().gui.setScreen(null);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        for (int i = 0; i < this.races.size(); ++i) {
            int y = this.listY + i * (this.buttonH + this.gap);
            if (!(mouseX >= (double)this.listX) || !(mouseX <= (double)(this.listX + this.listW)) || !(mouseY >= (double)y) || !(mouseY <= (double)(y + this.buttonH))) continue;
            this.selectedIndex = i;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void confirmRace() {
        Race race = this.races.get(this.selectedIndex);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.sendCommand("race " + race.name().toLowerCase(Locale.ROOT));
        }
        Minecraft.getInstance().gui.setScreen(null);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private String formatValue(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int)value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
