package com.bioforceanalytics.dashboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum used to identify an {@link com.bioforceanalytics.dashboard.AxisDataSeries AxisDataSeries}.
 * <p>Used by the BioForce Graph for retrieving the index of an axis in {@link com.bioforceanalytics.dashboard.GenericTest GenericTest},
 * as well as keeping track of which data sets are currently displayed in {@link com.bioforceanalytics.dashboard.GraphNoSINCController GraphNoSINCController}.</p>
 */
public enum AxisType {
	
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
	MagnetMag(27),

	MomentumX(28),
	MomentumY(29),
	MomentumZ(30),
	MomentumMag(31);
	
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

}