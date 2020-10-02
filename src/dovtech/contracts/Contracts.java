package dovtech.contracts;

import api.DebugFile;
import api.common.GameClient;
import api.common.GameCommon;
import api.common.GameServer;
import api.entity.Fleet;
import api.entity.Ship;
import api.entity.StarPlayer;
import api.entity.StarStation;
import api.faction.StarFaction;
import api.listener.Listener;
import api.listener.events.fleet.FleetLoadSectorEvent;
import api.listener.events.gui.ControlManagerActivateEvent;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.MainWindowTabAddEvent;
import api.listener.events.player.BuyTradeEvent;
import api.listener.events.player.PlayerDeathEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.listener.events.player.SellTradeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.server.Server;
import api.universe.StarSector;
import api.universe.StarUniverse;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import api.utils.game.inventory.ItemStack;
import api.utils.gui.GUIUtils;
import com.ctc.wstx.util.DataUtil;
import dovtech.contracts.commands.*;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.CargoTarget;
import dovtech.contracts.contracts.target.PlayerTarget;
import dovtech.contracts.faction.Opinion;
import dovtech.contracts.gui.SpecialDealsTab;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.gui.contracts.ContractsTab;
import dovtech.contracts.gui.contracts.PlayerContractsScrollableList;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.ContractUtils;
import dovtech.contracts.util.DataUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.view.gui.PlayerPanel;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.game.client.view.gui.shop.shopnew.ShopPanelNew;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.simulation.npc.NPCFaction;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
            "mod-compatibility-enabled: true",
            "cargo-contracts-enabled: true",
            "cargo-escort-bonus: 1.3",
            "contract-timer-max: 30",
            "npc-contracts-enabled: true",
            "traders-faction-id: -10000000"
    };

    public enum Mode {
        CLIENT,
        SERVER,
        SINGLEPLAYER
    }
    public Mode gameState;
    //Config Settings
    public boolean debugMode;
    public int writeFrequency;
    public boolean modCompatibility;
    public boolean cargoContractsEnabled;
    public double cargoEscortBonus;
    public int contractTimerMax;
    public boolean npcContractsEnabled;
    public int tradersFactionID;

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
        setModVersion("0.5.1");
        setModDescription("Adds Contracts for trade and player interaction.");

        if(GameCommon.isDedicatedServer()) {
            gameState = Mode.SERVER;
        } else if(GameCommon.isClientConnectedToServer()) {
            gameState = Mode.CLIENT;
        } else if(GameCommon.isOnSinglePlayer()) {
            gameState = Mode.SINGLEPLAYER;
        } else {
            DebugFile.err("[CRITICAL]: Game State is invalid!");
            throw new IllegalStateException();
        }


        if(gameState.equals(Mode.SERVER) || gameState.equals(Mode.SINGLEPLAYER)) {
            if (!moddataFolder.exists()) moddataFolder.mkdirs();
            if (!contractsDataFolder.exists()) contractsDataFolder.mkdirs();
            if (!contractsFolder.exists()) contractsFolder.mkdirs();
            if (!playerDataFolder.exists()) playerDataFolder.mkdirs();

            initConfig();
            checkMods();
            registerCommands();

            try {
                DataUtils.readData();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (writeFrequency != -1) {
                new StarRunnable() {
                    @Override
                    public void run() {
                        DataUtils.writeData();
                    }
                }.runTimer(writeFrequency);
            }
        }
        registerListeners();
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
        if (modCompatibility) {
            for (StarMod mod : StarLoader.starMods) {
                if (mod.isEnabled()) {
                    switch (mod.getName()) {
                        case "BetterFactions":
                            mods.add(mod);
                            betterFactionsEnabled = true;
                    }
                }
            }
        }
    }

    private void registerListeners() {

        if (cargoContractsEnabled) {
            if (gameState.equals(Mode.CLIENT) || gameState.equals(Mode.SINGLEPLAYER)) {
                StarLoader.registerListener(ControlManagerActivateEvent.class, new Listener<ControlManagerActivateEvent>() {
                    @Override
                    public void onEvent(ControlManagerActivateEvent event) {
                        if (event.isActive() && event.getControlManager() instanceof ShopControllerManager) {
                            try {
                                PlayerPanel playerPanel = GameClient.getClientState().getWorldDrawer().getGuiDrawer().getPlayerPanel();
                                if (debugMode) DebugFile.log("[DEBUG]: ShopControllerManager activated", getMod());
                                Field shopPanelField = PlayerPanel.class.getDeclaredField("shopPanelNew");
                                shopPanelField.setAccessible(true);
                                ShopPanelNew shopPanelNew = (ShopPanelNew) shopPanelField.get(playerPanel);
                                Collection<GUIContentPane> tabs = shopPanelNew.shopPanel.getTabs();
                                SpecialDealsTab specialDealsTab = null;
                                StarSector sector = StarUniverse.getUniverse().getSector(GameClient.getClientPlayerState().getCurrentSector());
                                for (StarStation station : sector.getStations()) {
                                    if (station.getFaction().getID() == tradersFactionID) {
                                        PlayerData playerData = DataUtils.getPlayerData(GameClient.getClientPlayerState().getName());
                                        switch (playerData.getOpinion(station.getFaction()).getOpinion()) {
                                            case HATED:
                                                GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(false);
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Go away scum!");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "It appears the traders have a strong hatred of you, and are unwilling to even speak to you.");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        shopPanelField.set(playerPanel, shopPanelNew);
                                                        return;
                                                    }
                                                }
                                                break;
                                            case HOSTILE:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: You better have something worth our time...");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "It appears the traders have a strong distrust of you, and may be unwilling to sell you some items or services.");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        shopPanelField.set(playerPanel, shopPanelNew);
                                                        return;
                                                    }
                                                }
                                                break;
                                            case POOR:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Welcome to our shop space travel-oh... it's you again.");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "It appears the traders have a slight distrust of you, and may charge more for some items or services.");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        shopPanelField.set(playerPanel, shopPanelNew);
                                                        return;
                                                    }
                                                }
                                                break;
                                            case COOL:
                                            case NEUTRAL:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Welcome to our shop space traveller!");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        shopPanelField.set(playerPanel, shopPanelNew);
                                                        return;
                                                    }
                                                }
                                                break;
                                            case CORDIAL:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Welcome back to our shop space traveller!");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        shopPanelField.set(playerPanel, shopPanelNew);
                                                        return;
                                                    }
                                                }
                                                break;
                                            case GOOD:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Welcome back to our shop space traveller! What can we do for you?");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "The trader recognizes you and greets you with some enthusiasm. Perhaps they may be willing to get you a Special Deal...");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        break;
                                                    }
                                                }
                                                specialDealsTab = new SpecialDealsTab(shopPanelNew.shopPanel.getState(), shopPanelNew.shopPanel, Opinion.GOOD);
                                                specialDealsTab.onInit();
                                                shopPanelNew.shopPanel.getTabs().add(specialDealsTab);
                                                shopPanelField.set(playerPanel, shopPanelNew);
                                                break;
                                            case EXCELLENT:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Welcome back to our shop friend! What can we do for you?");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "The trader recognizes you and greets you enthusiastically as if you were a good friend. Perhaps they may be willing to share some valuable information with you...");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        break;
                                                    }
                                                }
                                                specialDealsTab = new SpecialDealsTab(shopPanelNew.shopPanel.getState(), shopPanelNew.shopPanel, Opinion.EXCELLENT);
                                                specialDealsTab.onInit();
                                                shopPanelNew.shopPanel.getTabs().add(specialDealsTab);
                                                shopPanelField.set(playerPanel, shopPanelNew);
                                                break;
                                            case TRUSTED:
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "[TRADERS]: Good to see you again! Welcome back to our shop!");
                                                PlayerUtils.sendMessage(GameClient.getClientPlayerState(), "The trader recognizes you and greets you enthusiastically as if you were a close friend. It is clear the Trading Guild sees you as a close and trusted ally...");
                                                for (GUIContentPane tab : tabs) {
                                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                                        shopPanelNew.recreateTabs();
                                                        break;
                                                    }
                                                }
                                                specialDealsTab = new SpecialDealsTab(shopPanelNew.shopPanel.getState(), shopPanelNew.shopPanel, Opinion.TRUSTED);
                                                specialDealsTab.onInit();
                                                shopPanelNew.shopPanel.getTabs().add(specialDealsTab);
                                                shopPanelField.set(playerPanel, shopPanelNew);
                                                break;
                                        }
                                        break;
                                    }
                                }

                                for (GUIContentPane tab : tabs) {
                                    if (tab.getTabName().equals("SPECIAL DEALS")) {
                                        shopPanelNew.recreateTabs();
                                        shopPanelField.set(playerPanel, shopPanelNew);
                                        return;
                                    }
                                }
                            } catch (NoSuchFieldException | IllegalAccessException | NullPointerException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }

            if (gameState.equals(Mode.SERVER) || gameState.equals(Mode.SINGLEPLAYER)) {
                StarLoader.registerListener(BuyTradeEvent.class, new Listener<BuyTradeEvent>() {
                    @Override
                    public void onEvent(BuyTradeEvent event) {
                        if (event.getBuyer().getFactionId() != 0 && event.getSeller().getFactionId() != 0) {
                            Faction buyerFaction = GameServer.getServerState().getFactionManager().getFaction(event.getBuyer().getFactionId());
                            Faction sellerFaction = GameServer.getServerState().getFactionManager().getFaction(event.getSeller().getFactionId());
                            if (buyerFaction.isNPC() && sellerFaction.isNPC()) return;
                            int totalCost = event.getTotalCost();
                            ItemStack[] items = new ItemStack[event.getItems().size()];
                            for (int i = 0; i < items.length; i++) {
                                ItemStack itemStack = new ItemStack(event.getItems().get(i).getType());
                                itemStack.setAmount(event.getItems().get(i).amount);
                                items[i] = itemStack;
                            }
                            String contractName = "Escort cargo to " + event.getTo().toString();
                            CargoTarget cargoTarget = new CargoTarget();
                            cargoTarget.setTargets(items);
                            Contract cargoContract = new Contract(event.getBuyer().getFactionId(), contractName, Contract.ContractType.CARGO_ESCORT, (int) (totalCost * cargoEscortBonus), UUID.randomUUID().toString(), cargoTarget);
                            cargoTarget.setLocation(StarUniverse.getUniverse().getSector(event.getTo()));
                            cargoContract.setTarget(cargoTarget);
                            StarFaction traders = new StarFaction(GameServer.getServerState().getFactionManager().getFaction(tradersFactionID));
                            NPCFaction npcFaction = (NPCFaction) traders.getInternalFaction();
                            ElementCountMap elementCountMap = new ElementCountMap();
                            for (ItemStack item : items) {
                                elementCountMap.inc(item.getId(), item.getAmount());
                            }
                            Fleet tradeFleet = new Fleet(npcFaction.getFleetManager().spawnTradingFleet(elementCountMap, event.getFrom(), event.getTo()));
                            tradeFleet.idle();
                            ContractUtils.tradeFleets.put(cargoContract, tradeFleet.getInternalFleet().dbid);
                            ContractUtils.startCargoClaimTimer(cargoContract);

                            DataUtils.addContract(cargoContract);
                            if (ContractsScrollableList.getInst() != null) {
                                ContractsScrollableList.getInst().clear();
                                ContractsScrollableList.getInst().handleDirty();
                            }
                            //Todo: Pause the trade progress
                        }
                    }
                });

                StarLoader.registerListener(SellTradeEvent.class, new Listener<SellTradeEvent>() {
                    @Override
                    public void onEvent(SellTradeEvent event) {
                        if (event.getBuyer().getFactionId() != 0 && event.getSeller().getFactionId() != 0) {
                            Faction buyerFaction = GameServer.getServerState().getFactionManager().getFaction(event.getBuyer().getFactionId());
                            Faction sellerFaction = GameServer.getServerState().getFactionManager().getFaction(event.getSeller().getFactionId());
                            if (buyerFaction.isNPC() && sellerFaction.isNPC()) return;
                            int totalCost = event.getTotalCost();
                            ItemStack[] items = new ItemStack[event.getItems().size()];
                            for (int i = 0; i < items.length; i++) {
                                ItemStack itemStack = new ItemStack(event.getItems().get(i).getType());
                                itemStack.setAmount(event.getItems().get(i).amount);
                                items[i] = itemStack;
                            }
                            String contractName = "Escort cargo to " + event.getTo().toString();
                            CargoTarget cargoTarget = new CargoTarget();
                            cargoTarget.setTargets(items);
                            Contract cargoContract = new Contract(event.getBuyer().getFactionId(), contractName, Contract.ContractType.CARGO_ESCORT, (int) (totalCost * cargoEscortBonus), UUID.randomUUID().toString(), cargoTarget);
                            cargoTarget.setLocation(StarUniverse.getUniverse().getSector(event.getTo()));
                            cargoContract.setTarget(cargoTarget);
                            StarFaction traders = new StarFaction(GameServer.getServerState().getFactionManager().getFaction(tradersFactionID));
                            NPCFaction npcFaction = (NPCFaction) traders.getInternalFaction();
                            ElementCountMap elementCountMap = new ElementCountMap();
                            for (ItemStack item : items) {
                                elementCountMap.inc(item.getId(), item.getAmount());
                            }
                            Fleet tradeFleet = new Fleet(npcFaction.getFleetManager().spawnTradingFleet(elementCountMap, event.getFrom(), event.getTo()));
                            tradeFleet.idle();
                            ContractUtils.tradeFleets.put(cargoContract, tradeFleet.getInternalFleet().dbid);
                            ContractUtils.startCargoClaimTimer(cargoContract);

                            DataUtils.addContract(cargoContract);
                            if (ContractsScrollableList.getInst() != null) {
                                ContractsScrollableList.getInst().clear();
                                ContractsScrollableList.getInst().handleDirty();
                            }
                            //Todo: Pause the trade progress
                        }
                    }
                });

                StarLoader.registerListener(FleetLoadSectorEvent.class, new Listener<FleetLoadSectorEvent>() {
                    @Override
                    public void onEvent(FleetLoadSectorEvent event) {
                        StarSector newSector = StarUniverse.getUniverse().getSector(event.getNewPosition());
                        if (ContractUtils.cargoSectors.containsValue(newSector)) {
                            for (FleetMember member : event.getFleet().getMembers()) {
                                Ship ship = new Ship(member.getLoaded());
                                PlayerData playerData = DataUtils.getPlayerData((ship.getDockedRoot().getPilot().getName()));
                                for (Contract contract : DataUtils.getPlayerContracts(playerData.getName())) {
                                    if (contract.getContractType().equals(Contract.ContractType.CARGO_ESCORT)) {
                                        Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(ContractUtils.tradeFleets.get(contract)));
                                        if (tradeFleet.getFlagshipSector().equals(contract.getTarget().getLocation())) {
                                            DebugFile.log("[DEBUG]: Trade fleet for contract " + contract.getName() + " has arrived at their target destination.");
                                            try {
                                                StarPlayer player = new StarPlayer(GameServer.getServerState().getPlayerFromName(playerData.getName()));
                                                StarFaction traders = new StarFaction(GameServer.getServerState().getFactionManager().getFaction(tradersFactionID));
                                                StarFaction contractor = contract.getContractor();
                                                contract.setFinished(true);
                                                DataUtils.removeContract(contract, false, player);
                                                PlayerData pData = DataUtils.getPlayerData((player.getName()));
                                                pData.modOpinionScore(traders, 5);
                                                pData.modOpinionScore(contractor, 10);
                                                DataUtils.addPlayer(pData);
                                            } catch (PlayerNotFountException e) {
                                                e.printStackTrace();
                                            }
                                            //Todo: Pause the trade progress
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }

            StarLoader.registerListener(PlayerSpawnEvent.class, new Listener<PlayerSpawnEvent>() {
                @Override
                public void onEvent(PlayerSpawnEvent event) {
                    StarPlayer player = new StarPlayer(event.getPlayer().getOwnerState());
                    if (DataUtils.getPlayerData(player.getName()) == null) {
                        PlayerData playerData = new PlayerData(player);
                        DataUtils.addPlayer(playerData);
                        if (debugMode)
                            DebugFile.log("[DEBUG]: Registered PlayerData for " + player.getName() + ".", Contracts.getInstance());
                    }
                }
            });

            StarLoader.registerListener(PlayerDeathEvent.class, new Listener<PlayerDeathEvent>() {
                @Override
                public void onEvent(PlayerDeathEvent event) {
                    if (event.getDamager().isSegmentController()) {
                        SegmentController controller = (SegmentController) event.getDamager();
                        if (controller.getType().equals(SimpleTransformableSendableObject.EntityType.SHIP)) {
                            Ship ship = new Ship(controller);
                            if (ship.getDockedRoot().getPilot() != null) {
                                StarPlayer attacker = ship.getDockedRoot().getPilot();
                                StarPlayer target = new StarPlayer(event.getPlayer());
                                for (Contract contract : DataUtils.getPlayerContracts(attacker.getName())) {
                                    PlayerTarget playerTarget = (PlayerTarget) contract.getTarget();
                                    if (contract.getContractType().equals(Contract.ContractType.BOUNTY) && playerTarget.getTargets()[0].equals(target.getName())) {
                                        Server.broadcastMessage("[CONTRACTS]: " + attacker.getName() + " has claimed the bounty on " + target.getName() + " for a reward of " + contract.getReward() + " credits!");
                                        attacker.setCredits(attacker.getCredits() + contract.getReward());
                                        DataUtils.removeContract(contract, false, attacker);
                                    }
                                }
                            }
                        }
                    } else if (event.getDamager().getOwnerState() instanceof PlayerState) {
                        StarPlayer attacker = new StarPlayer((PlayerState) event.getDamager().getOwnerState());
                        StarPlayer target = new StarPlayer(event.getPlayer());
                        for (Contract contract : DataUtils.getPlayerContracts(attacker.getName())) {
                            PlayerTarget playerTarget = (PlayerTarget) contract.getTarget();
                            if (contract.getContractType().equals(Contract.ContractType.BOUNTY) && playerTarget.getTargets()[0].equals(target.getName())) {
                                Server.broadcastMessage("[CONTRACTS]: " + attacker.getName() + " has claimed the bounty on " + target.getName() + " for a reward of " + contract.getReward() + " credits!");
                                attacker.setCredits(attacker.getCredits() + contract.getReward());
                                PlayerData attackerData = DataUtils.getPlayerData(attacker.getName());
                                attackerData.modOpinionScore(contract.getContractor(), 10);
                                DataUtils.addPlayer(attackerData);
                                DataUtils.removeContract(contract, false, attacker);
                                break;
                            }
                        }
                    }
                }
            });
        }
        if(gameState.equals(Mode.CLIENT) || gameState.equals(Mode.SINGLEPLAYER)) {

            StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
                @Override
                public void onEvent(final GUITopBarCreateEvent guiTopBarCreateEvent) {
                    GUITopBar.ExpandedButton dropDownButton = guiTopBarCreateEvent.getDropdownButtons().get(guiTopBarCreateEvent.getDropdownButtons().size() - 1);
                    dropDownButton.addExpandedButton("CONTRACTS", new GUICallback() {
                        @Override
                        public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                            if (mouseEvent.pressedLeftMouse()) {
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
                            if (mouseEvent.pressedLeftMouse()) {
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
                    if (event.getTitle().equals(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHOP_SHOPNEW_SHOPPANELNEW_2)) {
                        ContractsTab contractsTab = new ContractsTab(event.getWindow().getState(), event.getWindow());
                        contractsTab.onInit();
                        event.getWindow().getTabs().add(contractsTab);

                    } else if (event.getTitle().equals(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FACTION_NEWFACTION_FACTIONPANELNEW_2)) {
                        GUIContentPane diplomacyTab = event.getPane();
                        diplomacyTab.getTextboxes().clear();
                        diplomacyTab.setTabName("DIPLOMACY");

                        //Todo: Diplomacy Tab

                        ObjectArrayList<GUIContentPane> tabs = event.getWindow().getTabs();
                        tabs.set(tabs.indexOf(event.getPane()), diplomacyTab);
                        event.getWindow().clearTabs();
                        for (GUIContentPane tab : tabs) {
                            event.getWindow().addTab(tab);
                        }
                    }
                }
            });
        }
        DebugFile.log("Registered Listeners!", this);
    }

    private void registerCommands() {

        StarLoader.registerCommand(new EndContractsCommand());
        StarLoader.registerCommand(new SpawnTradeFleetCommand());
        StarLoader.registerCommand(new SetOpinionCommand());

        DebugFile.log("Registered Commands!", this);
    }

    private void initConfig() {
        //Config
        FileConfiguration config = getConfig("config");
        config.saveDefault(defaultConfig);

        this.debugMode = Boolean.parseBoolean(config.getString("debug-mode"));
        this.writeFrequency = config.getInt("write-frequency");
        this.modCompatibility = Boolean.parseBoolean(config.getString("mod-compatibility-enabled"));
        this.cargoContractsEnabled = Boolean.parseBoolean(config.getString("cargo-contracts-enabled"));
        this.cargoEscortBonus = config.getDouble("cargo-escort-bonus");
        this.contractTimerMax = config.getInt("contract-timer-max");
        this.npcContractsEnabled = Boolean.parseBoolean(config.getString("npc-contracts-enabled"));
        this.tradersFactionID = config.getInt("traders-faction-id");

    }

    public static Contracts getInstance() {
        return inst;
    }
}
