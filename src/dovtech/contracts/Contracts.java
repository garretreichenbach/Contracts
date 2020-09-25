package dovtech.contracts;

import api.DebugFile;
import api.common.GameClient;
import api.config.BlockConfig;
import api.entity.StarPlayer;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.MainWindowTabAddEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.utils.StarRunnable;
import api.utils.gui.GUIUtils;
import dovtech.contracts.commands.RandomContractCommand;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.gui.contracts.NewContractPanel;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.util.DataUtil;
import org.newdawn.slick.Image;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;
import java.io.File;
import java.io.IOException;

public class Contracts extends StarMod {

    static Contracts inst;

    public Contracts() {
        inst = this;
    }

    //Resources
    private String resourcesPath;

    //Server
    private final File moddataFolder = new File("moddata");
    private final File contractsDataFolder = new File("moddata/Contracts");
    private final File contractsFolder = new File("moddata/Contracts/contract");
    private final File playerDataFolder = new File("moddata/Contracts/player");
    private final File factionDataFolder = new File("moddata/Contracts/faction");
    private final File allianceDataFolder = new File("moddata/Contracts/alliance");

    public Image defaultLogo;

    //Config
    private FileConfiguration config;
    private String[] defaultConfig = {
            "debug-mode: false",
            "write-frequency: 12000"
    };

    //Config Settings
    public boolean debugMode;
    public int writeFrequency;

    public static void main(String[] args) {

    }

    @Override
    public void onGameStart() {
        inst = this;
        setModName("Contracts");
        setModAuthor("Dovtech");
        setModVersion("0.3.3");
        setModDescription("Adds Contracts for trade and player interaction.");

        resourcesPath = this.getClass().getResource("").getPath();

        if (!moddataFolder.exists()) moddataFolder.mkdirs();
        if (!contractsDataFolder.exists()) contractsDataFolder.mkdirs();
        if (!contractsFolder.exists()) contractsFolder.mkdirs();
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
        if (!factionDataFolder.exists()) factionDataFolder.mkdirs();
        if (!allianceDataFolder.exists()) allianceDataFolder.mkdirs();

        initConfig();
        registerListeners();
        registerCommands();

        try {
            DataUtil.readData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new StarRunnable() {
            @Override
            public void run() {
                DataUtil.writeData();
            }
        }.runTimer(writeFrequency);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DebugFile.log("Enabled", this);
    }

    @Override
    public void onBlockConfigLoad(BlockConfig config) {

    }

    private void registerListeners() {
        StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
            @Override
            public void onEvent(final GUITopBarCreateEvent guiTopBarCreateEvent) {
                GUITopBar.ExpandedButton dropDownButton = guiTopBarCreateEvent.getDropdownButtons().get(guiTopBarCreateEvent.getDropdownButtons().size() - 1);
                dropDownButton.addExpandedButton("CONTRACTS", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            GUIMainWindow guiWindow = new GUIMainWindow(GameClient.getClientState(), 1000, 600, "CONTRACTS");
                            guiWindow.onInit();

                            GUIContentPane contractsPane = guiWindow.addTab("CONTRACTS");
                            contractsPane.setTextBoxHeightLast(300);

                            PlayerContractsScrollableList playerContractsList = new PlayerContractsScrollableList(GameClient.getClientState(), 500, 300, contractsPane.getContent(0));
                            playerContractsList.onInit();
                            contractsPane.getContent(0).attach(playerContractsList);

                            GUIUtils.activateCustomGUIWindow(guiWindow);
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

                dropDownButton.addExpandedButton("STATS", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            GUIMainWindow guiWindow = new GUIMainWindow(GameClient.getClientState(), 1000, 600, "STATS");
                            guiWindow.onInit();

                            GUIContentPane statsPane = guiWindow.addTab("STATS");
                            statsPane.setTextBoxHeightLast(300);

                            GUIUtils.activateCustomGUIWindow(guiWindow);
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
        });


        StarLoader.registerListener(MainWindowTabAddEvent.class, new Listener<MainWindowTabAddEvent>() {
            @Override
            public void onEvent(MainWindowTabAddEvent event) {
                if(event.getTitle().equals(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHOP_SHOPNEW_SHOPPANELNEW_2)) {

                    GUIContentPane contractsTab = event.createTab("CONTRACTS");
                    contractsTab.setName("CONTRACTS");
                    contractsTab.setTextBoxHeightLast(300);

                    contractsTab.addDivider(250);
                    /* Faction/Contractor Logo
                    contractsTab.addNewTextBox(0, 150);
                    Sprite contractorLogo = new Sprite(new Texture(0, 0, resourcesPath + "/gui/logo/trading-guild-logo.png")); //Default Contractor
                    GUIOverlay logoOverlay = new GUIOverlay();
                    contractsTab.getContent(0, 0).attach(logoOverlay);
                     */

                    GUITextOverlay contractorDescOverlay = new GUITextOverlay(250, 300, contractsTab.getState());
                    contractorDescOverlay.onInit();
                    contractorDescOverlay.setTextSimple("Placeholder Text");
                    contractsTab.getContent(0, 0).attach(contractorDescOverlay);

                    contractsTab.addNewTextBox(0, 85);

                    final ContractsScrollableList contractsScrollableList = new ContractsScrollableList(contractsTab.getState(), 500, 300, contractsTab.getContent(1, 0));
                    contractsScrollableList.onInit();
                    contractsTab.getContent(1, 0).attach(contractsScrollableList);

                    final StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
                    final InputState state = contractsTab.getState();
                    if(player.getFaction() != null && !player.getFaction().getName().equals("NO FACTION")) {
                        contractsTab.setTextBoxHeightLast(1, 850);
                        contractsTab.addNewTextBox(1,32);
                        GUIAncor buttonPane = new GUIAncor(state, 300, 32);

                        GUITextButton addContractButton = new GUITextButton(state, (int) (buttonPane.getWidth() / 2) - 4, 24, GUITextButton.ColorPalette.OK, "ADD CONTRACT", new GUICallback() {
                            @Override
                            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                if(mouseEvent.pressedLeftMouse()) {
                                    //Todo: Open add contract menu
                                    GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                    NewContractPanel newContractPanel = new NewContractPanel(GameClient.getClientState(), player.getFaction());
                                    newContractPanel.activate();

                                    contractsScrollableList.updateContracts();
                                }
                            }

                            @Override
                            public boolean isOccluded() {
                                return false;
                            }
                        });
                        addContractButton.setPos(2, 2, 0);
                        buttonPane.attach(addContractButton);

                        GUITextButton removeContractButton = new GUITextButton(state, (int) (buttonPane.getWidth() / 2) - 4, 24, GUITextButton.ColorPalette.OK, "CANCEL CONTRACT", new GUICallback() {
                            @Override
                            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                                if(mouseEvent.pressedLeftMouse()) {
                                    if(contractsScrollableList.getSelectedRow() != null && contractsScrollableList.getSelectedRow().f != null) {
                                        final Contract contract = contractsScrollableList.getSelectedRow().f;
                                        if(contract.getContractor().equals(player.getFaction())) {
                                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                            PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("ConfirmBox", state, "Confirm Cancellation", "Are you sure you wish to cancel this contract? You won't get a refund...") {
                                                @Override
                                                public void onDeactivate() {
                                                }

                                                @Override
                                                public void pressedOK() {
                                                    GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                                    DataUtil.removeContract(contract);
                                                    contractsScrollableList.updateContracts();
                                                }
                                            };
                                            confirmBox.getInputPanel().onInit();
                                            confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
                                            confirmBox.getInputPanel().background.setWidth((float)(GLFrame.getWidth() - 435));
                                            confirmBox.getInputPanel().background.setHeight((float)(GLFrame.getHeight() - 70));
                                            confirmBox.activate();
                                        }
                                    }
                                }
                            }

                            @Override
                            public boolean isOccluded() {
                                return false;
                            }
                        });
                        removeContractButton.setPos(2 + addContractButton.getWidth() + 2, 2, 0);
                        buttonPane.attach(removeContractButton);

                        contractsTab.getContent(1, 1).attach(buttonPane);
                    }
                }
            }
        });

        DebugFile.log("Registered Listeners!", this);
    }

    private void registerCommands() {

        StarLoader.registerCommand(new RandomContractCommand());

        DebugFile.log("Registered Commands!", this);
    }

    private void initConfig() {
        this.config = getConfig("config");
        this.config.saveDefault(defaultConfig);

        this.debugMode = Boolean.parseBoolean(this.config.getString("debug-mode"));
        this.writeFrequency = this.config.getInt("write-frequency");
    }

    public static Contracts getInstance() {
        return inst;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public String getResourcesPath() {
        return resourcesPath;
    }
}
