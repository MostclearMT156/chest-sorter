//package com.example.addon.targets;
//
//import com.example.addon.database.ChestDatabase;
//import com.example.addon.database.ChestEntry;
//
//import net.minecraft.item.Item;
//import net.minecraft.util.math.BlockPos;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Менеджер назначения целевых сундуков.
// * Позволяет:
// *  - назначить сундук как целевой для определённого предмета
// *  - снять назначение
// *  - получить назначенный предмет
// *  - получить список всех целевых сундуков
// *
// * Работает поверх ChestDatabase.
// */
//public class TargetManager {
//
//    private final ChestDatabase database;
//
//    public TargetManager(ChestDatabase database) {
//        this.database = database;
//    }
//
//    /**
//     * Назначает сундук как целевой для указанного предмета.
//     */
//    public void assignTarget(BlockPos pos, Item item) {
//        ChestEntry entry = database.getChestByPos(pos);
//        if (entry == null) {
//            System.out.println("[TargetManager] Нельзя назначить цель: сундук не найден в базе " + pos);
//            return;
//        }
//
//        entry.setTarget(item);
//        database.updateChest(entry);
//
//        System.out.println("[TargetManager] Назначен целевой сундук " + pos + " для предмета " + item);
//    }
//
//    /**
//     * Снимает назначение целевого сундука.
//     */
//    public void removeTarget(BlockPos pos) {
//        ChestEntry entry = database.getChestByPos(pos);
//        if (entry == null) return;
//
//        entry.clearTarget();
//        database.updateChest(entry);
//
//        System.out.println("[TargetManager] Снято назначение целевого сундука " + pos);
//    }
//
//    /**
//     * Возвращает предмет, назначенный сундуку.
//     * Если сундук не целевой — возвращает null.
//     */
//    public Item getTargetForChest(BlockPos pos) {
//        ChestEntry entry = database.getChestByPos(pos);
//        if (entry == null || !entry.isTarget()) return null;
//
//        return entry.getTargetItem();
//    }
//
//    /**
//     * Возвращает список всех сундуков, которые имеют назначенный предмет.
//     */
//    public List<ChestEntry> getAllTargets() {
//        List<ChestEntry> result = new ArrayList<>();
//
//        for (ChestEntry entry : database.getAllChests()) {
//            if (entry.isTarget()) {
//                result.add(entry);
//            }
//        }
//
//        return result;
//    }
//}
