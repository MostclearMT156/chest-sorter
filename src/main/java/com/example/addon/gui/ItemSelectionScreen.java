package com.example.addon.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemSelectionScreen extends Screen {

    private final Consumer<Item> callback;

    private TextFieldWidget searchField;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();

    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 8;

    public ItemSelectionScreen(Consumer<Item> callback) {
        super(Text.literal("Выбор предмета"));
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();

        // Поле поиска
        searchField = new TextFieldWidget(
            textRenderer,
            width / 2 - 100,
            20,
            200,
            20,
            Text.literal("Поиск")
        );
        searchField.setChangedListener(s -> updateFilter());
        addSelectableChild(searchField);

        loadItems();
        updateFilter();

        // Кнопка закрытия
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Отмена"), btn -> close())
                .dimensions(width / 2 - 40, height - 30, 80, 20)
                .build()
        );
    }

    private void loadItems() {
        allItems.clear();
        Registries.ITEM.forEach(allItems::add);
    }

    private void updateFilter() {
        String query = searchField.getText().toLowerCase();

        filteredItems = allItems.stream()
            .filter(item -> item.getName().getString().toLowerCase().contains(query))
            .toList();

        scrollOffset = 0;
    }


    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxOffset = Math.max(0, filteredItems.size() - ITEMS_PER_PAGE);
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) amount, maxOffset));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        searchField.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
            textRenderer,
            "Выберите предмет",
            width / 2,
            5,
            0xFFFFFF
        );

        renderItemList(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderItemList(DrawContext context, int mouseX, int mouseY) {
        int startX = width / 2 - 100;
        int startY = 60;

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int index = scrollOffset + i;
            if (index >= filteredItems.size()) break;

            Item item = filteredItems.get(index);
            String name = item.getName().getString();

            int y = startY + i * 22;

            // Фон строки
            context.fill(startX, y, startX + 200, y + 20, 0x55000000);

            // Иконка предмета
            context.drawItem(new ItemStack(item), startX + 4, y + 2);

            // Название предмета
            context.drawTextWithShadow(textRenderer, name, startX + 26, y + 6, 0xFFFFFF);

            // Hover highlight
            boolean hovered = isMouseOver(mouseX, mouseY, startX, y, 200, 20);
            if (hovered) {
                context.fill(startX, y, startX + 200, y + 20, 0x22FFFFFF);

                // Click detection
                if (MinecraftClient.getInstance().mouse.wasLeftButtonClicked()) {
                    callback.accept(item);
                    close();
                }
            }
        }
    }

    private boolean isMouseOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}
