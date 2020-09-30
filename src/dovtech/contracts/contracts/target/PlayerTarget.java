package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtil;

import java.io.Serializable;

public class PlayerTarget implements ContractTarget, Serializable {

    private String target;

    @Override
    public int getAmount() {
        return 1;
    }

    @Override
    public PlayerData getTarget() {
        return DataUtil.players.get(target);
    }

    @Override
    public void setTarget(Object obj) {
        PlayerData playerData = (PlayerData) obj;
        if(playerData != null) {
            this.target = playerData.getName();
        } else {
            this.target = "Nobody";
        }
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
