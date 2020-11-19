package dovtech.contracts.commands;

import api.utils.game.chat.ChatCommand;
import dovtech.contracts.Contracts;
import dovtech.contracts.util.ContractUtils;
import org.schema.game.common.data.player.PlayerState;

public class RandomContractCommand extends ChatCommand {

    public RandomContractCommand() {
        super("random_contract", "/random_contract", "Generates a random contract and adds it to the list. Does not generate Cargo or Bounty targets", true, Contracts.getInstance());
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        ContractUtils.generateRandomContract();
        return true;
    }
}
