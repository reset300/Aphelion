package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class NotAvailableModule implements PanelModule {

    private final ControlPanelBlockEntity be;
    private int x, y, w, h;

    public NotAvailableModule(ControlPanelBlockEntity be) {
        this.be = be;
    }

    @Override public String getName() { return "N/A"; }

    @Override
    public void init(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        var font = Minecraft.getInstance().font;

        g.fill(x, y, x + w, y + h, 0xFF050000);
        g.renderOutline(x, y, w, h, 0xFFB04A3A);

        String text = "NOT AVAILABLE";

        int tx = x + (w - font.width(text)) / 2;
        int ty = y + h / 2 - 5;

        g.drawString(font, text, tx, ty, 0xFFB04A3A);
    }

    @Override public boolean mouseClicked(double mx, double my, int b) { return false; }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    @Override public void mouseReleased(double mx, double my, int b) {}
    @Override public boolean keyPressed(int key, int scan, int mod) { return false; }
    @Override public boolean charTyped(char c, int m) { return false; }
}