package dovtech.contracts.gui.contracts;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.utils.gui.GUIUtils;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtil;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.game.client.view.gui.catalog.newcatalog.CatalogScrollableListNew;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.*;

public class PlayerContractsScrollableList extends ScrollableTableList<Contract> implements GUIActiveInterface {

    private ArrayList<Contract> contracts;
    public static boolean updated;

    public PlayerContractsScrollableList(InputState state, float var2, float var3, GUIElement guiElement) {
        super(state, var2, var3, guiElement);
        this.contracts = new ArrayList<>();
        updated = false;
        updateContracts();
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
        if (!updated) updateContracts();
        return contracts;
    }

    public void updateContracts() {
        this.contracts = DataUtil.getPlayerData(new StarPlayer(GameClient.getClientPlayerState())).getContracts();
        flagDirty();
        updated = true;
    }

    @Override
    public void update(Observable observable, Object object) {
        if(!updated) {
            updateContracts();
            updated = true;
        }
        super.update(observable, object);
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<Contract> set) {
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
            (contractorTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(String.valueOf(contract.getContractor().getName()));
            GUIClippedRow contractorRowElement;
            (contractorRowElement = new GUIClippedRow(this.getState())).attach(contractorTextElement);

            GUITextOverlayTable rewardTextElement;
            (rewardTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(String.valueOf(contract.getReward()));
            GUIClippedRow rewardRowElement;
            (rewardRowElement = new GUIClippedRow(this.getState())).attach(rewardTextElement);

            ContractListRow contractListRow;
            (contractListRow = new ContractListRow(this.getState(), contract, nameRowElement, contractTypeRowElement, contractorRowElement, rewardRowElement)).onInit();

            GUIAncor buttonPane = new GUIAncor(getState(), 100, 32);

            final StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());

            GUITextButton viewClaimantsButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.TUTORIAL, "VIEW CLAIMANTS", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        getState().getController().queueUIAudio("0022_menu_ui - enter");
                        GUIMainWindow guiWindow = new GUIMainWindow(GameClient.getClientState(), 1200, 650, "CLAIMANTS");
                        guiWindow.onInit();

                        GUIContentPane claimantsPane = guiWindow.addTab("CLAIMANTS");
                        claimantsPane.setTextBoxHeightLast(300);
                        ContractClaimantsScrollableList contractClaimantsList = new ContractClaimantsScrollableList(getState(), 500, 300, claimantsPane.getContent(0), contract);
                        contractClaimantsList.onInit();
                        contractClaimantsList.updateClaimants();
                        claimantsPane.getContent(0).attach(contractClaimantsList);

                        GUIUtils.activateCustomGUIWindow(guiWindow);
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });

            buttonPane.attach(viewClaimantsButton);
            viewClaimantsButton.setPos(2, 2, 0);

            GUITextButton cancelClaimButton = new GUITextButton(getState(), 130, 24, GUITextButton.ColorPalette.CANCEL, "CANCEL CLAIM", new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if (mouseEvent.pressedLeftMouse()) {
                        getState().getController().queueUIAudio("0022_menu_ui - back");
                        DataUtil.contracts.remove(contract);
                        ArrayList<StarPlayer> claimants = contract.getClaimants();
                        claimants.remove(player);
                        contract.setClaimants(claimants);
                        DataUtil.contractWriteBuffer.add(contract);
                        PlayerData playerData = DataUtil.getPlayerData(player);
                        DataUtil.players.remove(playerData);
                        ArrayList<Contract> playerContracts = playerData.getContracts();
                        playerContracts.remove(contract);
                        playerData.setContracts(playerContracts);
                        DataUtil.players.add(playerData);
                        DataUtil.playerDataWriteBuffer.add(playerData);
                        DataUtil.contracts.add(contract);
                        ContractClaimantsScrollableList.updated = false;
                        PlayerContractsScrollableList.updated = false;
                        updated = false;
                        updateContracts();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return !isActive();
                }
            });
            buttonPane.attach(cancelClaimButton);
            cancelClaimButton.setPos(2 + viewClaimantsButton.getWidth() + 2, 2, 0);

            contractListRow.expanded = new GUIElementList(getState());

            buttonPane.setPos(contractListRow.expanded.getPos());
            contractListRow.expanded.add(new GUIListElement(buttonPane, buttonPane, getState()));
            contractListRow.expanded.attach(buttonPane);

            contractListRow.onInit();
            guiElementList.addWithoutUpdate(contractListRow);
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

        @Override
        public void clickedOnRow() {
            updateContracts();
            super.clickedOnRow();
        }
    }
}