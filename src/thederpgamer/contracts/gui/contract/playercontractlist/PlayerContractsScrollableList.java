package thederpgamer.contracts.gui.contract.playercontractlist;

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
    private float width;
    private float height;

    public PlayerContractsScrollableList(InputState state, float width, float height, GUIElement guiElement) {
        super(state, width, height, guiElement);
        this.player = GameClient.getClientPlayerState();
        this.width = width;
        this.height = height;
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
    public ArrayList<Contract> getElementList() {
        return ServerDatabase.getPlayerContracts(ServerDatabase.getPlayerData(player));
    }

    public GUIHorizontalButtonTablePane redrawButtonPane(final Contract contract, GUIAncor anchor) throws PlayerNotFountException {
        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
        buttonPane.onInit();
        final PlayerData playerData = ServerDatabase.getPlayerData(player);

        buttonPane.addButton(0, 0, "CANCEL CLAIM", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    getState().getController().queueUIAudio("0022_menu_ui - back");
                    contract.getClaimants().remove(playerData);
                    playerData.contracts.remove(contract);
                    ServerDatabase.updatePlayerData(playerData);
                    ServerDatabase.updateContract(contract);
                    ServerDatabase.updateContractGUI();
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return true;
            }
        });

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
                            ServerDatabase.updateContractGUI();
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
                            ServerDatabase.updateContractGUI();
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
            buttonPane.addButton(1, 0, "COMPLETE CONTRACT", GUIHorizontalArea.HButtonColor.GREEN, completeContractCallback, new GUIActivationCallback() {
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

        return buttonPane;
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<Contract> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        for(Contract contract : set) {
            try {
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

                PlayerContractListRow playerContractListRow = new PlayerContractListRow(this.getState(), contract, nameRowElement, contractTypeRowElement, contractorRowElement, rewardRowElement);
                GUIAncor anchor = new GUIAncor(getState(), width, 28.0f);
                anchor.attach(redrawButtonPane(contract, anchor));
                playerContractListRow.expanded = new GUIElementList(getState());
                playerContractListRow.expanded.add(new GUIListElement(anchor, getState()));
                playerContractListRow.expanded.attach(anchor);
                playerContractListRow.onInit();
                guiElementList.add(playerContractListRow);
            } catch(PlayerNotFountException e) {
                e.printStackTrace();
            }
        }
        guiElementList.updateDim();
    }

    public class PlayerContractListRow extends ScrollableTableList<Contract>.Row {

        public PlayerContractListRow(InputState inputState, Contract contract, GUIElement... guiElements) {
            super(inputState, contract, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }

        @Override
        public void clickedOnRow() {
            super.clickedOnRow();
            PlayerContractsScrollableList.this.setSelectedRow(this);
            setChanged();
            notifyObservers();
        }
    }
}