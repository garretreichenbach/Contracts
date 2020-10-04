/**
 * Packet [Client -> Server]
 */
package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import dovtech.contracts.Contracts;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.PlayerNotFountException;
import java.io.IOException;

public class RemoveContractPacket extends Packet {

    private String contractUID;
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public RemoveContractPacket() {

    }

    public RemoveContractPacket(Contract contract) {
        this.contractUID = contract.getUID();
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            contractUID = packetReadBuffer.readString();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.CLIENT)) {
            packetWriteBuffer.writeString(contractUID);
        }
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        try {
            DataUtils.cancelContract(contractUID);
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }
}
