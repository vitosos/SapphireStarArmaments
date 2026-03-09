package vitosos.sapphireweapons.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.recipe.ForgeRecipe;
import vitosos.sapphireweapons.recipe.ForgeRecipeRegistry;
import vitosos.sapphireweapons.recipe.WeaponCategory;
import vitosos.sapphireweapons.util.ISapphirePlayerData;

import java.util.Map;

public class ForgeScreen extends HandledScreen<ForgeScreenHandler> {
    // Make sure this path perfectly matches your UI texture location!
    private static final Identifier TEXTURE = new Identifier(SapphireStarArmaments.MOD_ID, "textures/gui/forge_gui.png");

    private int selectedRecipeIndex = 0; // Tracks which weapon is clicked in the list
    private int scrollOffset = 0; // How many items we have scrolled down
    private final int visibleRecipes = 4; // How many recipes fit in the UI at once
    private WeaponCategory currentCategory = WeaponCategory.GLAIVE;
    private boolean isCategoryMenuOpen = false;
    private ButtonWidget forgeButton;

    public ForgeScreen(ForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // Expand the background size to fit your large custom UI
        this.backgroundWidth = 376;
        this.backgroundHeight = 220;
        this.playerInventoryTitleY = this.backgroundHeight - 94; // Moves the "Inventory" text down
    }

    @Override
    protected void init() {
        super.init();

        // --- THE FORGE BUTTON ---
        int buttonX = this.x + 185;
        int buttonY = this.y + 190;

        this.forgeButton = ButtonWidget.builder(Text.literal("Forge"), button -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(currentCategory.name());
            buf.writeInt(selectedRecipeIndex);
            ClientPlayNetworking.send(ServerNetworking.FORGE_WEAPON_PACKET, buf);
        }).dimensions(buttonX, buttonY, 40, 20).build();

        this.addDrawableChild(this.forgeButton);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // The last two numbers tell the game the actual size of your PNG file!
        context.drawTexture(TEXTURE, this.x - 16, this.y - 2, 0, 0, this.backgroundWidth, this.backgroundHeight, 512, 512);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        if (this.forgeButton != null) {
            boolean isShelfFull = !this.handler.output.getStack(0).isEmpty() || !this.handler.output.getStack(1).isEmpty();
            this.forgeButton.active = !isShelfFull;
        }

        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        drawCategoryHeader(context, mouseX, mouseY);
        drawSelectedRecipeDetails(context);

        // THE FIX: Only draw the recipe list if the menu is closed!
        if (this.isCategoryMenuOpen) {
            // Push the Z-Index up to 300 so the overlay renders on top of EVERYTHING
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 300.0F);
            drawCategoryOverlay(context, mouseX, mouseY);
            context.getMatrices().pop();
        } else {
            // Only draw the list when the overlay is gone
            drawRecipeList(context, mouseX, mouseY);
        }
    }

    private void drawCategoryHeader(DrawContext context, int mouseX, int mouseY) {
        // Positioned right above your recipe list (which starts at Y + 40)
        int headerX = this.x;
        int headerY = this.y + 10;

        // Draw the current category text
        context.drawText(this.textRenderer, currentCategory.getDisplayName(), headerX, headerY, 0xFFFFFF, true);

        // Draw the button box for the Icon (to the right of the text, or wherever you prefer)
        int buttonX = headerX + 90;

        // Highlight the button if hovered
        if (mouseX >= buttonX && mouseX <= buttonX + 20 && mouseY >= headerY - 2 && mouseY <= headerY + 18) {
            // Draw Hover Button Texture
            context.drawTexture(TEXTURE, buttonX, headerY - 2, 140, 230, 20, 20, 512, 512);
        } else {
            // Draw Normal Button Texture
            context.drawTexture(TEXTURE, buttonX, headerY - 2, 120, 230, 20, 20, 512, 512);
        }

        // Draw the current category icon inside the button
        context.drawItem(new ItemStack(currentCategory.getIconItem()), buttonX + 2, headerY - 1);
    }

    private void drawCategoryOverlay(DrawContext context, int mouseX, int mouseY) {
        // Shifted 5 pixels to the left of your list! Change this number to move the whole box.
        int startX = this.x - 5;

        // Draw the custom dropdown background texture instead of a color fill
        context.drawTexture(TEXTURE, startX, this.y + 5, 0, 300, 115, 125, 512, 512);

        context.drawText(this.textRenderer, "Select Weapon Type:", startX + 5, this.y + 10, 0xFFAA00, true);

        int iconX = startX + 5;
        int iconY = this.y + 25;

        for (WeaponCategory category : WeaponCategory.values()) {
            if (mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18) {
                context.fill(iconX, iconY, iconX + 18, iconY + 18, 0x55FFFFFF);
                context.drawTooltip(this.textRenderer, Text.literal(category.getDisplayName()), mouseX, mouseY);
            }

            context.drawItem(new ItemStack(category.getIconItem()), iconX + 1, iconY + 1);

            iconX += 20;
            // Drop to a new row if we hit the right edge of the box
            if (iconX > startX + 90) {
                iconX = startX + 5;
                iconY += 20;
            }
        }
    }

    private void drawRecipeList(DrawContext context, int mouseX, int mouseY) {
        int listX = this.x;
        int listY = this.y + 32; // The top of your scrollable window

        int startIndex = this.scrollOffset;
        java.util.List<ForgeRecipe> currentList = getCurrentRecipeList();
        int endIndex = Math.min(startIndex + this.visibleRecipes, currentList.size());

        for (int i = startIndex; i < endIndex; i++) {
            ForgeRecipe recipe = currentList.get(i);

            // displayIndex is where it draws on the screen (0 to 4), NOT its actual recipe number!
            int displayIndex = i - startIndex;
            int yPos = listY + (displayIndex * 20);

            // Highlight if selected
            // Check if it's selected OR hovered
            if (i == selectedRecipeIndex || (mouseX >= listX && mouseX <= listX + 110 && mouseY >= yPos && mouseY <= yPos + 18)) {
                // Draw Selected/Hover Texture
                context.drawTexture(TEXTURE, listX, yPos, 0, 250, 110, 18, 512, 512);
            } else {
                // Draw Normal Texture
                context.drawTexture(TEXTURE, listX, yPos, 0, 230, 110, 18, 512, 512);
            }

            // Draw Icon and Text (Keep these the same!)
            context.drawItem(new ItemStack(recipe.getDisplayIcon()), listX + 2, yPos + 1);
            context.drawText(this.textRenderer, recipe.getWeaponName(), listX + 22, yPos + 6, 0xFFFFFF, true);

            // Visual hover effect
            if (mouseX >= listX && mouseX <= listX + 110 && mouseY >= yPos && mouseY <= yPos + 18) {
                context.fill(listX, yPos, listX + 110, yPos + 18, 0x33FFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // --- 1. IF OVERLAY IS OPEN ---
            if (this.isCategoryMenuOpen) {
                int iconX = this.x;
                int iconY = this.y + 25;

                for (WeaponCategory category : WeaponCategory.values()) {
                    if (mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18) {
                        // They clicked a new category!
                        this.currentCategory = category;
                        this.isCategoryMenuOpen = false; // Close the menu
                        this.scrollOffset = 0; // Reset scroll to the top
                        this.selectedRecipeIndex = 0; // Reset selected weapon
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                    iconX += 20;
                    if (iconX > this.x + 90) { iconX = this.x + 10; iconY += 20; }
                }

                // If they click anywhere else while the menu is open, just close the menu!
                this.isCategoryMenuOpen = false;
                return true;
            }

            // --- 2. CHECK HEADER BUTTON (To open the overlay) ---
            int headerX = this.x;
            int headerY = this.y + 10;
            int buttonX = headerX + 90;

            if (mouseX >= buttonX && mouseX <= buttonX + 20 && mouseY >= headerY - 2 && mouseY <= headerY + 18) {
                this.isCategoryMenuOpen = true;
                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            // --- 3. STANDARD RECIPE LIST CLICKS ---
            int listX = this.x;
            int listY = this.y + 32;

            int startIndex = this.scrollOffset;
            java.util.List<ForgeRecipe> currentList = getCurrentRecipeList();
            int endIndex = Math.min(startIndex + this.visibleRecipes, currentList.size());

            for (int i = startIndex; i < endIndex; i++) {
                int displayIndex = i - startIndex;
                int yPos = listY + (displayIndex * 20);

                if (mouseX >= listX && mouseX <= listX + 100 && mouseY >= yPos && mouseY <= yPos + 18) {
                    this.selectedRecipeIndex = i; // Select the actual recipe!
                    this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxScroll = Math.max(0, getCurrentRecipeList().size() - this.visibleRecipes);

        if (amount > 0 && this.scrollOffset > 0) {
            this.scrollOffset--; // Scrolled Up
        } else if (amount < 0 && this.scrollOffset < maxScroll) {
            this.scrollOffset++; // Scrolled Down
        }

        return true;
    }


    private void drawSelectedRecipeDetails(DrawContext context) {
        java.util.List<ForgeRecipe> currentList = getCurrentRecipeList();
        if (currentList.isEmpty()) return;
        ForgeRecipe recipe = currentList.get(selectedRecipeIndex);

        // --- 1. THE FLOATING WEAPON DISPLAY ---
        // Adjust these to match the exact position of your drawn black square!
        int squareX = this.x + 120;
        int squareY = this.y + 20;
        int squareSize = 48; // A nice, large display area

        // Draw the solid black background square
        context.fill(squareX, squareY, squareX + squareSize, squareY + squareSize, 0xFF000000);

        // Calculate a smooth floating animation (Sine wave based on game time)
        float time = this.client.world.getTime() + this.client.getTickDelta();
        float floatingOffset = (float) Math.sin(time / 10.0f) * 3.0f; // Bobs up and down by 3 pixels

        // Grab the rendering matrices to manipulate the item's size and position
        net.minecraft.client.util.math.MatrixStack matrices = context.getMatrices();
        matrices.push();

        // 1. Move to the absolute center of the black square, and apply the floating math
        matrices.translate(squareX + (squareSize / 2.0f), squareY + (squareSize / 2.0f) + floatingOffset, 100.0f);

        // 2. Scale the item up! (2.5x normal size)
        matrices.scale(2.5f, 2.5f, 2.5f);

        // 3. Draw the item. Because we already translated to the center,
        // we draw at -8, -8 to ensure the 16x16 item is perfectly centered on our new coordinates.
        context.drawItem(new ItemStack(recipe.getResultWeapon()), -8, -8);

        // Clean up so we don't accidentally scale the rest of the UI!
        matrices.pop();

        // --- 2. THE MATERIAL REQUIREMENTS ---
        // Pushed directly under the floating weapon square
        int matX = this.x + 180;
        int matY = this.y + 96;
        int matIndex = 0;

        ISapphirePlayerData playerData = (ISapphirePlayerData) this.client.player;

        boolean isCreative = this.client.player.isCreative();

        for (Map.Entry<Item, Integer> entry : recipe.getRequiredMaterials().entrySet()) {
            Item requiredItem = entry.getKey();
            int amountNeeded = entry.getValue();

            int yPos = matY + (matIndex * 18);

            // Draw the custom material slot background FIRST
            context.drawTexture(TEXTURE, matX - 2, yPos - 2, 0, 270, 140, 20, 512, 512);

            // Then draw the item and text on top of it!
            context.drawItem(new ItemStack(requiredItem), matX, yPos);

            int amountFound = countItemAcrossInventories(requiredItem);
            String countText = amountFound + " / " + amountNeeded;

            // If they are in creative, or have enough items, turn it green!
            int color = (isCreative || amountFound >= amountNeeded) ? 0x55FF55 : 0xFF5555;

            context.drawText(this.textRenderer, requiredItem.getName(), matX + 20, yPos + 4, 0xFFFFFF, true);
            context.drawText(this.textRenderer, countText, matX + 142, yPos + 4, color, true);

            matIndex++;
        }

        // --- 3. THE STATS (Green Scroll Area) ---
        int statsX = this.x + 120;
        int statsY = this.y + 80;

        float pwr = 1.0f; // 1.0 is the player's base fist damage
        String spdText = "SPD: Standard"; // Default fallback
        Item resultItem = recipe.getResultWeapon();

        // Check if the item is a standard sword/weapon to extract its damage
        if (resultItem instanceof net.minecraft.item.SwordItem sword) {
            pwr += sword.getAttackDamage();
            spdText = "SPD: Fast"; // Swords have a standard 1.6 speed
        } else if (resultItem instanceof net.minecraft.item.MiningToolItem tool) {
            pwr += tool.getAttackDamage();
            spdText = "SPD: Slow"; // Axes/Tools are generally slower
        }

        String pwrText = "PWR: " + String.format("%.1f", pwr);

        context.drawText(this.textRenderer, pwrText, statsX, statsY, 0xFFFFFF, true);
        context.drawText(this.textRenderer, spdText, statsX, statsY + 12, 0xFFFFFF, true);

        // --- 4. WEAPON NAME, DESC & COST ---
        int textX = this.x + 180;

        // 1. Get the official localized name from en_US.json
        Text wepName = Text.translatable(recipe.getResultWeapon().getTranslationKey());
        context.drawText(this.textRenderer, wepName, textX, this.y + 20, 0xFFAA00, true);

        // 2. Word-wrap and draw the description box!
        // We append ".desc" to the translation key. Add this to your en_US.json!
        Text descText = Text.translatable(recipe.getResultWeapon().getTranslationKey() + ".desc.1")
                .append(Text.translatable(recipe.getResultWeapon().getTranslationKey() + ".desc.2"));

        // Wrap the text so it doesn't spill
        java.util.List<net.minecraft.text.OrderedText> wrappedLines = this.textRenderer.wrapLines(descText, 180);

        // Draw each line directly under the weapon name
        for (int i = 0; i < wrappedLines.size(); i++) {
            context.drawText(this.textRenderer, wrappedLines.get(i), textX, this.y + 35 + (i * 10), 0xAAAAAA, true);
        }

        // 3. Draw the cost
        String costText = "Cost: " + recipe.getPointCost() + " Pts";
        context.drawText(this.textRenderer, costText, textX + 5, this.y + 175, 0x55FFFF, true);

        // Current Points text (Green if they have enough, Red if they are broke)
        int currentPoints = playerData.getSapphirePoints();
        String pointsText = currentPoints + " Pts";
        int pointsWidth = this.textRenderer.getWidth(pointsText);
        int rightAnchorX = this.x + 160;

        int pointsColor = currentPoints >= recipe.getPointCost() ? 0x55FF55 : 0xFF5555;
        context.drawText(this.textRenderer, pointsText, rightAnchorX - pointsWidth, this.y + 125, pointsColor, true);
    }

    // Helper method to accurately count items across the Client's pockets and box
    private int countItemAcrossInventories(Item item) {
        int count = 0;
        PlayerInventory pockets = this.client.player.getInventory();
        net.minecraft.inventory.Inventory box = ((ISapphirePlayerData) this.client.player).getBoxInventory();

        // 1. Scan Pockets
        for (int i = 0; i < pockets.size(); i++) {
            ItemStack stack = pockets.getStack(i);
            // Must not be empty, must be the exact item, and must NOT have enchantments
            if (!stack.isEmpty() && stack.getItem() == item && !stack.hasEnchantments()) {
                count += stack.getCount();
            }
        }

        // 2. Scan Box
        for (int i = 0; i < box.size(); i++) {
            ItemStack stack = box.getStack(i);
            // Exact same bulletproof check for the Item Box
            if (!stack.isEmpty() && stack.getItem() == item && !stack.hasEnchantments()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    // Grabs the current list of recipes, or an empty list if there are none yet
    private java.util.List<ForgeRecipe> getCurrentRecipeList() {
        return ForgeRecipeRegistry.RECIPES.getOrDefault(this.currentCategory, new java.util.ArrayList<>());
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Leaving this empty stops the game from rendering the default
        // "container.sapphire-star-armaments.forge" text and the "Inventory" text!
    }
}