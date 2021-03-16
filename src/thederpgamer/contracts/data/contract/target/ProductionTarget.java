package thederpgamer.contracts.data.contract.target;

import thederpgamer.contracts.data.contract.Contract;
import thederpgamer.contracts.data.inventory.ItemStack;
import java.io.Serializable;

public class ProductionTarget implements ContractTarget, Serializable {

    private ItemStack[] targets;

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
        return Contract.ContractType.PRODUCTION;
    }
}