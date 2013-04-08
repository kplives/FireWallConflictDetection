package com.fwcd.app;

import java.util.ArrayList;
import java.util.List;

import com.fwcd.algorithm.*;
import com.fwcd.util.ConvertIP;

/**
 * 实际规则类，包含规则源和算法适用规则以及二者的转换方法和实际规则的冲突求法
 * @author horace
 *
 */
public class RealRule {
	private List<String> ruleSrc; // 规则源
	private List<Rule> algRule; // 算法适用规则集
	private List<Rule> algRuleClone; // 算法适用规则集拷贝(主要用于冗余冲突检测时保留原有检测结果)

	private FirewallDecisionTree fwdt_instance;
	private boolean isCoverDetect;
	private boolean isRedundanceDetect;

	public final static String FIELD_SEPARATOR = "\t";

	public RealRule() {
		ruleSrc = new ArrayList<String>();
		algRule = new ArrayList<Rule>();
		algRuleClone = new ArrayList<Rule>();

		fwdt_instance = new FirewallDecisionTree();
		isCoverDetect = false;
		isRedundanceDetect = false;
	}

	// 将一个域集转换为String
	public String convertFieldSetToString(FieldSet fs) {
		String result = "";
		result += fs.getRuleNum() + FIELD_SEPARATOR;

		List<Field> flist_temp = fs.getFields();
		List<Value> vlist_temp = flist_temp.get(0).getValue(); // 协议域
		result += convertValueToProtocol(vlist_temp.get(0)) + FIELD_SEPARATOR;

		vlist_temp = flist_temp.get(1).getValue(); // 源IP
		for (int i = 0; i < vlist_temp.size(); i++) {
			result += convertValueToIP(vlist_temp.get(i));
			if (i < vlist_temp.size() - 1) {
				result += ", ";
			}
		}

		result += FIELD_SEPARATOR;
		vlist_temp = flist_temp.get(2).getValue(); // 源端口
		result += convertValueToPort(vlist_temp) + FIELD_SEPARATOR;

		vlist_temp = flist_temp.get(3).getValue(); // 目的IP
		for (int i = 0; i < vlist_temp.size(); i++) {
			result += convertValueToIP(vlist_temp.get(i));
			if (i < vlist_temp.size() - 1) {
				result += ", ";
			}
		}

		result += FIELD_SEPARATOR;
		vlist_temp = flist_temp.get(4).getValue(); // 目的端口
		result += convertValueToPort(vlist_temp) + FIELD_SEPARATOR;

		vlist_temp = flist_temp.get(5).getValue(); // Action
		result += convertValueToAction(vlist_temp.get(0));

		return result;
	}

	// 冗余冲突检测
	public void redundanceCollisionDetect() {
		if (!isCoverDetect) {
			convertSrcRuleToAlgRule(); // 首先得到算法适用规则

			fwdt_instance.setRules(algRule); // 设置规则集
			fwdt_instance.BuildFDT(); // 构建防火墙决策树
			fwdt_instance.findCollisionSrc(); // 查找冲突源（部分覆盖，完全覆盖）

			isCoverDetect = true;
		}
		if (!isRedundanceDetect) {
			cloneRules();
			fwdt_instance.findRedundanceCollision();
			isRedundanceDetect = true;
		}
		fwdt_instance.printResult(); // 用于测试
	}

	// 覆盖冲突检测
	public void coverCollisionDetect() {
		if (!isCoverDetect) {
			convertSrcRuleToAlgRule(); // 首先得到算法适用规则

			fwdt_instance.setRules(algRule); // 设置规则集
			fwdt_instance.BuildFDT(); // 构建防火墙决策树
			fwdt_instance.findCollisionSrc(); // 查找冲突源（部分覆盖，完全覆盖）

			isCoverDetect = true;
		}
		fwdt_instance.printResult(); // 用于测试
	}

	// 求算法适用规则集拷贝
	private void cloneRules() {
		for (Rule src_r : algRule) {
			algRuleClone.add(src_r.Clone());
		}
	}

	// 将原规则转换为算法适用的规则
	private void convertSrcRuleToAlgRule() {
		String str_temp[];
		Rule r_temp;
		Field f_temp;
		for (String str : ruleSrc) {
			str_temp = str.split(FIELD_SEPARATOR);
			r_temp = new Rule(Integer.parseInt(str_temp[0].trim())); // 规则编号
			f_temp = new Field();
			f_temp.addValue(convertProToValue(str_temp[1].trim())); // 协议域
			r_temp.addFieldForRSet(f_temp);

			f_temp = new Field();
			f_temp.addValue(convertIPToValue(str_temp[2].trim())); // 源IP
			r_temp.addFieldForRSet(f_temp);

			f_temp = new Field();
			f_temp.setValue(convertPortToValue(str_temp[3].trim())); // 源端口
			r_temp.addFieldForRSet(f_temp);

			f_temp = new Field();
			f_temp.addValue(convertIPToValue(str_temp[4].trim())); // 目的IP
			r_temp.addFieldForRSet(f_temp);

			f_temp = new Field();
			f_temp.setValue(convertPortToValue(str_temp[5].trim())); // 目的端口
			r_temp.addFieldForRSet(f_temp);

			f_temp = new Field();
			f_temp.addValue(convertActionToValue(str_temp[6].trim())); // Action
			r_temp.addFieldForRSet(f_temp);

			algRule.add(r_temp);
		}
	}

	// 将Value转换为String Action
	private String convertValueToAction(Value v) {
		String result = "";
		switch (v.getAction()) {
		case Action.ACCEPT:
			result += "accept";
			break;
		case Action.DENY:
			result += "deny";
			break;
		}
		return result;
	}

	// 将String Action转换为Value
	private Value convertActionToValue(String action) {
		Value result = new Value();
		if (action.equals("accept")) {
			result.setAction(Action.ACCEPT);
		} else if (action.equals("deny")) {
			result.setAction(Action.DENY);
		}
		return result;
	}

	// 将Value转换为端口
	private String convertValueToPort(List<Value> vlist) {
		String result = "";
		Value v_temp;
		long l_temp, h_temp;
		for (int i = 0; i < vlist.size(); i++) {
			v_temp = vlist.get(i);
			l_temp = v_temp.getLow();
			h_temp = v_temp.getHigh();
			if (l_temp == h_temp) {
				result += String.valueOf(l_temp);
			} else { // 是一个端口段
				result += "[" + String.valueOf(l_temp) + ","
						+ String.valueOf(h_temp) + "]";
			}
			if (i < (vlist.size() - 1)) {
				result += ",";
			}
		}

		return result;
	}

	// 将端口转换为Value形式
	private List<Value> convertPortToValue(String port) {
		List<Value> result = new ArrayList<Value>();
		Value v_temp = new Value();
		String str_temp[] = port.split(",");
		int iTemp = 0;
		if (port.equals("ANY")) {
			v_temp.setLow(0x0);
			v_temp.setHigh(0xFFFF);
			result.add(v_temp);
		} else if (str_temp[0].substring(0, 1).equals("[")) { // 如果是一段端口
			v_temp.setLow(Integer.valueOf(str_temp[0].substring(1, str_temp[0]
					.length()), 10));
			v_temp.setHigh(Integer.parseInt(str_temp[1].substring(0,
					str_temp[1].length() - 1)));
			result.add(v_temp);
		} else {
			if (str_temp.length == 1) { // 只有一个端口
				iTemp = Integer.valueOf(str_temp[0], 10);
				v_temp.setLow(iTemp);
				v_temp.setHigh(iTemp);
				result.add(v_temp);
			} else { // 两个端口
				iTemp = Integer.valueOf(str_temp[0], 10);
				v_temp.setLow(iTemp);
				v_temp.setHigh(iTemp);
				result.add(v_temp);

				iTemp = Integer.valueOf(str_temp[1], 10);
				v_temp = new Value();
				v_temp.setLow(iTemp);
				v_temp.setHigh(iTemp);
				result.add(v_temp);
			}
		}
		return result;
	}

	public boolean isCoverDetect() {
		return isCoverDetect;
	}

	public boolean isRedundanceDetect() {
		return isRedundanceDetect;
	}

	// 将Value转换为协议
	private String convertValueToProtocol(Value v) {
		String result = "";
		if (v.getLow() != v.getHigh()) {
			result = "ANY";
		} else {
			switch ((int) v.getLow()) {
			case Protocol.TCP:
				result = "TCP";
				break;
			case Protocol.UDP:
				result = "UDP";
				break;
			case Protocol.ICMP:
				result = "ICMP";
				break;
			}
		}
		return result;
	}

	// 将协议转换为Value的形式
	private Value convertProToValue(String protocol) {
		Value result = new Value();
		if (protocol.equals("ANY")) {
			result.setLow(0x0);
			result.setHigh(0xFFFF);
		} else if (protocol.equals("TCP")) {
			result.setLow(Protocol.TCP);
			result.setHigh(Protocol.TCP);
		} else if (protocol.equals("UDP")) {
			result.setLow(Protocol.UDP);
			result.setHigh(Protocol.UDP);
		} else if (protocol.equals("ICMP")) {
			result.setLow(Protocol.ICMP);
			result.setHigh(Protocol.ICMP);
		}
		return result;
	}

	// 将Value转换为IP的形式
	private String convertValueToIP(Value v) {
		String result = "";
		long l_temp = v.getLow();
		long h_temp = v.getHigh();
		if (l_temp == h_temp) { // 单个IP
			result = ConvertIP.longToIP(h_temp);
		} else { // IP段
			result += ConvertIP.longToIP(l_temp);
			result += "--" + ConvertIP.longToIP(h_temp);
		}
		return result;
	}

	// 将Ip转换为Value的形式
	private Value convertIPToValue(String ipAndMask) {
		Value result = new Value();
		if (ipAndMask.equals("ANY")) {
			result.setLow(0x0);
			result.setHigh(ConvertIP.ipToLong("255.255.255.255"));
		} else {
			String[] ipAdd = ipAndMask.split("/");
			long IP_Low = ConvertIP.ipToLong(ipAdd[0]);
			if (ipAdd.length == 2) {
				int netMask = Integer.valueOf(ipAdd[1].trim());
				if (netMask < 0 || netMask > 31) {
					throw new IllegalArgumentException(
							"invalid ipAndMask with: " + ipAndMask);
				}
				IP_Low = IP_Low & (0xFFFFFFFF << (32 - netMask)) + 1;
				long IP_High = IP_Low + (0xFFFFFFFF >>> netMask) - 1;
				result.setLow(IP_Low);
				result.setHigh(IP_High);
			} else {
				result.setLow(IP_Low);
				result.setHigh(IP_Low);
			}
		}
		return result;
	}

	public List<String> getRuleSrc() {
		return ruleSrc;
	}

	// 添加一条原规则
	public void addRuleSrc(String src) {
		if (src != null && !src.equals("")) {
			ruleSrc.add(src);
		}
	}

	public void setRuleSrc(List<String> ruleSrc) {
		this.ruleSrc = ruleSrc;
	}

	public List<Rule> getAlgRule() {
		return algRule;
	}

	public void setAlgRule(List<Rule> algRule) {
		this.algRule = algRule;
	}

	public List<Rule> getAlgRuleClone() {
		return algRuleClone;
	}

	public void setAlgRuleClone(List<Rule> algRuleClone) {
		this.algRuleClone = algRuleClone;
	}

}
