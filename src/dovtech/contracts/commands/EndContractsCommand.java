package dovtech.contracts.commands;

import api.entity.StarPlayer;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtil;
import org.schema.game.common.data.player.PlayerState;

public class EndContractsCommand extends ChatCommand {

    public EndContractsCommand() {
        super("endContracts", "/endContracts [claimRewards]", "Cancels all contracts the user has taken. Can also claim their rewards instead if specified.", true);
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        try {
            boolean claim = Boolean.parseBoolean(args[0]);
            PlayerData playerData = DataUtil.players.get(sender.getName());
            for (Contract contract : playerData.getContracts()) {
                if (claim) {
                    sender.setCredits(sender.getCredits() + contract.getReward());
                    DataUtil.removeContract(contract, false, new StarPlayer(sender));
                } else {
                    DataUtil.removeContract(contract, true);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
