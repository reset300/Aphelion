package com.BreadRes.astronautics.content.blocks.control_panel.screen.modules;

import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import com.BreadRes.astronautics.network.ControlPanelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class EnginesModule implements PanelModule {

    private final ControlPanelBlockEntity be;
    private int x, y, w, h;

    private int CARD_W, CARD_H, PADDING, SPACING, HEADER_H, FOOTER_H;
    private float textScale = 1f;

    private int cols, rows, perPage;
    private int page = 0;
    private int prevArrowX, nextArrowX, arrowY, arrowW, arrowH;

    private final List<EngineWidget> widgets = new ArrayList<>();

    public EnginesModule(ControlPanelBlockEntity be) {
        this.be = be;
    }

    @Override public String getName() { return "ENGINES"; }

    private void drawScaledString(GuiGraphics g, String text, int x, int y, int color, float scale) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1f);
        g.drawString(Minecraft.getInstance().font, text, 0, 0, color);
        pose.popPose();
    }

    private void drawScaledCenteredString(GuiGraphics g, String text, int cx, int y, int color, float scale) {
        int textW = (int)(Minecraft.getInstance().font.width(text) * scale);
        drawScaledString(g, text, cx - textW / 2, y, color, scale);
    }

    @Override
    public void init(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        PADDING  = 14;
        HEADER_H = 22;
        FOOTER_H = 16;
        rebuild();
    }

    private void rebuild() {
        widgets.clear();
        List<ControlPanelBlockEntity.EngineEntry> engines = be.getEngines();

        int total = engines.size();
        int from  = page * 10;
        int count = Math.min(10, total - from);
        if (count <= 0) return;

        cols = count <= 1 ? 1
                : count <= 2 ? 2
                : count <= 4 ? 2
                : count <= 6 ? 3
                : count <= 9 ? 3
                : 4;

        rows = (int) Math.ceil(count / (float) cols);

        int innerW = w - PADDING * 2;
        int innerH = h - HEADER_H - FOOTER_H - PADDING * 2;

        float scaleX = innerW / (float)(cols * 110 + (cols - 1) * 8);
        float scaleY = innerH / (float)(rows * 160 + (rows - 1) * 8);
        float scale  = Math.min(scaleX, scaleY);

        CARD_W    = (int)(100 * scale);
        CARD_H    = (int)(160 * scale);
        SPACING   = Math.max(4, (int)(8 * scale));
        textScale = Math.max(0.5f, Math.min(1f, scale));
        perPage   = 10;

        int totalGridW = cols * (CARD_W + SPACING) - SPACING;
        int totalGridH = rows * (CARD_H + SPACING) - SPACING;
        int startX = x + PADDING + (innerW - totalGridW) / 2;
        int startY = y + HEADER_H + PADDING + (innerH - totalGridH) / 2;

        int to = Math.min(from + count, total);
        for (int i = from; i < to; i++) {
            int local = i - from;
            int col   = local % cols;
            int row   = local / cols;
            int cx    = startX + col * (CARD_W + SPACING);
            int cy    = startY + row * (CARD_H + SPACING);
            widgets.add(new EngineWidget(engines.get(i), cx, cy, i));
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(x, y, x + w, y + h, 0xFF060C04);

        for (int i = y; i < y + h; i += 4)
            g.fill(x, i, x + w, i + 1, 0x0D000000);
        for (int i = x; i < x + w; i += 4)
            g.fill(i, y, i + 1, y + h, 0x07000000);

        g.renderOutline(x,     y,     w,     h,     0xFF3A5C3A);
        g.renderOutline(x + 2, y + 2, w - 4, h - 4, 0xFF1E2E1E);

        drawHeader(g);
        for (EngineWidget card : widgets) card.render(g, mx, my);
        drawEmptySlots(g);
        drawFooter(g);
    }

    private void drawHeader(GuiGraphics g) {
        var font = Minecraft.getInstance().font;

        g.fill(x + 3, y + 3, x + w - 3, y + HEADER_H - 1, 0xFF0B1509);
        g.fill(x + 3, y + HEADER_H - 2, x + w - 3, y + HEADER_H - 1, 0xFF2A4A2A);

        boolean blink = (System.currentTimeMillis() / 600) % 2 == 0;
        g.fill(x + 8, y + 7, x + 14, y + 13, blink ? 0xFF4FAF6F : 0xFF1A5A2A);
        g.renderOutline(x + 8, y + 7, 6, 6, 0xFF2A3A2A);

        g.drawString(font, "ENGINE CONTROL", x + 20, y + 7, 0xFFB89A4A);

        int total   = be.getEngines().size();
        int active  = (int) be.getEngines().stream().filter(e -> e.active).count();
        int maxPage = Math.max(1, (int) Math.ceil(total / (float) perPage));

        String pageStr = (page + 1) + "/" + maxPage;
        int pw = font.width(pageStr);

        int arrowRX = x + w - 10;
        int arrowLX = arrowRX - pw - 20;
        int ay = y + 6;

        boolean canPrev = page > 0;
        boolean canNext = page < maxPage - 1;

        g.drawString(font, "<", arrowLX, ay, canPrev ? 0xFFB89A4A : 0xFF3A4A3A);
        g.drawString(font, pageStr, arrowLX + 10, ay, 0xFF5A7A5A);
        g.drawString(font, ">", arrowLX + 10 + pw + 4, ay, canNext ? 0xFFB89A4A : 0xFF3A4A3A);

        prevArrowX = arrowLX;
        nextArrowX = arrowLX + 10 + pw + 4;
        arrowY     = ay;
        arrowW     = font.width("<");
        arrowH     = font.lineHeight;

        String info = active + "/" + total + " ACTIVE";
        g.drawString(font, info, x + 20 + font.width("ENGINE CONTROL") + 10, y + 7,
                active > 0 ? 0xFF4FAF6F : 0xFF5A5A5A);
    }

    private void drawEmptySlots(GuiGraphics g) {
        int total   = be.getEngines().size();
        int from    = page * perPage;
        int showing = Math.min(perPage, total - from);
        int empty   = perPage - showing;
        if (empty <= 0 || widgets.isEmpty()) return;

        int innerW     = w - PADDING * 2;
        int totalGridW = cols * (CARD_W + SPACING) - SPACING;
        int startX     = x + PADDING + Math.max(0, (innerW - totalGridW) / 2);
        int startY     = y + HEADER_H + PADDING;

        for (int i = 0; i < empty; i++) {
            int local = showing + i;
            int col   = local % cols;
            int row   = local / cols;
            int sx    = startX + col * (CARD_W + SPACING);
            int sy    = startY + row * (CARD_H + SPACING);

            g.fill(sx, sy, sx + CARD_W, sy + CARD_H, 0xFF080E06);
            g.renderOutline(sx, sy, CARD_W, CARD_H, 0xFF1A2A1A);
            drawScaledCenteredString(g, "- - -", sx + CARD_W / 2, sy + CARD_H / 2 - 3, 0xFF2A3A2A, textScale);
        }
    }

    private void drawFooter(GuiGraphics g) {
        int fy = y + h - FOOTER_H;
        g.fill(x + 3, fy, x + w - 3, fy + 1, 0xFF2A4A2A);
        g.fill(x + 3, fy + 1, x + w - 3, y + h - 3, 0xFF080E06);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int total   = be.getEngines().size();
        int maxPage = Math.max(1, (int) Math.ceil(total / (float) perPage));

        if (my >= arrowY && my <= arrowY + arrowH) {
            if (mx >= prevArrowX && mx <= prevArrowX + arrowW && page > 0) {
                page--; rebuild(); return true;
            }
            if (mx >= nextArrowX && mx <= nextArrowX + arrowW && page < maxPage - 1) {
                page++; rebuild(); return true;
            }
        }

        for (EngineWidget w : widgets)
            if (w.mouseClicked(mx, my, button)) return true;
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (EngineWidget w : widgets)
            if (w.mouseDragged(mx, my)) return true;
        return false;
    }

    @Override
    public void mouseReleased(double mx, double my, int button) {
        for (EngineWidget w : widgets) w.mouseReleased();
    }

    @Override public boolean keyPressed(int key, int scan, int mod) { return false; }
    @Override public boolean charTyped(char c, int mod) { return false; }

    private class EngineWidget {
        final ControlPanelBlockEntity.EngineEntry entry;
        final int x, y, index;
        boolean dragging = false;

        int BAR_X, BAR_Y, BAR_W, BAR_H;

        EngineWidget(ControlPanelBlockEntity.EngineEntry entry, int x, int y, int index) {
            this.entry = entry;
            this.x     = x;
            this.y     = y;
            this.index = index;

            BAR_X = CARD_W / 8;
            BAR_Y = (int)(CARD_H * 0.38f);
            BAR_W = (int)(CARD_W * 0.28f);
            BAR_H = (int)(CARD_H * 0.50f);
        }

        void render(GuiGraphics g, int mx, int my) {
            boolean hover = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;

            int borderColor = entry.active ? 0xFF3A6A3A : 0xFF3A3A2A;
            int bgColor     = entry.active ? 0xFF0A140A : 0xFF0C0C08;

            g.fill(x, y, x + CARD_W, y + CARD_H, bgColor);
            if (hover) g.fill(x, y, x + CARD_W, y + CARD_H, 0x08FFFFFF);
            g.renderOutline(x, y, CARD_W, CARD_H, borderColor);
            g.fill(x + 2, y + 2, x + CARD_W - 2, y + 3, entry.active ? 0xFF2A5A2A : 0xFF3A3A1A);

            renderTitleBar(g);
            renderStatusStrip(g);
            renderThrottleBar(g, mx, my);
            renderThrustMeter(g);
            renderToggleButton(g, mx, my);
        }

        private void renderTitleBar(GuiGraphics g) {
            String label = "ENG-" + String.format("%02d", index + 1);
            drawScaledCenteredString(g, label, x + CARD_W / 2, y + 6, 0xFFB89A4A, textScale);
            g.fill(x + 4, y + (int)(16 * textScale) + 4, x + CARD_W - 4, y + (int)(16 * textScale) + 5, 0xFF1E3A1E);
        }

        private void renderStatusStrip(GuiGraphics g) {
            int stripH = Math.max(8, (int)(10 * textScale));
            int sy = y + (int)(20 * textScale) + 4;

            String status;
            int color, stripColor;

            if (!entry.active) {
                status = "STANDBY"; color = 0xFF7A7A4A; stripColor = 0xFF1A1A0A;
            } else if (!entry.hasFuel) {
                status = "NO FUEL"; color = 0xFFB04A3A; stripColor = 0xFF4A1A1A;
            } else if (entry.ignition >= 100) {
                status = "WORKING"; color = 0xFF4FFFFF; stripColor = 0xFF0A2A3A;
            } else {
                status = "FIRING";  color = 0xFF4FAF6F; stripColor = 0xFF1A4A1A;
            }

            g.fill(x + 4, sy, x + CARD_W - 4, sy + stripH, stripColor);
            g.renderOutline(x + 4, sy, CARD_W - 8, stripH, 0xFF2A3A2A);
            drawScaledCenteredString(g, status, x + CARD_W / 2, sy + 1, color, textScale);
        }

        private void renderThrottleBar(GuiGraphics g, int mx, int my) {
            int bx = x + BAR_X;
            int by = y + BAR_Y;

            drawScaledString(g, "THR", bx, by - (int)(10 * textScale), 0xFF6A8A6A, textScale);

            g.fill(bx, by, bx + BAR_W, by + BAR_H, 0xFF040A03);
            g.renderOutline(bx, by, BAR_W, BAR_H, 0xFF2A3A2A);

            int fill  = (int)((entry.throttle / 100f) * BAR_H);
            int fillY = by + BAR_H - fill;

            int barColor = entry.throttle > 75 ? 0xFFCFA040
                    : entry.throttle > 40 ? 0xFF8AAA4A
                    : 0xFF4A8A4A;
            g.fill(bx + 2, fillY, bx + BAR_W - 2, by + BAR_H, barColor);

            for (int t = 0; t <= 4; t++) {
                int ty        = by + BAR_H - (t * BAR_H / 4);
                boolean major = (t % 2 == 0);
                int tickColor = major ? 0xFF6A8A6A : 0xFF3A5A3A;
                g.fill(bx - 3, ty, bx, ty + 1, tickColor);
                if (major) {
                    String lbl = String.valueOf(t * 25);
                    int lw = (int)(Minecraft.getInstance().font.width(lbl) * textScale);
                    drawScaledString(g, lbl, bx - 3 - lw, ty - 3, 0xFF4A6A4A, textScale);
                }
            }

            boolean overBar = mx >= bx && mx <= bx + BAR_W && my >= by && my <= by + BAR_H;
            if (dragging || overBar)
                g.fill(bx - 1, fillY - 1, bx + BAR_W + 1, fillY + 2, 0xFFFFFFAA);

            drawScaledCenteredString(g, entry.throttle + "%", bx + BAR_W / 2, by + BAR_H + 3, 0xFFB89A4A, textScale);
        }

        private void renderThrustMeter(GuiGraphics g) {
            int mx2 = x + BAR_X + BAR_W + 10;
            int my2 = y + BAR_Y;
            int mw  = CARD_W - BAR_X - BAR_W - 14;
            int mh  = BAR_H;

            drawScaledString(g, "KN", mx2, my2 - (int)(10 * textScale), 0xFF6A8A6A, textScale);

            float thrust   = entry.active ? 180f * (entry.throttle / 100f) : 0f;
            int segments   = 10;
            int segH       = (mh - segments) / segments;

            for (int s = 0; s < segments; s++) {
                int sy2 = my2 + mh - (s + 1) * (segH + 1);
                float threshold = (s + 1f) / segments;
                boolean lit = entry.active && (thrust / 180f) >= threshold;

                int segColor;
                if (!lit)       segColor = 0xFF0D1A0D;
                else if (s < 4) segColor = 0xFF2A7A2A;
                else if (s < 7) segColor = 0xFF8AAA2A;
                else            segColor = 0xFFCFA040;

                g.fill(mx2, sy2, mx2 + mw, sy2 + segH, segColor);
                g.fill(mx2, sy2 + segH, mx2 + mw, sy2 + segH + 1, 0xFF060C04);
            }

            g.renderOutline(mx2, my2, mw, mh, 0xFF2A3A2A);
            drawScaledCenteredString(g, (int) thrust + "", mx2 + mw / 2, my2 + mh + 3, 0xFF8AAA6A, textScale);
        }

        private void renderToggleButton(GuiGraphics g, int mx, int my) {
            int bh  = Math.max(10, (int)(12 * textScale));
            int bw  = CARD_W - 8;
            int bx  = x + 4;
            int by2 = y + CARD_H - bh - 4;

            boolean hover = mx >= bx && mx <= bx + bw && my >= by2 && my <= by2 + bh;

            int bgColor = entry.active
                    ? (hover ? 0xFF2A6A2A : 0xFF1E521E)
                    : (hover ? 0xFF5A3A1A : 0xFF3A2A10);

            g.fill(bx, by2, bx + bw, by2 + bh, bgColor);
            g.renderOutline(bx, by2, bw, bh, entry.active ? 0xFF4FAF6F : 0xFFB89A4A);

            g.fill(bx,          by2,          bx + 3,     by2 + 1,  entry.active ? 0xFF4FAF6F : 0xFFB89A4A);
            g.fill(bx + bw - 3, by2,          bx + bw,    by2 + 1,  entry.active ? 0xFF4FAF6F : 0xFFB89A4A);
            g.fill(bx,          by2 + bh - 1, bx + 3,     by2 + bh, entry.active ? 0xFF4FAF6F : 0xFFB89A4A);
            g.fill(bx + bw - 3, by2 + bh - 1, bx + bw,   by2 + bh, entry.active ? 0xFF4FAF6F : 0xFFB89A4A);

            String label    = entry.active ? "[ SHUTDOWN ]" : "[  IGNITE  ]";
            int    txtColor = entry.active ? 0xFF4FAF6F : 0xFFCFA040;
            drawScaledCenteredString(g, label, bx + bw / 2, by2 + 2, txtColor, textScale);
        }

        boolean mouseClicked(double mx, double my, int button) {
            int bh  = Math.max(10, (int)(12 * textScale));
            int bw  = CARD_W - 8;
            int bx  = x + 4;
            int by2 = y + CARD_H - bh - 4;

            if (mx >= bx && mx <= bx + bw && my >= by2 && my <= by2 + bh) {
                entry.active = !entry.active;
                ControlPanelPayload.sendSetActive(entry.pos, be.getBlockPos(), entry.active);
                return true;
            }

            int barX = x + BAR_X;
            int barY = y + BAR_Y;
            if (mx >= barX && mx <= barX + BAR_W && my >= barY && my <= barY + BAR_H) {
                dragging = true;
                updateThrottle(my, barY);
                return true;
            }

            return false;
        }

        boolean mouseDragged(double mx, double my) {
            if (dragging) { updateThrottle(my, y + BAR_Y); return true; }
            return false;
        }

        void mouseReleased() { dragging = false; }

        private void updateThrottle(double my, int barY) {
            float t = (float)(my - barY) / BAR_H;
            t = Math.max(0, Math.min(1, t));
            entry.throttle = (int)((1f - t) * 100);
            ControlPanelPayload.sendSetThrottle(entry.pos, be.getBlockPos(), entry.throttle);
        }
    }
}