/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.example.addon.modules;


import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.utils.BetterBlockPos;
import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ModuleExample2 extends Module {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRendering = settings.createGroup("Rendering");
    private ArrayList<BetterBlockPos> chests = new ArrayList<>();

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

    private final Setting<Boolean> clearSelection = sgGeneral.add(new BoolSetting.Builder()
        .name("clear-selection")
        .description("Clears the selection of chests")
            .onChanged((v) -> resetSelection())
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
        SEL_END,
        WORKING
    }



    private Status status = Status.SEL_START;
    private BetterBlockPos start, end;

    private void resetSelection(){
        chests.clear();
        baritone.getSelectionManager().removeAllSelections();

    }

    public ModuleExample2() {
        super(AddonTemplate.CATEGORY, "excavator", "Excavate a selection area.");
    }

    @Override
    public void onDeactivate() {
        baritone.getSelectionManager().removeSelection(baritone.getSelectionManager().getLastSelection());
        if (baritone.getBuilderProcess().isActive()) baritone.getCommandManager().execute("stop");
        status = Status.SEL_START;
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
            chests = findChestInSelection();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (status == Status.SEL_START || status == Status.SEL_END || isActive()) {
            if (!(mc.crosshairTarget instanceof BlockHitResult result)) return;
            event.renderer.box(result.getBlockPos(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }

        //Render chest highlight
        if (!chests.isEmpty()) {
            for (BetterBlockPos chest : chests) {
                event.renderer.box(chest,
                    new SettingColor(255, 165, 0, 125),   // orange sides
                    new SettingColor(255, 165, 0, 125),  // orange outline
                    ShapeMode.Both,
                    0
                );
            }
        }

        // Stop logic
//            if (!baritone.getBuilderProcess().isActive()) {
//                if (keepActive.get()) {
//                    baritone.getSelectionManager().removeSelection(baritone.getSelectionManager().getLastSelection());
//                    status = Status.SEL_START;
//                } else toggle();
//            }
    }


    private ArrayList<BetterBlockPos> findChestInSelection() {
        if (start == null || end == null) return new ArrayList<BetterBlockPos>();
        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());
        int dx = Math.abs(end.getX() - start.getX()) + 1;
        int dy = Math.abs(end.getY() - start.getY()) + 1;
        int dz = Math.abs(end.getZ() - start.getZ()) + 1;
        int area = calculateSelectionVolume();
        for (int i = 0; i < area; i++) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BetterBlockPos pos = new BetterBlockPos(x, y, z);
                        BlockState state = mc.world.getBlockState(pos);

                        if (!(state.getBlock() instanceof ChestBlock chestBlock)) continue;

                        // Get chest block entity
                        BlockEntity be = mc.world.getBlockEntity(pos);
                        if (!(be instanceof ChestBlockEntity chestEntity)) continue;

                        // Determine if this is LEFT, RIGHT, or SINGLE
                        ChestType type = chestEntity.getCachedState().get(ChestBlock.CHEST_TYPE);

                        BetterBlockPos chestPosToAdd = pos;


                        if (type == ChestType.SINGLE) {
                            if (!chests.contains(chestPosToAdd)) {
                                chests.add(chestPosToAdd);
                            }
                        }

                        Direction facing = state.get(ChestBlock.FACING);
                        BetterBlockPos otherHalf;

                        if (type == ChestType.LEFT) {
                            otherHalf = new BetterBlockPos(
                                pos.getX() + facing.rotateYClockwise().getOffsetX(),
                                pos.getY(),
                                pos.getZ() + facing.rotateYClockwise().getOffsetZ()
                            );
                        } else {
                            otherHalf = new BetterBlockPos(
                                pos.getX() + facing.rotateYCounterclockwise().getOffsetX(),
                                pos.getY(),
                                pos.getZ() + facing.rotateYCounterclockwise().getOffsetZ()
                            );
                        }

                        boolean otherInside =
                            otherHalf.getX() >= minX && otherHalf.getX() <= maxX &&
                            otherHalf.getY() >= minY && otherHalf.getY() <= maxY &&
                            otherHalf.getZ() >= minZ && otherHalf.getZ() <= maxZ;
                        if (!otherInside) continue;

                        if (type == ChestType.LEFT) {
                            if (!chests.contains(pos)) chests.add(pos);
                        }
                    }
                }
            }
        }

        for (BetterBlockPos chest : chests) {
            System.out.println("List of chests: " + chest);
        }
        return chests;
    }

    private int calculateSelectionVolume() {
        if (start == null || end == null) return 0;
        int dx = Math.abs(end.getX() - start.getX()) + 1;
        int dy = Math.abs(end.getY() - start.getY()) + 1;
        int dz = Math.abs(end.getZ() - start.getZ()) + 1;
        return dx * dy * dz;
    }

}

