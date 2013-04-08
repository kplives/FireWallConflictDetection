package com.fwcd.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 防火墙决策树中的节点类,当边列表为空的时候表示叶子节点
 * 
 * @author horace
 * 
 */
public class Node {
	private List<TreeBranch> branch_list;
	private List<Value> unionSet;
	private TreeBranch enterBranch; // 进入结点的边，用于搜索某边的路径，头结点没有入边

	public Node() {
		branch_list = new ArrayList<TreeBranch>();
		unionSet = null;
		enterBranch = null;
	}

	public List<TreeBranch> getBranch_list() {
		return branch_list;
	}

	public void setBranch_list(List<TreeBranch> branch_list) {
		this.branch_list = branch_list;
	}

	// 取得当前节点所有分支值的并集,由于节点的任意一条出边的值没有交集,可简单的取出其边值集合即可
	public List<Value> getUnionSet() {
		unionSet = new ArrayList<Value>();
		for (TreeBranch branch : branch_list) {
			unionSet.addAll(branch.getValue());
		}
		return unionSet;
	}

	public void addBranch(TreeBranch branch) {
		branch_list.add(branch);
	}

	// 判断该节点是否为根节点
	public boolean isRootNode() {
		if (enterBranch == null) {
			return true;
		}
		return false;
	}

	// 判断该节点是否为叶子节点
	public boolean isLeafNode() {
		if (branch_list.size() == 0) {
			return true;
		}
		return false;
	}

	public void setEnterBranch(TreeBranch enterBranch) {
		this.enterBranch = enterBranch;
	}

	public TreeBranch getEnterBranch() {
		return enterBranch;
	}
}
