package dovtech.contracts.commands;

import api.entity.StarPlayer;
import api.utils.game.chat.ChatCommand;
import com.ctc.wstx.util.DataUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
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
