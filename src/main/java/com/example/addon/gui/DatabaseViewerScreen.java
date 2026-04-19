//package com.example.addon.gui;
//
//
//import com.example.addon.database.ChestDatabase;
//import com.example.addon.database.ChestEntry;
//import com.example.addon.database.ChestType;
//import com.example.addon.targets.TargetManager;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.ButtonWidget;
//import net.minecraft.client.gui.widget.TextFieldWidget;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.registry.Registries;
//import net.minecraft.text.Text;
//import net.minecraft.util.math.BlockPos;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Экран просмотра базы данных сундуков.
// * Показывает:
// *  - координаты сундуков
// *  - тип (одинарный/двойной)
// *  - назначенный предмет (если есть)
// *  - содержимое сундука
// *
// * Позволяет:
// *  - фильтровать по предмету
// *  - кликать по сундуку и назначать ему предмет
// */
//public class DatabaseViewerScreen extends Screen {
//
//    private final ChestDatabase database;
//    private final TargetManager targetManager;
//
//    private TextFieldWidget searchField;
//
//    private List<ChestEntry> allEntries = new ArrayList<>();
//    private List<ChestEntry> filteredEntries = new ArrayList<>();
//
//    private int scrollOffset = 0;
//    private static final int ROWS_PER_PAGE = 10;
//
//    public DatabaseViewerScreen(ChestDatabase database, TargetManager targetManager) {
//        super(Text.literal("База сундуков"));
//        this.database = database;
//        this.targetManager = targetManager;
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//
//        // Поле поиска по предмету
//        searchField = new TextFieldWidget(
//            textRenderer,
//            width / 2 - 120,
//            20,
//            240,
//            20,
//            Text.literal("Поиск по предмету")
//        );
//        searchField.setChangedListener(s -> updateFilter());
//        addSelectableChild(searchField);
//
//        // Загружаем сундуки
//        loadEntries();
//        updateFilter();
//
//        // Кнопка закрытия
//        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), btn -> close())
//            .dimensions(width / 2 - 40, height - 30, 80, 20)
//            .build());
//    }
//
//    private void loadEntries() {
//        allEntries = new ArrayList<>(database.getAllChests());
//    }
//
//    private void updateFilter() {
//        String query = searchField.getText().toLowerCase();
//
//        if (query.isEmpty()) {
//            filteredEntries = allEntries;
//        } else {
//            filteredEntries = allEntries.stream()
//                .filter(entry -> entry.getContents().keySet().stream()
//                    .anyMatch(item -> item.getName().getString().toLowerCase().contains(query)))
//                .toList();
//        }
//
//        scrollOffset = 0;
//    }
//
//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
//        int maxOffset = Math.max(0, filteredEntries.size() - ROWS_PER_PAGE);
//        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) amount, maxOffset));
//        return true;
//    }
//
//    @Override
//    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        renderBackground(matrices);
//
//        drawCenteredText(matrices, textRenderer, "База данных сундуков", width / 2, 5, 0xFFFFFF);
//
//        searchField.render(matrices, mouseX, mouseY, delta);
//
//        renderChestList(matrices, mouseX, mouseY);
//
//        super.render(matrices, mouseX, mouseY, delta);
//    }
//
//    private void renderChestList(MatrixStack matrices, int mouseX, int mouseY) {
//        int startX = width / 2 - 200;
//        int startY = 60;
//
//        for (int i = 0; i < ROWS_PER_PAGE; i++) {
//            int index = scrollOffset + i;
//            if (index >= filteredEntries.size()) break;
//
//            ChestEntry entry = filteredEntries.get(index);
//            BlockPos pos = entry.getPosition();
//
//            int y = startY + i * 24;
//
//            // Фон строки
//            fill(matrices, startX, y, startX + 400, y + 22, 0x55000000);
//
//            // Координаты
//            String coords = "X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ();
//            textRenderer.draw(matrices, coords, startX + 6, y + 6, 0xFFFFFF);
//
//            // Тип сундука
//            String type = entry.getType() == ChestType.DOUBLE ? "Двойной" : "Одинарный";
//            textRenderer.draw(matrices, type, startX + 120, y + 6, 0xAAAAFF);
//
//            // Назначенный предмет
//            if (entry.isTarget()) {
//                Item item = entry.getTargetItem();
//                textRenderer.draw(matrices,
//                    "Цель: " + item.getName().getString(),
//                    startX + 200,
//                    y + 6,
//                    0x55FF55
//                );
//            } else {
//                textRenderer.draw(matrices,
//                    "Цель: нет",
//                    startX + 200,
//                    y + 6,
//                    0xFF5555
//                );
//            }
//
//            // Обработка клика
//            if (isMouseOver(mouseX, mouseY, startX, y, 400, 22)) {
//                fill(matrices, startX, y, startX + 400, y + 22, 0x22FFFFFF);
//
//                if (MinecraftClient.getInstance().mouse.wasLeftButtonClicked()) {
//                    openItemSelection(entry);
//                }
//            }
//        }
//    }
//
//    private void openItemSelection(ChestEntry entry) {
//        MinecraftClient.getInstance().setScreen(new ItemSelectionScreen(item -> {
//            if (item != null) {
//                targetManager.assignTarget(entry.getPosition(), item);
//            }
//        }));
//    }
//
//    private boolean isMouseOver(double mx, double my, int x, int y, int w, int h) {
//        return mx >= x && mx <= x + w && my >= y && my <= y + h;
//    }
//
//    @Override
//    public void close() {
//        MinecraftClient.getInstance().setScreen(null);
//    }
//}
