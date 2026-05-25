package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class EmptyModule implements PanelModule {
    private int x, y, w, h;

    @Override public String getName() { return "Empty"; }
    @Override public void init(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(x, y, x + w, y + h, 0x44333333);
        g.renderOutline(x, y, w, h, 0xFF333333);
    }
    @Override public boolean mouseClicked(double mx, double my, int b) { return false; }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    @Override public void mouseReleased(double mx, double my, int b) {}
    @Override public boolean keyPressed(int k, int s, int m) { return false; }
    @Override public boolean charTyped(char c, int m) { return false; }
}