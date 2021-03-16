package thederpgamer.contracts.gui.contract.newcontract;

import api.common.GameClient;
import api.common.GameCommon;
import api.utils.gui.SimplePopup;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.core.GLFW;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import thederpgamer.contracts.gui.contract.ContractsScrollableList;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.inventory.ItemStack;
import thederpgamer.contracts.data.player.PlayerData;
import java.util.UUID;

public class NewContractDialog extends PlayerInput {

    private NewContractPanel panel;
    private int contractMode;


    public NewContractDialog(GameClientState gameClientState, int contractorId) {
        super(gameClientState);
        this.panel = new NewContractPanel(getState(), this, GameCommon.getGameState().getFactionManager().getFaction(contractorId));
        this.panel.onInit();
        contractMode = 0;
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void handleKeyEvent(KeyEventInterface e) {
        if (KeyboardMappings.getEventKeyState(e, getState())) {
            if (KeyboardMappings.getEventKeyRaw(e) == GLFW.GLFW_KEY_ESCAPE) {
                deactivate();
            }
        }
    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {

    }


    @Override
    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
        if (mouseEvent.pressedLeftMouse()) {
            if (guiElement != null && guiElement.getUserPointer() != null) {
                PlayerState currentPlayer = GameClient.getClientPlayerState();
                if (guiElement.getUserPointer().equals("OK")) {
                    if (currentPlayer.getFactionId() == 0) {
                        (new SimplePopup(getState(), "Cannot Add Contract", "You must be in a faction to do this!")).activate();
                        return;
                    }
                    if (panel.getReward() <= 0) {
                        (new SimplePopup(getState(), "Cannot Add Bounty", "The reward must be above 0!")).activate();
                    } else if (currentPlayer.getCredits() < panel.getReward()) {
                        (new SimplePopup(getState(), "Cannot Add Bounty", "You do not have enough credits!")).activate();
                    } else {
                        if (contractMode == 1) {
                            String name = panel.getName();
                            int bountyAmount = panel.getReward();
                            PlayerData playerData = ServerDatabase.getPlayerData(name);
                            PlayerData currentPlayerData = ServerDatabase.getPlayerData(currentPlayer);
                            if (playerData == null) {
                                (new SimplePopup(getState(), "Cannot Add Bounty", "Player " + name + " does not exist!")).activate();
                            } else {
                                if (currentPlayer.getName().equals(name) && !currentPlayer.isAdmin()) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on yourself!")).activate();
                                } else if (currentPlayer.getFactionId() == playerData.factionID && !currentPlayer.isAdmin()) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of your own faction!")).activate();
                                } else if (ServerDatabase.getFactionAllies(currentPlayerData.factionID).contains(playerData.factionID) && !currentPlayer.isAdmin()) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of an allied faction!")).activate();
                                } else {

                                    Contract contract = new Contract(currentPlayer.getFactionId(), "Kill " + name, Contract.ContractType.BOUNTY, bountyAmount, UUID.randomUUID().toString(), playerData);
                                    ServerDatabase.addContract(contract);
                                    currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                    if (ContractsScrollableList.getInst() != null) {
                                        ContractsScrollableList.getInst().clear();
                                        ContractsScrollableList.getInst().handleDirty();
                                    }
                                    deactivate();
                                }
                            }
                        } else if (contractMode == 2) {
                            int count = panel.getCount();
                            if (count <= 0) {
                                (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                            } else {
                                ItemStack itemStack = new ItemStack(panel.getSelectedBlockType());
                                itemStack.count = count;
                                Contract contract = new Contract(currentPlayer.getFactionId(), "Mine x" + count + " " + itemStack.name, Contract.ContractType.MINING, panel.getReward(), UUID.randomUUID().toString(), itemStack);
                                ServerDatabase.addContract(contract);
                                currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                if (ContractsScrollableList.getInst() != null) {
                                    ContractsScrollableList.getInst().clear();
                                    ContractsScrollableList.getInst().handleDirty();
                                }
                                deactivate();
                            }
                        } else if (contractMode == 3) {
                            int count = panel.getCount();
                            if (count <= 0) {
                                (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                            } else {
                                ItemStack itemStack = new ItemStack(panel.getSelectedBlockType());
                                itemStack.count = count;
                                Contract contract = new Contract(currentPlayer.getFactionId(), "Produce x" + count + " " + itemStack.name, Contract.ContractType.PRODUCTION, panel.getReward(), UUID.randomUUID().toString(), itemStack);
                                ServerDatabase.addContract(contract);
                                currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                if (ContractsScrollableList.getInst() != null) {
                                    ContractsScrollableList.getInst().clear();
                                    ContractsScrollableList.getInst().handleDirty();
                                }
                                deactivate();
                            }
                        }
                    }
                } else if (guiElement.getUserPointer().equals("CANCEL") || guiElement.getUserPointer().equals("X")) {
                    deactivate();
                } else if (guiElement.getUserPointer().equals("BOUNTY")) {
                    contractMode = 1;
                    panel.drawBountyPanel();
                } else if (guiElement.getUserPointer().equals("MINING")) {
                    contractMode = 2;
                    panel.drawMiningPanel();
                } else if (guiElement.getUserPointer().equals("PRODUCTION")) {
                    contractMode = 3;
                    panel.drawProductionPanel();
                }
            }
        }
    }

    @Override
    public NewContractPanel getInputPanel() {
        return panel;
    }

    public Faction getContractor() {
        return panel.getContractor();
    }
}
