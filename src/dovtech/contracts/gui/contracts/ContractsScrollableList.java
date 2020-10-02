package dovtech.contracts.gui.contracts;

import api.common.GameClient;
import api.common.GameServer;
import api.entity.Fleet;
import api.entity.StarPlayer;
import api.utils.game.PlayerUtils;
import api.utils.game.inventory.InventoryUtils;
import api.utils.game.inventory.ItemStack;
import api.utils.gui.GUIUtils;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import api.utils.gui.SimplePopup;
import com.ctc.wstx.util.DataUtil;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.CargoTarget;
import dovtech.contracts.contracts.target.MiningTarget;
import dovtech.contracts.contracts.target.PlayerTarget;
import dovtech.contracts.contracts.target.ProductionTarget;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.ContractUtils;
import dovtech.contracts.util.DataUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class ContractsScrollableList extends ScrollableTableList<Contract> implements GUIActiveInterface {

    private StarPlayer player;
    private static ContractsScrollableList inst;

    public static ContractsScrollableList getInst() {
        if (inst != null) return inst;
        return null;
    }

    public ContractsScrollableList(InputState state, float var2, float var3, GUIElement guiElement) {
        super(state, var2, var3, guiElement);
        this.player = new StarPlayer(GameClient.getClientPlayerState());
        inst = this;
    }

    @Override
    public void initColumns() {
        new StringComparator();

        this.addColumn("Task", 15.0F, new Comparator<Contract>() {
            public int compare(Contract o1, Contract o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        this.addColumn("Type", 7.0F, new Comparator<Contract>() {
            public int compare(Contract o1, Contract o2) {
                return o1.getContractType().compareTo(o2.getContractType());
            }
        });

        this.addColumn("Contractor", 7.0F, new Comparator<Contract>() {
            public int compare(Contract o1, Contract o2) {
                return o1.getContractor().getName().compareTo(o2.getContractor().getName());
            }
        });

        this.addColumn("Reward", 5.0F, new Comparator<Contract>() {
            public int compare(Contract o1, Contract o2) {
                return CompareTools.compare(o1.getReward(), o2.getReward());
            }
        });

        this.addTextFilter(new GUIListFilterText<Contract>() {
            public boolean isOk(String s, Contract contract) {
                return contract.getName().toLowerCase().contains(s.toLowerCase());
            }
        }, ControllerElement.FilterRowStyle.LEFT);

        this.addDropdownFilter(new GUIListFilterDropdown<Contract, Contract.ContractType>(Contract.ContractType.values()) {
            public boolean isOk(Contract.ContractType contractType, Contract contract) {
                switch (contractType) {
                    case ALL:
                        return true;
                    case CARGO_ESCORT:
                        return contract.getContractType().equals(Contract.ContractType.CARGO_ESCORT);
                    case PRODUCTION:
                        return contract.getContractType().equals(Contract.ContractType.PRODUCTION);
                    case BOUNTY:
                        return contract.getContractType().equals(Contract.ContractType.BOUNTY);
                    case MINING:
                        return contract.getContractType().equals(Contract.ContractType.MINING);
                }
                return true;
            }

        }, new CreateGUIElementInterface<Contract.ContractType>() {
            @Override
            public GUIElement create(Contract.ContractType contractType) {
                GUIAncor anchor = new GUIAncor(getState(), 10.0F, 24.0F);
                GUITextOverlayTableDropDown dropDown;
                (dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(contractType.displayName);
                dropDown.setPos(4.0F, 4.0F, 0.0F);
                anchor.setUserPointer(contractType);
                anchor.attach(dropDown);
                return anchor;
            }

            @Override
            public GUIElement createNeutral() {
                return null;
            }
        }, ControllerElement.FilterRowStyle.RIGHT);

        this.activeSortColumnIndex = 0;
    }

    @Override
    protected Collection<Contract> getElementList() {
        inst = this;
        return DataUtils.getAllContracts();
    }

    public SimpleGUIHorizontalButtonPane redrawButtonPane(final Contract contract) {
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), 300, 32, 2);
        final PlayerData playerData = DataUtils.getPlayerData(player.getName());
        final ArrayList<Contract> playerContracts = DataUtils.getPlayerContracts(playerData.getName());
        if (!playerContracts.contains(contract)) {
            GUITextButton claimContractButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.OK, "CLAIM CONTRACT", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        boolean canClaim = true;
                        if (contract.getContractor().getID() == player.getPlayerState().getFactionId() && !player.getPlayerState().isAdmin()) {
                            (new SimplePopup(getState(), "Cannot Claim Contract", "You can't claim your own contract!")).activate();
                        } else {
                            if((contract.getContractor().getEnemies().contains(player.getFaction()) || contract.getContractor().getPersonalEnemies().contains(player)) && !player.getPlayerState().isAdmin()) {
                                (new SimplePopup(getState(), "Cannot Claim Contract", "You are enemies with the contractor!")).activate();
                                return;
                            }
                            if (contract.getContractType().equals(Contract.ContractType.BOUNTY)) {
                                PlayerTarget target = (PlayerTarget) contract.getTarget();
                                PlayerData targetPlayer = DataUtils.getPlayerData(target.getTargets()[0]);
                                int targetFaction = targetPlayer.getFactionID();
                                int playerFaction = player.getPlayerState().getFactionId();
                                if (!player.getPlayerState().isAdmin() && (targetPlayer.getName().equals(player.getName()) || targetFaction == playerFaction || GameServer.getServerState().getFactionManager().getFaction(targetFaction).getFriends().contains(GameServer.getServerState().getFactionManager().getFaction(playerFaction)))) {
                                    canClaim = false;
                                }
                            }

                            if (canClaim) {
                                if (playerContracts.size() >= 5) {
                                    (new SimplePopup(getState(), "Cannot Claim Contract", "You have too many active contracts!")).activate();
                                } else {
                                    getState().getController().queueUIAudio("0022_menu_ui - enter");
                                    contract.setTimer(0);
                                    contract.addClaimant(player);
                                    playerData.addContract(contract);
                                    DataUtils.addPlayer(playerData);
                                    DataUtils.addContract(contract);
                                    if (contract.getContractType().equals(Contract.ContractType.CARGO_ESCORT)) {
                                        Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(ContractUtils.tradeFleets.get(contract)));
                                        player.sendMail("Trading Guild", "Cargo Escort Contract", "Head to " + tradeFleet.getFlagshipSector().getCoordinates().toString() + " to start the contract. \nIf you do not show up within 15 minutes, the contract will be cancelled automatically. \nMake sure all ships you bring are registered in a fleet.");
                                        ContractUtils.startCargoTimer(contract, player, tradeFleet.getFlagshipSector());
                                    }
                                }
                            } else {
                                SimplePopup popup = new SimplePopup(getState(), "Cannot Claim Contract", "You can't claim this bounty!");
                                popup.activate();
                            }
                            if (PlayerContractsScrollableList.getInst() != null) {
                                PlayerContractsScrollableList.getInst().clear();
                                PlayerContractsScrollableList.getInst().handleDirty();
                            }
                            clear();
                            handleDirty();
                        }
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(claimContractButton);
        } else {
            GUITextButton cancelClaimButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.CANCEL, "CANCEL CLAIM", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        getState().getController().queueUIAudio("0022_menu_ui - back");
                        contract.removeClaimant(player);
                        playerData.removeContract(contract);
                        DataUtils.addPlayer(playerData);
                        DataUtils.addContract(contract);
                        if (PlayerContractsScrollableList.getInst() != null) {
                            PlayerContractsScrollableList.getInst().clear();
                            PlayerContractsScrollableList.getInst().handleDirty();
                        }
                        clear();
                        handleDirty();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(cancelClaimButton);
        }

        if (playerContracts.contains(contract)) {
            if (contract.getContractType().equals(Contract.ContractType.CARGO_ESCORT)) {
                final CargoTarget cargoTarget = (CargoTarget) contract.getTarget();
                GUITextButton beginContractButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.OK, "START CONTRACT", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        Fleet tradeFleet = new Fleet(Fleet.getServerFleetManager().getByFleetDbId(ContractUtils.tradeFleets.get(contract)));
                        if (tradeFleet.getFlagshipSector().equals(player.getSector())) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            PlayerUtils.sendMessage(player.getPlayerState(), "[TRADERS]: Heading to " + cargoTarget.getLocation()[0] + ", " + cargoTarget.getLocation()[1] + ", " + cargoTarget.getLocation()[2] + ".");
                            ContractUtils.startCargoContract(contract, player);
                        } else {
                            (new SimplePopup(getState(), "Cannot Start Contract", "You must be in the starting sector to begin this contract!")).activate();
                        }

                    }

                    @Override
                    public boolean isOccluded() {
                        return !isActive();
                    }
                });
                buttonPane.addButton(beginContractButton);
            } else if (contract.getContractType().equals(Contract.ContractType.MINING)) {
                final MiningTarget miningTarget = (MiningTarget) contract.getTarget();
                GUITextButton completeContractButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.OK, "COMPLETE CONTRACT", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        boolean hasItems = true;
                        for(ItemStack itemStack : miningTarget.getTargets()) {
                            short id = itemStack.getId();
                            int amount = itemStack.getAmount();
                            if(InventoryUtils.getItemAmount(player.getInventory().getInternalInventory(), id) < amount) {
                                hasItems = false;
                                break;
                            }
                        }

                        if (hasItems) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");

                            for(Object requiredObject : miningTarget.getTargets()) {
                                ItemStack requiredStack = (ItemStack) requiredObject;
                                InventoryUtils.consumeItems(player.getInventory().getInternalInventory(), requiredStack);
                            }

                            DataUtils.removeContract(contract, false, player);
                            player.setCredits(player.getCredits() + contract.getReward());
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }

                    }

                    @Override
                    public boolean isOccluded() {
                        return !isActive();
                    }
                });
                buttonPane.addButton(completeContractButton);
            } else if (contract.getContractType().equals(Contract.ContractType.PRODUCTION)) {
                final ProductionTarget productionTarget = (ProductionTarget) contract.getTarget();
                GUITextButton completeContractButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.OK, "COMPLETE CONTRACT", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        boolean hasItems = true;
                        for(ItemStack itemStack : productionTarget.getTargets()) {
                            short id = itemStack.getId();
                            int amount = itemStack.getAmount();
                            if(InventoryUtils.getItemAmount(player.getInventory().getInternalInventory(), id) < amount) {
                                hasItems = false;
                                break;
                            }
                        }

                        if (hasItems) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");

                            for(Object requiredObject : productionTarget.getTargets()) {
                                ItemStack requiredStack = (ItemStack) requiredObject;
                                InventoryUtils.consumeItems(player.getInventory().getInternalInventory(), requiredStack);
                            }

                            DataUtils.removeContract(contract, false, player);
                            player.setCredits(player.getCredits() + contract.getReward());
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }

                    }

                    @Override
                    public boolean isOccluded() {
                        return !isActive();
                    }
                });
                buttonPane.addButton(completeContractButton);
            }
        }
        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, final Set<Contract> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        for (final Contract contract : set) {

            GUITextOverlayTable nameTextElement;
            (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(contract.getName());
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

            GUITextOverlayTable contractTypeTextElement;
            (contractTypeTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(contract.getContractType().displayName);
            GUIClippedRow contractTypeRowElement;
            (contractTypeRowElement = new GUIClippedRow(this.getState())).attach(contractTypeTextElement);

            GUITextOverlayTable contractorTextElement;
            (contractorTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(contract.getContractor().getName());
            GUIClippedRow contractorRowElement;
            (contractorRowElement = new GUIClippedRow(this.getState())).attach(contractorTextElement);

            GUITextOverlayTable rewardTextElement;
            (rewardTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(String.valueOf(contract.getReward()));
            GUIClippedRow rewardRowElement;
            (rewardRowElement = new GUIClippedRow(this.getState())).attach(rewardTextElement);

            ContractListRow contractListRow = new ContractListRow(this.getState(), contract, nameRowElement, contractTypeRowElement, contractorRowElement, rewardRowElement);
            contractListRow.expanded = new GUIElementList(getState());

            SimpleGUIHorizontalButtonPane buttonPane = redrawButtonPane(contract);
            buttonPane.setPos(contractListRow.expanded.getPos());
            contractListRow.expanded.add(new GUIListElement(buttonPane, buttonPane, getState()));
            contractListRow.expanded.attach(buttonPane);
            contractListRow.onInit();
            guiElementList.add(contractListRow);
        }
        guiElementList.updateDim();
    }

    public class ContractListRow extends ScrollableTableList<Contract>.Row {

        public ContractListRow(InputState inputState, Contract contract, GUIElement... guiElements) {
            super(inputState, contract, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }
}
