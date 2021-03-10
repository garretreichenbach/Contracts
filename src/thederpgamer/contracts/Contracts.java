package thederpgamer.contracts;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.MainWindowTabAddEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.commands.RandomContractCommand;
import thederpgamer.contracts.gui.contract.ContractsTab;
import thederpgamer.contracts.gui.contract.PlayerContractsScrollableList;

/**
 * Contracts.java
 * Contracts mod main class
 * ==================================================
 * Created 03/09/2021
 * @author TheDerpGamer
 */
public class Contracts extends StarMod {

    //Instance
    static Contracts instance;
    public Contracts() { }
    public static void main(String[] args) { }

    //Config
    private final String[] defaultConfig = {
            "debug-mode: false",
            "contract-timer-max: 30",
            "traders-faction-id: -10000000",
    };

    public boolean debugMode = false;
    public int contractTimerMax = 30;
    public int tradersFactionID = -10000000;

    @Override
    public void onEnable() {
        instance = this;
        initConfig();
        registerRunners();
        registerPackets();
        registerCommands();
        registerFastListeners();
        registerListeners();
    }

    @Override
    public void onDisable() {

    }

    private void registerFastListeners() {

    }

    private void registerListeners() {
        StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
            @Override
            public void onEvent(final GUITopBarCreateEvent guiTopBarCreateEvent) {
                GUITopBar.ExpandedButton dropDownButton = guiTopBarCreateEvent.getDropdownButtons().get(guiTopBarCreateEvent.getDropdownButtons().size() - 1);
                dropDownButton.addExpandedButton("CONTRACTS", new GUICallback() {
                    @Override
                    public void callback(final GUIElement guiElement, MouseEvent mouseEvent) {
                        if (mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            final GUIMainWindow guiWindow = new GUIMainWindow(GameClient.getClientState(), 850, 550, "CONTRACTS");
                            guiWindow.onInit();
                            guiWindow.setCloseCallback(new GUICallback() {
                                @Override
                                public void callback(GUIElement guiElement, MouseEvent event) {
                                    if (event.pressedLeftMouse()) {
                                        GameClient.getClientState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
                                        GameClient.getClientState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().recreateTabs();
                                        GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
                                    }
                                }

                                @Override
                                public boolean isOccluded() {
                                    return !guiWindow.getState().getController().getPlayerInputs().isEmpty();
                                }
                            });

                            GUIContentPane contractsPane = guiWindow.addTab("CONTRACTS");
                            contractsPane.setTextBoxHeightLast(300);

                            PlayerContractsScrollableList playerContractsList = new PlayerContractsScrollableList(GameClient.getClientState(), 500, 300, contractsPane.getContent(0));
                            playerContractsList.onInit();
                            contractsPane.getContent(0).attach(playerContractsList);

                            GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
                            GameClient.getClientState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().inventoryPanel = guiWindow;
                            GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setActive(true);
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationHighlightCallback() {
                    @Override
                    public boolean isHighlighted(InputState inputState) {
                        return false;
                    }

                    @Override
                    public boolean isVisible(InputState inputState) {
                        return true;
                    }

                    @Override
                    public boolean isActive(InputState inputState) {
                        return true;
                    }
                });
            }
        }, this);

        StarLoader.registerListener(MainWindowTabAddEvent.class, new Listener<MainWindowTabAddEvent>() {
            @Override
            public void onEvent(MainWindowTabAddEvent event) {
                if (event.getTitle().equals(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHOP_SHOPNEW_SHOPPANELNEW_2)) {
                    ContractsTab contractsTab = new ContractsTab(event.getWindow().getState(), event.getWindow());
                    contractsTab.onInit();
                    event.getWindow().getTabs().add(contractsTab);
                }
            }
        }, this);
    }

    private void registerCommands() {
        StarLoader.registerCommand(new RandomContractCommand());
    }

    private void registerPackets() {

    }

    private void registerRunners() {

    }

    private void initConfig() {
        FileConfiguration config = getConfig("config");
        config.saveDefault(defaultConfig);

        this.debugMode = Boolean.parseBoolean(config.getString("debug-mode"));
        this.contractTimerMax = config.getInt("contract-timer-max");
        this.tradersFactionID = config.getInt("traders-faction-id");
    }

    public static Contracts getInstance() {
        return instance;
    }
}
