package dataorganizer;

import java.util.HashMap;
import java.util.Map;

public enum Axis {
	
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
	private static Map map = new HashMap();
	
	private Axis(int value) {
		this.value = value;
	}
	
	static {
		for (Axis a : Axis.values()) {
			map.put(a.value, a);
		}
	}
	
	public static Axis valueOf(int i) {
		return (Axis) map.get(i);
	}
	
	public int getValue() {
		return value;
	}

}
