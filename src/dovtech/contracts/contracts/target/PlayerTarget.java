package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.util.DataUtils;

import java.io.Serializable;

public class PlayerTarget implements ContractTarget, Serializable {

    private String target;

    @Override
    public String[] getTargets() {
        return new String[] {target};
    }

    @Override
    public void setTargets(Object... obj) {
        String playerName = (String) obj[0];
        if(playerName != null) {
            this.target = playerName;
        } else {
            this.target = "NULL";
        }
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.BOUNTY;
    }

    @Override
    public int[] getLocation() {
        return null;
    }

    @Override
    public void setLocation(StarSector sector) {

    }

    @Override
    public void setTargetsFromString(String s) {

    }

    @Override
    public void setLocationFromString(String s) {

    }
}
