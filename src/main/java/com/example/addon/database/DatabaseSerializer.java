package com.example.addon.database;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Сериализация/десериализация ChestEntry в JSON.
 */
public class DatabaseSerializer {

    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ChestEntry.class, new ChestEntryAdapter())
        .create();

    public static String serialize(Map<BlockPos, ChestEntry> entries) {
        return gson.toJson(entries);
    }

    public static Map<BlockPos, ChestEntry> deserialize(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();

        Type type = new com.google.gson.reflect.TypeToken<Map<BlockPos, ChestEntry>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ===================== АДАПТЕР =====================

    private static class ChestEntryAdapter implements JsonSerializer<ChestEntry>, JsonDeserializer<ChestEntry> {

        @Override
        public JsonElement serialize(ChestEntry entry, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();

            obj.addProperty("x", entry.getPosition().getX());
            obj.addProperty("y", entry.getPosition().getY());
            obj.addProperty("z", entry.getPosition().getZ());

            obj.addProperty("type", entry.getType().name());
            obj.addProperty("isTarget", entry.isTarget());
            obj.addProperty("targetItem", entry.getTargetItem() != null
                ? Registries.ITEM.getId(entry.getTargetItem()).toString()
                : null
            );

            JsonObject contentsObj = new JsonObject();
            for (Map.Entry<Item, Integer> e : entry.getContents().entrySet()) {
                contentsObj.addProperty(
                    Registries.ITEM.getId(e.getKey()).toString(),
                    e.getValue()
                );
            }
            obj.add("contents", contentsObj);

            return obj;
        }

        @Override
        public ChestEntry deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
            JsonObject obj = json.getAsJsonObject();

            BlockPos pos = new BlockPos(
                obj.get("x").getAsInt(),
                obj.get("y").getAsInt(),
                obj.get("z").getAsInt()
            );

            ChestType chestType = ChestType.valueOf(obj.get("type").getAsString());
            ChestEntry entry = new ChestEntry(pos, chestType);

            // Target
            if (obj.get("isTarget").getAsBoolean()) {
                String id = obj.get("targetItem").getAsString();
                Item item = Registries.ITEM.get(net.minecraft.util.Identifier.of(id));
                entry.setTarget(item);
            }

            // Contents
            JsonObject contentsObj = obj.getAsJsonObject("contents");
            Map<Item, Integer> contents = new HashMap<>();

            for (String key : contentsObj.keySet()) {
                Item item = Registries.ITEM.get(net.minecraft.util.Identifier.of(key));
                int count = contentsObj.get(key).getAsInt();
                contents.put(item, count);
            }

            entry.updateContents(contents);

            return entry;
        }
    }
}

