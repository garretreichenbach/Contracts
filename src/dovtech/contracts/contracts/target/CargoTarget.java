package dovtech.contracts.contracts.target;

import api.element.inventory.ItemStack;
import api.universe.StarSector;
import api.universe.StarUniverse;
import dovtech.contracts.contracts.Contract;

public class CargoTarget implements ContractTarget {

    private short target;
    private int amount;
    private int[] location;

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
        return Contract.ContractType.CARGO_ESCORT;
    }

    @Override
    public StarSector getLocation() {
        if(location != null) {
            return StarUniverse.getUniverse().getSector(location[0], location[1], location[2]);
        } else {
            return null;
        }
    }

    @Override
    public void setLocation(StarSector sector) {
        location = new int[] {sector.getCoordinates().x, sector.getCoordinates().y, sector.getCoordinates().z};
    }
}
