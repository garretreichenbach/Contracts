package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import api.utils.game.inventory.ItemStack;
import dovtech.contracts.contracts.Contract;
import java.io.Serializable;

public class MiningTarget implements ContractTarget, Serializable {

    private short[] targets;
    private int[] amounts;

    @Override
    public ItemStack[] getTargets() {
        ItemStack[] itemStacks = new ItemStack[targets.length];
        for(int i = 0; i < itemStacks.length; i ++) {
            ItemStack itemStack = new ItemStack(targets[i]);
            itemStack.setAmount(amounts[i]);
        }
        return itemStacks;
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
    public Contract.ContractType getContractType() {
        return Contract.ContractType.MINING;
    }

    @Override
    public int[] getLocation() {
        return null;
    }

    @Override
    public void setLocation(StarSector sector) {

    }
}