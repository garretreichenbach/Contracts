package dovtech.contracts;

import api.DebugFile;
import api.common.GameClient;
import api.entity.Ship;
import api.entity.StarPlayer;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.MainWindowTabAddEvent;
import api.listener.events.player.PlayerDeathEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.server.Server;
import api.utils.StarRunnable;
import api.utils.gui.GUIUtils;
import dovtech.contracts.commands.EndContractsCommand;
import dovtech.contracts.commands.RandomContractCommand;
import dovtech.contracts.commands.TradeGuildTakeContractCommand;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.PlayerTarget;
import dovtech.contracts.gui.contracts.ContractsTab;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtil;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Contracts extends StarMod {

    static Contracts inst;

    public Contracts() {
        inst = this;
    }

    //Server
    private final File moddataFolder = new File("moddata");
    private final File contractsDataFolder = new File("moddata/Contracts");
    private final File contractsFolder = new File("moddata/Contracts/contractdata");
    private final File playerDataFolder = new File("moddata/Contracts/playerdata");

    private String[] defaultConfig = {
            "debug-mode: false",
            "write-frequency: 5000",
            "mod-compatibility-enabled: true"
    };

    //Config Settings
    public boolean debugMode;
    public int writeFrequency;
    public boolean modCompatibility;

    //Mod Compatibility
    public ArrayList<StarMod> mods;
    public boolean betterFactionsEnabled = false; //Temp value

    public static void main(String[] args) {

    }

    @Override
    public void onGameStart() {
        inst = this;
        setModName("Contracts");
        setModAuthor("Dovtech");
        setModVersion("0.3.11");
        setModDescription("Adds Contracts for trade and player interaction.");

        if (!moddataFolder.exists()) moddataFolder.mkdirs();
        if (!contractsDataFolder.exists()) contractsDataFolder.mkdirs();
        if (!contractsFolder.exists()) contractsFolder.mkdirs();
        if (!playerDataFolder.exists()) playerDataFolder.mkdirs();

        initConfig();
        checkMods();
        registerListeners();
        registerCommands();

        try {
            DataUtil.readData();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(writeFrequency != -1) {
            new StarRunnable() {
                @Override
                public void run() {
                    DataUtil.writeData();
                }
            }.runTimer(writeFrequency);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DebugFile.log("Enabled", this);
    }

    /*@Override
    public void onBlockConfigLoad(BlockConfig config) {

    }
     */

    private void checkMods() {
        this.mods = new ArrayList<>();
        if(modCompatibility) {
            for(StarMod mod : StarLoader.starMods) {
                if(mod.isEnabled()) {
                    switch(mod.getName()) {
                        case "BetterFactions":
                            mods.add(mod);
                            betterFactionsEnabled = true;
                    }
                }
            }
        }
    }

    private void registerListeners() {

        StarLoader.registerListener(PlayerSpawnEvent.class, new Listener<PlayerSpawnEvent>() {
            @Override
            public void onEvent(PlayerSpawnEvent event) {
                StarPlayer player = new StarPlayer(event.getPlayer().getOwnerState());
                if(!DataUtil.players.containsKey(player.getName())) {
                    PlayerData playerData = new PlayerData(player);
                    DataUtil.players.put(player.getName(), playerData);
                    DataUtil.playerDataWriteBuffer.add(playerData);
                    if(debugMode) DebugFile.log("[DEBUG]: Registered PlayerData for " + player.getName() + ".", Contracts.getInstance());
                }
            }
        });

        StarLoader.registerListener(PlayerDeathEvent.class, new Listener<PlayerDeathEvent>() {
            @Override
            public void onEvent(PlayerDeathEvent event) {
                if(event.getDamager().isSegmentController()) {
                    SegmentController controller = (SegmentController) event.getDamager();
                    if(controller.getType().equals(SimpleTransformableSendableObject.EntityType.SHIP)) {
                        Ship ship = new Ship(controller);
                        if(ship.getDockedRoot().getPilot() != null) {
                            StarPlayer attacker = ship.getDockedRoot().getPilot();
                            StarPlayer target = new StarPlayer(event.getPlayer());
                            PlayerData attackerData = DataUtil.players.get(attacker.getName());
                            PlayerData targetData = DataUtil.players.get(target.getName());
                            for(Contract contract : attackerData.getContracts()) {
                                PlayerTarget playerTarget = (PlayerTarget) contract.getTarget();
                                if(contract.getContractType().equals(Contract.ContractType.BOUNTY) && playerTarget.getTarget().equals(targetData)) {
                                    Server.broadcastMessage("[CONTRACTS]: " + attacker.getName() + " has claimed the bounty on " + target.getName() + " for a reward of " + contract.getReward() + " credits!");
                                    attacker.setCredits(attacker.getCredits() + contract.getReward());
                                    DataUtil.removeContract(contract, false, attacker);
                                }
                            }
                        }
                    }
                } else if(event.getDamager().getOwnerState() instanceof PlayerState) {
                    StarPlayer attacker = new StarPlayer((PlayerState) event.getDamager().getOwnerState());
                    StarPlayer target = new StarPlayer(event.getPlayer());
                    PlayerData attackerData = DataUtil.players.get(attacker.getName());
                    PlayerData targetData = DataUtil.players.get(target.getName());
                    for(Contract contract : attackerData.getContracts()) {
                        PlayerTarget playerTarget = (PlayerTarget) contract.getTarget();
                        if(contract.getContractType().equals(Contract.ContractType.BOUNTY) && playerTarget.getTarget().equals(targetData)) {
                            Server.broadcastMessage("[CONTRACTS]: " + attacker.getName() + " has claimed the bounty on " + target.getName() + " for a reward of " + contract.getReward() + " credits!");
                            attacker.setCredits(attacker.getCredits() + contract.getReward());
                            DataUtil.removeContract(contract, false, attacker);
                        }
                    }
                }
            }
        });

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
                    ContractsTab contractsTab = new ContractsTab(event.getWindow().getState(), event.getWindow());
                    contractsTab.onInit();
                    event.getWindow().getTabs().add(contractsTab);
                }
            }
        });

        DebugFile.log("Registered Listeners!", this);
    }

    private void registerCommands() {

        StarLoader.registerCommand(new RandomContractCommand());
        StarLoader.registerCommand(new TradeGuildTakeContractCommand());
        StarLoader.registerCommand(new EndContractsCommand());

        DebugFile.log("Registered Commands!", this);
    }

    private void initConfig() {
        //Config
        FileConfiguration config = getConfig("config");
        config.saveDefault(defaultConfig);

        this.debugMode = Boolean.parseBoolean(config.getString("debug-mode"));
        this.writeFrequency = config.getInt("write-frequency");
        this.modCompatibility = Boolean.parseBoolean(config.getString("mod-compatibility-enabled"));
    }

    public static Contracts getInstance() {
        return inst;
    }
}
