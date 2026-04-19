package com.example.addon.selection;


import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер выделения области.
 * Отвечает за:
 *  - начало выделения
 *  - обновление конечной точки
 *  - завершение выделения
 *  - вычисление всех блоков внутри прямоугольного региона
 *
 * Используется другими модулями:
 *  - SelectionRenderer (визуализация)
 *  - ChestLocator (поиск сундуков)
 *  - GUI (подсветка)
 */
public class SelectionManager {

    private BlockPos startPos;
    private BlockPos endPos;

    private boolean selecting;

    /**
     * Начинает выделение области.
     * Вызывается при нажатии ЛКМ.
     */
    public void startSelection(BlockPos pos) {
        this.startPos = pos;
        this.endPos = pos;
        this.selecting = true;
    }

    /**
     * Обновляет конечную точку выделения.
     * Вызывается при движении мыши с зажатой ЛКМ.
     */
    public void updateSelection(BlockPos pos) {
        if (!selecting) return;
        this.endPos = pos;
    }

    /**
     * Завершает выделение.
     * Вызывается при отпускании ЛКМ.
     */
    public void finishSelection() {
        this.selecting = false;
    }

    /**
     * Сбрасывает выделение.
     */
    public void clearSelection() {
        this.startPos = null;
        this.endPos = null;
        this.selecting = false;
    }

    /**
     * Возвращает true, если сейчас идёт выделение.
     */
    public boolean isSelecting() {
        return selecting;
    }

    /**
     * Возвращает стартовую точку выделения.
     */
    public BlockPos getStartPos() {
        return startPos;
    }

    /**
     * Возвращает конечную точку выделения.
     */
    public BlockPos getEndPos() {
        return endPos;
    }

    /**
     * Возвращает список всех блоков внутри выделенной области.
     * Используется ChestLocator для поиска сундуков.
     */
    public List<BlockPos> getSelectedArea() {
        List<BlockPos> result = new ArrayList<>();

        if (startPos == null || endPos == null) return result;

        int x1 = Math.min(startPos.getX(), endPos.getX());
        int y1 = Math.min(startPos.getY(), endPos.getY());
        int z1 = Math.min(startPos.getZ(), endPos.getZ());

        int x2 = Math.max(startPos.getX(), endPos.getX());
        int y2 = Math.max(startPos.getY(), endPos.getY());
        int z2 = Math.max(startPos.getZ(), endPos.getZ());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }

        return result;
    }

    /**
     * Проверяет, есть ли активное выделение (даже если мышь уже отпущена).
     */
    public boolean hasSelection() {
        return startPos != null && endPos != null;
    }
}

