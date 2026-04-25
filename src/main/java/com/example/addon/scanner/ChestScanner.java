package com.example.addon.scanner;

import com.example.addon.database.ChestDatabase;
import com.example.addon.database.ChestEntry;
import com.example.addon.database.ChestType;
import com.example.addon.baritone.BaritoneController;


import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Отвечает за физическое сканирование сундуков:
 *  - навигация к сундуку через Baritone
 *  - открытие сундука
 *  - чтение содержимого
 *  - обновление базы данных
 *
 * Работает в связке с:
 *  - ChestDatabase
 *  - BaritoneController
 */
public class ChestScanner {

    private final ChestDatabase database;
    private final BaritoneController baritone;

    public ChestScanner(ChestDatabase database, BaritoneController baritone) {
        this.database = database;
        this.baritone = baritone;
    }

    /**
     * Сканирует список сундуков по координатам.
     */
    public void scanChests(List<BlockPos> chestPositions) {
        for (BlockPos pos : chestPositions) {
            scanSingleChest(pos);
        }
    }

    /**
     * Сканирует один сундук:
     *  - бот идёт к сундуку
     *  - открывает его
     *  - читает содержимое
     *  - сохраняет в базу
     */
    public void scanSingleChest(BlockPos pos) {
        if (!baritone.navigateTo(pos)) {
            System.out.println("[ChestScanner] Не удалось добраться до сундука: " + pos);
            return;
        }

        if (!baritone.openChest(pos)) {
            System.out.println("[ChestScanner] Не удалось открыть сундук: " + pos);
            return;
        }

        ChestBlockEntity chest = getChestEntity(pos);
        if (chest == null) {
            System.out.println("[ChestScanner] Сундук не найден в мире: " + pos);
            return;
        }

        Map<Item, Integer> contents = extractContents(chest);
        ChestEntry entry = createOrUpdateEntry(pos, chest, contents);

        database.updateChest(entry);
        System.out.println("[ChestScanner] Сундук просканирован: " + pos);
    }

    /**
     * Получает ChestBlockEntity по координатам.
     */
    private ChestBlockEntity getChestEntity(BlockPos pos) {
        BlockEntity be = MinecraftClient.getInstance().world.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            return chest;
        }
        return null;
    }

    /**
     * Извлекает содержимое сундука.
     */
    public Map<Item, Integer> extractContents(Inventory inv) {
        Map<Item, Integer> map = new HashMap<>();

        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                map.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        return map;
    }

    /**
     * Создаёт или обновляет запись сундука в базе.
     */
    private ChestEntry createOrUpdateEntry(BlockPos pos, ChestBlockEntity chest, Map<Item, Integer> contents) {
        ChestEntry entry = database.getChestByPos(pos);

        if (entry == null) {
            ChestType type = chest.getCachedState().getBlock().toString().contains("double")
                ? ChestType.DOUBLE
                : ChestType.SINGLE;

            entry = new ChestEntry(pos, type);
        }

        entry.updateContents(contents);
        return entry;
    }
}
