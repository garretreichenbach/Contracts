package dovtech.contracts.contracts.target;

import api.element.inventory.ItemStack;
import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;

public class MiningTarget implements ContractTarget {

    private short target;
    private int amount;

    @Override
    public int getAmount() {
        return -1;
    }

    @Override
    public ItemStack getTarget() {
        ItemStack itemStack = new ItemStack(target);
        itemStack.setAmount(amount);
        return itemStack;
    }

    @Override
    public void setTarget(Object obj) {
        ItemStack itemStack = (ItemStack) obj;
        this.target = itemStack.getId();
        this.amount = itemStack.getAmount();
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.MINING;
    }

    @Override
    public StarSector getLocation() {
        return null;
    }

    @Override
    public void setLocation(StarSector sector) {
    }
}
