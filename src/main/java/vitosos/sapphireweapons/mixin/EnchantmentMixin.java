package vitosos.sapphireweapons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.FireAspectEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vitosos.sapphireweapons.item.InsectGlaiveItem;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void denySpecificEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // If the item is a Glaive AND the enchantment is Fire Aspect, reject it!
        if (stack.getItem() instanceof InsectGlaiveItem) {
            if ((Object) this instanceof FireAspectEnchantment) {
                cir.setReturnValue(false);
            }
        }
    }
}