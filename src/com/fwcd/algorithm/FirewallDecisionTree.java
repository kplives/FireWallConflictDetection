package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 防火墙决策树类，主要用于根据防火墙内设计的规则构造决策树
 * 
 * @author horace
 * 
 */
public class FirewallDecisionTree {
	private List<Rule> rules;
	private Node rootNode;

	public FirewallDecisionTree() {
		rootNode = new Node();
		rules = new ArrayList<Rule>();
	}

	// 控制台打印结果
	public void printResult() {
		// 首先打印各匹配集
		System.out.println("----------以下为匹配集----------");
		for (Rule r : rules) {
			r.getRSet().printFieldSet();
		}
		System.out.println("----------以下为判定集----------");
		for (Rule r : rules) {
			for (FieldSet fs : r.getEvalSet()) {
				fs.printFieldSet();
			}
		}
		System.out.println("----------以下为冲突源----------");
		List<String> str_ctype = new ArrayList<String>();
		str_ctype.add("没有冲突");
		str_ctype.add("部分覆盖");
		str_ctype.add("完全覆盖");
		str_ctype.add("冗余冲突");
		for (Rule r : rules) {
			System.out.println("规则" + r.getRuleNum() + ":\n" + "冲突类型:"
					+ str_ctype.get(r.getCollisionType()));
			System.out.println("冲突源：");
			for (FieldSet fs : r.getCollisionSrc()) {
				fs.printFieldSet();
			}

		}

	}

	// 查找冗余冲突
	public void findRedundanceCollision() {
		int rules_size = rules.size();
		List<Rule> rlist_temp;
		Node rn_temp;
		Rule r_temp;
		List<FieldSet> sSet_temp; // 临时变量，记录相同行为的域集

		for (int i = rules_size - 2; i >= 0; i--) { // 最后一个规则一定不为冗余
			r_temp = rules.get(i);
			if (r_temp.getCollisionType() == Rule.FULL_OVERLAP
					|| r_temp.getCollisionType() == Rule.REDUNDANCE_COLLISION)// 如果第i个规则已确定被完全覆盖则不予考虑
				continue;

			rlist_temp = new ArrayList<Rule>();
			for (int sub_i = i + 1; sub_i < rules_size; sub_i++) { // 去除被完全覆盖的规则
				if (rules.get(sub_i).getCollisionType() != Rule.FULL_OVERLAP
						&& rules.get(sub_i).getCollisionType() != Rule.REDUNDANCE_COLLISION) {
					rlist_temp.add(Rule.copyRule(rules.get(sub_i)));
				}
			}
			rn_temp = new Node();

			// 求规则i+1到规则n(除去被完全覆盖的规则)的决策树
			for (Rule r : rlist_temp) {
				int fieldIndex = 0;
				BuildSubFDT(r, fieldIndex, rn_temp);
			}

			sSet_temp = new ArrayList<FieldSet>();
			Field r_action_field = r_temp.getRSet().getFields().get(
					r_temp.getRSet().getFields().size() - 1);
			for (Rule r : rlist_temp) {
				for (FieldSet fs : r.getEvalSet()) {
					if (r_action_field.isEqual(fs.getFields().get(
							fs.getFields().size() - 1))) {// 具有相同的行为域
						sSet_temp.add(fs);
					}
				}
			}

			// 查找冲突源
			List<FieldSet> collisionTemp = new ArrayList<FieldSet>();
			for (FieldSet r_efs : r_temp.getEvalSet()) {
				for (int j = 0; j < sSet_temp.size(); j++) {
					FieldSet one_s_set = sSet_temp.get(j);
					FieldSet fset_temp = new FieldSet();
					int size_temp = one_s_set.getFields().size();
					for (int k = 0; k < size_temp - 1; k++) {// 不对Action求交
						Field f_temp = Field.calcIntersect(r_efs.getFields()
								.get(k), one_s_set.getFields().get(k));
						if (f_temp.getValue().size() != 0) {
							fset_temp.addField(f_temp);
						}
					}

					if (fset_temp.getFields().size() == size_temp - 1) { // 只有当所有的域都有交集才添加该冲突源
						Field action = new Field();
						action.addValue(Value.copyValue(one_s_set.getFields()
								.get(size_temp - 1).getValue().get(0)));
						fset_temp.addField(action);
						fset_temp.setRuleNum(one_s_set.getRuleNum());
						collisionTemp.add(fset_temp);
					}
				}
			}

			if (collisionTemp.size() != 0) {
				// 确定是否为冗余冲突
				rlist_temp = new ArrayList<Rule>();
				Rule rule_temp;
				int flag = sSet_temp.size();
				int a = 0;
				for (a = 0; a < flag; a++) {
					rule_temp = new Rule(a + 1);
					sSet_temp.get(a).setRuleNum(a + 1);
					rule_temp.setRSet(sSet_temp.get(a));
					rlist_temp.add(rule_temp);
				}
				for (int b = 0; b < r_temp.getEvalSet().size(); b++) {
					FieldSet fset_temp = FieldSet.copyFieldSet(r_temp
							.getEvalSet().get(b));
					fset_temp.setRuleNum(a + 1);
					rule_temp = new Rule(a + 1);
					rule_temp.setRSet(fset_temp);
					rlist_temp.add(rule_temp);
					a++;
				}

				rn_temp = new Node();
				for (Rule r : rlist_temp) {
					int fieldIndex = 0;
					BuildSubFDT(r, fieldIndex, rn_temp);
				}
				for (; flag < rlist_temp.size(); flag++) {
					if (rlist_temp.get(flag).getEvalSet().size() != 0) {
						break;
					}
				}
				if (flag == rlist_temp.size()) {
					// 重新设置冲突源和冲突类型
					r_temp.setCollisionType(Rule.REDUNDANCE_COLLISION);
					r_temp.setCollisionSrc(collisionTemp);
				}
			}

		}

	}

	// 查找冲突源(只适合查找部分覆盖和完全覆盖两种冲突源)并设置相应的冲突类型,存入相应的规则中
	public void findCollisionSrc() {

		calcMSetForRules(); // 首先为每一个规则计算覆盖集
		int rule_index = 0;
		// 从第二个规则开始逐个寻找冲突源(一个规则不可能发生冲突)
		for (rule_index = 1; rule_index < rules.size(); rule_index++) {
			Rule currentRule = rules.get(rule_index);
			FieldSet current_rset = currentRule.getRSet();
			List<FieldSet> current_mset = currentRule.getMSet();
			for (int i = 0; i < current_mset.size(); i++) {
				FieldSet one_mset = current_mset.get(i);
				FieldSet fset_temp = new FieldSet();
				int size_temp = one_mset.getFields().size();
				for (int j = 0; j < size_temp - 1; j++) {// 不对Action求交
					Field f_temp = Field.calcIntersect(one_mset.getFields()
							.get(j), current_rset.getFields().get(j));
					if (f_temp.getValue().size() != 0) {
						fset_temp.addField(f_temp);
					}
				}
				// 只有当所有的域都有交集才添加该冲突源
				if (fset_temp.getFields().size() == size_temp - 1) {
					Field action = new Field();
					action.addValue(Value.copyValue(one_mset.getFields().get(
							size_temp - 1).getValue().get(0)));
					fset_temp.addField(action);
					fset_temp.setRuleNum(one_mset.getRuleNum());
					currentRule.addCollisionSrc(fset_temp);
					currentRule.setCollisionType(Rule.PART_OVERLAP); // 由于匹配集和覆盖集有交集，先假定为部分覆盖
				}
			}

			if (currentRule.getEvalSet().size() == 0) { // 判定集为空，说明为完全覆盖
				currentRule.setCollisionType(Rule.FULL_OVERLAP);
			}
		}
	}

	// 求每一个规则的覆盖集，存储于相应规则中(其中存储的仅是前面规则判定集的一份引用，因此覆盖集不可修改)
	private void calcMSetForRules() {
		int rule_index = 0;
		for (rule_index = 1; rule_index < rules.size(); rule_index++) {// 从第二个规则开始，第一个规则没有覆盖集
			Rule currentRule = rules.get(rule_index);
			Rule preRule = rules.get(rule_index - 1);

			List<FieldSet> fs_temp = new ArrayList<FieldSet>();
			fs_temp.addAll(preRule.getMSet()); // 添加M[i-1]
			fs_temp.addAll(preRule.getEvalSet()); // 添加E[i-1]
			currentRule.setMSet(fs_temp);
		}
	}

	// 根据当前所有规则构造完整的FDT
	public void BuildFDT() {
		for (Rule rule : rules) {
			int fieldIndex = 0;
			BuildSubFDT(rule, fieldIndex, rootNode);
		}
	}

	// 构造子FDT，其中参数：rule是一条过滤规则，fieldIndex是规则匹配集判定域索引，node是FDT树中的节点
	private void BuildSubFDT(Rule rule, int fieldIndex, Node node) {

		if (fieldIndex < rule.getRSet().getFields().size()) {// 防止越界
			List<Value> vlist_temp;
			Field field = rule.getRSet().getFields().get(fieldIndex);
			// 如果还不是叶子节点
			if (!field.getValue().get(0).isAction()) {
				// 增加一条出边
				if ((vlist_temp = Value.calcDifferenceSet(field.getValue(),
						node.getUnionSet())).size() != 0) {
					TreeBranch new_branch = new TreeBranch();
					new_branch.CreateNewBranch(rule, fieldIndex, vlist_temp);
					new_branch.setTail(node); // 将新边加入到node的集合里面
					FieldSet eset_temp = new_branch.getPath();
					eset_temp.setRuleNum(rule.getRuleNum()); // 为判定集设置规则编号
					rule.addEvalSet(eset_temp);
				}

				List<TreeBranch> bList = node.getBranch_list();
				for (int i = 0; i < bList.size(); i++) {
					TreeBranch branch = bList.get(i);
					vlist_temp = Value.calcIntersect(branch.getValue(), field
							.getValue());
					// 交集为空或者边的节点为叶子节点
					if (vlist_temp.size() == 0 || branch.getHead().isLeafNode()) {
						continue;
					}

					// rule中field的值完全包含了某分支filed的值
					if (Value.isEqual(branch.getValue(), vlist_temp)) {

						BuildSubFDT(rule, fieldIndex + 1, branch.getHead());// 考虑rule的下一个判定域

					} else {
						TreeBranch new_branch = new TreeBranch();
						new_branch = new_branch.copyBranch(branch, branch
								.getTail());
						new_branch.setValue(Value.calcIntersect(branch
								.getValue(), field.getValue()));
						branch.setValue(Value.calcDifferenceSet(branch
								.getValue(), field.getValue()));
						BuildSubFDT(rule, fieldIndex + 1, new_branch.getHead());
					}
				}
			}
		}
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rule) {
		this.rules = rule;
	}

	public void addRule(Rule r) {
		rules.add(r);
	}

	public Node getRootNode() {
		return rootNode;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
}
