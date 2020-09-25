package dovtech.contracts.contracts.target;

import api.common.GameServer;
import api.entity.StarPlayer;
import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;
import org.schema.game.server.data.PlayerNotFountException;

public class PlayerTarget implements ContractTarget {

    private String target;

    @Override
    public int getAmount() {
        return 1;
    }

    @Override
    public StarPlayer getTarget() {
        try {
            return new StarPlayer(GameServer.getServerState().getPlayerFromName(target));
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setTarget(Object obj) {
        StarPlayer player = (StarPlayer) obj;
        this.target = player.getName();
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.BOUNTY;
    }

    @Override
    public StarSector getLocation() {
        return null;
    }

    @Override
    public void setLocation(StarSector sector) {

    }
}
