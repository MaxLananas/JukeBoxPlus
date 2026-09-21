package com.example.jukeboxplus.compat;

import com.example.jukeboxplus.JukeboxPlus;
import com.example.jukeboxplus.gui.GfxImpl;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/** Key bindings and HUD hook for this Minecraft family. */
public final class Platform {
    private Platform() {}

    public static void registerKeys(JukeboxPlus mod) {
        KeyMapping.Category category = KeyMapping.Category.create(ResourceLocation.fromNamespaceAndPath(JukeboxPlus.MOD_ID, "main"));
        for (JukeboxPlus.Action a : JukeboxPlus.Action.values()) {
            InputConstants.Key key = keyFor(a.defaultKey);
            KeyMapping km = new KeyMapping(a.translationKey(), key.getValue(), category);
            KEYS.put(a, KeyBindingHelper.registerKeyBinding(km));
        }
    }

    public static void registerHud(JukeboxPlus mod) {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> mod.renderHud(new GfxImpl(graphics)));
    }

    private static final Map<JukeboxPlus.Action, KeyMapping> KEYS = new EnumMap<>(JukeboxPlus.Action.class);

    /** Key code of the version's keyboard layout for a character, found through the vanilla name table. */
    private static InputConstants.Key keyFor(char c) {
        String name;
        switch (c) {
            case '[': name = "key.keyboard.left.bracket"; break;
            case ']': name = "key.keyboard.right.bracket"; break;
            case '=': name = "key.keyboard.equal"; break;
            case '-': name = "key.keyboard.minus"; break;
            default:  name = "key.keyboard." + Character.toLowerCase(c);
        }
        try {
            return InputConstants.getKey(name);
        } catch (Exception e) {
            return InputConstants.UNKNOWN;
        }
    }

    public static void pollKeys(JukeboxPlus mod) {
        for (Map.Entry<JukeboxPlus.Action, KeyMapping> e : KEYS.entrySet()) {
            while (e.getValue().consumeClick()) mod.run(e.getKey());
        }
    }
}
