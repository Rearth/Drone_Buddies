package rearth.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import rearth.Drones;
import rearth.drone.DroneData;
import rearth.drone.behaviour.DroneBehaviour;

import java.util.HashMap;
import java.util.List;

public class DroneCreatorScreen extends Screen {
    
    private static final Identifier BACKGROUND_TEXTURE = Drones.id("textures/gui/assembler.png");
    
    private static final Identifier BIG_BUTTON_TEXTURE = Drones.id("textures/gui/big_button.png");
    private static final Identifier BIG_BUTTON_HOVER_TEXTURE = Drones.id("textures/gui/big_button_hover.png");
    private static final Identifier BIG_BUTTON_PRESSED_TEXTURE = Drones.id("textures/gui/big_button_pressed.png");
    
    private static final Identifier SLOT_PANEL_TEXTURE = Drones.id("textures/gui/slot_panel.png");
    
    private final DroneData droneData;
    private final HashMap<Vec3i, BlockEntity> renderedEntities = new HashMap<>();
    private final float openTime = System.nanoTime();
    
    private float previewAngle = 0;
    
    public DroneCreatorScreen(DroneData data) {
        super(Text.empty());
        
        if (data == null)
            data = new DroneData(List.of(), Vec3d.ZERO, Vec3d.ZERO);
        
        this.droneData = data;
        
        
        for (var pair : droneData.getBlocks()) {
            var state = pair.state();
            if (state.getBlock() instanceof BlockEntityProvider blockEntityProvider) {
                var blockEntity = blockEntityProvider.createBlockEntity(new BlockPos(pair.localPos()), state);
                var renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);
                if (renderer != null) {
                    renderedEntities.put(pair.localPos(), blockEntity);
                }
            }
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        var centerX = this.width / 2;
        var centerY = this.height / 2;
        var backgroundStartX = centerX - (300 / 2);
        var backgroundStartY = centerY - (183 / 2);
        var buttonX = backgroundStartX + 154;
        var buttonY = backgroundStartY + 113;
        
        var buttonWidget = new BigDroneButton(buttonX, buttonY, 138, 59, Text.literal("BUILD!").formatted(Formatting.BOLD), button -> System.out.println("Pressed!"));
        
        this.addDrawableChild(buttonWidget);
        
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // openTime += delta;
        var textColor = 13685204;
        
        var centerX = this.width / 2;
        var centerY = this.height / 2;
        var backgroundStartX = centerX - (300 / 2);
        var backgroundStartY = centerY - (183 / 2);
        
        context.drawTexture(BACKGROUND_TEXTURE, backgroundStartX, backgroundStartY, 10, 0, 0, 300, 183, 300, 183);
        
        for (var pair : droneData.getBlocks()) {
            var entity = renderedEntities.get(pair.localPos());
            renderBlock(context, pair.localPos(), pair.state(), entity, delta);
        }
        
        for (var drawable : this.drawables) {
            drawable.render(context, mouseX, mouseY, delta);
        }
        
        context.drawText(this.textRenderer, Text.literal("Speed:"), backgroundStartX + 161, backgroundStartY + 13, textColor, false);
        context.drawText(this.textRenderer, Text.literal("Size:"), backgroundStartX + 161, backgroundStartY + 44, textColor, false);
        
        // render bars
        var greenColor = -12810969;
        var orangeColor = -1012726;
        var markerColor = -3092012;
        var endColor = -526345;
        
        // speed bar
        var speedBarFill = getSpeedProgress();
        var speedColor = speedBarFill > 0.25 ? greenColor : orangeColor;
        drawBarPart(context, backgroundStartX, backgroundStartY, 0, speedBarFill, speedColor, 5, 0);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.248f, 0.252f, markerColor, 3, 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.498f, 0.502f, markerColor, 3, 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.748f, 0.752f, markerColor, 3, 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, speedBarFill - 0.005f, speedBarFill + 0.005f, endColor, 5, 0);
        
        // size bar
        var sizeBarFill = getSizeProgress();
        var sizeColor = sizeBarFill < 0.75 ? greenColor : orangeColor;
        drawBarPart(context, backgroundStartX, backgroundStartY, 0, sizeBarFill, sizeColor, 5, 32);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.248f, 0.252f, markerColor, 3, 32 + 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.498f, 0.502f, markerColor, 3, 32 + 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, 0.748f, 0.752f, markerColor, 3, 32 + 1);
        drawBarPart(context, backgroundStartX, backgroundStartY, sizeBarFill - 0.005f, sizeBarFill + 0.005f, endColor, 5, 32);
        
        // installed behaviours
        var abilitiesStartX = backgroundStartX + 159;
        var abilitiesStartY = backgroundStartY + 80;
        var index = 0;
        
        for (var ability : droneData.installed) {
            
            if (ability.equals(DroneBehaviour.BlockFunctions.FLIGHT)) continue;
            
            var startAtX = abilitiesStartX + index * (20 + 3);
            
            context.drawTexture(SLOT_PANEL_TEXTURE, startAtX, abilitiesStartY, 0, 0, 0, 20, 20, 20, 20);
            
            var isHovered = mouseX > startAtX && mouseX < startAtX + 20 && mouseY > abilitiesStartY && mouseY < abilitiesStartY + 20;
            if (isHovered) {
                context.drawTooltip(this.textRenderer, Text.translatable("drones.ability." + ability.name().toLowerCase()), startAtX, abilitiesStartY + 40);
            }
            
            var renderedItem = DroneBehaviour.getItem(ability);
            drawItem(context, renderedItem, startAtX + 2, abilitiesStartY + 2, 16);
            
            index++;
        }
        
        if (index == 0) {
            context.drawText(this.textRenderer, Text.literal("No Abilities"), abilitiesStartX + 5, abilitiesStartY + 7, textColor, false);
        }
        
        
    }
    
    private void drawBarPart(DrawContext context, int backgroundStartX, int backgroundStartY, float fillStart, float fillEnd, int color, int height, int yOffset) {
        var speedFromX = backgroundStartX + 161 + (int) (124 * fillStart);
        var speedFromY = backgroundStartY + 26 + yOffset;
        var speedToX = backgroundStartX + 161 + (int) (124 * fillEnd);
        var speedToY = speedFromY + height;
        context.fill(speedFromX, speedFromY, speedToX, speedToY, 10, color);
    }
    
    private void renderBlock(DrawContext context, Vec3i offset, BlockState state, @Nullable BlockEntity entity, float partialTicks) {
        
        var x = this.width / 2 - (300 / 2) + 70;
        var y = this.height / 2 - 15;
        
        var size = 20;
        var age = System.nanoTime() - openTime;
        var rotation = (age / 30_000_000f) % 360;
        
        var scale = droneData.getRenderScale();
        
        context.getMatrices().push();
        
        context.getMatrices().translate(x + size / 2f, y + size / 2f, 400);
        context.getMatrices().scale(40 * size / 64f, -40 * size / 64f, 40);
        context.getMatrices().scale(scale, scale, scale);
        
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(30 + previewAngle));
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180 + rotation));
        
        context.getMatrices().translate(-.5 + offset.getX(), -.5 + offset.getY(), -.5 + offset.getZ());
        
        RenderSystem.runAsFancy(() -> {
            final var vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            if (state.getRenderType() != BlockRenderType.ENTITYBLOCK_ANIMATED) {
                this.client.getBlockRenderManager().renderBlockAsEntity(
                  state, context.getMatrices(), vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV
                );
            }
            
            if (entity != null) {
                var entityRenderer = this.client.getBlockEntityRenderDispatcher().get(entity);
                if (entityRenderer != null) {
                    entityRenderer.render(entity, partialTicks, context.getMatrices(), vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
                }
            }
            
            RenderSystem.setShaderLights(new Vector3f(-1.5f, -.5f, 0), new Vector3f(0, -1, 0));
            vertexConsumers.draw();
            DiffuseLighting.enableGuiDepthLighting();
        });
        
        context.getMatrices().pop();
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        
        if (Math.abs(deltaY) > 0.001) {
            var centerX = this.width / 2;
            var centerY = this.height / 2;
            var backgroundStartX = centerX - (300 / 2);
            var backgroundStartY = centerY - (183 / 2);
            
            var previewStartX = backgroundStartX + 5;
            var previewEndX = previewStartX + 140;
            var previewStartY = backgroundStartY + 5;
            var previewEndY = previewStartY + 170;
            
            if (mouseX < previewEndX && mouseX > previewStartX && mouseY < previewEndY && mouseY > previewStartY) {
                previewAngle += deltaY;
            }
            
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private float getSpeedProgress() {
        return Math.clamp(droneData.power / 10f, 0.01f, 1);
    }
    
    private float getSizeProgress() {
        return Math.clamp(droneData.getSize() / 10f, 0.1f, 1);
    }
    
    private static void drawItem(DrawContext context, Item item, int x, int y, int size) {
        var itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        var entityBuffers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        var stack = new ItemStack(item);
        
        final boolean notSideLit = !itemRenderer.getModel(stack, null, null, 0).isSideLit();
        if (notSideLit) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        
        var matrices = context.getMatrices();
        matrices.push();
        
        // Translate to the root of the component
        matrices.translate(x, y, 100);
        
        // Scale according to component size and translate to the center
        matrices.scale(size / 16f, size / 16f, 1);
        matrices.translate(8.0, 8.0, 0.0);
        
        // Vanilla scaling and y inversion
        if (notSideLit) {
            matrices.scale(16, -16, 16);
        } else {
            matrices.multiplyPositionMatrix(new Matrix4f().scaling(16, -16, 16));
        }
        
        var client = MinecraftClient.getInstance();
        
        itemRenderer.renderItem(
          stack,
          ModelTransformationMode.GUI,
          LightmapTextureManager.MAX_LIGHT_COORDINATE,
          OverlayTexture.DEFAULT_UV,
          matrices, entityBuffers,
          client.world,
          0);
        entityBuffers.draw();
        
        // Clean up
        matrices.pop();
        
        if (notSideLit) {
            DiffuseLighting.enableGuiDepthLighting();
        }
    }
    
    private static class BigDroneButton extends ButtonWidget {
        
        private boolean isPressed = false;
        
        protected BigDroneButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }
        
        @Override
        public void onPress() {
            isPressed = true;
            super.onPress();
        }
        
        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            var valid = super.mouseReleased(mouseX, mouseY, button);
            if (valid) isPressed = false;
            return valid;
        }
        
        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            
            var usedTexture = BIG_BUTTON_TEXTURE;
            
            if (this.isHovered())
                usedTexture = BIG_BUTTON_HOVER_TEXTURE;
            
            if (isPressed)
                usedTexture = BIG_BUTTON_PRESSED_TEXTURE;
            
            
            context.drawTexture(usedTexture, this.getX(), this.getY(), 0, 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
            
            var scale = 3.4f;
            
            var textX = this.getX() + 11;
            var textY = this.getY() + 13;
            
            var textColor = 13685204;
            
            if (this.isHovered())
                textY += 4;
            
            if (this.isPressed)
                textY += 6;
            
            
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, scale);
            
            textX /= scale;
            textY /= scale;
            
            context.drawText(MinecraftClient.getInstance().textRenderer, this.getMessage(), textX, textY, textColor, false);
            
            context.getMatrices().pop();
        }
    }
}
