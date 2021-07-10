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
import api.utils.gui.ModGUIHandler;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.gui.contract.contractlist.ContractsTab;
import thederpgamer.contracts.gui.contract.playercontractlist.PlayerContractsControlManager;
import thederpgamer.contracts.server.commands.ContractsCommand;

/**
 * Contracts mod main class.
 *
 * @author TheDerpGamer
 * @since 09/25/2020
 */
public class Contracts extends StarMod {

    //Instance
    private static Contracts instance;
    public static Contracts getInstance() {
        return instance;
    }
    public Contracts() {

    }
    public static void main(String[] args) {

    }

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

    //GUI
    public ContractsTab contractsTab;
    public PlayerContractsControlManager playerContractsControlManager;

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
                                ModGUIHandler.registerNewControlManager(getSkeleton(), playerContractsControlManager);
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
                if(event.getTitle().equals("SHOP") && contractsTab == null) {
                    contractsTab = new ContractsTab(event.getWindow().getState(), event.getWindow());
                    contractsTab.onInit();
                    event.getWindow().getTabs().add(contractsTab);
                }
            }
        }, this);
    }

    private void registerCommands() {
        StarLoader.registerCommand(new ContractsCommand());
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

        debugMode = config.getConfigurableBoolean("debug-mode", false);
        autoSaveFrequency = config.getConfigurableLong("auto-save-frequency", 10000);
        contractTimerMax = config.getConfigurableInt("contract-timer-max", 30);
        tradersFactionID = config.getConfigurableInt("traders-faction-id", -10000000);
    }
}