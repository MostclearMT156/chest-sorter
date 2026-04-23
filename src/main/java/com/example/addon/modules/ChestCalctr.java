/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.example.addon.modules;


import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.selection.ISelection;
import baritone.api.utils.BetterBlockPos;
import com.example.addon.AddonTemplate;
import com.example.addon.utils.SelScanner;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;


public class ChestCalctr extends Module {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

    private ISelection[] selections = new ISelection[0];

    private int doublechests = SelScanner.getDoubleChests();
    private int singlechests = SelScanner.getSingleChests();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRendering = settings.createGroup("Rendering");

    // Keybindings
    private final Setting<Keybind> selectionBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("selection-bind")
        .description("Bind to draw selection.")
        .defaultValue(Keybind.fromButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
        .build()
    );



    private enum Status {
        SEL_START,
        SEL_END
    }

    private Status status = Status.SEL_START;
    private BetterBlockPos start, end;

    private void resetSelection() {
        baritone.getSelectionManager().removeAllSelections();
        resetChestMemory();
    }

    private void resetChestMemory(){
        doublechests = 0;
        singlechests = 0;
    }

    private void hideSelections() {
        selections = baritone.getSelectionManager().getSelections();
        baritone.getSelectionManager().removeAllSelections();
    }

    private void revealSelections() {
        for (ISelection selection : selections) {
            baritone.getSelectionManager().addSelection(selection);
        }
    }


    public ChestCalctr() {
        super(AddonTemplate.CATEGORY, "Chest-Checker", "Scan the amount of double chests in the selected area");
    }

    @Override
    public void onDeactivate() {
        hideSelections();
        if (baritone.getBuilderProcess().isActive()) baritone.getCommandManager().execute("stop");
        status = Status.SEL_START;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        revealSelections();
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action != KeyAction.Press || !selectionBind.get().isPressed() || mc.currentScreen != null) {
            return;
        }
        selectCorners();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press || !selectionBind.get().isPressed() || mc.currentScreen != null) {
            return;
        }
        selectCorners();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {

        WVerticalList list = theme.verticalList();

        // Clear buttons
        WHorizontalList hl = theme.horizontalList();
        WButton clear = hl.add(theme.button("Clear Selection")).widget();

        list.add(hl);


        clear.action = () -> {
            resetSelection();
        };


        return list;
    }

    private void selectCorners() {
        if (!(mc.crosshairTarget instanceof BlockHitResult result)) return;

        if (status == Status.SEL_START) {
            start = BetterBlockPos.from(result.getBlockPos());
            status = Status.SEL_END;
            info("Start corner set: (%d, %d, %d)".formatted(start.getX(), start.getY(), start.getZ()));

        } else if (status == Status.SEL_END) {
            end = BetterBlockPos.from(result.getBlockPos());
            status = Status.SEL_START;
            info("End corner set: (%d, %d, %d)".formatted(end.getX(), end.getY(), end.getZ()));
            baritone.getSelectionManager().addSelection(start, end);
            //baritone.getBuilderProcess().clearArea(start, end);
            SelScanner.findChestInSelection(mc, start, end);
            doublechests += SelScanner.getDoubleChests();
            singlechests += SelScanner.getSingleChests();

        }
    }


    @Override
    public String getInfoString() {
        return "Double chests: " + doublechests + " Single Chests: " + singlechests;
    }
}

