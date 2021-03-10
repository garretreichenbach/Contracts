package thederpgamer.contracts.commands;

import api.utils.game.chat.ChatCommand;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.contracts.Contract;
import thederpgamer.contracts.player.PlayerData;
import thederpgamer.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;

public class EndContractsCommand extends ChatCommand {

    public EndContractsCommand() {
        super("end_contracts", "/end_contracts [claim_rewards]", "Cancels all contracts the user has taken. Can also claim their rewards instead if specified.", true, Contracts.getInstance());
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        try {
            boolean claim = Boolean.parseBoolean(args[0]);
            for (Contract contract : DataUtils.getPlayerContracts(sender.getName())) {
                if (claim) {
                    sender.setCredits(sender.getCredits() + contract.getReward());
                    DataUtils.removeContract(contract, false, new StarPlayer(sender));
                } else {
                    DataUtils.removeContract(contract, true);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
