package vitosos.sapphireweapons.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.item.InsectGlaiveItem;
import vitosos.sapphireweapons.item.KinsectItem;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;

public class InsectGlaiveHudOverlay implements HudRenderCallback {

    private static final Identifier UI_TEXTURE = new Identifier("sapphire-star-armaments", "textures/gui/essences.png");
    private static final Identifier TOP_LAYER_TEXTURE = new Identifier("sapphire-star-armaments", "textures/gui/top_layer.png");

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        boolean holdsGlaive = player.getMainHandStack().getItem() instanceof InsectGlaiveItem;
        boolean holdsKinsect = player.getOffHandStack().getItem() instanceof KinsectItem;

        if (!holdsGlaive || !holdsKinsect) return;

        IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
        boolean hasTriple = glaiveUser.getTripleBuffTicks() > 0;

        float redPct = hasTriple ? (glaiveUser.getTripleBuffTicks() / 1800f) : (glaiveUser.getRedEssenceTicks() / 1200f);
        float whitePct = hasTriple ? (glaiveUser.getTripleBuffTicks() / 1800f) : (glaiveUser.getWhiteEssenceTicks() / 1200f);
        float orangePct = hasTriple ? (glaiveUser.getTripleBuffTicks() / 1800f) : (glaiveUser.getOrangeEssenceTicks() / 1200f);

        // --- 1. COORDINATE SETUP ---
        int frameX = 5;
        int frameY = 10;

        // --- NEW: Mask Render Size ---
        // Change these to shrink or stretch the mask on your screen!
        int maskRenderWidth = 70;
        int maskRenderHeight = 16;

        // The exact positions of the diamonds UNDER the frame.
        // Tweak these to slide them left or right to align with the new, smaller cutouts!
        int redX = frameX + 18;
        int whiteX = frameX + 36 ;
        int orangeX = frameX + 54;

        // Since the mask and the diamonds are now both 16px high, we probably don't need a Y-offset
        int diamondY = frameY;

        // --- 2. DRAW BACKGROUNDS (Empty Diamonds) ---
        context.drawTexture(UI_TEXTURE, redX, diamondY, 0, 0, 16, 16, 64, 64);
        context.drawTexture(UI_TEXTURE, whiteX, diamondY, 0, 0, 16, 16, 64, 64);
        context.drawTexture(UI_TEXTURE, orangeX, diamondY, 0, 0, 16, 16, 64, 64);

        // --- 3. DRAW FOREGROUNDS (Depleting Colored Diamonds) ---
        drawDepletingTexture(context, redX, diamondY, 16, redPct);
        drawDepletingTexture(context, whiteX, diamondY, 32, whitePct);
        drawDepletingTexture(context, orangeX, diamondY, 48, orangePct);

        // --- 4. DRAW THE TOP LAYER FRAME (The Mask) ---
        // Render Width = 64, Render Height = 16 (Squishes it by 25% to match diamonds)
        // Texture Width = 80, Texture Height = 20 (The actual file size on your hard drive)
        context.drawTexture(TOP_LAYER_TEXTURE, frameX, frameY, 0, 0, 80, 20, 80, 20);
    }

    private void drawDepletingTexture(DrawContext context, int x, int y, int uOffset, float percentage) {
        if (percentage <= 0.0f) return;

        int fullHeight = 16;
        int fillHeight = Math.max(1, (int) (fullHeight * percentage));
        int yOffset = fullHeight - fillHeight;

        context.drawTexture(
                UI_TEXTURE,
                x, y + yOffset,
                uOffset, yOffset,
                16, fillHeight,
                64, 64
        );
    }
}