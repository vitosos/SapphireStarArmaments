package vitosos.sapphireweapons.mixin.client;

import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {

    @Shadow @Final
    private List<String> splashTexts;

    @Inject(method = "apply", at = @At("TAIL"))
    private void addModSplashes(List<String> splashes, ResourceManager manager, Profiler profiler, CallbackInfo ci)
    {
        var player = net.minecraft.client.MinecraftClient.getInstance().getSession().getUsername();

        splashTexts.add("Suis Monté!");
        splashTexts.add("SNEK URGENT PLS");
        splashTexts.add("So Tasty!");
        splashTexts.add("IT'S UP TO SOMETHING!");
        splashTexts.add("You can now eat again.");
        splashTexts.add("Hunter, the Guild authorizes...");
        splashTexts.add("Do Wyverians lay eggs?");
        splashTexts.add("Monstie Huntie");
        splashTexts.add("Shoutouts to CantaPerMe");
        splashTexts.add("Shoutouts to BannedGammoth");
        splashTexts.add("Shoutouts to TeamDarkside");
        splashTexts.add("Shoutouts to Arekkz Gaming");

        splashTexts.add(player + " used a Gourmet Voucher!");
        splashTexts.add(player + " has fainted. Continues remaining: 2");
    }
}