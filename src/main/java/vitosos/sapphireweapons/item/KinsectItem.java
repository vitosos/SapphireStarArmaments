package vitosos.sapphireweapons.item;

import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class KinsectItem extends Item implements GeoItem {
    private final float kinsectDamage;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    // --- NEW: THE INJECTION SLOT ---
    // This allows the Client to safely inject the renderer without the Server ever knowing!
    public static Consumer<Consumer<Object>> renderProviderInjector = consumer -> {};

    public KinsectItem(Settings settings, float damage) {
        super(settings);
        this.kinsectDamage = damage;
    }

    public float getKinsectDamage() {
        return this.kinsectDamage;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- NEW: ACCEPT THE INJECTED RENDERER ---
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        renderProviderInjector.accept(consumer);
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
}