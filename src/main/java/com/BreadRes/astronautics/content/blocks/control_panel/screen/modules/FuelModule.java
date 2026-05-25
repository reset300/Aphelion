package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class FuelModule implements PanelModule {

    private final ControlPanelBlockEntity be;

    private int x, y, w, h;

    private float flow;
    private float smoothedFlow;

    public FuelModule(ControlPanelBlockEntity be) {
        this.be = be;
    }

    @Override
    public String getName() {
        return "FUEL";
    }

    @Override
    public void init(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    private void update() {
        flow = 0f;

        for (var e : be.getEngines()) {
            if (e.active) {
                flow += e.flow;
            }
        }

        smoothedFlow += (flow - smoothedFlow) * 0.15f;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        update();

        Font font = Minecraft.getInstance().font;

        drawBackground(g);
        drawHeader(g);

        int totalU = 0;
        int totalN = 0;

        for (var e : be.getEngines()) {
            totalU += e.udmh;
            totalN += e.n2o4;
        }

        float maxA = be.getMaxUDMH();
        float maxB = be.getMaxN2O4();

        float pctA = maxA == 0 ? 0 : (totalU / maxA) * 100f;
        float pctB = maxB == 0 ? 0 : (totalN / maxB) * 100f;

        int y0 = y + 50;

        drawTank(g, "UDMH", pctA, x + 40, y0);
        drawTank(g, "N2O4", pctB, x + 40, y0 + 50);

        drawFlow(g);
        drawStatus(g, totalU, totalN);
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
        g.drawString(font, "⚙ FUEL SYSTEM ⚙", x + 10, y + 6, 0xFFB89A4A);
    }

    private void drawTank(GuiGraphics g, String name, float val, int x, int y) {
        var font = Minecraft.getInstance().font;

        int bw = w - 120;
        int bh = 12;

        int fill = (int)((val / 100f) * bw);

        g.drawString(font, name, x, y - 12, 0xFFB89A4A);

        g.fill(x, y, x + bw, y + bh, 0xFF040A03);
        g.fill(x, y, x + fill, y + bh, 0xFFB89A4A);

        g.renderOutline(x, y, bw, bh, 0xFF2A3A2F);

        g.drawString(font, (int)val + "%", x + bw + 8, y, 0xFFB89A4A);
    }

    private void drawFlow(GuiGraphics g) {
        var font = Minecraft.getInstance().font;

        g.drawString(font,
                "FLOW: " + String.format("%.1f", smoothedFlow) + " L/s",
                x + 40,
                y + h - 40,
                0xFFB89A4A);
    }

    private void drawStatus(GuiGraphics g, float a, float b) {
        var font = Minecraft.getInstance().font;

        float avg = (a + b) * 0.5f;

        String status;
        int color;

        if (avg > 50000) {
            status = "STABLE";
            color = 0xFF4FAF6F;
        } else if (avg > 20000) {
            status = "LOW";
            color = 0xFFB89A4A;
        } else {
            status = "CRITICAL";
            color = 0xFFB04A3A;
        }

        g.drawString(font,
                "[ " + status + " ]",
                x + w - 140,
                y + h - 20,
                color);
    }

    @Override public boolean mouseClicked(double mx, double my, int b) { return false; }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    @Override public void mouseReleased(double mx, double my, int b) {}
    @Override public boolean keyPressed(int key, int scan, int mod) { return false; }
    @Override public boolean charTyped(char c, int m) { return false; }
}