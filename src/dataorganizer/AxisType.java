package dataorganizer;

import java.util.HashMap;
import java.util.Map;

public enum AxisType {
	
	Time(0),
	AccelX(1),
	AccelY(2),
	AccelZ(3),
	GyroX(4),
	GyroY(5),
	GyroZ(6),
	MagX(7),
	MagY(8),
	MagZ(9);
	
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
	
	public static AxisType valueOf(int i) {
		return (AxisType) map.get(i);
	}
	
	public int getValue() {
		return value;
	}

}