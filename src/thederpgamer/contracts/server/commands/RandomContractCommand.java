package thederpgamer.contracts.server.commands;

import api.utils.game.chat.ChatCommand;
import thederpgamer.contracts.Contracts;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.contracts.data.ServerDatabase;

public class RandomContractCommand extends ChatCommand {

    public RandomContractCommand() {
        super("random_contract", "/random_contract", "Generates a random contract and adds it to the list. Does not generate Cargo or Bounty targets", true, Contracts.getInstance());
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        ServerDatabase.generateRandomContract();
        ServerDatabase.updateContractGUI();
        return true;
    }
}