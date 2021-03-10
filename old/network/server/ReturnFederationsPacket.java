/**
 * Packet [Server -> Client]
 */

package thederpgamer.contracts.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.federation.Federation;
import thederpgamer.contracts.federation.FederationGovernmentType;
import thederpgamer.contracts.util.FederationUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;
import java.util.ArrayList;

public class ReturnFederationsPacket extends Packet {

    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    private ArrayList<Integer> federationIDs;
    private ArrayList<String> federationNames;
    private ArrayList<Integer> govTypeIDs;
    private ArrayList<String> descriptions;
    private ArrayList<Integer> powers;


    public ReturnFederationsPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if (gameState.equals(Contracts.Mode.CLIENT)) {
            federationIDs = packetReadBuffer.readIntList();
            federationNames = packetReadBuffer.readStringList();
            govTypeIDs = packetReadBuffer.readIntList();
            descriptions = packetReadBuffer.readStringList();
            powers = packetReadBuffer.readIntList();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if (gameState.equals(Contracts.Mode.SERVER) || gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            packetWriteBuffer.writeIntList(federationIDs);
            packetWriteBuffer.writeStringList(federationNames);
            packetWriteBuffer.writeIntList(govTypeIDs);
            packetWriteBuffer.writeStringList(descriptions);
            packetWriteBuffer.writeIntList(powers);
        }
    }

    @Override
    public void processPacketOnClient() {
        ArrayList<Federation> federations = new ArrayList<>();
        for(int i = 0; i < federationIDs.size(); i ++) {
            int id = federationIDs.get(i);
            String name = federationNames.get(i);
            FederationGovernmentType govType = FederationGovernmentType.values()[govTypeIDs.get(i)];
            String description = descriptions.get(i);
            int power = powers.get(i);
            federations.add(new Federation(id, name, govType, description, power));
        }
        FederationUtils.clientFederationList = federations;
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ArrayList<Federation> federations = FederationUtils.getAllFederations();
        federationIDs = new ArrayList<>();
        federationNames = new ArrayList<>();
        govTypeIDs = new ArrayList<>();
        descriptions = new ArrayList<>();
        powers = new ArrayList<>();
        assert federations != null;
        for(Federation federation : federations) {
            federationIDs.add(federation.getFederationID());
            federationNames.add(federation.getName());
            govTypeIDs.add(federation.getGovernmentType().ordinal());
            descriptions.add(federation.getDescription());
            powers.add(federation.getPower());
        }
    }
}
