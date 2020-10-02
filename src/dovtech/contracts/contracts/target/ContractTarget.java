package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;

public interface ContractTarget {

    Object[] getTargets();

    void setTargets(Object... target);

    Contract.ContractType getContractType();

    int[] getLocation();

    void setLocation(StarSector sector);

    void setTargetsFromString(String s);

    void setLocationFromString(String s);
}
