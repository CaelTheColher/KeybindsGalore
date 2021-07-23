/*
 * This class is modified from the PSI mod created by Vazkii
 * Psi Source Code: https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package me.cael.keybindsgalore;

import com.mojang.blaze3d.systems.RenderSystem;
import me.cael.keybindsgalore.mixins.AccessorKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

//If I had a dollar for every time I had to copy code from a Vazkii mod, I would have 2 dollars. Which isn't a lot, but its weird it happened twice.
public class KeybindsScreen extends Screen {

    int timeIn = 0;
    int slotSelected = -1;

    private InputUtil.Key conflictedKey = InputUtil.UNKNOWN_KEY;

    final MinecraftClient mc;

    public KeybindsScreen() {
        super(NarratorManager.EMPTY);
        mc = MinecraftClient.getInstance();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int x = width / 2;
        int y = height / 2;
        int maxRadius = 80;

        double angle = mouseAngle(x, y, mouseX, mouseY);

        int segments = KeybindsManager.getConflicting(conflictedKey).size();
        float step = (float) Math.PI / 180;
        float degPer = (float) Math.PI * 2 / segments;

        slotSelected = -1;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = degPer * seg < angle && angle < degPer * (seg + 1);
            float radius = Math.max(0F, Math.min((timeIn + delta - seg * 6F / segments) * 40F, maxRadius));
            if (mouseInSector) {
                radius *= 1.025f;
            }

            int gs = 0x40;
            if (seg % 2 == 0) {
                gs += 0x19;
            }
            int r = gs;
            int g = gs;
            int b = gs;
            int a = 0x66;

            if (seg == 0) {
                buf.vertex(x, y, 0).color(r, g, b, a).next();
            }

            if (mouseInSector) {
                slotSelected = seg;
                r = g = b = 0xFF;
            }

            for (float i = 0; i < degPer + step / 2; i += step) {
                float rad = i + seg * degPer;
                float xp = x + MathHelper.cos(rad) * radius;
                float yp = y + MathHelper.sin(rad) * radius;

                if (i == 0) {
                    buf.vertex(xp, yp, 0).color(r, g, b, a).next();
                }
                buf.vertex(xp, yp, 0).color(r, g, b, a).next();
            }
        }
        tess.draw();

        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = degPer * seg < angle && angle < degPer * (seg + 1);
            float radius = Math.max(0F, Math.min((timeIn + delta - seg * 6F / segments) * 40F, maxRadius));
            if (mouseInSector) {
                radius *= 1.025f;
            }

            float rad = (seg + 0.5f) * degPer;
            float xp = x + MathHelper.cos(rad) * radius;
            float yp = y + MathHelper.sin(rad) * radius;

            String boundKey = new TranslatableText(KeybindsManager.getConflicting(conflictedKey).get(seg).getTranslationKey()).getString();
            float xsp = xp - 4;
            float ysp = yp;
            String name = (mouseInSector ? Formatting.UNDERLINE : Formatting.RESET) + boundKey;
            int width = textRenderer.getWidth(name);
            if (xsp < x) {
                xsp -= width - 8;
            }
            if (ysp < y) {
                ysp -= 9;
            }
            textRenderer.drawWithShadow(matrices, name, xsp, ysp, 0xFFFFFF);
        }
    }

    public void setConflictedKey(InputUtil.Key key) {
        this.conflictedKey = key;
    }

    private static double mouseAngle(int x, int y, int mx, int my) {
        return (MathHelper.atan2(my - y, mx - x) + Math.PI * 2) % (Math.PI * 2);
    }

    @Override
    public void tick() {
        super.tick();
        if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), conflictedKey.getCode())) {
            mc.setScreen(null);
            if (slotSelected != -1) {
                KeyBinding bind = KeybindsManager.getConflicting(conflictedKey).get(slotSelected);
                ((AccessorKeyBinding) bind).setPressed(true);
                ((AccessorKeyBinding) bind).setTimesPressed(1);
            }
        }
        timeIn++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
