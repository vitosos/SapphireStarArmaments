package vitosos.sapphireweapons.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;

public interface ISapphireAnimatedPlayer {
    void setSapphireLayer(ModifierLayer<IAnimation> layer);
    ModifierLayer<IAnimation> getSapphireLayer();
}