package thederpgamer.contracts.gui.contract;

import api.common.GameClient;
import api.utils.game.inventory.InventoryUtils;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import api.utils.gui.SimplePopup;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.contract.target.MiningTarget;
import thederpgamer.contracts.data.contract.target.ProductionTarget;
import thederpgamer.contracts.data.inventory.ItemStack;
import thederpgamer.contracts.data.player.PlayerData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

public class PlayerContractsScrollableList extends ScrollableTableList<Contract> implements GUIActiveInterface {

    private PlayerState player;
    private static PlayerContractsScrollableList inst;

    public static PlayerContractsScrollableList getInst() {
        if(inst != null) return inst;
        return null;
    }

    public PlayerContractsScrollableList(InputState state, float var2, float var3, GUIElement guiElement) {
        super(state, var2, var3, guiElement);
        this.player = GameClient.getClientPlayerState();
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
                try {
                    return o1.getContractor().getName().compareTo(o2.getContractor().getName());
                } catch (PlayerNotFountException e) {
                    e.printStackTrace();
                }
                return 0;
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
    protected ArrayList<Contract> getElementList() {
        inst = this;
        return ServerDatabase.getPlayerContracts(ServerDatabase.getPlayerData(player));
    }

    public SimpleGUIHorizontalButtonPane redrawButtonPane(final Contract contract) throws PlayerNotFountException {
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), 300, 32, 2);
        final PlayerData playerData = ServerDatabase.getPlayerData(player);

        GUITextButton cancelClaimButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.CANCEL, "CANCEL CLAIM", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    getState().getController().queueUIAudio("0022_menu_ui - back");
                    contract.getClaimants().remove(playerData);
                    playerData.contracts.remove(contract);
                    ServerDatabase.updatePlayerData(playerData);
                    ServerDatabase.updateContract(contract);
                    if(ContractsScrollableList.getInst() != null) {
                        ContractsScrollableList.getInst().clear();
                        ContractsScrollableList.getInst().handleDirty();
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

        if (contract.getContractType().equals(Contract.ContractType.MINING)) {
            final MiningTarget miningTarget = (MiningTarget) contract.getTarget();
            GUITextButton completeContractButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.OK, "COMPLETE CONTRACT", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        boolean hasItems = true;
                        for (ItemStack itemStack : miningTarget.getTargets()) {
                            short id = itemStack.id;
                            int amount = itemStack.count;
                            if (InventoryUtils.getItemAmount(player.getInventory(), id) < amount) {
                                hasItems = false;
                                break;
                            }
                        }

                        if (hasItems || (player.isUseCreativeMode() && player.isAdmin())) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            for (ItemStack itemStack : miningTarget.getTargets()) InventoryUtils.consumeItems(player.getInventory(), itemStack.id, itemStack.count);
                            player.setCredits(player.getCredits() + contract.getReward());
                            ServerDatabase.removeContract(contract);
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }
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
                    if (mouseEvent.pressedLeftMouse()) {
                        boolean hasItems = true;
                        for (ItemStack itemStack : productionTarget.getTargets()) {
                            short id = itemStack.id;
                            int amount = itemStack.count;
                            if (InventoryUtils.getItemAmount(player.getInventory(), id) < amount) {
                                hasItems = false;
                                break;
                            }
                        }

                        if (hasItems || (player.isUseCreativeMode() && player.isAdmin())) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            for (ItemStack itemStack : productionTarget.getTargets()) InventoryUtils.consumeItems(player.getInventory(), itemStack.id, itemStack.count);
                            ServerDatabase.removeContract(contract);
                            player.setCredits(player.getCredits() + contract.getReward());
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.addButton(completeContractButton);
        }
        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<Contract> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        try {
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
                (contractorTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(String.valueOf(contract.getContractor().getName()));
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
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
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