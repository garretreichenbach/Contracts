package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.network.server.ReturnClientContractsPacket;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;
import java.util.ArrayList;

public class GetClientContractsPacket extends Packet {

    private ReturnClientContractsPacket returnClientContractsPacket;

    public GetClientContractsPacket() {

    }

    public ArrayList<Contract> getPlayerContracts() {
        return returnClientContractsPacket.getPlayerContracts();
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {

    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        returnClientContractsPacket = new ReturnClientContractsPacket(DataUtils.getPlayerContracts(playerState.getName()));
        PacketUtil.sendPacket(playerState, returnClientContractsPacket);
    }
}
