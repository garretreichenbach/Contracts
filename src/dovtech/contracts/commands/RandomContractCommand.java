package dovtech.contracts.commands;

import api.common.GameClient;
import api.common.GameServer;
import api.element.block.Blocks;
import api.element.inventory.ItemStack;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.universe.StarUniverse;
import api.utils.game.chat.ChatCommand;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.contracts.target.*;
import dovtech.contracts.util.DataUtil;
import org.schema.game.common.data.player.PlayerState;
import java.util.Random;
import java.util.UUID;

public class RandomContractCommand extends ChatCommand {

    public RandomContractCommand() {
        super("debug_randomcontract", "/debug_randomcontract", "Generates a random contract and writes it to file.", true);
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        Random random = new Random();
        Contract.ContractType randomContractType = Contract.ContractType.BOUNTY;
        int randomTypeInt = random.nextInt(5 - 1) + 1;
        switch (randomTypeInt) {
            case 1:
                randomContractType = Contract.ContractType.BOUNTY;
                break;
            case 2:
                randomContractType = Contract.ContractType.MINING;
                break;
            case 3:
                randomContractType = Contract.ContractType.PRODUCTION;
                break;
            case 4:
                randomContractType = Contract.ContractType.CARGO_ESCORT;
                break;
        }
        int randomReward = (random.nextInt(1000 - 100) + 100) * 1000;

        ContractTarget contractTarget = new PlayerTarget();
        contractTarget.setTarget(new StarPlayer(GameClient.getClientPlayerState()));
        String contractName = "";
        switch (randomContractType) {
            case MINING:
                contractTarget = new MiningTarget();
                ItemStack miningTargetStack = new ItemStack(Blocks.THRENS_ORE_RAW.getId());
                int randomAmount = (random.nextInt(300 - 1) + 1) * 10;
                miningTargetStack.setAmount(randomAmount);
                contractTarget.setTarget(miningTargetStack);
                contractName = "Mine x" + miningTargetStack.getAmount() + " " + miningTargetStack.getName();
                break;
            case BOUNTY:
                contractTarget = new PlayerTarget();
                contractTarget.setTarget(new StarPlayer(sender));
                contractName = "Kill " + sender.getName();
                break;
            case PRODUCTION:
                contractTarget = new ProductionTarget();
                ItemStack productionTargetStatck = new ItemStack(Blocks.REACTOR_POWER.getId());
                int randomProductionAmount = (random.nextInt(300 - 1) + 1) * 10;
                productionTargetStatck.setAmount(randomProductionAmount);
                contractTarget.setTarget(productionTargetStatck);
                contractName = "Produce x" + randomProductionAmount + " " + productionTargetStatck.getName();
                break;
            case CARGO_ESCORT:
                contractTarget = new CargoTarget();
                ItemStack cargoTargetStack = new ItemStack(Blocks.REACTOR_POWER.getId());
                int randomCargoAmount = (random.nextInt(300 - 1) + 1) * 10;
                cargoTargetStack.setAmount(randomCargoAmount);
                contractTarget.setTarget(cargoTargetStack);
                contractTarget.setLocation(StarUniverse.getUniverse().getSector(2, 2, 2));
                contractName = "Deliver cargo to (" + contractTarget.getLocation().getCoordinates().x + "." + contractTarget.getLocation().getCoordinates().y + "." + contractTarget.getLocation().getCoordinates().z + ")";
                break;
        }

        Contract randomContract = new Contract(new StarFaction(GameServer.getServerState().getFactionManager().getFaction(-2)), contractName, randomContractType, randomReward, UUID.randomUUID().toString(), contractTarget);
        DataUtil.contractWriteBuffer.add(randomContract);
        DataUtil.writeData();
        return true;
    }
}
