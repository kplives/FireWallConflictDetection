package com.fwcd.app;

/**
 * 统一定义协议宏，便于将协议转换为Value形式
 * @author horace
 *
 */
public interface Protocol {
	public final static int TCP = 0;
	public final static int UDP = 1;
	public final static int ICMP =2;
}
