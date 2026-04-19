package com.example.addon.database;


import net.minecraft.util.math.BlockPos;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Модель сундука, хранящая координаты, тип, содержимое и назначенный предмет.
 */
public class ChestEntry {

    private final BlockPos position;
    private ChestType type;

    private final Map<Item, Integer> contents = new HashMap<>();

    private boolean isTarget;
    private Item targetItem;

    public ChestEntry(BlockPos position, ChestType type) {
        this.position = position;
        this.type = type;
    }

    public BlockPos getPosition() {
        return position;
    }

    public ChestType getType() {
        return type;
    }

    public void setType(ChestType type) {
        this.type = type;
    }

    public Map<Item, Integer> getContents() {
        return contents;
    }

    public void updateContents(Map<Item, Integer> newContents) {
        contents.clear();
        contents.putAll(newContents);
    }

    public boolean isTarget() {
        return isTarget;
    }

    public Item getTargetItem() {
        return targetItem;
    }

    public void setTarget(Item item) {
        this.isTarget = true;
        this.targetItem = item;
    }

    public void clearTarget() {
        this.isTarget = false;
        this.targetItem = null;
    }
}
