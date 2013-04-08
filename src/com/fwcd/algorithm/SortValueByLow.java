package com.fwcd.algorithm;

import java.util.Comparator;

/**
 * 对Value进行优先按low值排序，low相等时按high值排序
 * @author horace
 * 
 */
public class SortValueByLow implements Comparator<Value> {
	public int compare(Value v1, Value v2) {
		if (v1.getLow() > v2.getLow()) {
			return 1;
		} else if (v1.getLow() == v2.getLow()) {
			if (v1.getHigh() > v2.getHigh()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
