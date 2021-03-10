package thederpgamer.contracts.data.contract.target;

import org.schema.common.util.linAlg.Vector3i;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.player.PlayerData;
import java.io.Serializable;

public class PlayerTarget implements ContractTarget, Serializable {

    private PlayerData target;

    @Override
    public PlayerData[] getTargets() {
        return new PlayerData[] {target};
    }

    @Override
    public void setTargets(Object... obj) {
        target = (PlayerData) obj[0];
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.BOUNTY;
    }

    @Override
    public Vector3i getSector() {
        return null;
    }

    @Override
    public void setSector(Vector3i sector) {

    }
}
