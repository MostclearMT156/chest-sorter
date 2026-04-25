package com.example.addon.baritone;


import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.input.Input;

import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Контроллер Baritone.
 * Отвечает за:
 *  - навигацию к сундуку
 *  - открытие сундука
 *  - взаимодействие с блоками
 *
 * Используется ChestScanner.
 */
public class BaritoneController {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * Навигация к указанной точке.
     * Возвращает true, если Baritone смог построить путь.
     */
    public boolean navigateTo(BlockPos pos) {
        if (mc.player == null) return false;

        try {
            System.out.println("Got primary process");
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            var pathing = baritone.getCustomGoalProcess();

            pathing.setGoalAndPath(new GoalBlock(pos.up(1)));

            System.out.println("Waiting until baritone stops pathing");
            // Wait until Baritone stops pathing
            System.out.println("finished waiting");
            System.out.println("reached the goal");
            // SUCCESS = Baritone reached the goal → goal becomes null
            return pathing.getGoal() == null || pathing.getGoal().isInGoal(mc.player.getBlockPos());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * Открывает сундук по координатам.
     * Возвращает true, если сундук успешно открыт.
     */
    public boolean openChest(BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;

        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof ChestBlock)) return false;

        // Наводим камеру на сундук
        lookAt(pos);

        // Кликаем ПКМ
        return rightClickBlock(pos);
    }

    /**
     * Поворачивает камеру игрока к указанному блоку.
     */
    private void lookAt(BlockPos pos) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        double dx = pos.getX() + 0.5 - player.getX();
        double dy = pos.getY() + 0.5 - (player.getY() + player.getEyeHeight(player.getPose()));
        double dz = pos.getZ() + 0.5 - player.getZ();

        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float pitch = (float) -Math.toDegrees(Math.asin(dy / dist));

        player.setYaw(yaw);
        player.setPitch(pitch);
    }

    /**
     * Выполняет ПКМ по блоку.
     */
    private boolean rightClickBlock(BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;

        try {
            BlockHitResult hit = new BlockHitResult(
                mc.player.getEyePos(),
                Direction.UP,
                pos,
                false
            );

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Принудительно останавливает Baritone.
     */
    public void stop() {
        try {
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            baritone.getPathingBehavior().cancelEverything();
        } catch (Exception ignored) {}
    }
}
