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
import thederpgamer.contracts.data.player.PlayerData;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * ContractsCommand
 * Contracts base command.
 *
 * @since 09/25/2020
 * @author TheDerpGamer
 */
public class ContractsCommand implements CommandInterface {

    @Override
    public String getCommand() {
        return "contracts";
    }

    @Override
    public String[] getAliases() {
        return new String[] {
                "contract"
        };
    }

    @Override
    public String getDescription() {
        return "Base command for the Contracts mod.\n" +
                "%COMMAND% random [amount] : Creates a randomly generated contract and adds it to the contracts list. If an amount is specified, generates multiple random contracts.\n" +
                "%COMMAND% purge <amount|all/*> [filter...] : Purges the specified amount of contracts from the list. If a filter is specified, only matching contracts will be purged.\n" +
                "%COMMAND% complete <contract id|all/*> [player] : Completes the specified contract based off it's id for the sender, or a specific player if listed.\n" +
                "%COMMAND% list [player] [filter...] : Lists the active contracts for the sender (or a player, if specified) as well as their ids, rewards, and progress. Can be filtered to only show specific types.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            switch(subCommand) {
                case "random":
                    if(args.length == 2) {
                        if(NumberUtils.isNumber(args[1].trim()) && Integer.parseInt(args[1].trim()) > 0) generateRandomContract(sender, Integer.parseInt(args[1].trim()));
                        else return false;
                    } else generateRandomContract(sender, 1);
                    break;
                case "purge":
                    if(args.length >= 2) {
                        String[] purgeFilter = (args.length > 2) ? Arrays.copyOfRange(args, 2, args.length - 1) : new String[0];
                        if(args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("*")) purgeContracts(sender, ServerDatabase.getAllContracts().size(), purgeFilter);
                        else if(NumberUtils.isNumber(args[1])) purgeContracts(sender, Integer.parseInt(args[1]), purgeFilter);
                        else return false;
                    } else return false;
                    break;
                case "complete":
                    if(args.length >= 2) {
                        PlayerData target = null;
                        if(args.length > 2) {
                            if(ServerDatabase.getPlayerData(args[2]) != null) target = ServerDatabase.getPlayerData(args[2]);
                            else PlayerUtils.sendMessage(sender, "Player " + args[2] + " doesn't exist!");
                        } else target = ServerDatabase.getPlayerData(sender);
                        if(args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("*")) {
                            completeContracts(sender, target, ServerDatabase.getPlayerContracts(target).toArray(new Contract[0]));
                        } else {
                            if(NumberUtils.isNumber(args[1].trim())) {
                                Contract contract = ServerDatabase.getContractFromId(Integer.parseInt(args[1].trim()));
                                if(contract != null) completeContracts(sender, target, contract);
                                else PlayerUtils.sendMessage(sender, "No valid contracts found with id " + args[1].trim());
                            } else PlayerUtils.sendMessage(sender, "No valid contracts found with id " + args[1].trim());
                        }
                    } else return false;
                    break;
                case "list":
                    if(args.length == 1) listContracts(sender, ServerDatabase.getPlayerData(sender));
                    else {
                        PlayerData target = ServerDatabase.getPlayerData(args[1]);
                        if(target == null) PlayerUtils.sendMessage(sender, "Player " + args[1] + " doesn't exist!");
                        else {
                            if(args.length > 2) listContracts(sender, target, Arrays.copyOfRange(args, 2, args.length - 1));
                            else listContracts(sender, target);
                        }
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return Contracts.getInstance();
    }

    private void generateRandomContract(PlayerState sender, int amount) {
        int i;
        for(i = 0; i < amount; i ++) ServerDatabase.generateRandomContract();
        PlayerUtils.sendMessage(sender, "Generated " + (i + 1) + " random contracts.");
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
        } else {
            for(i = 0; i < amount && i < ServerDatabase.getAllContracts().size(); i ++) {
                toRemove.add(ServerDatabase.getAllContracts().get(i));
            }
        }

        for(Contract contract : toRemove) ServerDatabase.removeContract(contract);
        PersistentObjectUtil.save(Contracts.getInstance().getSkeleton());
        PlayerUtils.sendMessage(sender, "Removed " + (i + 1) + " contracts from database.");
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

    private void listContracts(PlayerState sender, PlayerData target, String... filter) {
        if(target.contracts != null && target.contracts.size() > 0) {
            ArrayList<String> contractList= new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            builder.append("Active contracts for ").append(sender.getName()).append(":\n");
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
                for(Contract contract : ServerDatabase.getPlayerContracts(target)) {
                    if(types.contains(contract.getContractType())) {
                        contractList.add(contract.getName().trim() + "[" + contract.getId() + "] - $" + contract.getReward() + " | " + contract.getContractorName().trim());
                    }
                }
            } else {
                for(Contract contract : ServerDatabase.getPlayerContracts(target)) {
                    contractList.add(contract.getName().trim() + "[" + contract.getId() + "] - $" + contract.getReward() + " | " + contract.getContractorName().trim());
                }
            }

            for(int i = 0; i < contractList.size(); i ++) {
                builder.append(contractList.get(i));
                if(i < contractList.size() - 1) builder.append("\n");
            }
            PlayerUtils.sendMessage(sender, builder.toString().trim());
        } else PlayerUtils.sendMessage(sender, "No active contracts found for player " + target.name + ".");
    }
}