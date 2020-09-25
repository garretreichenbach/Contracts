package dovtech.contracts.contracts.target;

import api.universe.StarSector;
import dovtech.contracts.contracts.Contract;

public interface ContractTarget {

    int getAmount();

    Object getTarget();

    void setTarget(Object target);

    Contract.ContractType getContractType();

    StarSector getLocation();

    void setLocation(StarSector sector);
}
