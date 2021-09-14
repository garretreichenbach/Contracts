package thederpgamer.contracts.server.commands;

import api.mod.StarMod;
import api.mod.config.PersistentObjectUtil;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.apache.commons.lang3.math.NumberUtils;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.Contract;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <Description>
 *
 * @author TheDerpGamer
 */
public class PurgeContractsCommand implements CommandInterface  {

    @Override
    public String getCommand() {
        return "contracts_purge";
    }

    @Override
    public String[] getAliases() {
        return new String[] {
                "contract_purge",
                "purge_contracts",
                "purge_contract"
        };
    }

    @Override
    public String getDescription() {
        return "Purges multiple contracts from the list matching a type filter if specified.\n" +
               "- /%COMMAND% <amount|all/*> [filter...] : Purges the specified amount of contracts from the list. If a type filter is specified, only contracts of the matching type will be purged.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(args.length >= 1) {
            String[] purgeFilter = (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length - 1) : new String[0];
            if(args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("*")) purgeContracts(sender, ServerDatabase.getAllContracts().size(), purgeFilter);
            else if(NumberUtils.isNumber(args[0])) purgeContracts(sender, Integer.parseInt(args[0]), purgeFilter);
            else return false;
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

    private void purgeContracts(PlayerState sender, int amount, String... filter) {
        int i;
        ArrayList<Contract> toRemove = new ArrayList<>();
        if(filter != null && filter.length > 0) {
            ArrayList<Contract.ContractType> types = new ArrayList<>();
            for(String s : filter) {
                Contract.ContractType type = Contract.ContractType.fromString(s);
                if(type != null) types.add(type);
                else {
                    PlayerUtils.sendMessage(sender, s + " is not a valid contract type.");
                    return;
                }
            }
            for(i = 0; i < amount && i < ServerDatabase.getAllContracts().size(); i ++) {
                Contract contract = ServerDatabase.getAllContracts().get(i);
                if(types.contains(contract.getContractType())) toRemove.add(contract);
            }
        } else for(i = 0; i < amount && i < ServerDatabase.getAllContracts().size(); i ++) toRemove.add(ServerDatabase.getAllContracts().get(i));

        for(Contract contract : toRemove) ServerDatabase.removeContract(contract);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        PlayerUtils.sendMessage(sender, "Removed " + (i + 1) + " contracts from database.");
    }
}
