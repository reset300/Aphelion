package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public interface PanelModule {
    String getName();
    void init(int x, int y, int width, int height);
    void render(GuiGraphics g, int mx, int my, float pt);
    boolean mouseClicked(double mx, double my, int button);
    boolean mouseDragged(double mx, double my, int button, double dx, double dy);
    void mouseReleased(double mx, double my, int button);
    boolean keyPressed(int key, int scan, int mod);
    boolean charTyped(char c, int mod);
}