/**
 * 
 */
package view;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import client.FTPFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * PopupMenu for ftpTable in main frame
 * 
 * @author Jakub Fortunka
 *
 */
public class FTPPopupMenu extends JPopupMenu {

	/**
	 * needed for eventual serialization
	 */
	private static final long serialVersionUID = -7831472688804082458L;

	/**
	 * Elements of Popup Menu
	 */
	private JMenuItem mntmGetFile,mntmMakeFile,mntmMakeDirectory,mntmDelete,mntmChangeName,mntmChangeRights;

	/**
	 * class containing dialog window with rights choose, when this option is choosen.
	 */
	RightsDialog dialog = null ;

	/**
	 * Main Class of GUI
	 */
	private ClientMainFrame view=null;

	/**
	 * @param view main class of GUI
	 */
	public FTPPopupMenu(ClientMainFrame view) {
		this.view = view;
		mntmGetFile = new JMenuItem("Get File");
		mntmGetFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					getFile();
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		add(mntmGetFile);

		mntmMakeFile = new JMenuItem("Make File");
		mntmMakeFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				makeFile();
			}
		});
		add(mntmMakeFile);

		mntmMakeDirectory = new JMenuItem("Make Directory");
		mntmMakeDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				makeDirectory();
			}
		});
		add(mntmMakeDirectory);

		mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				delete();
			}
		});
		add(mntmDelete);

		mntmChangeName = new JMenuItem("Change name");
		mntmChangeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				changeName();
			}
		});
		add(mntmChangeName);

		mntmChangeRights = new JMenuItem("Change rights");
		mntmChangeRights.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					changeRigths();
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		add(mntmChangeRights);
	}

	/** 
	 * This method is needed for one thing - when popup menu is invoked from JScrollPane, it has to make unable to click some options.
	 * 
	 * @see javax.swing.JPopupMenu#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (this.getInvoker() instanceof JScrollPane) {
			setMenuItems(false);
		}
		else if (view.getFTPTable().getValueAt(view.getFTPTable().getSelectedRow(), 1).equals("..")) {
			setMenuItems(false);
		}
		else {
			setMenuItems(true);
		}			
	}

	/**
	 * method that turns of (if needed) some options from PopupMenu
	 * 
	 * @param isClickable true if options should be clickable
	 */
	private void setMenuItems(boolean isClickable) {
		mntmChangeName.setEnabled(isClickable);
		mntmDelete.setEnabled(isClickable);
		mntmGetFile.setEnabled(isClickable);
		mntmChangeRights.setEnabled(isClickable);
	}

	/**
	 * method which gets file (or directory) that is choosen from server
	 * 
	 * @throws IOException
	 */
	private void getFile() throws IOException {
		int row = view.getFTPTable().getSelectedRow();
		String filename = (String) view.getFTPTable().getValueAt(row, 1);
		boolean isDirectory = false;
		for (FTPFile f : view.getFTPFiles()) {
			if (f.getFilename().equals(filename)) {
				if (f.isDirectory()) isDirectory = true;
			}
		}
		String localPath = view.getFileroot().getAbsolutePath() + File.separator + filename;
		File fileToGet = new File(localPath);
		view.getFileOrDirectory(fileToGet, filename, isDirectory);
	}


	/**
	 * makes file on server
	 */
	private void makeFile() {
		mk(false);
		//System.out.println("Test");

	}

	/**
	 * makes directory on server
	 */
	private void makeDirectory() {
		mk(true);
	}

	/**
	 * manages of making file/directory on server. Shows InputDialog for name.
	 * 
	 * @param createDirectory
	 */
	private void mk(boolean createDirectory) {
		String filename = JOptionPane.showInputDialog("Enter a name");
		view.createFileOrDirectoryOnServer(filename, createDirectory);
	}

	/**
	 * Uses method from {@link view.FTPPopupMenu#view} to delete file/directory from server
	 */
	private void delete() {
		int row = view.getFTPTable().getSelectedRow();
		String filename = (String) view.getFTPTable().getValueAt(row, 1);
		boolean isDirectory = false;
		for (FTPFile f : view.getFTPFiles()) {
			if (f.getFilename().equals(filename)) if (f.isDirectory()) isDirectory=true;
		}
		if (isDirectory) isDirectory=true;
		else isDirectory=false;
		view.deleteFileOrDirectoryFromServer(filename, isDirectory);
	}

	/**
	 * Executes method from main Class to change name of file on server
	 */
	private void changeName() {
		String newFilename = JOptionPane.showInputDialog("Enter a name");
		int row = view.getFTPTable().getSelectedRow();
		String oldFilename = (String) view.getFTPTable().getValueAt(row, 1);
		String extension = "";
		if (oldFilename.contains(".")) extension = oldFilename.substring(oldFilename.lastIndexOf("."));
		newFilename+=extension;
		view.changeNameOnServer(oldFilename, newFilename);
	}

	/**
	 * Executes method from main class to change rights of choosen file/directory
	 * 
	 * @throws IOException
	 */
	private void changeRigths() throws IOException {
		int row = view.getFTPTable().getSelectedRow();
		FTPFile f = view.getFTPFiles().get(row);
		String currentRights = String.valueOf(f.getUserRights()) + String.valueOf(f.getGroupRights()) + String.valueOf(f.getOthersRights());
		if (dialog == null) {
			dialog = new RightsDialog(view.getFrame(), currentRights);
		}
		dialog.setRights(currentRights);
		dialog.pack();
		dialog.setSize(dialog.getPreferredSize().width+100, dialog.getPreferredSize().height);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		String rights = dialog.getRights();
		if (!rights.equals("-1")) view.changeRights(f.getFilename(), rights);
	}

	/**
	 * shows messageDialog with exception message
	 * 
	 * @param e
	 */
	private void throwException(Exception e) {
		JOptionPane.showMessageDialog(view.getFrame(),
				e.getMessage(),
				"Problem!",
				JOptionPane.WARNING_MESSAGE);
		return ;
	}

	/**
	 * checks if mouse, when popupMenu is showed was on JTable or JScrollPane
	 * 
	 * @return true if mouse is on JTable; false otherwise
	 */
	public boolean isMouseOnTable() {
		if (this.getInvoker() instanceof JTable) return true;
		else return false;
	}

}
