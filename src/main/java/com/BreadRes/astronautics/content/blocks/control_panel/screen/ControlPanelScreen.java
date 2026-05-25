package com.BreadRes.astronautics.content.blocks.control_panel.screen;

import com.BreadRes.astronautics.client.shader.AstronauticsShaders;
import com.BreadRes.astronautics.client.shader.CrtRenderType;
import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import com.BreadRes.astronautics.content.blocks.control_panel.screen.modules.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;

public class ControlPanelScreen extends Screen {

    private final ControlPanelBlockEntity be;
    private List<PanelModule> modules;
    private int current;
    private float progress = 0f;
    private boolean opening = true;

    private int glitchTimer = 0;
    private int target;
    private float switchProgress = 1f;
    private int dir = 0;

    private TextureTarget crtTarget;

    public ControlPanelScreen(ControlPanelBlockEntity be, int startModule) {
        super(Component.literal("Control Panel"));
        this.be = be;
        this.startModule = startModule;
    }

    @Override
    protected void init() {
        modules = List.of(
                new EnginesModule(be),
                new StatsModule(be),
                new FuelModule(be),
                new NotAvailableModule(be),
                new NotAvailableModule(be)
        );

        current = Mth.clamp(startModule, 0, modules.size() - 1);
        target = current;
        switchProgress = 1f;
        dir = 0;

        current = Mth.clamp(startModule, 0, modules.size() - 1);

        for (PanelModule m : modules) {
            m.init(0, 0, width, height);
        }
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        if (modules != null) {
            for (PanelModule m : modules) {
                m.init(0, 0, width, height);
            }
        }
        resizeCrtTarget();
    }

    private void resizeCrtTarget() {
        Minecraft mc = Minecraft.getInstance();
        int fbW = mc.getWindow().getWidth();
        int fbH = mc.getWindow().getHeight();

        if (crtTarget != null) {
            crtTarget.destroyBuffers();
        }

        crtTarget = new TextureTarget(fbW, fbH, true, Minecraft.ON_OSX);
    }

    private void ensureCrtTarget() {
        Minecraft mc = Minecraft.getInstance();
        int fbW = mc.getWindow().getWidth();
        int fbH = mc.getWindow().getHeight();

        if (crtTarget == null || crtTarget.width != fbW || crtTarget.height != fbH) {
            resizeCrtTarget();
        }
    }

    @Override
    public void tick() {
        if (opening) {
            progress = Math.min(1f, progress + 0.1f);
        } else {
            progress = Math.max(0f, progress - 0.14f);
            if (progress <= 0f) super.onClose();
        }
        if (glitchTimer > 0) glitchTimer--;
        if (switchProgress < 1f) {
            switchProgress += 0.12f;

            if (switchProgress >= 1f) {
                switchProgress = 1f;
                current = target;
                dir = 0;
            }
        }
    }

    private void renderGuiIntoTarget(GuiGraphics g, int mouseX, int mouseY, float pt, float eased) {
        int mid = height / 2;

        g.fill(0, 0, width, height, 0xFF000000);

        if (eased < 0.06f) {
            int half = (int)(width * (eased / 0.06f));
            g.fill(width / 2 - half, mid - 1, width / 2 + half, mid + 1, 0xFFFFFFFF);
            return;
        }

        float scaleY = Math.max(0.001f, eased);

        var pose = g.pose();
        pose.pushPose();

        pose.translate(width / 2f, mid, 0);
        pose.scale(1f, scaleY, 1f);
        pose.translate(-width / 2f, -mid, 0);

        int mx = mouseX;
        int my = mouseY;

        renderTabs(g, mx, my);

        float t = switchProgress;
        float smooth = switchProgress * switchProgress * (3f - 2f * switchProgress);
        int slide = (int)((1f - smooth) * width);

        int currentOffset = dir * slide;
        int targetOffset = dir * (slide - width);

        pose.pushPose();
        pose.translate(currentOffset, 0, 0);
        modules.get(current).render(g, mx, my, pt);

        if (glitchTimer > 0) {
            renderGlitch(g);
        }
        pose.popPose();

        if (switchProgress < 1f) {
            pose.pushPose();
            pose.translate(targetOffset, 0, 0);
            modules.get(target).render(g, mx, my, pt);
            pose.popPose();
        }

        pose.popPose();
    }

    private void renderGlitch(GuiGraphics g) {
        for (int i = 0; i < 6; i++) {
            int y = (int)(Math.random() * height);
            int h = 2 + (int)(Math.random() * 3);

            int xShift = (int)(Math.random() * 20 - 10);

            g.fill(
                    xShift,
                    y,
                    xShift + width,
                    y + h,
                    0x22FFFFFF
            );
        }
    }
    private final List<Tab> tabs = new ArrayList<>();

    private class Tab {
        String name;
        int x, y, width, height;

        Tab(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean mouseOver(double mx, double my) {
            return mx >= x && mx <= x + width &&
                    my >= y && my <= y + height;
        }
    }
    private void renderTabs(GuiGraphics g, int mx, int my) {
        var font = Minecraft.getInstance().font;

        int tx = 120;
        int ty = 10;

        tabs.clear();

        for (int i = 0; i < modules.size(); i++) {
            String name = modules.get(i).getName();
            int w = font.width(name) + 16;

            boolean hover = mx >= tx && mx <= tx + w && my >= ty && my <= ty + 10;
            int bg = (i == current) ? 0x55222222 : 0x33000000;
            int color = (i == current)
                    ? 0xFFFFFFFF
                    : (hover ? 0xFFCCCCCC : 0xFFB89A4A);

            g.fill(tx, ty, tx + w, ty + 10, bg);
            g.renderOutline(tx, ty, w, 10, 0xFFB89A4A);
            g.drawString(font, name, tx + 6, ty + 1, color);

            tabs.add(new Tab(name, tx, ty, w, 10));

            tx += w + 8;
        }
    }

    private int startModule;



    private double[] fixMouse(double mx, double my) {
        double x = mx / width;
        double y = my / height;

        double cx = x * 2.0 - 1.0;
        double cy = y * 2.0 - 1.0;

        double k = 0.08;

        cx = Math.atan(cx * Math.tan(k)) / k;
        cy = Math.atan(cy * Math.tan(k)) / k;

        return new double[]{
                (cx * 0.5 + 0.5) * width,
                (cy * 0.5 + 0.5) * height
        };
    }

    private void renderCRT(GuiGraphics g, float pt) {
        Minecraft mc = Minecraft.getInstance();

        var shader = AstronauticsShaders.crt();
        if (shader != null) {
            var u = shader.getUniform("GameTime");
            if (u != null && mc.level != null) {
                u.set((mc.level.getGameTime() + pt) / 20f);
            }

            u = shader.getUniform("ScreenWidth");
            if (u != null) u.set((float) width);

            u = shader.getUniform("ScreenHeight");
            if (u != null) u.set((float) height);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, crtTarget.getColorTextureId());

        var buffer = mc.renderBuffers().bufferSource();
        var pose = g.pose();

        pose.pushPose();

        var vc = buffer.getBuffer(CrtRenderType.crt());
        Matrix4f m = pose.last().pose();

        vc.addVertex(m, 0, height, 0).setColor(255,255,255,255).setUv(0, 0);
        vc.addVertex(m, width, height, 0).setColor(255,255,255,255).setUv(1, 0);
        vc.addVertex(m, width, 0, 0).setColor(255,255,255,255).setUv(1, 1);
        vc.addVertex(m, 0, 0, 0).setColor(255,255,255,255).setUv(0, 1);

        buffer.endBatch(CrtRenderType.crt());

        pose.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        if (modules == null || modules.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ensureCrtTarget();

        float t = Mth.lerp(pt, progress, progress);
        float eased = t * t * (3f - 2f * t);

        crtTarget.bindWrite(true);
        RenderSystem.clearColor(0f, 0f, 0f, 1f);
        RenderSystem.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        double[] fixed = fixMouse(mouseX, mouseY);

        renderGuiIntoTarget(g,
                (int) fixed[0],
                (int) fixed[1],
                pt,
                eased
        );

        mc.renderBuffers().bufferSource().endBatch();

        mc.getMainRenderTarget().bindWrite(true);
        RenderSystem.clearColor(0f, 0f, 0f, 1f);
        RenderSystem.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        renderCRT(g, pt);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { opening = false; return true; }

        if (modules == null || modules.isEmpty()) return false;

        if (switchProgress < 1f) return true;

        if (key == 65) {
            current = (current - 1 + modules.size()) % modules.size();
            glitchTimer = 6;
            return true;
        }

        if (key == 68) {
            current = (current + 1) % modules.size();
            glitchTimer = 6;
            return true;
        }

        return modules.get(current).keyPressed(key, scanCode, modifiers)
                || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (progress < 0.98f || modules == null || modules.isEmpty()) return false;

        double[] fixed = fixMouse(mx, my);

        var font = Minecraft.getInstance().font;

        int tx = 120;
        int ty = 10;

        for (int i = 0; i < modules.size(); i++) {
            String name = modules.get(i).getName();
            int w = font.width(name) + 16;

            if (fixed[0] >= tx && fixed[0] <= tx + w &&
                    fixed[1] >= ty && fixed[1] <= ty + 10) {

                if (i != current) {
                    current = i;
                    glitchTimer = 6;
                }

                return true;
            }

            tx += w + 8;
        }

        return modules.get(current).mouseClicked(fixed[0], fixed[1], button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (progress < 0.98f || modules == null) return false;

        double[] fixed = fixMouse(mx, my);

        return modules.get(current).mouseDragged(fixed[0], fixed[1], button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (progress >= 0.98f && modules != null) {
            double[] fixed = fixMouse(mx, my);
            modules.get(current).mouseReleased(fixed[0], fixed[1], button);
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return progress >= 0.98f &&
                modules != null &&
                modules.get(current).charTyped(c, modifiers)
                || super.charTyped(c, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        opening = false;
    }

    @Override
    public void removed() {
        super.removed();
        if (crtTarget != null) {
            crtTarget.destroyBuffers();
            crtTarget = null;
        }
    }
}