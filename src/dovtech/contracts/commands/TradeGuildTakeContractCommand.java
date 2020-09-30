package dovtech.contracts.commands;

import api.utils.game.PlayerUtils;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtil;
import org.schema.game.common.data.player.PlayerState;

public class TradeGuildTakeContractCommand extends ChatCommand {

    public TradeGuildTakeContractCommand() {
        super("takecontract", "/takecontract <enableBounties>", "Makes the trading guild try to fulfill the first contract in the list favoring ones with the least amount of claims. However, they will avoid picking bounties unless enableBounties is true.", true);
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(args.length <= 1) {
            if(DataUtil.contracts.size() > 0) {
                if(args[0].equals("true")) {
                    Contract leastClaims = DataUtil.contracts.values().iterator().next();
                    for(Contract contract : DataUtil.contracts.values()) {
                        if (contract.getClaimants().size() < leastClaims.getClaimants().size()) {
                            leastClaims = contract;
                        }
                    }
                    takeContract(leastClaims);
                } else {
                    Contract leastClaims = DataUtil.contracts.values().iterator().next();
                    for(Contract contract : DataUtil.contracts.values()) {
                        if (!contract.getContractType().equals(Contract.ContractType.BOUNTY) && contract.getClaimants().size() < leastClaims.getClaimants().size()) {
                            leastClaims = contract;
                        }
                    }
                    takeContract(leastClaims);
                }
                return true;
            } else {
                PlayerUtils.sendMessage(sender, "[ERROR]: There are no contracts available right now!");
                return true;
            }
        } else {
            return false;
        }
    }

    private void takeContract(Contract contract) {
        //Todo
    }
}
