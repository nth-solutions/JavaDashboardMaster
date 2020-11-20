package com.bioforceanalytics.dashboard;

/**
 * This is the parent of Conservation of Energy and Conservation of Momentum
 * tests.
 */
public abstract class TwoModuleTest {
    
    private GenericTest moduleOne;
    private GenericTest moduleTwo;
    
    /**
     * Gets the first module in this two-module test.
     * @return the first module's data test
     */
    public GenericTest getModuleOne() {
		return moduleOne;
    }
    
    /**
     * Gets the second module in this two-module test.
     * @return the second module
     */
	public GenericTest getModuleTwo() {
		return moduleTwo;
    }
    
    /**
     * Adds a module to this two-module test.
	 * @return the number of the newly added module (1 or 2)
     */
    public int addModule(GenericTest test) {

        // if the 1st module slot is empty, fill it in
		if (moduleOne == null) {
			moduleOne = test;
            return 1;
            
        }
        // the 2nd module slot is empty, so fill it in
        else {
			moduleTwo = test;
			return 2;
        }

	}

    /**
     * Checks if two modules are already assigned to this two-module test.
     * @return whether two modules are already assigned
     */
	public boolean isFilled() {
		return (moduleOne != null && moduleTwo != null);
	}

}