package com.example.addon.gui;

import com.example.addon.database.ChestDatabase;
import com.example.addon.database.ChestEntry;
import com.example.addon.targets.TargetManager;
import com.example.addon.selection.SelectionManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

/**
 * GUI-оверлей, отображающий интерактивные элементы над сундуками.
 * Позволяет:
 *  - назначать целевой предмет
 *  - снимать назначение
 *  - показывать статус сундука
 *
 * Работает в связке с TargetManager, ChestDatabase и SelectionManager.
 */
public class ChestOverlay {

    private final ChestDatabase database;
    private final TargetManager targetManager;
    private final SelectionManager selectionManager;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ChestOverlay(ChestDatabase database,
                        TargetManager targetManager,
                        SelectionManager selectionManager) {

        this.database = database;
        this.targetManager = targetManager;
        this.selectionManager = selectionManager;
    }

    /**
     * Основной метод рендера.
     * Вызывается каждый кадр в world-render фазе.
     */
    public void render(MatrixStack matrices, float tickDelta) {
        if (mc.world == null || mc.player == null) return;

        BlockPos lookingAt = getLookedChest();
        if (lookingAt == null) return;

        ChestEntry entry = database.getChestByPos(lookingAt);
        if (entry == null) return;

        renderOverlayText(matrices, lookingAt, entry);
    }

    /**
     * Определяет, смотрит ли игрок на сундук.
     */
    private BlockPos getLookedChest() {
        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof BlockHitResult bhr)) return null;

        BlockPos pos = bhr.getBlockPos();
        ChestEntry entry = database.getChestByPos(pos);

        return entry != null ? pos : null;
    }

    /**
     * Рисует текст над сундуком:
     *  - статус (целевой / обычный)
     *  - подсказки управления
     */
    private void renderOverlayText(MatrixStack matrices, BlockPos pos, ChestEntry entry) {
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double x = pos.getX() + 0.5 - cam.x;
        double y = pos.getY() + 1.2 - cam.y;
        double z = pos.getZ() + 0.5 - cam.z;

        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        String status = entry.isTarget()
            ? "Целевой сундук: " + Objects.requireNonNull(entry.getTargetItem()).getName().getString()
            : "Обычный сундук";

        drawText(matrices, status, 0, 0, 0xFFFFFF);

        drawText(matrices, "[ПКМ] Назначить предмет", 0, 12, 0x00FFAA);
        drawText(matrices, "[SHIFT+ПКМ] Снять назначение", 0, 24, 0xFF5555);

        matrices.pop();
    }

    /**
     * Рисует текст в 3D‑мире.
     */
    private void drawText(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(
            matrices,
            text,
            x - mc.textRenderer.getWidth(text) / 2f,
            y,
            color
        );
    }

    /**
     * Обрабатывает клик игрока по сундуку.
     * Вызывается из EventBusListeners.
     */
    public void onRightClick(BlockPos pos, boolean shift) {
        ChestEntry entry = database.getChestByPos(pos);
        if (entry == null) return;

        if (shift) {
            // Снять назначение
            targetManager.removeTarget(pos);
            return;
        }

        // Открыть окно выбора предмета
        openItemSelectionScreen(pos);
    }

    /**
     * Открывает GUI выбора предмета.
     */
    private void openItemSelectionScreen(BlockPos pos) {
        mc.setScreen(new ItemSelectionScreen(item -> {
            if (item != null) {
                targetManager.assignTarget(pos, item);
            }
        }));
    }
}
