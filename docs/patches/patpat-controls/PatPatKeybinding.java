package net.lopymine.patpat.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.List;

import net.lopymine.patpat.client.config.PatPatClientConfig;
import net.lopymine.patpat.client.keybinding.KeybindingCombination;
import net.lopymine.patpat.client.keybinding.PatPatClientKeybindingManager;
import net.lopymine.patpat.client.keybinding.PressableKeybindingCombination;
import net.lopymine.patpat.client.manager.PatPatClientManager;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public class PatPatKeybinding
extends KeyMapping {
    public static final KeybindingCombination DEFAULT_COMBINATION = PatPatKeybinding.getDefaultCombination();
    private final PressableKeybindingCombination combination = new PressableKeybindingCombination();
    private boolean binding;
    private boolean canStartBinding = true;

    public PatPatKeybinding(KeybindingCombination patCombination) {
        super("patpat.keybinding.pat", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.MOUSE, 1, PatPatClientKeybindingManager.CATEGORY);
        this.combination.setAttributeKey(patCombination.getAttributeKey());
        this.combination.setKey(patCombination.getKey());
        syncNativeBinding();
    }

    private static KeybindingCombination getDefaultCombination() {
        return new KeybindingCombination(InputConstants.Type.KEYSYM.getOrCreate(341), InputConstants.Type.MOUSE.getOrCreate(1));
    }

    public void startBinding() {
        this.combination.setAttributeKey(null);
        this.combination.setKey(null);
        this.binding = true;
        this.canStartBinding = false;
    }

    public boolean addBindingKey(InputConstants.Key key) {
        if (this.isCanStartBinding()) {
            this.startBinding();
        }
        if (!this.isBinding()) {
            return true;
        }
        if (key.getValue() == 256) {
            this.refreshPressedState();
            this.combination.setKey(null);
            this.combination.setAttributeKey(null);
            return true;
        }
        if (KeybindingCombination.isAttributeKey(key.getValue())) {
            if (this.combination.isComplete()) {
                return true;
            }
            this.combination.setAttributeKey(key);
            return this.combination.isComplete();
        }
        this.combination.setKey(key);
        return true;
    }

    public void sendBindingKeys() {
        this.saveCombination();
        this.combination.setAll(false);
        this.binding = false;
        this.canStartBinding = true;
    }

    // NeoForge loads options.txt through this method, rather than setKey.
    @Override
    public void setKeyModifierAndCode(KeyModifier modifier, InputConstants.Key key) {
        super.setKeyModifierAndCode(modifier, key);
        if (this.combination == null) return; // Defensive against superclass initialization.
        this.combination.setAll(false);
        this.setDown(false);
        this.combination.setKey(key);
        this.combination.setAttributeKey(modifier == KeyModifier.CONTROL ? InputConstants.Type.KEYSYM.getOrCreate(341)
            : modifier == KeyModifier.SHIFT ? InputConstants.Type.KEYSYM.getOrCreate(340)
            : modifier == KeyModifier.ALT ? InputConstants.Type.KEYSYM.getOrCreate(342) : null);
    }

    @Override
    public void setKey(InputConstants.Key key) {
        setKeyModifierAndCode(key.equals(getDefaultKey()) ? getDefaultKeyModifier() : KeyModifier.NONE, key);
        saveCombination();
    }

    private void syncNativeBinding() {
        InputConstants.Key modifierKey = this.combination.getAttributeKey();
        KeyModifier modifier = modifierKey == null ? KeyModifier.NONE : switch (modifierKey.getValue()) {
            case 341, 345 -> KeyModifier.CONTROL;
            case 340, 344 -> KeyModifier.SHIFT;
            case 342, 346 -> KeyModifier.ALT;
            default -> KeyModifier.NONE;
        };
        // Keep custom combinations (including nonstandard modifier keys) intact.
        super.setKeyModifierAndCode(modifier, this.combination.getKey() == null ? InputConstants.UNKNOWN : this.combination.getKey());
    }

    private void saveCombination() {
        syncNativeBinding();
        PatPatClientConfig config = PatPatClientConfig.getInstance();
        config.getMainConfig().setPatCombination(this.combination);
        config.saveAsync();
    }

    public boolean onKeyAction(InputConstants.Key key, boolean pressed) {
        if (!this.combination.contains(key)) {
            return false;
        }
        if (this.combination.onlyOneKey()) {
            this.combination.set(key, pressed);
            this.setDown(pressed);
            if (!pressed) {
                PatPatClientManager.setPatCooldown(0);
            }
            return this.isDown();
        }
        if (!pressed) {
            this.combination.set(key, false);
            this.setDown(false);
            PatPatClientManager.setPatCooldown(0);
            return false;
        }
        this.combination.set(key, true);
        boolean allPressedState = this.combination.allPressed();
        this.setDown(allPressedState);
        return allPressedState && !KeybindingCombination.isAttributeKey(key.getValue());
    }

    public boolean isDefault() {
        return this.combination.equals(DEFAULT_COMBINATION);
    }

    public Component getTranslatedKeyMessage() {
        return this.getFullTranslatedKeyMessage();
    }

    public Component getFullTranslatedKeyMessage() {
        return this.combination.getCombinationLocalizedComponent(!this.isBinding());
    }

    public void refreshPressedState() {
        List<InputConstants.Key> keys = this.combination.getKeys();
        keys.forEach(key -> {
            if (key.getType() == InputConstants.Type.KEYSYM) {
                this.combination.set((InputConstants.Key)key, InputConstants.isKeyDown((Window)Minecraft.getInstance().getWindow(), (int)key.getValue()));
            } else {
                this.combination.set((InputConstants.Key)key, GLFW.glfwGetMouseButton((long)Minecraft.getInstance().getWindow().handle(), (int)key.getValue()) == 1);
            }
        });
        boolean allPressed = this.combination.allPressed();
        this.setDown(allPressed);
    }

    public boolean isBinding() {
        return this.binding;
    }

    public boolean isCanStartBinding() {
        return this.canStartBinding;
    }
}

