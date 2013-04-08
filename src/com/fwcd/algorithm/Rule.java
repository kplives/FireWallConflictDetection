package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则类，实际规则类可继承该类或者内嵌该类对象做扩展
 * 
 * @author horace
 * 
 */
public class Rule {
	private int ruleNum; // 规则编号
	private FieldSet rSet; // 匹配集
	private List<FieldSet> evalSet; // 判定集(evalSet.size()=0表示判定集为空),且其Action即为匹配集的Action
	private List<FieldSet> mSet; // 覆盖集
	private List<FieldSet> collisionSrc; // 冲突源
	private int collisionType; // 冲突类型（自定义数值映射）,-1表示尚未求得冲突类型

	public final static int NO_COLLISION = 0; // 木有冲突
	public final static int PART_OVERLAP = 1; // 部分覆盖
	public final static int FULL_OVERLAP = 2; // 完全覆盖
	public final static int REDUNDANCE_COLLISION = 3; // 冗余冲突

	public Rule() {
		ruleNum = -1;
		collisionType = NO_COLLISION;
		rSet = new FieldSet();
		evalSet = new ArrayList<FieldSet>();
		setMSet(new ArrayList<FieldSet>());
		collisionSrc = new ArrayList<FieldSet>();
	}

	public Rule(int r_num) {
		ruleNum = r_num;
		collisionType = NO_COLLISION;
		rSet = new FieldSet();
		rSet.setRuleNum(r_num);
		evalSet = new ArrayList<FieldSet>();
		setMSet(new ArrayList<FieldSet>());
		collisionSrc = new ArrayList<FieldSet>();
	}

	// 添加一个判定集
	public void addEvalSet(FieldSet fields) {
		Field f_temp = new Field();
		int action_index = rSet.getFields().size() - 1;
		Value v_temp = Value.copyValue(rSet.getFields().get(action_index)
				.getValue().get(0));
		f_temp.addValue(v_temp);
		fields.addField(f_temp); // 为判定集添加Action

		fields.setRuleNum(getRuleNum()); // 为判定集添加规则编号
		evalSet.add(fields);
	}

	// 添加一个冲突源,添加的冲突源中必须有规则编号,方法內沒有執行檢查,使用方法的的人自行檢查
	public void addCollisionSrc(FieldSet cFieldSet) {
		collisionSrc.add(cFieldSet);
	}

	// 为匹配集添加一个判定域
	public void addFieldForRSet(Field f) {
		rSet.addField(f);
	}

	// 实现本规则的一个完全克隆体
	public Rule Clone() {
		Rule dstRule = new Rule(this.ruleNum);
		dstRule.setCollisionType(collisionType);
		Field f_temp;
		FieldSet fs_temp = new FieldSet();
		fs_temp.setRuleNum(ruleNum);
		// 复制匹配集
		for (Field f : rSet.getFields()) {
			f_temp = new Field();
			List<Value> vlist_temp = f.getValue();
			for (Value v : vlist_temp) {
				f_temp.addValue(Value.copyValue(v));
			}
			fs_temp.addField(f_temp);
		}
		dstRule.setRSet(fs_temp);

		List<FieldSet> fs_list_temp = new ArrayList<FieldSet>();
		// 复制覆盖集
		for (FieldSet fs : mSet) {
			fs_temp = new FieldSet();
			fs_temp.setRuleNum(fs.getRuleNum());
			for (Field f : fs.getFields()) {
				f_temp = new Field();
				List<Value> vlist_temp = f.getValue();
				for (Value v : vlist_temp) {
					f_temp.addValue(Value.copyValue(v));
				}
				fs_temp.addField(f_temp);
			}
			fs_list_temp.add(fs_temp);
		}
		dstRule.setMSet(fs_list_temp);

		fs_list_temp = new ArrayList<FieldSet>();
		// 复制判定集
		for (FieldSet fs : collisionSrc) {
			fs_temp = new FieldSet();
			fs_temp.setRuleNum(fs.getRuleNum());
			for (Field f : fs.getFields()) {
				f_temp = new Field();
				List<Value> vlist_temp = f.getValue();
				for (Value v : vlist_temp) {
					f_temp.addValue(Value.copyValue(v));
				}
				fs_temp.addField(f_temp);
			}
			fs_list_temp.add(fs_temp);
		}
		dstRule.setCollisionSrc(fs_list_temp);

		return dstRule;
	}

	// 复制一条规则（这里只覆盖传入规则的匹配集及编号等初始值）
	public static Rule copyRule(Rule srcRule) {
		Rule dstRule = new Rule(srcRule.getRuleNum());
		List<Field> flist_temp = srcRule.getRSet().getFields();
		FieldSet dst_fset = new FieldSet();
		dst_fset.setRuleNum(srcRule.getRuleNum()); // 替匹配集设置规则编号
		Field f_temp;

		for (Field f : flist_temp) {
			f_temp = new Field();
			List<Value> vlist_temp = f.getValue();
			for (Value v : vlist_temp) {
				f_temp.addValue(Value.copyValue(v));
			}
			dst_fset.addField(f_temp);
		}
		dstRule.setRSet(dst_fset); // 为新规则添加匹配集

		return dstRule;
	}

	public void setRuleNum(int ruleNum) {
		this.ruleNum = ruleNum;
		if (rSet != null) {
			rSet.setRuleNum(ruleNum);
		}
	}

	public int getRuleNum() {
		return ruleNum;
	}

	public void setRSet(FieldSet rSet) {
		rSet.setRuleNum(getRuleNum());
		this.rSet = rSet;
	}

	public FieldSet getRSet() {
		return rSet;
	}

	public List<FieldSet> getEvalSet() {
		return evalSet;
	}

	public void setCollisionSrc(List<FieldSet> collisionSrc) {
		this.collisionSrc = collisionSrc;
	}

	public List<FieldSet> getCollisionSrc() {
		return collisionSrc;
	}

	// 设置冲突类型
	public void setCollisionType(int collisionType) {
		this.collisionType = collisionType;
	}

	public int getCollisionType() {
		return collisionType;
	}

	public void setMSet(List<FieldSet> mSet) {
		this.mSet = mSet;
	}

	public List<FieldSet> getMSet() {
		return mSet;
	}
}
