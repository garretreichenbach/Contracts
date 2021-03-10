package thederpgamer.contracts.data.contract.target;
import org.schema.common.util.linAlg.Vector3i;
import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.inventory.ItemStack;
import java.io.Serializable;

public class MiningTarget implements ContractTarget, Serializable {

    private ItemStack[] targets;
    private Vector3i sector;

    @Override
    public ItemStack[] getTargets() {
        return targets;
    }

    @Override
    public void setTargets(Object... obj) {
        targets = (ItemStack[]) obj;
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.MINING;
    }

    @Override
    public Vector3i getSector() {
        return sector;
    }

    @Override
    public void setSector(Vector3i sector) {
        this.sector = sector;
    }
}