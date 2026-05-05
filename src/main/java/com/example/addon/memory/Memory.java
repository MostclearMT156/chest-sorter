package com.example.addon.memory;

import java.util.List;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Represents the remembered state of a single container (or group of connected containers).
 */
public final class Memory {

    private final BlockPos rootPos;
    private final List<BlockPos> connectedPositions;
    private final List<ItemStack> items;
    private final String customName; // nullable
    private final String blockId;    // e.g. "minecraft:chest"

    public Memory(BlockPos rootPos,
                  List<BlockPos> connectedPositions,
                  List<ItemStack> items,
                  String customName,
                  String blockId) {

        this.rootPos = Objects.requireNonNull(rootPos, "rootPos");
        this.connectedPositions = List.copyOf(connectedPositions);
        this.items = List.copyOf(items);
        this.customName = customName; // may be null
        this.blockId = Objects.requireNonNull(blockId, "blockId");
    }

    public BlockPos getRootPos() {
        return rootPos;
    }

    public List<BlockPos> getConnectedPositions() {
        return connectedPositions;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String getCustomName() {
        return customName;
    }

    public String getBlockId() {
        return blockId;
    }

    @Override
    public String toString() {
        return "Memory{" +
            "rootPos=" + rootPos +
            ", connectedPositions=" + connectedPositions +
            ", items=" + items +
            ", customName='" + customName + '\'' +
            ", blockId='" + blockId + '\'' +
            '}';
    }
}
