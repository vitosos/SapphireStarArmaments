package vitosos.sapphireweapons.mixin.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import vitosos.sapphireweapons.client.animation.ISapphireAnimatedPlayer;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin
        implements ISapphireAnimatedPlayer {

    @Unique
    private ModifierLayer<IAnimation> sapphireLayer;

    @Override
    public void setSapphireLayer(ModifierLayer<IAnimation> layer) {
        this.sapphireLayer = layer;
    }

    @Override
    public ModifierLayer<IAnimation> getSapphireLayer() {
        return this.sapphireLayer;
    }
}