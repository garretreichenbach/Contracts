package thederpgamer.contracts.gui.contract.contractlist;

import api.common.GameClient;
import api.utils.gui.SimplePopup;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.gui.contract.newcontract.NewContractDialog;

public class ContractsTab extends GUIContentPane {

    private final int width;
    private final int height;
    private GUIHorizontalButtonTablePane buttonPane;
    private ContractsScrollableList contractsScrollableList;

    public ContractsTab(InputState inputState, GUIWindowInterface guiWindowInterface) {
        super(inputState, guiWindowInterface, "CONTRACTS");
        this.width = guiWindowInterface.getInnerWidth();
        this.height = guiWindowInterface.getInnerHeigth();
    }

    @Override
    public void onInit() {
        super.onInit();
        createTab();
    }

    public ContractsScrollableList getContractList() {
        return contractsScrollableList;
    }

    private void createTab() {
        setTextBoxHeightLast(height - 82);

        contractsScrollableList = new ContractsScrollableList(getState(), width, height - 86, getContent(0));
        contractsScrollableList.onInit();
        final PlayerState player = GameClient.getClientPlayerState();
        final InputState state = getState();

        addNewTextBox(0, 22);
        (buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, getContent(1))).onInit();

        buttonPane.addButton(0, 0, "ADD CONTRACT", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    if(player.getFactionId() != 0) {
                        getState().getController().queueUIAudio("0022_menu_ui - enter");
                        (new NewContractDialog(GameClient.getClientState(), player.getFactionId())).activate();
                    } else {
                        (new SimplePopup(getState(), "Cannot Add Contract", "You must be in a faction to add new contracts!")).activate();
                    }
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
                return ContractsTab.this.isActive();
            }
        });

        buttonPane.addButton(1, 0, "CANCEL CONTRACT", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    if(contractsScrollableList.getSelectedRow() != null && contractsScrollableList.getSelectedRow().f != null) {
                        final Contract contract = contractsScrollableList.getSelectedRow().f;
                        if(player.getFactionId() == contract.getContractor().getIdFaction() || player.isAdmin()) {
                            getState().getController().queueUIAudio("0022_menu_ui - enter");
                            PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("ConfirmBox", state, "Confirm Cancellation", "Are you sure you wish to cancel this contract? You won't get a refund...") {
                                @Override
                                public void onDeactivate() {
                                }

                                @Override
                                public void pressedOK() {
                                    getState().getController().queueUIAudio("0022_menu_ui - cancel");
                                    ServerDatabase.removeContract(contract);
                                    ServerDatabase.updateContractGUI();
                                }
                            };
                            confirmBox.getInputPanel().onInit();
                            confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
                            confirmBox.getInputPanel().background.setWidth((float) (GLFrame.getWidth() - 435));
                            confirmBox.getInputPanel().background.setHeight((float) (GLFrame.getHeight() - 70));
                            confirmBox.activate();
                        } else {
                            getState().getController().queueUIAudio("0022_menu_ui - error 1");
                            (new SimplePopup(getState(), "Cannot Cancel Contract", "You cannot cancel this contract as you aren't the contractor!")).activate();
                        }
                    }
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
                return ContractsTab.this.isActive();
            }
        });

        getContent(1).attach(buttonPane);
        getContent(0).attach(contractsScrollableList);
    }
}
