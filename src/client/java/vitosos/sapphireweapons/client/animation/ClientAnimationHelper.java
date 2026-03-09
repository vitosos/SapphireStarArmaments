package vitosos.sapphireweapons.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientAnimationHelper {

    public static void playCustomAnimation(AbstractClientPlayerEntity player, String animationName) {
        if (player instanceof ISapphireAnimatedPlayer sapphirePlayer) {
            ModifierLayer<IAnimation> layer = sapphirePlayer.getSapphireLayer();
            if (layer != null) {
                KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(new Identifier("sapphire-star-armaments", animationName));
                if (anim != null) {
                    layer.setAnimation(new KeyframeAnimationPlayer(anim));
                }
            }
        }
    }
}