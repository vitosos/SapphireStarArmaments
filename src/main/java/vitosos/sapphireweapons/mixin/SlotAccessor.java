package vitosos.sapphireweapons.mixin;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {

    // @Mutable strips the 'final' modifier so we can change the value!
    @Mutable
    @Accessor("x")
    void setX(int x);

    @Mutable
    @Accessor("y")
    void setY(int y);
}