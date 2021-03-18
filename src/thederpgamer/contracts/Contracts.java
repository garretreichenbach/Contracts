package thederpgamer.contracts;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.MainWindowTabAddEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import api.utils.gui.ControlManagerHandler;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.gui.contract.playercontractlist.PlayerContractsControlManager;
import thederpgamer.contracts.server.commands.RandomContractCommand;
import thederpgamer.contracts.gui.contract.contractlist.ContractsTab;

/**
 * Contracts.java
 * Contracts mod main class.
 *
 * @since 03/09/2021
 * @author TheDerpGamer
 */
public class Contracts extends StarMod {

    //Instance
    static Contracts instance;
    public Contracts() { }
    public static void main(String[] args) { }

    //Controller
    public PlayerContractsControlManager playerContractsControlManager;

    //Config
    private final String[] defaultConfig = {
            "debug-mode: false",
            "auto-save-frequency: 10000",
            "contract-timer-max: 30",
            "traders-faction-id: -10000000",
    };
    public boolean debugMode = false;
    public long autoSaveFrequency = 10000;
    public int contractTimerMax = 30;
    public int tradersFactionID = -10000000;

    @Override
    public void onEnable() {
        instance = this;
        initConfig();
        registerRunners();
        registerCommands();
        registerListeners();
    }

    private void registerListeners() {
        StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
            @Override
            public void onEvent(final GUITopBarCreateEvent event) {
                GUITopBar.ExpandedButton dropDownButton = event.getDropdownButtons().get(event.getDropdownButtons().size() - 1);
                dropDownButton.addExpandedButton("CONTRACTS", new GUICallback() {
                    @Override
                    public void callback(final GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            if(playerContractsControlManager == null) {
                                playerContractsControlManager = new PlayerContractsControlManager(event.getGuiTopBar().getState());
                                ControlManagerHandler.registerNewControlManager(getSkeleton(), playerContractsControlManager);
                            }
                            playerContractsControlManager.setActive(true);
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
                if(event.getTitle().equals(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHOP_SHOPNEW_SHOPPANELNEW_2)) {
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

    private void registerRunners() {
        new StarRunnable() {
            @Override
            public void run() {
                PersistentObjectUtil.save(getSkeleton());
            }
        }.runTimer(this, autoSaveFrequency);
    }

    private void initConfig() {
        FileConfiguration config = getConfig("config");
        config.saveDefault(defaultConfig);

        this.debugMode = config.getConfigurableBoolean("debug-mode", false);
        this.autoSaveFrequency = config.getConfigurableLong("auto-save-frequency", 10000);
        this.contractTimerMax = config.getConfigurableInt("contract-timer-max", 30);
        this.tradersFactionID = config.getConfigurableInt("traders-faction-id", -10000000);
    }

    public static Contracts getInstance() {
        return instance;
    }
}
