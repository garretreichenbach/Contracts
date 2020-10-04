package dovtech.contracts.gui.contracts.newcontract;

import api.common.GameClient;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.utils.game.inventory.ItemStack;
import api.utils.gui.SimplePopup;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.MiningTarget;
import dovtech.contracts.contracts.target.PlayerTarget;
import dovtech.contracts.contracts.target.ProductionTarget;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.core.GLFW;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import java.util.Objects;
import java.util.UUID;

public class NewContractDialog extends PlayerInput {

    private NewContractPanel panel;
    private int contractMode;


    public NewContractDialog(GameClientState gameClientState, StarFaction contractor) {
        super(gameClientState);
        this.panel = new NewContractPanel(getState(), this, contractor);
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
                try {
                    StarPlayer currentPlayer = new StarPlayer(GameClient.getClientPlayerState());
                    if (guiElement.getUserPointer().equals("OK")) {
                        if (currentPlayer.getPlayerState().getFactionId() == 0) {
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
                                PlayerData playerData = DataUtils.getPlayerData(name);
                                PlayerData currentPlayerData = DataUtils.getPlayerData(currentPlayer.getName());
                                if (playerData == null) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "Player " + name + " does not exist!")).activate();
                                } else {
                                    if (currentPlayer.getName().equals(name) && !currentPlayer.getPlayerState().isAdmin()) {
                                        (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on yourself!")).activate();
                                    } else if (currentPlayer.getFaction().getID() == playerData.getFactionID() && !currentPlayer.getPlayerState().isAdmin()) {
                                        (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of your own faction!")).activate();
                                    } else if (DataUtils.getAllies(currentPlayerData.getFactionID()).contains(playerData.getFactionID()) && !currentPlayer.getPlayerState().isAdmin()) {
                                        (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of an allied faction!")).activate();
                                    } else {

                                        PlayerTarget target = new PlayerTarget();
                                        target.setTargets(playerData.getName());
                                        Objects.requireNonNull(DataUtils.getPlayerData(currentPlayer.getName())).modOpinionScore(playerData.getFactionID(), -15);
                                        Contract contract = new Contract(currentPlayer.getFaction().getID(), "Kill " + name, Contract.ContractType.BOUNTY, bountyAmount, UUID.randomUUID().toString(), target);
                                        DataUtils.addContract(contract);
                                        currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                        if (ContractsScrollableList.getInst() != null) {
                                            ContractsScrollableList.getInst().clear();
                                            ContractsScrollableList.getInst().handleDirty();
                                        }
                                        deactivate();
                                    }
                                    //Todo: Add checks/modifiers for non-aggression pacts, relations, trade deals, etc if BetterFactions mod is installed
                                }
                            } else if (contractMode == 2) {
                                MiningTarget target = new MiningTarget();
                                int count = panel.getCount();
                                if (count <= 0) {
                                    (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                                } else {
                                    ItemStack itemStack = new ItemStack(panel.getSelectedBlockType().getId());
                                    itemStack.setAmount(count);
                                    target.setTargets(itemStack);
                                    Contract contract = new Contract(currentPlayer.getFaction().getID(), "Mine x" + count + " " + itemStack.getName(), Contract.ContractType.MINING, panel.getReward(), UUID.randomUUID().toString(), target);
                                    DataUtils.addContract(contract);
                                    currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                    if (ContractsScrollableList.getInst() != null) {
                                        ContractsScrollableList.getInst().clear();
                                        ContractsScrollableList.getInst().handleDirty();
                                    }
                                    deactivate();
                                }
                            } else if (contractMode == 3) {
                                ProductionTarget target = new ProductionTarget();
                                int count = panel.getCount();
                                if (count <= 0) {
                                    (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                                } else {
                                    ItemStack itemStack = new ItemStack(panel.getSelectedBlockType().getId());
                                    itemStack.setAmount(count);
                                    target.setTargets(itemStack);
                                    Contract contract = new Contract(currentPlayer.getFaction().getID(), "Produce x" + count + " " + itemStack.getName(), Contract.ContractType.PRODUCTION, panel.getReward(), UUID.randomUUID().toString(), target);
                                    DataUtils.addContract(contract);
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
                } catch (PlayerNotFountException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public NewContractPanel getInputPanel() {
        return panel;
    }

    public StarFaction getContractor() {
        return panel.getContractor();
    }
}
