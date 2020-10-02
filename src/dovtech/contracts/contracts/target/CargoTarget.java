package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import api.utils.game.inventory.ItemStack;
import dovtech.contracts.contracts.Contract;
import java.io.Serializable;

public class CargoTarget implements ContractTarget, Serializable {

    private short[] targets;
    private int[] amounts;
    private int[] location;

    @Override
    public ItemStack[] getTargets() {
        ItemStack[] itemStacks = new ItemStack[targets.length];
        for(int i = 0; i < itemStacks.length; i ++) {
            ItemStack itemStack = new ItemStack(targets[i]);
            itemStack.setAmount(amounts[i]);
            itemStacks[i] = itemStack;
        }
        return itemStacks;
    }

    @Override
    public void setTargets(Object... obj) {
        ItemStack[] itemStack = new ItemStack[obj.length];
        for(int j = 0; j < obj.length; j ++) {
            itemStack[j] = (ItemStack) obj[j];
        }
        this.targets = new short[itemStack.length];
        this.amounts = new int[itemStack.length];
        for(int i = 0; i < targets.length; i ++) {
            this.targets[i] = itemStack[i].getId();
            this.amounts[i] = itemStack[i].getAmount();
        }
    }

    @Override
    public void setTargetsFromString(String s) {
        String[] items = s.split(";");
        this.targets = new short[items.length];
        this.amounts = new int[items.length];
        for(int i = 0; i < items.length; i ++) {
            String[] item = items[i].split(",");
            targets[i] = Short.parseShort(item[0]);
            amounts[i] = Integer.parseInt(item[1]);
        }
    }

    @Override
    public void setLocationFromString(String s) {
        String[] coords = s.split(",");
        location = new int[] {Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])};
    }

    @Override
    public Contract.ContractType getContractType() {
        return Contract.ContractType.CARGO_ESCORT;
    }

    @Override
    public int[] getLocation() {
        return location;
    }

    @Override
    public void setLocation(StarSector sector) {
        location = new int[] {sector.getCoordinates().x, sector.getCoordinates().y, sector.getCoordinates().z};
    }
}
