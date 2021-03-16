package thederpgamer.contracts.data.contract.target;

import thederpgamer.contracts.data.contract.Contract;

public interface ContractTarget {

    Object[] getTargets();

    void setTargets(Object... target);

    Contract.ContractType getContractType();
}
