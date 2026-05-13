package com.example.addon.utils;

import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class SelScanner {

    static ArrayList<BetterBlockPos> chests = new ArrayList<>();

    public static int getDoubleChests() {
        return doubleChests;
    }

    public static int getSingleChests() {
        return singleChests;
    }

    public static boolean isDBChest(MinecraftClient mc, BlockPos pos){
        BlockState state = mc.world.getBlockState(pos);
        if(!(state.getBlock() instanceof ChestBlock)) return false;
        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        if(type == ChestType.SINGLE) return false;
        return true;
    }

    public static BlockPos getScndHalfOfDBChest(MinecraftClient mc, BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        Direction facing = state.get(ChestBlock.FACING);
        return new BetterBlockPos(
            pos.getX() + facing.rotateYClockwise().getOffsetX(),
            pos.getY(),
            pos.getZ() + facing.rotateYClockwise().getOffsetZ()
        );
    }

    static int doubleChests = 0;
    static int singleChests = 0;

    public static int calculateSelectionVolume(BetterBlockPos start, BetterBlockPos end) {
        if (start == null || end == null) return 0;
        int dx = Math.abs(end.getX() - start.getX()) + 1;
        int dy = Math.abs(end.getY() - start.getY()) + 1;
        int dz = Math.abs(end.getZ() - start.getZ()) + 1;
        return dx * dy * dz;
    }

    public static ArrayList<BetterBlockPos> findChestInSelection(MinecraftClient mc, BetterBlockPos start, BetterBlockPos end) {
        ArrayList<BetterBlockPos> result = new ArrayList<>();
        doubleChests = 0;
        singleChests = 0;
        if (start == null || end == null) return result;
        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());
        int area = calculateSelectionVolume(start,end);
        for (int i = 0; i < area; i++) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BetterBlockPos pos = new BetterBlockPos(x, y, z);
                        BlockPos mcPos = new BlockPos(pos);
                        BlockState state = mc.world.getBlockState(mcPos);

                        if (!(state.getBlock() instanceof ChestBlock chestBlock)) continue;

                        // Get chest block entity
                        BlockEntity be = mc.world.getBlockEntity(mcPos);
                        if (!(be instanceof ChestBlockEntity chestEntity)) continue;

                        // Determine if this is LEFT, RIGHT, or SINGLE
                        ChestType type = chestEntity.getCachedState().get(ChestBlock.CHEST_TYPE);

                        BetterBlockPos chestPosToAdd = pos;


                        if (type == ChestType.SINGLE) {
                            if (!result.contains(chestPosToAdd)) {
                                result.add(chestPosToAdd);
                                singleChests++;
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
                            if (!result.contains(pos)){
                                result.add(pos);
                                doubleChests++;
                            }
                        }
                    }
                }
            }
        }

        for (BetterBlockPos chest : result) {
            System.out.println("List of chests: " + chest);
        }
        chests.addAll(result);
        return result;
    }
}
