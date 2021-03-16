package thederpgamer.contracts.gui.contract;

import api.common.GameClient;
import api.utils.game.inventory.InventoryUtils;
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

    public GUIHorizontalButtonTablePane redrawButtonPane(final Contract contract, final GUIElementList content) throws PlayerNotFountException {
        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, content);
        buttonPane.onInit();
        final PlayerData playerData = ServerDatabase.getPlayerData(player);
        GUIActivationCallback activationCallback = new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return PlayerContractsScrollableList.this.isActive();
            }
        };

        buttonPane.addButton(0, 0, "CANCEL CLAIM", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    getState().getController().queueUIAudio("0022_menu_ui - back");
                    contract.getClaimants().remove(playerData);
                    playerData.contracts.remove(contract);
                    ServerDatabase.updatePlayerData(playerData);
                    ServerDatabase.updateContract(contract);
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        }, activationCallback);

        GUICallback completeContractCallback = null;
        if(contract.getContractType().equals(Contract.ContractType.MINING)) {
            completeContractCallback = new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        boolean hasItems = true;
                        ItemStack itemStack = (ItemStack) contract.getTarget();
                        short id = itemStack.id;
                        int amount = itemStack.count;
                        if(InventoryUtils.getItemAmount(player.getInventory(), id) < amount) hasItems = false;

                        if(hasItems || (player.isUseCreativeMode() && player.isAdmin())) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            InventoryUtils.consumeItems(player.getInventory(), itemStack.id, itemStack.count);
                            player.setCredits(player.getCredits() + contract.getReward());
                            ServerDatabase.removeContract(contract);
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }
                    }
                }

                @Override
                public boolean isOccluded() {
                    return false;
                }
            };
        } else if(contract.getContractType().equals(Contract.ContractType.PRODUCTION)) {
            completeContractCallback = new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        boolean hasItems = true;
                        ItemStack itemStack = (ItemStack) contract.getTarget();
                        short id = itemStack.id;
                        int amount = itemStack.count;
                        if(InventoryUtils.getItemAmount(player.getInventory(), id) < amount) hasItems = false;

                        if(hasItems || (player.isUseCreativeMode() && player.isAdmin())) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            InventoryUtils.consumeItems(player.getInventory(), itemStack.id, itemStack.count);
                            player.setCredits(player.getCredits() + contract.getReward());
                            ServerDatabase.removeContract(contract);
                        } else {
                            (new SimplePopup(getState(), "Cannot Complete Contract", "You must have the contract items in your inventory!")).activate();
                        }
                    }
                }

                @Override
                public boolean isOccluded() {
                    return false;
                }
            };
        }

        if(completeContractCallback != null) {
            buttonPane.addColumn();
            buttonPane.addButton(1, 0, "COMPLETE CONTRACT", GUIHorizontalArea.HButtonType.BUTTON_BLUE_LIGHT, completeContractCallback, activationCallback);
        }

        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<Contract> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        guiElementList.clear();
        try {
            for(final Contract contract : set) {

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
                contractListRow.onInit();

                GUIHorizontalButtonTablePane buttonPane = redrawButtonPane(contract, contractListRow.expanded);
                buttonPane.setPos(contractListRow.expanded.getPos());
                contractListRow.expanded.add(new GUIListElement(buttonPane, buttonPane, getState()));
                contractListRow.expanded.attach(buttonPane);
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