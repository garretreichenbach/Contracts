package dovtech.contracts.gui.contracts;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.utils.gui.SimpleGUIHorizontalButtonPane;
import api.utils.gui.SimplePopup;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.gui.contracts.newcontract.NewContractDialog;
import dovtech.contracts.util.DataUtil;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIWindowInterface;
import org.schema.schine.input.InputState;

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

    public void createTab() {

        /* Faction/Contractor Logo
        contractsTab.addNewTextBox(0, 150);
        Sprite contractorLogo = new Sprite(new Texture(0, 0, resourcesPath + "/gui/logo/trading-guild-logo.png")); //Default Contractor
        GUIOverlay logoOverlay = new GUIOverlay();
        contractsTab.getContent(0, 0).attach(logoOverlay); */

        setTextBoxHeightLast(height - 86);

        final ContractsScrollableList contractsScrollableList = new ContractsScrollableList(getState(), width, height - 86, getContent(0));
        contractsScrollableList.onInit();
        final StarPlayer player = new StarPlayer(GameClient.getClientPlayerState());
        final InputState state = getState();
        if (Contracts.getInstance().betterFactionsEnabled) {
            addDivider(250);
            setTextBoxHeightLast(1, height - 86);
            addNewTextBox(1, 32);
        } else {
            addNewTextBox(0, 32);
        }
        SimpleGUIHorizontalButtonPane buttonPane = new SimpleGUIHorizontalButtonPane(getState(), width - 20, 32, 2);

        GUITextButton addContractButton = new GUITextButton(state,(width / 2) - 4, 24, GUITextButton.ColorPalette.OK, "ADD CONTRACT", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse()) {
                    if (player.getPlayerState().getFactionId() != 0) {
                        //Todo: Open add contract menu
                        GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                        (new NewContractDialog(GameClient.getClientState(), player.getFaction())).activate();
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
                    if (contractsScrollableList.getSelectedRow() != null && contractsScrollableList.getSelectedRow().f != null) {
                        final Contract contract = contractsScrollableList.getSelectedRow().f;
                        if (player.getPlayerState().getFactionId() == contract.getContractor().getID() || player.getPlayerState().isAdmin()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("ConfirmBox", state, "Confirm Cancellation", "Are you sure you wish to cancel this contract? You won't get a refund...") {
                                @Override
                                public void onDeactivate() {
                                }

                                @Override
                                public void pressedOK() {
                                    GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                                    DataUtil.removeContract(contract);
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
                    }
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        buttonPane.addButton(removeContractButton);

        if (Contracts.getInstance().betterFactionsEnabled) {
            getContent(1, 1).attach(buttonPane);
            getContent(1, 0).attach(contractsScrollableList);

            GUITextOverlay contractorDescOverlay = new GUITextOverlay(250, 300, getState());
            contractorDescOverlay.onInit();
            contractorDescOverlay.setTextSimple("Placeholder Text");
            getContent(0, 0).attach(contractorDescOverlay);
            //Todo
        } else {
            getContent(1).attach(buttonPane);
            getContent(0).attach(contractsScrollableList);
        }
    }
}
