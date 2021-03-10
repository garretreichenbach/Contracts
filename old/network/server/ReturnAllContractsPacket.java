/**
 * Packet [Server -> Client]
 */
package thederpgamer.contracts.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.inventory.ItemStack;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.contracts.Contract;
import thederpgamer.contracts.contracts.target.*;
import thederpgamer.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.PlayerNotFountException;
import java.io.IOException;
import java.util.ArrayList;

public class ReturnAllContractsPacket extends Packet {

    private ArrayList<String> contractNames = new ArrayList<>();
    private ArrayList<String> contractorIDs = new ArrayList<>();
    private ArrayList<String> contractTypes = new ArrayList<>();
    private ArrayList<String> contractRewards = new ArrayList<>();
    private ArrayList<String> contractLocations = new ArrayList<>();
    private ArrayList<String> contractTargets = new ArrayList<>();
    private ArrayList<String> contractUIDs = new ArrayList<>();
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public ReturnAllContractsPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.CLIENT)) {
            contractNames = packetReadBuffer.readStringList();
            contractorIDs = packetReadBuffer.readStringList();
            contractTypes = packetReadBuffer.readStringList();
            contractRewards = packetReadBuffer.readStringList();
            contractLocations = packetReadBuffer.readStringList();
            contractTargets = packetReadBuffer.readStringList();
            contractUIDs = packetReadBuffer.readStringList();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            packetWriteBuffer.writeStringList(contractNames);
            packetWriteBuffer.writeStringList(contractorIDs);
            packetWriteBuffer.writeStringList(contractTypes);
            packetWriteBuffer.writeStringList(contractRewards);
            packetWriteBuffer.writeStringList(contractLocations);
            packetWriteBuffer.writeStringList(contractTargets);
            packetWriteBuffer.writeStringList(contractUIDs);
        }
    }

    @Override
    public void processPacketOnClient() {
        ArrayList<Contract> contracts = new ArrayList<>();
        int size = contractUIDs.size();
        for(int i = 0; i < size; i ++) {
            ContractTarget contractTarget = null;
            if(Contract.ContractType.valueOf(contractTypes.get(i)).equals(Contract.ContractType.BOUNTY)) {
                contractTarget = new PlayerTarget();
                contractTarget.setTargets(contractTargets.get(i));
            } else if(Contract.ContractType.valueOf(contractTypes.get(i)).equals(Contract.ContractType.CARGO_ESCORT)) {
                contractTarget = new CargoTarget();
                contractTarget.setTargetsFromString(contractTargets.get(i));
                contractTarget.setLocationFromString(contractLocations.get(i));
            } else if(Contract.ContractType.valueOf(contractTypes.get(i)).equals(Contract.ContractType.MINING)) {
                contractTarget = new MiningTarget();
                contractTarget.setTargetsFromString(contractTargets.get(i));
            } else if(Contract.ContractType.valueOf(contractTypes.get(i)).equals(Contract.ContractType.PRODUCTION)) {
                contractTarget = new ProductionTarget();
                contractTarget.setTargetsFromString(contractTargets.get(i));
            }

            Contract contract = new Contract(Integer.parseInt(contractorIDs.get(i)), contractNames.get(i), Contract.ContractType.valueOf(contractTypes.get(i)), Integer.parseInt(contractRewards.get(i)), contractUIDs.get(i), contractTarget);
            contracts.add(contract);
        }
        DataUtils.localContracts = contracts;
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        try {
            for (Contract contract : DataUtils.getAllContracts()) {
                contractNames.add(contract.getName());
                contractorIDs.add(String.valueOf(contract.getContractor().getID()));
                contractTypes.add(contract.getContractType().toString());
                contractRewards.add(String.valueOf(contract.getReward()));
                if (contract.getContractType().equals(Contract.ContractType.CARGO_ESCORT)) {
                    contractLocations.add(contract.getTarget().getLocation()[0] + "," + contract.getTarget().getLocation()[1] + "," + contract.getTarget().getLocation()[2]);
                    for (Object object : contract.getTarget().getTargets()) {
                        ItemStack itemStack = (ItemStack) object;
                        contractTargets.add(itemStack.getId() + "," + itemStack.getAmount() + ";");
                    }
                } else {
                    contractLocations.add("null");
                }
                contractUIDs.add(contract.getUID());
            }
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }
}
