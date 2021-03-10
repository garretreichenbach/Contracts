package thederpgamer.contracts.gui.contract;

import api.common.GameClient;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import api.utils.gui.SimplePopup;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;
import thederpgamer.contracts.gui.contract.newcontract.NewContractDialog;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;

public class ContractsTab extends GUIContentPane {

    private int width;
    private int height;

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

    private void createTab() {
        setTextBoxHeightLast(height - 86);

        final ContractsScrollableList contractsScrollableList = new ContractsScrollableList(getState(), width, height - 86, getContent(0));
        contractsScrollableList.onInit();
        final PlayerState player = GameClient.getClientPlayerState();
        final InputState state = getState();
        addNewTextBox(0, 32);
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), width - 20, 32, 2);

        GUITextButton addContractButton = new GUITextButton(state,(width / 2) - 4, 24, GUITextButton.ColorPalette.OK, "ADD CONTRACT", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    if (player.getFactionId() != 0) {
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        (new NewContractDialog(GameClient.getClientState(), player.getFactionId())).activate();
                        contractsScrollableList.clear();
                        contractsScrollableList.handleDirty();
                    } else {
                        (new SimplePopup(getState(), "Cannot Add Contract", "You must be in a faction to add new contracts!")).activate();
                    }
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(addContractButton);

        GUITextButton removeContractButton = new GUITextButton(state, (width / 2) - 4, 24, GUITextButton.ColorPalette.OK, "CANCEL CONTRACT", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    if (contractsScrollableList.getSelectedRow() != null && contractsScrollableList.getSelectedRow().getSort() != null) {
                        final Contract contract = contractsScrollableList.getSelectedRow().getSort();
                        try {
                            if (player.getFactionId() == contract.getContractor().getIdFaction() || player.isAdmin()) {
                                GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("ConfirmBox", state, "Confirm Cancellation", "Are you sure you wish to cancel this contract? You won't get a refund...") {
                                    @Override
                                    public void onDeactivate() {
                                    }

                                    @Override
                                    public void pressedOK() {
                                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                        ServerDatabase.removeContract(contract);
                                        contractsScrollableList.clear();
                                        contractsScrollableList.handleDirty();
                                    }
                                };
                                confirmBox.getInputPanel().onInit();
                                confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
                                confirmBox.getInputPanel().background.setWidth((float) (GLFrame.getWidth() - 435));
                                confirmBox.getInputPanel().background.setHeight((float) (GLFrame.getHeight() - 70));
                                confirmBox.activate();
                            } else {
                                (new SimplePopup(getState(), "Cannot Cancel Contract", "You cannot cancel this contract as you aren't the contractor!")).activate();
                            }
                        } catch (PlayerNotFountException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(removeContractButton);
        getContent(1).attach(buttonPane);
        getContent(0).attach(contractsScrollableList);
    }
}
