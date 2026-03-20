package com.rts.model;

import java.util.List;

public class ComparaisonResult {
    private List<Scenario> scenariosAjoutes;
    private List<Scenario> scenariosSupprimes;
    private List<Scenario> scenariosModifies;

    public ComparaisonResult(List<Scenario> ajoutes, List<Scenario> supprimes, List<Scenario> modifies) {
        this.scenariosAjoutes = ajoutes;
        this.scenariosSupprimes = supprimes;
        this.scenariosModifies = modifies;
    }

    public boolean aChange() {
        return !scenariosAjoutes.isEmpty() || !scenariosSupprimes.isEmpty() || !scenariosModifies.isEmpty();
    }

    public List<Scenario> getScenariosAjoutes() { return scenariosAjoutes; }
    public List<Scenario> getScenariosSupprimes() { return scenariosSupprimes; }
    public List<Scenario> getScenariosModifies() { return scenariosModifies; }
}
