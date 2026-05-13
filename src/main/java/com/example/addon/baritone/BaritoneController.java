package com.example.addon.baritone;


import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;

import baritone.utils.player.BaritonePlayerContext;
import com.example.addon.utils.SelScanner;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.Optional;

/**
 * Контроллер Baritone.
 * Отвечает за:
 * - навигацию к сундуку
 * - открытие сундука
 * - взаимодействие с блоками
 * <p>
 * Используется ChestScanner.
 */
public class BaritoneController {

    BlockPos pos;
    IBaritone baritone;

    public BaritoneController(IBaritone baritone) {
        this.baritone = baritone;
    }

    /**
     * Навигация к указанной точке.
     * Возвращает true, если Baritone смог построить путь.
     */
    public boolean navigateTo(BlockPos pos) {
        if (mc.player == null) return false;
        this.pos = pos;

        try {
//            System.out.println("Got primary process");
//            var baritone = this.baritone.getProvider().getPrimaryBaritone();
//            var pathing = baritone.getCustomGoalProcess();
//
//            pathing.setGoalAndPath(new GoalBlock(pos));
//
//            System.out.println("Waiting until baritone stops pathing");
//            // Wait until Baritone stops pathing
//            System.out.println("finished waiting");
//            System.out.println("reached the goal");
//            // SUCCESS = Baritone reached the goal → goal becomes null
//            return pathing.getGoal() == null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean isInGoal() {
        BlockPos temp = pos;
        pos = null;
        return temp == mc.player.getBlockPos();
    }


    /**
     * Открывает сундук по координатам.
     * Возвращает true, если сундук успешно открыт.
     */
    public boolean openChest(BlockPos pos) {
        System.out.println("running chest method, called from inside the method");
        if (mc.player == null || mc.world == null) return false;

        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof ChestBlock)) return false;

        // Наводим камеру на сундук
        //lookAt(pos);
        // Кликаем ПКМ
        if(!rightClick(pos) && SelScanner.isDBChest(mc, pos)) return rightClick(SelScanner.getScndHalfOfDBChest(mc, pos));
        return rightClick(pos);
    }

    /**
     * Поворачивает камеру игрока к указанному блоку.
     */
    public static Direction lookAt(BlockPos pos) {
        double eyePos = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        VoxelShape outline = mc.world.getBlockState(pos).getCollisionShape(mc.world, pos);

        if (eyePos > pos.getY() + outline.getMax(Direction.Axis.Y) && mc.world.getBlockState(pos.up()).isReplaceable()) {
            return Direction.UP;
        } else if (eyePos < pos.getY() + outline.getMin(Direction.Axis.Y) && mc.world.getBlockState(pos.down()).isReplaceable()) {
            return Direction.DOWN;
        } else {
            BlockPos difference = pos.subtract(mc.player.getBlockPos());

            if (Math.abs(difference.getX()) > Math.abs(difference.getZ())) {
                return difference.getX() > 0 ? Direction.WEST : Direction.EAST;
            } else {
                return difference.getZ() > 0 ? Direction.NORTH : Direction.SOUTH;
            }
        }
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

    public boolean rightClick(BlockPos pos) {
        IPlayerContext ctx = this.baritone.getPlayerContext();
        Optional<Rotation> reachable = RotationUtils.reachable(ctx, pos, ctx.playerController().getBlockReachDistance());
        if (reachable.isPresent()) {
            System.out.println("Is reachable: true");
            baritone.getLookBehavior().updateTarget(reachable.get(), true);
            //if (pos.equals(ctx.getSelectedBlock().orElse(null))) {
            baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);// TODO find some way to right click even if we're in an ESC menu
            boolean running = true;
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!(mc.currentScreen == null)) {
                    System.out.println("getInventory not null");
                    baritone.getInputOverrideHandler().clearAllKeys();
                    return true;
                }
            }

            //}
            return false;
        }
        return false;
    }

    public boolean escape() {
        mc.currentScreen.close();
        return true;
    }

    public boolean closeChest() {
        escape();
        return true;
    }


    /**
     * Принудительно останавливает Baritone.
     */
    public void stop() {
        try {
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            baritone.getPathingBehavior().cancelEverything();
        } catch (Exception ignored) {
        }
    }
}
