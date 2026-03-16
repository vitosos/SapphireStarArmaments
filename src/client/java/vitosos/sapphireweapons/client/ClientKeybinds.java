package vitosos.sapphireweapons.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientKeybinds {

    public static KeyBinding ultimateKey;
    public static KeyBinding fireKinsectKey; // NEW

    public static void register() {
        ultimateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sapphire-star-armaments.ultimate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.sapphire-star-armaments.combat"
        ));

        fireKinsectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sapphire-star-armaments.fire_kinsect",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.sapphire-star-armaments.combat"
        ));
    }
}