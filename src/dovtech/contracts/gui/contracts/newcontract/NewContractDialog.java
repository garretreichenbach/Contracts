package dovtech.contracts.gui.contracts.newcontract;

import api.common.GameClient;
import api.common.GameServer;
import api.element.inventory.ItemStack;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.utils.gui.SimplePopup;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.MiningTarget;
import dovtech.contracts.contracts.target.PlayerTarget;
import dovtech.contracts.contracts.target.ProductionTarget;
import dovtech.contracts.gui.contracts.ContractsScrollableList;
import dovtech.contracts.util.DataUtil;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.GLFW;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
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
                StarPlayer currentPlayer = new StarPlayer(GameClient.getClientPlayerState());
                if (guiElement.getUserPointer().equals("OK")) {
                    if (panel.getReward() <= 0) {
                        (new SimplePopup(getState(), "Cannot Add Bounty", "The reward must be above 0!")).activate();
                    } else if (currentPlayer.getCredits() < panel.getReward()) {
                        (new SimplePopup(getState(), "Cannot Add Bounty", "You do not have enough credits!")).activate();
                    } else {
                        if(contractMode == 1) {
                            String name = panel.getName();
                            int bountyAmount = panel.getReward();
                            if (DataUtil.players.containsKey(name)) {
                                if (currentPlayer.getName().equals(name)) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on yourself!")).activate();
                                } else if (currentPlayer.getFaction().getID() == DataUtil.players.get(name).getFactionID()) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of your own faction!")).activate();
                                } else if (GameServer.getServerState().getFactionManager().getFaction(currentPlayer.getPlayerState().getFactionId()).getFriends().contains(GameServer.getServerState().getFactionManager().getFaction(DataUtil.players.get(name).getFactionID()))) {
                                    (new SimplePopup(getState(), "Cannot Add Bounty", "You can't put a bounty on a member of an allied faction!")).activate();
                                } else {

                                    PlayerTarget target = new PlayerTarget();
                                    target.setTarget(DataUtil.players.get(name));
                                    Contract contract = new Contract(currentPlayer.getFaction(), "Kill" + name, Contract.ContractType.BOUNTY, bountyAmount, UUID.randomUUID().toString(), target);
                                    DataUtil.contracts.put(contract.getUid(), contract);
                                    DataUtil.contractWriteBuffer.add(contract);
                                    currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                    if (ContractsScrollableList.getInst() != null) {
                                        ContractsScrollableList.getInst().clear();
                                        ContractsScrollableList.getInst().handleDirty();
                                    }
                                    deactivate();
                                }
                                //Todo: Add checks/modifiers for non-aggression pacts, relations, trade deals, etc if BetterFactions mod is installed
                            }
                        } else if(contractMode == 2) {
                            MiningTarget target = new MiningTarget();
                            int count = panel.getCount();
                            if(count <= 0) {
                                (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                            } else {
                                ItemStack itemStack = new ItemStack(panel.getSelectedBlockType().getId());
                                itemStack.setAmount(count);
                                target.setTarget(itemStack);
                                Contract contract = new Contract(currentPlayer.getFaction(), "Mine x" + count + " " + itemStack.getName(), Contract.ContractType.MINING, panel.getReward(), UUID.randomUUID().toString(), target);
                                DataUtil.contracts.put(contract.getUid(), contract);
                                DataUtil.contractWriteBuffer.add(contract);
                                currentPlayer.setCredits(currentPlayer.getCredits() - contract.getReward());
                                if (ContractsScrollableList.getInst() != null) {
                                    ContractsScrollableList.getInst().clear();
                                    ContractsScrollableList.getInst().handleDirty();
                                }
                                deactivate();
                            }
                        } else if(contractMode == 3) {
                            ProductionTarget target = new ProductionTarget();
                            int count = panel.getCount();
                            if(count <= 0) {
                                (new SimplePopup(getState(), "Cannot Add Contract", "The amount must be above 0!")).activate();
                            } else {
                                ItemStack itemStack = new ItemStack(panel.getSelectedBlockType().getId());
                                itemStack.setAmount(count);
                                target.setTarget(itemStack);
                                Contract contract = new Contract(currentPlayer.getFaction(), "Produce x" + count + " " + itemStack.getName(), Contract.ContractType.PRODUCTION, panel.getReward(), UUID.randomUUID().toString(), target);
                                DataUtil.contracts.put(contract.getUid(), contract);
                                DataUtil.contractWriteBuffer.add(contract);
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
                } else if(guiElement.getUserPointer().equals("BOUNTY")) {
                    contractMode = 1;
                    panel.drawBountyPanel();
                } else if(guiElement.getUserPointer().equals("MINING")) {
                    contractMode = 2;
                    panel.drawMiningPanel();
                } else if(guiElement.getUserPointer().equals("PRODUCTION")) {
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

    public StarFaction getContractor() {
        return panel.getContractor();
    }
}
