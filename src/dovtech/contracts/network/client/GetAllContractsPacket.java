package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.network.server.ReturnAllContractsPacket;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;
import java.util.ArrayList;

public class GetAllContractsPacket extends Packet {

    private ReturnAllContractsPacket returnAllContractsPacket;

    public GetAllContractsPacket() {

    }

    public ArrayList<Contract> getContracts() {
        return returnAllContractsPacket.getContracts();
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
        returnAllContractsPacket = new ReturnAllContractsPacket(DataUtils.getAllContracts());
        PacketUtil.sendPacket(playerState, returnAllContractsPacket);
    }
}
