package me.cael.keybindsgalore;

import com.google.common.collect.Maps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeybindsManager {

    private static final Map<InputUtil.Key, List<KeyBinding>> conflictingKeys = Maps.newHashMap();

    public static boolean handleConflict(InputUtil.Key key) {
        List<KeyBinding> matches = new ArrayList<>();
        KeyBinding[] keysAll = MinecraftClient.getInstance().options.keysAll;
        for (KeyBinding bind: keysAll) {
            if (bind.matchesKey(key.getCode(), -1)) {
                matches.add(bind);
            }
        }
        if (matches.size() > 1) {
            KeybindsManager.conflictingKeys.put(key, matches);
            return true;
        } else {
            KeybindsManager.conflictingKeys.remove(key);
            return false;
        }
    }

    public static boolean isConflicting(InputUtil.Key key) {
        return conflictingKeys.containsKey(key);
    }

    public static void openConflictMenu(InputUtil.Key key) {
        KeybindsScreen screen = new KeybindsScreen();
        screen.setConflictedKey(key);
        MinecraftClient.getInstance().openScreen(screen);
    }

    public static List<KeyBinding> getConflicting(InputUtil.Key key) {
        return conflictingKeys.get(key);
    }
}
