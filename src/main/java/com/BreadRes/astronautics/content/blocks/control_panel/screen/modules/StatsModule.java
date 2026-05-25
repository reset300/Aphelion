package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class StatsModule implements PanelModule {

    private final ControlPanelBlockEntity be;

    private int x, y, w, h;

    private float thrust;
    private float mass;
    private float twr;
    private int activeEngines;
    private int avgThrottle;

    private float smoothedThrust;
    private float smoothedTwr;

    public StatsModule(ControlPanelBlockEntity be) {
        this.be = be;
    }

    @Override
    public String getName() {
        return "STATS";
    }

    @Override
    public void init(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    private void update() {
        List<ControlPanelBlockEntity.EngineEntry> engines = be.getEngines();

        activeEngines = 0;
        int totalThrottle = 0;

        thrust = 0f;

        for (var e : engines) {
            if (e.active) {
                activeEngines++;
                totalThrottle += e.throttle;

                float enginePower = 180f;
                thrust += enginePower * (e.throttle / 100f);
            }
        }

        avgThrottle = activeEngines == 0 ? 0 : totalThrottle / activeEngines;

        mass = 120f + engines.size() * 10f;

        twr = thrust / (mass * 9.81f);

        smoothedThrust += (thrust - smoothedThrust) * 0.1f;
        smoothedTwr += (twr - smoothedTwr) * 0.1f;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        update();

        var font = Minecraft.getInstance().font;

        drawBackground(g);
        drawHeader(g);

        int yy = y + 40;

        drawLine(g, "ENGINES", activeEngines + "/" + be.getEngines().size(), yy); yy += 14;
        drawLine(g, "AVG THR", avgThrottle + "%", yy); yy += 14;

        drawLine(g, "THRUST", (int) smoothedThrust + " kN", yy); yy += 14;
        drawLine(g, "MASS", (int) mass + " t", yy); yy += 14;

        drawLine(g, "TWR", String.format("%.2f", smoothedTwr), yy); yy += 20;

        drawGraph(g, yy + 10);

        drawStatus(g);
    }

    private void drawBackground(GuiGraphics g) {
        g.fill(x, y, x + w, y + h, 0xFF070D05);
        g.renderOutline(x, y, w, h, 0xFFB89A4A);

        for (int i = 0; i < h; i += 2) {
            g.fill(x, y + i, x + w, y + i + 1, 0x0A000000);
        }
    }

    private void drawHeader(GuiGraphics g) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, "⚙ STATS ⚙", x + 10, y + 6, 0xFFB89A4A);
    }

    private void drawLine(GuiGraphics g, String left, String right, int y) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, left, x + 20, y, 0xFFB89A4A);
        g.drawString(font, right, x + w - 100, y, 0xFFB89A4A);
    }

    private void drawGraph(GuiGraphics g, int yStart) {
        int gx = x + 20;
        int gy = yStart;
        int gw = w - 40;
        int gh = 40;

        g.renderOutline(gx, gy, gw, gh, 0xFF2A3A2F);

        int bar = (int)(smoothedTwr * 10);
        bar = Math.min(bar, gw);

        g.fill(gx, gy, gx + bar, gy + gh, 0xFF4FAF6F);
    }

    private void drawStatus(GuiGraphics g) {
        var font = Minecraft.getInstance().font;

        String status = smoothedTwr > 1.2f ? "READY" : "LOW THRUST";
        int color = smoothedTwr > 1.2f ? 0xFF4FAF6F : 0xFFB04A3A;

        g.drawString(font, "[ " + status + " ]", x + w - 120, y + h - 20, color);
    }

    @Override public boolean mouseClicked(double mx, double my, int b) { return false; }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    @Override public void mouseReleased(double mx, double my, int b) {}
    @Override public boolean keyPressed(int key, int scan, int mod) { return false; }
    @Override public boolean charTyped(char c, int m) { return false; }
}