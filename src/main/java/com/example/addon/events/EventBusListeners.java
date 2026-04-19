package com.example.addon.events;

package addon.events;

import com.example.addon.gui.ChestOverlay;
import com.example.addon.scanner.ChestScanner;
import com.example.addon.targets.TargetManager;
import com.example.addon.database.ChestDatabase;
import com.example.addon.selection.SelectionManager;

import meteordevelopment.meteorclient.events.world.WorldRenderEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;

import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Главный слушатель событий аддона.
 * Обрабатывает:
 *  - клики мыши
 *  - рендер мира
 *  - тики клиента
 *  - открытие сундуков
 */
public class EventBusListeners {

    private final SelectionManager selectionManager;
    private final ChestScanner chestScanner;
    private final TargetManager targetManager;
    private final ChestDatabase database;
    private final ChestOverlay overlay;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public EventBusListeners(
        SelectionManager selectionManager,
        ChestScanner chestScanner,
        TargetManager targetManager,
        ChestDatabase database
    ) {
        this.selectionManager = selectionManager;
        this.chestScanner = chestScanner;
        this.targetManager = targetManager;
        this.database = database;

        this.overlay = new ChestOverlay(database, targetManager, selectionManager);
    }

    // ============================================================
    // 1. РЕНДЕРИНГ МИРА
    // ============================================================

    @EventHandler
    private void onRenderWorld(WorldRenderEvent event) {
        overlay.render(event.matrixStack, event.tickDelta);
    }

    // ============================================================
    // 2. ТИКИ КЛИЕНТА
    // ============================================================

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Здесь можно выполнять периодические задачи
        // Например, авто-сканирование или обновление состояния
    }

    // ============================================================
    // 3. КЛИКИ МЫШИ
    // ============================================================

    @EventHandler
    private void onMouseClick(MouseButtonEvent event) {
        if (mc.player == null || mc.world == null) return;

        // ЛКМ — выделение области
        if (event.button == 0) {
            if (event.action == 1) { // нажата
                BlockPos pos = getLookingBlock();
                if (pos != null) selectionManager.startSelection(pos);
            }
            if (event.action == 0) { // отпущена
                selectionManager.finishSelection();
            }
        }

        // ПКМ — взаимодействие с сундуком (назначение цели)
        if (event.button == 1 && event.action == 1) {
            BlockPos pos = getLookingBlock();
            if (pos != null && database.getChestByPos(pos) != null) {
                boolean shift = mc.player.isSneaking();
                overlay.onRightClick(pos, shift);
            }
        }
    }

    // ============================================================
    // 4. ОТКРЫТИЕ СУНДУКОВ (InteractEvent)
    // ============================================================

    @EventHandler
    private void onInteract(InteractEvent event) {
        if (event.result instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();

            // Если сундук есть в базе — можно реагировать
            if (database.getChestByPos(pos) != null) {
                // Здесь можно добавить логику при открытии сундука
                // Например, авто-сканирование содержимого
            }
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private BlockPos getLookingBlock() {
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return null;
        return bhr.getBlockPos();
    }
}
