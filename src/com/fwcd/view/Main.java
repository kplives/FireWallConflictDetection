package com.fwcd.view;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 程序入口
 * @author horace
 *
 */
public class Main {

	/**
	 * 主函数，整个程序的入口
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(null);
		MainUI myUI = new MainUI(shell);
		myUI.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

}
