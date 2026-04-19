package com.example.addon.database;


import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;

/**
 * Главная база данных сундуков.
 */
public class ChestDatabase {

    private final Map<BlockPos, ChestEntry> entries = new HashMap<>();
    private final File dbFile = new File("chest_database.json");

    public void load() {
        String json = FileStorage.readFile(dbFile);
        Map<BlockPos, ChestEntry> loaded = DatabaseSerializer.deserialize(json);

        entries.clear();
        entries.putAll(loaded);

        System.out.println("[ChestDB] Loaded " + entries.size() + " entries");
    }

    public void save() {
        String json = DatabaseSerializer.serialize(entries);
        FileStorage.writeFile(dbFile, json);

        System.out.println("[ChestDB] Saved " + entries.size() + " entries");
    }

    public ChestEntry getChestByPos(BlockPos pos) {
        return entries.get(pos);
    }

    public void addChest(ChestEntry entry) {
        entries.put(entry.getPosition(), entry);
    }

    public void updateChest(ChestEntry entry) {
        entries.put(entry.getPosition(), entry);
    }

    public Collection<ChestEntry> getAllChests() {
        return entries.values();
    }

    public List<ChestEntry> getChestsByItem(Item item) {
        List<ChestEntry> result = new ArrayList<>();

        for (ChestEntry entry : entries.values()) {
            if (entry.getContents().containsKey(item)) {
                result.add(entry);
            }
        }

        return result;
    }

    public void markChestAsTarget(ChestEntry entry, Item item) {
        entry.setTarget(item);
        updateChest(entry);
    }
}
