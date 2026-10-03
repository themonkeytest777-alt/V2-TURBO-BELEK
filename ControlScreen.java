package com.turbominer.gui;

import com.turbominer.Ghost;
import com.turbominer.ModConfig;
import com.turbominer.Modules;
import com.turbominer.TurboMinerClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.BooleanSupplier;

public class ControlScreen extends Screen {
    private int top;

    public ControlScreen() {
        super(Text.literal("TurboMiner"));
    }

    @Override
    protected void init() {
        int colW = 120;
        int gap = 6;
        int sliderW = 190;
        int totalW = colW + gap + sliderW;
        int x = width / 2 - totalW / 2;
        int y = Math.max(34, height / 2 - 97);
        top = y - 22;
        int sx = x + colW + gap;

        addToggle(x, y, colW, "Minage rapide", () -> ModConfig.fastMine, Modules::toggleFastMine);
        addDrawableChild(new ValueSlider(sx, y, sliderW, 20, "Vitesse minage x", 1, 100,
                ModConfig.miningMultiplier, v -> ModConfig.miningMultiplier = v));
        y += 24;

        addToggle(x, y, colW, "Vitesse", () -> ModConfig.speedHack, Modules::toggleSpeed);
        addDrawableChild(new ValueSlider(sx, y, sliderW, 20, "Vitesse joueur x", 1, 100,
                ModConfig.speedMultiplier, v -> ModConfig.speedMultiplier = v));
        y += 24;

        addToggle(x, y, colW, "Fly", () -> ModConfig.fly, Modules::toggleFly);
        addDrawableChild(new ValueSlider(sx, y, sliderW, 20, "Vitesse fly / ghost x", 1, 20,
                ModConfig.flyMultiplier, v -> ModConfig.flyMultiplier = v));
        y += 24;

        addToggle(x, y, colW, "Step", () -> ModConfig.step, Modules::toggleStep);
        addDrawableChild(new ValueSlider(sx, y, sliderW, 20, "Hauteur step ", 1, 10,
                ModConfig.stepHeight, v -> ModConfig.stepHeight = v));
        y += 28;

        addToggle(x, y, totalW, "Minage auto (clic maintenu)", () -> ModConfig.autoMine, Modules::toggleAutoMine);
        y += 24;

        addToggle(x, y, totalW, "Ghost (traverse les blocs, re-appui = teleportation)", () -> Ghost.active, Modules::toggleGhost);
        y += 28;

        int half = (totalW - gap) / 2;
        addToggle(x, y, half, "Sans delai de minage", () -> ModConfig.noMineDelay, () -> ModConfig.noMineDelay = !ModConfig.noMineDelay);
        addToggle(x + half + gap, y, half, "Affichage HUD", () -> ModConfig.showHud, () -> ModConfig.showHud = !ModConfig.showHud);
        y += 28;

        addDrawableChild(ButtonWidget.builder(Text.literal("Fermer"), b -> close())
                .dimensions(x, y, totalW, 20).build());
    }

    private void addToggle(int x, int y, int w, String name, BooleanSupplier state, Runnable toggle) {
        addDrawableChild(ButtonWidget.builder(label(name, state.getAsBoolean()), b -> {
            toggle.run();
            b.setMessage(label(name, state.getAsBoolean()));
        }).dimensions(x, y, w, 20).build());
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + " : " + (on ? "ON" : "OFF")).formatted(on ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Touche 2 (pave num) pour fermer").formatted(Formatting.GRAY),
                width / 2, top + 11, 0xAAAAAA);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (TurboMinerClient.menuKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ModConfig.save();
    }
}
