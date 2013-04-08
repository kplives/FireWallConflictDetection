package com.fwcd.view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.fwcd.algorithm.FieldSet;
import com.fwcd.algorithm.Rule;
import com.fwcd.app.RealRule;

/**
 * 程序的主界面类
 * @author horace
 *
 */
public class MainUI extends Dialog {

	private Table detectResultTable;
	private StyledText ruleSrcText;
	protected Object result;
	protected Shell shell;

	private RealRule realRule;

	/**
	 * Create the dialog
	 * 
	 * @param parent
	 * @param style
	 */
	public MainUI(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * 
	 * @param parent
	 */
	public MainUI(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		getParent().dispose();
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(771, 460);
		shell.setText("防火墙冲突检测");

		final Button loadFileButton = new Button(shell, SWT.NONE);
		loadFileButton.setText("导入文件");
		loadFileButton.setBounds(10, 392, 60, 27);
		loadFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell);
				String fileIn = fileDialog.open();// get selected file path

				// read file content with FileInputStream,output content to
				// screen
				if (null != fileIn) {
					BufferedReader fileReader = null;
					try {
						fileReader = new BufferedReader(new FileReader(fileIn));
						StringBuffer buffer = new StringBuffer();
						String line = null;

						realRule = new RealRule();

						line = fileReader.readLine();
						buffer.append(line);
						buffer.append("\r\n");
						while ((line = fileReader.readLine()) != null) {
							buffer.append(line);
							buffer.append("\r\n");
							realRule.addRuleSrc(line);
						}
						ruleSrcText.setText(buffer.toString());
					} catch (FileNotFoundException fe) {
						fe.printStackTrace();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					} finally {
						try {
							fileReader.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}

			}
		});

		final Button coverDetectButton = new Button(shell, SWT.NONE);
		coverDetectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (realRule != null) {

					Color red = shell.getDisplay()
							.getSystemColor(SWT.COLOR_RED);
					Color blue = shell.getDisplay().getSystemColor(
							SWT.COLOR_DARK_CYAN);
					Color bark_red = shell.getDisplay().getSystemColor(
							SWT.COLOR_DARK_RED);
					Font font = new Font(shell.getDisplay(), "楷体", 9, SWT.BOLD);

					realRule.coverCollisionDetect(); // 执行覆盖冲突检测
					detectResultTable.removeAll();

					List<Rule> rlist_temp;
					if (!realRule.isRedundanceDetect()) {
						rlist_temp = realRule.getAlgRule();
					} else {
						rlist_temp = realRule.getAlgRuleClone();
					}
					List<String> str_list = realRule.getRuleSrc();
					String str;
					String str_array[];
					TableItem tbItem_temp;

					for (int i = 0; i < rlist_temp.size(); i++) {
						Rule rule_tmp = rlist_temp.get(i);
						// 显示冲突类型
						Color color_tmp = shell.getDisplay().getSystemColor(
								SWT.COLOR_BLACK);
						switch (rule_tmp.getCollisionType()) {
						case Rule.PART_OVERLAP:
							color_tmp = blue;
							break;
						case Rule.FULL_OVERLAP:
							color_tmp = red;
							break;
						case Rule.REDUNDANCE_COLLISION:
							color_tmp = bark_red;
							break;
						}

						// 显示匹配集
						tbItem_temp = new TableItem(detectResultTable, SWT.NONE);
						str = "匹配集" + RealRule.FIELD_SEPARATOR
								+ str_list.get(i);
						str_array = str.split(RealRule.FIELD_SEPARATOR);
						for (int a_i = 0; a_i < str_array.length; a_i++) {
							str_array[a_i] = str_array[a_i].trim();
						}
						tbItem_temp.setText(str_array);
						tbItem_temp.setForeground(color_tmp);
						tbItem_temp.setFont(font);

						// 显示判定集

						/*
						 * List<FieldSet> eval_set = rule_tmp.getEvalSet(); for
						 * (FieldSet fs : eval_set) { tbItem_temp = new
						 * TableItem(detectResultTable, SWT.NONE); str = "判定集" +
						 * RealRule.FIELD_SEPARATOR +
						 * realRule.convertFieldSetToString(fs); str_array =
						 * str.split(RealRule.FIELD_SEPARATOR); for (int a_i =
						 * 0; a_i < str_array.length; a_i++) { str_array[a_i] =
						 * str_array[a_i].trim(); }
						 * tbItem_temp.setText(str_array); }
						 */

						// 显示冲突源
						List<FieldSet> fs_list = rule_tmp.getCollisionSrc();
						for (FieldSet fs : fs_list) {
							tbItem_temp = new TableItem(detectResultTable,
									SWT.NONE);
							str = "冲突源" + RealRule.FIELD_SEPARATOR
									+ realRule.convertFieldSetToString(fs);
							str_array = str.split(RealRule.FIELD_SEPARATOR);
							for (int a_i = 0; a_i < str_array.length; a_i++) {
								str_array[a_i] = str_array[a_i].trim();
							}
							tbItem_temp.setText(str_array);
							tbItem_temp.setForeground(color_tmp);
						}
					}
				}
			}
		});
		coverDetectButton.setText("覆盖检测");
		coverDetectButton.setBounds(110, 392, 60, 27);

		final Button redundanceDetectButton = new Button(shell, SWT.NONE);
		redundanceDetectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (realRule != null) {
					Font font = new Font(shell.getDisplay(), "楷体", 9, SWT.BOLD);
					Color bark_red = shell.getDisplay().getSystemColor(
							SWT.COLOR_DARK_RED);

					realRule.redundanceCollisionDetect(); // 执行冗余冲突检测
					detectResultTable.removeAll();

					List<Rule> rlist_temp = realRule.getAlgRule();
					List<String> str_list = realRule.getRuleSrc();
					String str;
					String str_array[];
					TableItem tbItem_temp;

					for (int i = 0; i < rlist_temp.size(); i++) {
						Rule rule_tmp = rlist_temp.get(i);
						if (rule_tmp.getCollisionType() == Rule.REDUNDANCE_COLLISION) {

							// 显示匹配集
							tbItem_temp = new TableItem(detectResultTable,
									SWT.NONE);
							str = "匹配集" + RealRule.FIELD_SEPARATOR
									+ str_list.get(i);
							str_array = str.split(RealRule.FIELD_SEPARATOR);
							for (int a_i = 0; a_i < str_array.length; a_i++) {
								str_array[a_i] = str_array[a_i].trim();
							}
							tbItem_temp.setText(str_array);
							tbItem_temp.setForeground(bark_red);
							tbItem_temp.setFont(font);

							// 显示冲突源
							List<FieldSet> fs_list = rule_tmp.getCollisionSrc();
							for (FieldSet fs : fs_list) {
								tbItem_temp = new TableItem(detectResultTable,
										SWT.NONE);
								str = "冲突源" + RealRule.FIELD_SEPARATOR
										+ realRule.convertFieldSetToString(fs);
								str_array = str.split(RealRule.FIELD_SEPARATOR);
								for (int a_i = 0; a_i < str_array.length; a_i++) {
									str_array[a_i] = str_array[a_i].trim();
								}
								tbItem_temp.setText(str_array);
								tbItem_temp.setForeground(bark_red);
							}
						}
					}
				}
			}
		});
		redundanceDetectButton.setText("冗余检测");
		redundanceDetectButton.setBounds(212, 392, 60, 27);

		final Button exitButton = new Button(shell, SWT.NONE);
		exitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				shell.dispose();
			}
		});
		exitButton.setText("退    出");
		exitButton.setBounds(634, 392, 60, 27);

		ruleSrcText = new StyledText(shell, SWT.V_SCROLL | SWT.READ_ONLY
				| SWT.H_SCROLL | SWT.BORDER);
		ruleSrcText.setBounds(10, 25, 745, 183);

		final Label label = new Label(shell, SWT.NONE);
		label.setText("规则源");
		label.setBounds(10, 7, 48, 17);

		final Label label_1 = new Label(shell, SWT.NONE);
		label_1.setText("检测结果");
		label_1.setBounds(10, 216, 48, 17);

		detectResultTable = new Table(shell, SWT.BORDER);
		detectResultTable.setLinesVisible(true);
		detectResultTable.setHeaderVisible(true);
		detectResultTable.setBounds(10, 234, 745, 152);

		final TableColumn setTypeColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		setTypeColumn.setWidth(60);
		setTypeColumn.setText("集合类型");

		final TableColumn numColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		numColumn.setWidth(40);
		numColumn.setText("序号");

		final TableColumn protocolColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		protocolColumn.setWidth(40);
		protocolColumn.setText("协议");

		final TableColumn srcIPColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		srcIPColumn.setWidth(180);
		srcIPColumn.setText("源IP");

		final TableColumn srcPortColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		srcPortColumn.setWidth(70);
		srcPortColumn.setText("源端口");

		final TableColumn dstIPColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		dstIPColumn.setWidth(180);
		dstIPColumn.setText("目的IP");

		final TableColumn dstPortColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		dstPortColumn.setWidth(70);
		dstPortColumn.setText("目的端口");

		final TableColumn actionColumn = new TableColumn(detectResultTable,
				SWT.NONE);
		actionColumn.setWidth(60);
		actionColumn.setText("行为");

		final CLabel label_2 = new CLabel(shell, SWT.BORDER);
		label_2.setBackground(shell.getDisplay().getSystemColor(
				SWT.COLOR_DARK_CYAN));
		label_2.setBounds(115, 216, 10, 10);

		final Label label_3 = new Label(shell, SWT.NONE);
		label_3.setText("部分覆盖");
		label_3.setBounds(130, 216, 48, 17);

		final Label label_4 = new Label(shell, SWT.NONE);
		label_4.setText("完全覆盖");
		label_4.setBounds(230, 216, 48, 17);

		final Label label_5 = new Label(shell, SWT.NONE);
		label_5.setText("冗余冲突");
		label_5.setBounds(334, 216, 48, 17);

		final Label label_6 = new Label(shell, SWT.NONE);
		label_6.setText("没有冲突");
		label_6.setBounds(443, 216, 48, 17);

		final CLabel label_7 = new CLabel(shell, SWT.BORDER);
		label_7.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
		label_7.setBounds(215, 216, 10, 10);

		final CLabel label_8 = new CLabel(shell, SWT.BORDER);
		label_8.setBackground(shell.getDisplay().getSystemColor(
				SWT.COLOR_DARK_RED));
		label_8.setBounds(320, 216, 10, 10);

		final CLabel label_9 = new CLabel(shell, SWT.BORDER);
		label_9.setBackground(shell.getDisplay()
				.getSystemColor(SWT.COLOR_BLACK));
		label_9.setBounds(428, 216, 10, 10);
		//
	}

}
