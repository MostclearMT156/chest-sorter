package com.example.addon.memory;
import net.minecraft.util.math.BlockPos;
import java.util.*;

/**
 * Stores memories grouped by dimension and position.
 * Scales to hundreds or thousands of containers.
 */
public class MemoryBank {

    // dimensionId -> (blockPos -> memory)
    private final Map<String, Map<BlockPos, Memory>> memories = new HashMap<>();

    public void addMemory(String dimensionId, BlockPos pos, Memory memory) {
        memories
            .computeIfAbsent(dimensionId, k -> new HashMap<>())
            .put(pos, memory);
    }

    public Optional<Memory> getMemory(String dimensionId, BlockPos pos) {
        Map<BlockPos, Memory> map = memories.get(dimensionId);
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.get(pos));
    }

    public void removeMemory(String dimensionId, BlockPos pos) {
        Map<BlockPos, Memory> map = memories.get(dimensionId);
        if (map == null) return;
        map.remove(pos);
        if (map.isEmpty()) {
            memories.remove(dimensionId);
        }
    }

    public Map<String, Map<BlockPos, Memory>> getAllMemories() {
        // defensive copy
        Map<String, Map<BlockPos, Memory>> copy = new HashMap<>();
        for (var entry : memories.entrySet()) {
            copy.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public void clear() {
        memories.clear();
    }
}
