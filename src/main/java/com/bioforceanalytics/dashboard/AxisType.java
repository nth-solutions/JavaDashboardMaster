package com.bioforceanalytics.dashboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum used to identify an {@link com.bioforceanalytics.dashboard.AxisDataSeries AxisDataSeries}.
 * <p>Used by the BioForce Graph for retrieving the index of an axis in {@link com.bioforceanalytics.dashboard.GenericTest GenericTest},
 * as well as keeping track of which data sets are currently displayed in {@link com.bioforceanalytics.dashboard.GraphNoSINCController GraphNoSINCController}.</p>
 */
public enum AxisType implements Axis {
	
	AccelX(0),
	AccelY(1),
	AccelZ(2),
	AccelMag(3),
	
	VelX(4),
	VelY(5),
	VelZ(6),
	VelMag(7),
	
	DispX(8),
	DispY(9),
	DispZ(10),
	DispMag(11),
	
	AngAccX(12),
	AngAccY(13),
	AngAccZ(14),
	AngAccMag(15),
	
	AngVelX(16),
	AngVelY(17),
	AngVelZ(18),
	AngVelMag(19),
	
	AngDispX(20),
	AngDispY(21),
	AngDispZ(22),
	AngDispMag(23),
	
	MagnetX(24),
	MagnetY(25),
	MagnetZ(26),
	MagnetMag(27);


	
	private int value;
	private static Map<Integer, AxisType> map = new HashMap<Integer, AxisType>();
	
	private AxisType(int value) {
		this.value = value;
	}
	
	static {
		for (AxisType a : AxisType.values()) {
			map.put(a.value, a);
		}
	}
	
	/**
	 * Returns the AxisType associated with this integer.
	 * @param i the integer representing the desired AxisType
	 * @return the AxisType associated with this integer
	 */
	public static AxisType valueOf(int i) {
		return (AxisType) map.get(i);
	}
	
	/**
	 * Returns the integer associated with this AxisType.
	 * @return the integer associated with this AxisType
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		switch (getValue() / 4) {
            case 0: return "Acceleration";
            case 1: return "Velocity";
            case 2: return "Displacement";
            case 3: return "Angular Acceleration";
            case 4: return "Angular Velocity";
            case 5: return "Angular Displacement";
            case 6: return "Magnetic Field";
            default: return "Unnamed Axis";
        }
	}
	public String getExactName(){
		switch(getValue()){
			case 0: return "AccelX";
			case 1: return "AccelY";
			case 2: return "AccelZ";
			case 3: return "AccelMag";
            case 4: return "VelX";
			case 5: return "VelY";
			case 6: return "VelZ";
			case 7: return "VelMag";
			case 8: return "DispX";
			case 9: return "DispY";
			case 10: return "DispZ";
			case 11: return "DispMag";
			case 12: return "AngAccelX";
			case 13: return "AngAccelY";
			case 14: return "AngAccelZ";
			case 15: return "AngAccMag";
			case 16: return "AngVelX";
			case 17: return "AngVelY";
			case 18: return "AngVelZ";
			case 19: return "AngVelMag";
			case 20: return "AngDispX";
			case 21: return "AngDispY";
			case 22: return "AngDispZ";
			case 23: return "AngDispMag";
			case 24: return "MagnetX";
			case 25: return "MagnetY";
			case 26: return "MagnetZ";
			case 27: return "MagnetMag";
			default:
				return "";
		}
	}

	@Override
	public String getUnits() {
		switch (getValue() / 4) {
            case 0: return "m/s²";
            case 1: return "m/s";
            case 2: return "m";
            case 3: return "°/s²";
            case 4: return "°/s";
            case 5: return "°";
            case 6: return "µT";
            default: return "";
        }
	}

	@Override
	public boolean isCustomAxis() {
		return false;
	}
	@Override
	public String getNameAndUnits(){
		return getName() + " ("+ getUnits()+")";
	}
	@Override
	public double getAxisScalar() {
		 // if AxisType is Accel, Vel, Disp, or Momentum
		 if (getValue() / 4 < 3 || getValue() == 7) return 10;
		 //if AxisType is AngAccel
		 if (getValue() / 4 == 3) return 500;
		 //if AxisType is Momentum
		 if (getValue() / 4 == 7) return 1;
		 // all other data sets
		 else return 100;
	}

	@Override
	public int getIndex() {
		return value;
	}

}