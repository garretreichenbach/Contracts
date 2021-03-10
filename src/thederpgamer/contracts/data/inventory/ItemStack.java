package thederpgamer.contracts.data.inventory;

import org.schema.game.common.data.element.ElementInformation;
import java.io.Serializable;

/**
 * ItemStack.java
 * <Description>
 * ==================================================
 * Created 03/10/2021
 * @author TheDerpGamer
 */
public class ItemStack implements Serializable {

    public short id;
    public int count;
    public String name;

    public ItemStack(ElementInformation elementInfo) {
        id = elementInfo.getId();
        name = elementInfo.getName();
    }
}
