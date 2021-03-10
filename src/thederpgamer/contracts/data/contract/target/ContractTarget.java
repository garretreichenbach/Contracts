package thederpgamer.contracts.data.contract.target;

import org.schema.common.util.linAlg.Vector3i;
import thederpgamer.contracts.data.contract.Contract;

public interface ContractTarget {

    Object[] getTargets();

    void setTargets(Object... target);

    Contract.ContractType getContractType();

    Vector3i getSector();

    void setSector(Vector3i sector);
}
