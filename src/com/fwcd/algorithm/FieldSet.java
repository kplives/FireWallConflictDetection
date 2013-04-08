package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 判定域集类，可以是匹配集、判定集、覆盖集及冲突源中的一个域集，最后一个域设置为Action
 * @author horace
 *
 */
public class FieldSet {
	private int ruleNum;
	private List<Field> fields;

	public FieldSet() {
		setRuleNum(-1);
		fields = new ArrayList<Field>();
	}

	public void printFieldSet() {
		List<String> str_action = new ArrayList<String>();
		str_action.add(Action.NOT_ACTION, "Not Action");
		str_action.add(Action.ACCEPT, "Accept");
		str_action.add(Action.DENY, "Deny");

		System.out.print("<" + ruleNum + ">");
		int i = 0;
		for (i = 0; i < fields.size() - 1; i++) {
			System.out.print("^F" + i);
			for (Value v : fields.get(i).getValue()) {
				System.out.print("[" + v.getLow() + "," + v.getHigh() + "]");
			}
		}
		System.out.println("->"
				+ str_action.get(fields.get(i).getValue().get(0).getAction()));

	}

	// 求两组域集的差集
	public static List<FieldSet> calcDifferenceSet(List<FieldSet> m,
			List<FieldSet> s) {
		List<FieldSet> result = new ArrayList<FieldSet>();
		List<FieldSet> fs_list_temp;
		int s_size = s.size();

		if (s_size == 0) {
			for (FieldSet fs : m) {
				result.add(FieldSet.copyFieldSet(fs));
			}
		} else {
			for (FieldSet fs : m) {
				int s_index = 0;
				fs_list_temp = FieldSet.calcDifferenceSet(fs, s.get(s_index));
				s_index++;
				if (fs_list_temp.size() != 0) {// 如果减去第一个域集后返回值已经为空，则不用再减后面的域集
					for (; s_index < s_size; s_index++) {
						if (fs_list_temp.size() > 1) {
							fs_list_temp = FieldSet.calcDifferenceSet(
									fs_list_temp, s.subList(s_index, s_size));
						} else if (fs_list_temp.size() == 1) {
							fs_list_temp = FieldSet.calcDifferenceSet(
									fs_list_temp.get(0), s.get(s_index));
						} else {
							break;
						}
					}
				}
				result.addAll(fs_list_temp);
			}
		}

		return result;
	}

	// 求两个域的差集
	public static List<FieldSet> calcDifferenceSet(FieldSet m, FieldSet s) {
		List<FieldSet> result = new ArrayList<FieldSet>();
		FieldSet fs_temp;
		Field f_temp;
		List<Field> m_flist = m.getFields();
		List<Field> s_flist = s.getFields();
		int m_size = m_flist.size();
		int s_size = s_flist.size();

		if (m_size == s_size) { // 只有当两个域集的维数想同才可以求差集
			for (int i = 0; i < m_size - 1; i++) { // 处理其中的一个域并保留其它域为其差集结果之一，不对Action做求差处理
				fs_temp = FieldSet.copyFieldSet(m);
				f_temp = Field
						.calcDifferenceSet(m_flist.get(i), s_flist.get(i));
				if (f_temp.getValue().size() != 0) { // 当前处理域的差集不为空
					fs_temp.getFields().set(i, f_temp);
					result.add(fs_temp);
				}
			}
		}
		return result;
	}

	// 复制一个域集
	public static FieldSet copyFieldSet(FieldSet src) {
		FieldSet result = new FieldSet();
		Field f_temp;
		for (Field f : src.getFields()) {
			f_temp = new Field();
			for (Value v : f.getValue()) {
				f_temp.addValue(Value.copyValue(v));
			}
			result.addField(f_temp);
		}
		return result;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setRuleNum(int ruleNum) {
		this.ruleNum = ruleNum;
	}

	public int getRuleNum() {
		return ruleNum;
	}

}
