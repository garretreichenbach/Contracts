package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.network.server.ReturnPlayerDataPacket;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;

public class GetPlayerDataPacket extends Packet {

    private ReturnPlayerDataPacket returnPlayerDataPacket;

    public GetPlayerDataPacket() {

    }

    public PlayerData getPlayerData() {
        return returnPlayerDataPacket.getPlayerData();
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
        returnPlayerDataPacket = new ReturnPlayerDataPacket(DataUtils.getPlayerData(playerState.getName()));
        PacketUtil.sendPacket(playerState, returnPlayerDataPacket);
    }
}
