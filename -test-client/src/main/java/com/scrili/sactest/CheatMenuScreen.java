package com.scrili.sactest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CheatMenuScreen extends Screen {

    private static final int W = 200;
    private static final int H = 260;

    public CheatMenuScreen() {
        super(Component.literal("SAC Test Client"));
    }

    @Override
    protected void init() {
        int x = (this.width - W) / 2;
        int y = (this.height - H) / 2 + 10;
        int bw = W - 20;
        int bh = 20;
        int gap = 24;

        addToggle(x+10, y,           bw, bh, "WallHack",     CheatManager.wallHack,  v -> CheatManager.wallHack  = v);
        addToggle(x+10, y+gap,       bw, bh, "AimBot SNAP",  CheatManager.aimBot && CheatManager.aimBotMode == 0,
            v -> { CheatManager.aimBot = v; CheatManager.aimBotMode = 0; });
        addToggle(x+10, y+gap*2,     bw, bh, "AimBot SMOOTH",CheatManager.aimBot && CheatManager.aimBotMode == 1,
            v -> { CheatManager.aimBot = v; CheatManager.aimBotMode = 1; });
        addToggle(x+10, y+gap*3,     bw, bh, "Silent Aim",   CheatManager.silentAim, v -> CheatManager.silentAim = v);
        addToggle(x+10, y+gap*4,     bw, bh, "Fly",          CheatManager.fly,       v -> CheatManager.fly       = v);
        addToggle(x+10, y+gap*5,     bw, bh, "Velocity",     CheatManager.velocity,  v -> CheatManager.velocity  = v);
        addToggle(x+10, y+gap*6,     bw, bh, "Timer 1.5x",   CheatManager.timer,     v -> CheatManager.timer     = v);
        addToggle(x+10, y+gap*7,     bw, bh, "Freecam",      CheatManager.freecam,   v -> {
            CheatManager.freecam = v;
            if (v) FreecamTrigger.trigger();
        });

        this.addRenderableWidget(Button.builder(
            Component.literal("Закрыть"), btn -> this.onClose()
        ).bounds(x+10, y+gap*8+4, bw, bh).build());
    }

    private void addToggle(int x, int y, int w, int h, String label, boolean current,
                           java.util.function.Consumer<Boolean> setter) {
        final boolean[] state = { current };
        this.addRenderableWidget(Button.builder(
            Component.literal(getLabel(label, state[0])),
            btn -> {
                state[0] = !state[0];
                setter.accept(state[0]);
                btn.setMessage(Component.literal(getLabel(label, state[0])));
            }
        ).bounds(x, y, w, h).build());
    }

    private String getLabel(String name, boolean on) {
        return (on ? "§a[ВКЛ] " : "§c[ВЫКЛ] ") + "§f" + name;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = (this.width - W) / 2;
        int y = (this.height - H) / 2;
        graphics.fill(x, y, x+W, y+H, 0xCC000000);
        graphics.fill(x, y, x+W, y+14, 0xFF333333);
        graphics.drawCenteredString(this.font, "§6SAC Test Client", this.width/2, y+3, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
