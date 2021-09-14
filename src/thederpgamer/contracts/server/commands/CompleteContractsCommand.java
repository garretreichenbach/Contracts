package thederpgamer.contracts.server.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.apache.commons.lang3.math.NumberUtils;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.player.PlayerData;

import javax.annotation.Nullable;

/**
 * <Description>
 *
 * @author TheDerpGamer
 */
public class CompleteContractsCommand implements CommandInterface {

    @Override
    public String getCommand() {
        return "contracts_complete";
    }

    @Override
    public String[] getAliases() {
        return new String[] {
                "contract_complete",
                "complete_contracts",
                "complete_contract"
        };
    }

    @Override
    public String getDescription() {
        return "Sets the specified contract as completed.\n" +
               "- /%COMMAND% <contract id|all/*> [player] : Completes the specified contract based off it's id for the sender, or a specific player if listed.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(args.length >= 1) {
            PlayerData target = null;
            if(args.length > 1) {
                if(ServerDatabase.getPlayerData(args[1]) != null) target = ServerDatabase.getPlayerData(args[1]);
                else PlayerUtils.sendMessage(sender, "Player " + args[1] + " doesn't exist!");
            } else target = ServerDatabase.getPlayerData(sender);
            if(args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("*")) {
                completeContracts(sender, target, ServerDatabase.getPlayerContracts(target).toArray(new Contract[0]));
            } else {
                if(NumberUtils.isNumber(args[0].trim())) {
                    Contract contract = ServerDatabase.getContractFromId(Integer.parseInt(args[0].trim()));
                    if(contract != null) completeContracts(sender, target, contract);
                    else PlayerUtils.sendMessage(sender, "No valid contracts found with id " + args[0].trim());
                } else PlayerUtils.sendMessage(sender, "No valid contracts found with id " + args[0].trim());
            }
        } else return false;
        return true;
    }

    @Override
    public void serverAction(@Nullable PlayerState sender, String[] args) {

    }

    @Override
    public StarMod getMod() {
        return Contracts.getInstance();
    }

    private void completeContracts(PlayerState sender, PlayerData target, Contract... contracts) {
        if(contracts != null && contracts.length >= 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Completed the following contracts for player ").append(target.name).append(":\n");
            for(int i = 0; i < contracts.length; i ++) {
                Contract contract = contracts[i];
                if(!contract.getClaimants().contains(target)) PlayerUtils.sendMessage(sender, "Player " + target.name + " doesn't have any active contracts matching id " + contract.getId() + ".");
                else {
                    builder.append(contract.getId());
                    if(i < contracts.length - 1) builder.append(", ");
                    ServerDatabase.completeContract(contract, target);
                }
            }
            PlayerUtils.sendMessage(sender, builder.toString().trim());
        } else PlayerUtils.sendMessage(sender, "No active contracts found for player" + target.name + ".");
    }
}