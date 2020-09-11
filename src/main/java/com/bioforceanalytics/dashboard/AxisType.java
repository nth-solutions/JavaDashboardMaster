package com.bioforceanalytics.dashboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum used to identify an {@link com.bioforceanalytics.dashboard.AxisDataSeries AxisDataSeries}.
 * <p>Used by the Data Analysis Graph for retrieving the index of an axis in {@link com.bioforceanalytics.dashboard.GenericTest GenericTest},
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
	 * @return Returns the AxisType associated with this integer
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
            case 7: return "Momentum";
            default: return "Unnamed Axis";
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
            case 7: return "kg-m/s";
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