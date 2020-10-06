package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.network.server.ReturnFactionDataListPacket;
import org.schema.game.common.data.player.PlayerState;

public class GetFactionDataListPacket extends Packet {

    public GetFactionDataListPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) {

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) {

    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ReturnFactionDataListPacket returnFactionDataListPacket = new ReturnFactionDataListPacket();
        PacketUtil.sendPacket(playerState, returnFactionDataListPacket);
    }
}
