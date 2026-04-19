package com.example.addon;


import com.example.addon.database.ChestDatabase;
import com.example.addon.selection.SelectionManager;
import com.example.addon.scanner.ChestScanner;
import com.example.addon.targets.TargetManager;
import com.example.addon.gui.ChestOverlay;
import com.example.addon.events.EventBusListeners;
import com.example.addon.baritone.BaritoneController;


/**
 * Главный класс аддона.
 * Отвечает за инициализацию, жизненный цикл и доступ к подсистемам.
 */
public final class MainAddon {

    private static MainAddon instance;

    // Подсистемы
    private final ChestDatabase chestDatabase;
    private final SelectionManager selectionManager;
    private final ChestScanner chestScanner;
    private final TargetManager targetManager;
    private final BaritoneController baritoneController;
    private final ChestOverlay chestOverlay;
    private final EventBusListeners eventBusListeners;

    private boolean initialized;

    private MainAddon() {
        // Создание экземпляров подсистем (без реальной логики, только структура)
        this.chestDatabase = new ChestDatabase();
        this.selectionManager = new SelectionManager();
        this.baritoneController = new BaritoneController();
        this.chestScanner = new ChestScanner(chestDatabase, baritoneController);
        this.targetManager = new TargetManager(chestDatabase);
        this.chestOverlay = new ChestOverlay(chestDatabase, targetManager, selectionManager);
        this.eventBusListeners = new EventBusListeners(
            selectionManager,
            chestScanner,
            targetManager,
            chestDatabase
        );
    }

    /**
     * Точка доступа к синглтону аддона.
     */
    public static MainAddon getInstance() {
        if (instance == null) {
            instance = new MainAddon();
        }
        return instance;
    }

    /**
     * Инициализация аддона.
     * Вызывается при загрузке Meteor-модуля.
     */
    public void onInitialize() {
        if (initialized) return;

        // Загрузка базы данных сундуков
        chestDatabase.load();

        // Регистрация GUI-оверлея и экранов
        registerGui();

        // Регистрация слушателей событий
        registerEventListeners();

        // Регистрация команд (заглушка, структура)
        registerCommands();

        initialized = true;
    }

    /**
     * Завершение работы аддона.
     * Вызывается при выгрузке или закрытии клиента.
     */
    public void onShutdown() {
        if (!initialized) return;

        // Сохранение базы данных
        chestDatabase.save();

        // Отписка от событий и очистка ресурсов
        unregisterEventListeners();
        unregisterGui();

        initialized = false;
    }

    // ===== Регистрация/отмена GUI и событий (структура) =====

    private void registerGui() {
        // Здесь должна быть регистрация ChestOverlay в системе рендера Meteor
        // Например: Hud.add(chestOverlay); или аналогичный вызов
    }

    private void unregisterGui() {
        // Здесь должна быть отписка ChestOverlay от рендера
        // Например: Hud.remove(chestOverlay);
    }

    private void registerEventListeners() {
        // Регистрация слушателей в EventBus Meteor
        // Например: MeteorClient.EVENT_BUS.subscribe(eventBusListeners);
    }

    private void unregisterEventListeners() {
        // Отписка слушателей от EventBus
        // Например: MeteorClient.EVENT_BUS.unsubscribe(eventBusListeners);
    }

    private void registerCommands() {
        // Регистрация команд аддона
        // Например: Commands.add(new ChestDbCommand(this));
    }

    // ===== Геттеры для подсистем =====

    public ChestDatabase getDatabase() {
        return chestDatabase;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public ChestScanner getChestScanner() {
        return chestScanner;
    }

    public TargetManager getTargetManager() {
        return targetManager;
    }

    public BaritoneController getBaritoneController() {
        return baritoneController;
    }

    public ChestOverlay getChestOverlay() {
        return chestOverlay;
    }

    public EventBusListeners getEventBusListeners() {
        return eventBusListeners;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
