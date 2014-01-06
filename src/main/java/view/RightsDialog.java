/**
 * 
 */
package view;

import java.awt.Dialog;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

import java.awt.FlowLayout;
import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.JButton;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Jakub Fortunka
 *
 */
public class RightsDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4003666607636371386L;
	
	private JTextField textField;
	private JCheckBox chckbxReadOwner;
	private JCheckBox chckbxWriteOwner;
	private JCheckBox chckbxExecuteOwner;
	private JCheckBox chckbxReadGroup;
	private JCheckBox chckbxWriteGroup;
	private JCheckBox chckbxExecuteGroup;
	private JCheckBox chckbxReadOthers;
	private JCheckBox chckbxWriteOthers;
	private JCheckBox chckbxExecuteOthers;
	
	private String rights = null;

	/**
	 * @param fr
	 */
	public RightsDialog(JFrame fr, String currentRights) {
		super ( fr, "Rights chooser", false );

		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		JPanel mainPanel = new JPanel();
		getContentPane().add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel checkBoxesPanel = new JPanel();
		mainPanel.add(checkBoxesPanel);
		checkBoxesPanel.setLayout(new BoxLayout(checkBoxesPanel, BoxLayout.Y_AXIS));

		JPanel panelOwner = new JPanel();
		checkBoxesPanel.add(panelOwner);
		panelOwner.setLayout(new BoxLayout(panelOwner, BoxLayout.Y_AXIS));

		JLabel lblOwnerRights = new JLabel("Owner rights:");
		lblOwnerRights.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panelOwner.add(lblOwnerRights);

		JPanel panel = new JPanel();
		panelOwner.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		chckbxReadOwner = new JCheckBox("read");
		chckbxReadOwner.addActionListener(this);
		panel.add(chckbxReadOwner);

		chckbxWriteOwner = new JCheckBox("write");
		chckbxWriteOwner.addActionListener(this);
		panel.add(chckbxWriteOwner);

		chckbxExecuteOwner = new JCheckBox("execute");
		chckbxExecuteOwner.addActionListener(this);
		panel.add(chckbxExecuteOwner);

		JPanel panelGroup = new JPanel();
		checkBoxesPanel.add(panelGroup);
		panelGroup.setLayout(new BoxLayout(panelGroup, BoxLayout.Y_AXIS));

		JLabel lblGroupRights = new JLabel("Group rights:");
		lblGroupRights.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panelGroup.add(lblGroupRights);

		JPanel panel_1 = new JPanel();
		panelGroup.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

		chckbxReadGroup = new JCheckBox("read");
		chckbxReadGroup.addActionListener(this);
		panel_1.add(chckbxReadGroup);

		chckbxWriteGroup = new JCheckBox("write");
		chckbxWriteGroup.addActionListener(this);
		panel_1.add(chckbxWriteGroup);

		chckbxExecuteGroup = new JCheckBox("execute");
		chckbxExecuteGroup.addActionListener(this);
		panel_1.add(chckbxExecuteGroup);

		JPanel panelOthers = new JPanel();
		checkBoxesPanel.add(panelOthers);
		panelOthers.setLayout(new BoxLayout(panelOthers, BoxLayout.Y_AXIS));

		JLabel lblOthersRights = new JLabel("Others rights:");
		lblOthersRights.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panelOthers.add(lblOthersRights);

		JPanel panel_2 = new JPanel();
		panelOthers.add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

		chckbxReadOthers = new JCheckBox("read");
		chckbxReadOthers.addActionListener(this);
		panel_2.add(chckbxReadOthers);

		chckbxWriteOthers = new JCheckBox("write");
		chckbxWriteOthers.addActionListener(this);
		panel_2.add(chckbxWriteOthers);

		chckbxExecuteOthers = new JCheckBox("execute");
		chckbxExecuteOthers.addActionListener(this);
		panel_2.add(chckbxExecuteOthers);

		JPanel textAreaPanel = new JPanel();
		mainPanel.add(textAreaPanel);
		textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.Y_AXIS));

		JLabel lblRights = new JLabel("Rights");
		lblRights.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblRights.setHorizontalAlignment(SwingConstants.CENTER);
		textAreaPanel.add(lblRights);

		JPanel panel_3 = new JPanel();
		textAreaPanel.add(panel_3);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		textField = new JTextField();
		panel_3.add(textField);
		textField.setColumns(10);
		textField.setText(currentRights);

		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				if (textField.getText().length() == 3) changeCheckboxes(textField.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				if (textField.getText().length() == 3) changeCheckboxes(textField.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				if (textField.getText().length() == 3) changeCheckboxes(textField.getText());
			}
		});

		JPanel buttonsPanel = new JPanel();
		mainPanel.add(buttonsPanel);

		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonsPanel.add(btnOk);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textField.setText("-1");
				setVisible(false);
			}
		});
		buttonsPanel.add(btnCancel);
		changeCheckboxes(currentRights);
		//rights = currentRights;
	}
	
	@Override
	public void setVisible(boolean b) {
		if (rights != null && b) textField.setText(rights);
		super.setVisible(b);
	}

	private void changeCheckboxes(String rights) {
		int owner = Integer.parseInt(rights.substring(0,1));
		int group = Integer.parseInt(rights.substring(1,2));
		int others = Integer.parseInt(rights.substring(2,3));
		setCheckboxes(owner, chckbxReadOwner, chckbxWriteOwner, chckbxExecuteOwner);
		setCheckboxes(group, chckbxReadGroup, chckbxWriteGroup, chckbxExecuteGroup);
		setCheckboxes(others, chckbxReadOthers, chckbxWriteOthers, chckbxExecuteOthers);

	}

	private void setCheckboxes(int right, JCheckBox cr, JCheckBox cw, JCheckBox cx) {
		switch (right) {
		case 0:
			cx.setSelected(false);
			cw.setSelected(false);
			cr.setSelected(false);
			break;
		case 1:
			cx.setSelected(true);
			cw.setSelected(false);
			cr.setSelected(false);
			break;
		case 2:
			cx.setSelected(false);
			cw.setSelected(true);
			cr.setSelected(false);
			break;
		case 3:
			cx.setSelected(true);
			cw.setSelected(true);
			cr.setSelected(false);
			break;
		case 4:
			cx.setSelected(false);
			cw.setSelected(false);
			cr.setSelected(true);
			break;
		case 5:
			cx.setSelected(true);
			cw.setSelected(false);
			cr.setSelected(true);
			break;
		case 6:
			cx.setSelected(false);
			cw.setSelected(true);
			cr.setSelected(true);
			break;
		case 7:
			cx.setSelected(true);
			cw.setSelected(true);
			cr.setSelected(true);
			break;
		}
	}
	
	private void changeRightInTextField(ActionEvent e) {
		String oldRights = textField.getText();
		JCheckBox c = (JCheckBox) e.getSource();

		int change = getChangeValue(c);
		
		int changePosition = getChangePosition(c);
		int owner = Integer.parseInt(oldRights.substring(0,1));
		int group = Integer.parseInt(oldRights.substring(1,2));
		int others = Integer.parseInt(oldRights.substring(2,3));
		if (changePosition==0) owner+=change;
		else if (changePosition==1) group+=change;
		else others+=change;
		String newRights = String.valueOf(owner) + String.valueOf(group) + String.valueOf(others);
		textField.setText(newRights);
	}
	
	private int getChangeValue(JCheckBox c) {
		int value;
		if (c.isSelected()) {
			if (c == chckbxExecuteGroup || c == chckbxExecuteOthers || c == chckbxExecuteOwner) value=1;
			else if (c == chckbxWriteGroup || c == chckbxWriteOthers || c == chckbxWriteOwner) value=2;
			else value=4;
		}
		else {
			if (c == chckbxExecuteGroup || c == chckbxExecuteOthers || c == chckbxExecuteOwner) value=-1;
			else if (c == chckbxWriteGroup || c == chckbxWriteOthers || c == chckbxWriteOwner) value=-2;
			else value=-4;
		}
		return value;
	}
	
	private int getChangePosition(JCheckBox c) {
		int changePosition;
		if (c == chckbxExecuteOwner || c == chckbxReadOwner || c == chckbxWriteOwner) changePosition=0;
		else if (c == chckbxExecuteGroup || c == chckbxReadGroup || c == chckbxWriteGroup) changePosition=1;
		else changePosition=2;
		return changePosition;
	}
	
	public String getRights() {
		return textField.getText();
	}
	
	public void actionPerformed(ActionEvent e) {
		changeRightInTextField(e);
	}
	
	public void setRights(String r) {
		rights = r;
	}
}
