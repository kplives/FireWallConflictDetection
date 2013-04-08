package com.fwcd.algorithm;

/**
 * 行为接口，主要用于统一定义各种行为映射值
 * @author horace
 *
 */
public interface Action {
	public final static int NOT_ACTION = 0;
	public final static int ACCEPT = 1;
	public final static int DENY = 2;
}
