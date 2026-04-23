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
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import com.example.addon.utils.*;

import java.util.ArrayList;


public class ChestSorter extends Module {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

    private ISelection[] selections = new ISelection[0];
    private ISelection[] selectionsIN = new ISelection[0];
    private ISelection[] selectionsOUT = new ISelection[0];

    private ArrayList<BetterBlockPos> chests = new ArrayList<>();
    private ArrayList<BetterBlockPos> chestsIN = new ArrayList<>();
    private ArrayList<BetterBlockPos> chestsOUT = new ArrayList<>();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRendering = settings.createGroup("Rendering");

    // Keybindings
    private final Setting<Keybind> selectionBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("selection-bind")
        .description("Bind to draw selection.")
        .defaultValue(Keybind.fromButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
        .build()
    );

    // Logging
    private final Setting<Boolean> logSelection = sgGeneral.add(new BoolSetting.Builder()
        .name("log-selection")
        .description("Logs the selection coordinates to the chat.")
        .defaultValue(true)
        .build()
    );


    private final Setting<Boolean> keepActive = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-active")
        .description("Keep the module active after finishing the excavation.")
        .defaultValue(false)
        .build()
    );

    // Rendering
    private final Setting<ShapeMode> shapeMode = sgRendering.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRendering.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRendering.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private enum Status {
        SEL_START,
        SEL_END
    }

    private enum Selection{
        IN,
        OUT
    }

    private Selection selection = Selection.IN;
    private Status status = Status.SEL_START;
    private BetterBlockPos start, end;

    private void resetSelection() {
        chests.clear();
        baritone.getSelectionManager().removeAllSelections();
    }

    private void hideSelections() {
            selections = baritone.getSelectionManager().getSelections();
            baritone.getSelectionManager().removeAllSelections();
    }

    private void revealSelections(){
        for (ISelection selection : selections) {
            baritone.getSelectionManager().addSelection(selection);
        }
    }

    private void toggleSelections(){
        if (selection == Selection.IN) {
            selectionsIN = baritone.getSelectionManager().getSelections();
            baritone.getSelectionManager().removeAllSelections();
            selections = selectionsOUT;
            revealSelections();
            chestsIN = chests;
            chests = chestsOUT;
            selection = Selection.OUT;
            if (logSelection.get()) {
                info("Selection Mode OUT");
            }
        } else if (selection == Selection.OUT) {
            selectionsOUT = baritone.getSelectionManager().getSelections();
            baritone.getSelectionManager().removeAllSelections();
            selections = selectionsIN;
            revealSelections();
            chestsOUT = chests;
            chests = chestsIN;
            selections = selectionsIN;
            selection = Selection.IN;
            if (logSelection.get()) {
                info("Selection Mode IN");
            }
        }
    }

    public ChestSorter() {
        super(AddonTemplate.CATEGORY, "Chest-sorter", "Extract all all items out of stash and move to sorted storage");
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
        WButton toggleSelection = hl.add(theme.button("Toggle IN/OUT Selection")).widget();

        list.add(hl);

        WTable table = new WTable();
        if (!chests.isEmpty()) list.add(table);

        clear.action = () -> {
            resetSelection();
            table.clear();
        };

        toggleSelection.action = this::toggleSelections;


        return list;
    }

    private void selectCorners() {
        if (!(mc.crosshairTarget instanceof BlockHitResult result)) return;

        if (status == Status.SEL_START) {
            start = BetterBlockPos.from(result.getBlockPos());
            status = Status.SEL_END;
            if (logSelection.get()) {
                info("Start corner set: (%d, %d, %d)".formatted(start.getX(), start.getY(), start.getZ()));
            }
        } else if (status == Status.SEL_END) {
            end = BetterBlockPos.from(result.getBlockPos());
            status = Status.SEL_START;
            if (logSelection.get()) {
                info("End corner set: (%d, %d, %d)".formatted(end.getX(), end.getY(), end.getZ()));
            }
            baritone.getSelectionManager().addSelection(start, end);
            //baritone.getBuilderProcess().clearArea(start, end);
            chests = SelScanner.findChestInSelection(mc, start, end);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (status == Status.SEL_START || status == Status.SEL_END || isActive()) {
            if (!(mc.crosshairTarget instanceof BlockHitResult result)) return;
            event.renderer.box(result.getBlockPos(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }

        //Render chest highlight
        if (!chests.isEmpty() && isActive()) {
            for (BetterBlockPos chest : chests) {
                event.renderer.box(chest,
                    new SettingColor(255, 165, 0, 125),   // orange sides
                    new SettingColor(255, 165, 0, 125),  // orange outline
                    ShapeMode.Both,
                    0
                );
            }
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(chests.size());
    }
}

