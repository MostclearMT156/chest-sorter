package com.example.addon.scanner;

import com.example.addon.database.ChestType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Отвечает за поиск сундуков в выделенной области.
 * Используется ChestScanner и SelectionManager.
 */
public class ChestLocator {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * Находит все сундуки в списке блоков.
     */
    public List<BlockPos> findChestsInArea(List<BlockPos> area) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (BlockPos pos : area) {
            if (visited.contains(pos)) continue;

            ChestType type = getChestType(pos);
            if (type == null) continue;

            if (type == ChestType.SINGLE) {
                result.add(pos);
                visited.add(pos);
            }

            if (type == ChestType.DOUBLE) {
                // Добавляем оба блока двойного сундука
                BlockPos other = getSecondChestPart(pos);
                if (other != null) {
                    result.add(pos);
                    result.add(other);

                    visited.add(pos);
                    visited.add(other);
                } else {
                    // fallback: считаем как одиночный
                    result.add(pos);
                    visited.add(pos);
                }
            }
        }

        return result;
    }

    /**
     * Проверяет, является ли блок сундуком, и определяет его тип.
     */
    public ChestType getChestType(BlockPos pos) {
        if (mc.world == null) return null;

        BlockState state = mc.world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof ChestBlock)) return null;

        ChestBlockEntity chest = getChestEntity(pos);
        if (chest == null) return null;

        // Проверяем, является ли сундук частью двойного
        ChestBlockEntity other = getConnectedChest(pos);
        return other != null ? ChestType.DOUBLE : ChestType.SINGLE;
    }

    /**
     * Возвращает ChestBlockEntity по координатам.
     */
    private ChestBlockEntity getChestEntity(BlockPos pos) {
        if (mc.world == null) return null;

        BlockEntity be = mc.world.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            return chest;
        }
        return null;
    }

    /**
     * Находит вторую часть двойного сундука.
     */
    private BlockPos getSecondChestPart(BlockPos pos) {
        ChestBlockEntity chest = getChestEntity(pos);
        if (chest == null) return null;

        // Проверяем соседние блоки
        for (BlockPos offset : new BlockPos[]{
            pos.north(), pos.south(), pos.east(), pos.west()
        }) {
            BlockEntity be = mc.world.getBlockEntity(offset);
            if (be instanceof ChestBlockEntity other) {
                // Проверяем, что это действительно двойной сундук
                if (isSameDoubleChest(chest, other)) {
                    return offset;
                }
            }
        }

        return null;
    }

    /**
     * Проверяет, являются ли два сундука частями одного двойного.
     */
    private boolean isSameDoubleChest(ChestBlockEntity a, ChestBlockEntity b) {
        // Minecraft сам хранит ссылку на "pair" сундука
        return a.getCachedState().getBlock() instanceof ChestBlock &&
            b.getCachedState().getBlock() instanceof ChestBlock &&
            a.getPos().isWithinDistance(b.getPos(), 1.1);
    }

    /**
     * Возвращает вторую часть двойного сундука (если есть).
     */
    private ChestBlockEntity getConnectedChest(BlockPos pos) {
        for (BlockPos offset : new BlockPos[]{
            pos.north(), pos.south(), pos.east(), pos.west()
        }) {
            BlockEntity be = mc.world.getBlockEntity(offset);
            if (be instanceof ChestBlockEntity other) {
                if (isSameDoubleChest(getChestEntity(pos), other)) {
                    return other;
                }
            }
        }
        return null;
    }
}
