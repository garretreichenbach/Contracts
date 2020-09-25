package dovtech.contracts.contracts.target;

import api.element.inventory.ItemStack;
import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;

public class ProductionTarget implements ContractTarget {

    private short id;
    private int count;

    @Override
    public int getAmount() {
        return count;
    }

    @Override
    public ItemStack getTarget() {
        ItemStack itemStack = new ItemStack(id);
        itemStack.setAmount(count);
        return itemStack;
    }

    @Override
    public void setTarget(Object obj) {
        ItemStack itemStack = (ItemStack) obj;
        id = itemStack.getId();
        count = itemStack.getAmount();
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.PRODUCTION;
    }

    @Override
    public StarSector getLocation() {
        return null;
    }

    @Override
    public void setLocation(StarSector sector) {

    }
}
