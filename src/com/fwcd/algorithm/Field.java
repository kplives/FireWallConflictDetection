package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 判定域类
 * 
 * @author horace
 * 
 */
public class Field {
	private List<Value> value;

	public Field() {
		value = new ArrayList<Value>();
	}

	// 判断两个域是否相等，这里只简单的判断起值是否相等
	public boolean isEqual(Field dstField) {
		List<Value> vlist_temp = dstField.getValue();
		if (value.size() != vlist_temp.size()) {
			return false;
		} else {
			for (int vIndex = 0; vIndex < value.size(); vIndex++) {
				if (!value.get(vIndex).isEqual(vlist_temp.get(vIndex))) {
					return false;
				}
			}
		}
		return true;
	}

	// 求两个域的差集
	public static Field calcDifferenceSet(Field m, Field s) {
		Field result = new Field();
		result.getValue().addAll(
				Value.calcDifferenceSet(m.getValue(), s.getValue()));
		return result;
	}

	// 求两个域的交集
	public static Field calcIntersect(Field a, Field b) {
		Field result = new Field();
		List<Value> vlist_temp = Value
				.calcIntersect(a.getValue(), b.getValue());
		if (vlist_temp.size() != 0) {
			result.setValue(vlist_temp);
		}
		return result;
	}

	public List<Value> getValue() {
		return value;
	}

	public void addValue(Value v) {
		value.add(v);
	}

	public void setValue(List<Value> value) {
		this.value = value;
	}

}
