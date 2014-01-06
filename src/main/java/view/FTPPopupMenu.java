/**
 * 
 */
package view;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import client.FTPFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Jakub Fortunka
 *
 */
public class FTPPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7831472688804082458L;
	
	private JMenuItem mntmGetFile;
	private JMenuItem mntmMakeFile;
	private JMenuItem mntmMakeDirectory;
	private JMenuItem mntmDelete;
	private JMenuItem mntmChangeName;
	private JMenuItem mntmChangeRights;
	
	RightsDialog dialog = null ;
	
	private ClientMainFrame view=null;
	
	/**
	 * @param view
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
	
	private void setMenuItems(boolean isClickable) {
		mntmChangeName.setEnabled(isClickable);
		mntmDelete.setEnabled(isClickable);
		mntmGetFile.setEnabled(isClickable);
		mntmChangeRights.setEnabled(isClickable);
	}
	
	private void getFile() throws IOException {
		if (view.getConnector() == null) {
			throw new IOException("First you must connect to the FTP Server!");
		}
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
		if (!isDirectory) view.getFile(fileToGet, filename);
		else view.getDirectory(fileToGet, filename);
	}
	
	
	private void makeFile() {
		mk(false);
		//System.out.println("Test");
		
	}
	
	private void makeDirectory() {
		mk(true);
	}
	
	private void mk(boolean createDirectory) {
		String filename = JOptionPane.showInputDialog("Enter a name");
		//String path = view.getFileroot().getAbsolutePath() + File.separator;
		if (createDirectory) view.createRemoteDirectory(filename);
		else view.createRemoteFile(filename);
	}
	
	private void delete() {
		int row = view.getFTPTable().getSelectedRow();
		String filename = (String) view.getFTPTable().getValueAt(row, 1);
		view.deleteRemoteFile(filename,row);
	}
	
	private void changeName() {
		String newFilename = JOptionPane.showInputDialog("Enter a name");
		int row = view.getFTPTable().getSelectedRow();
		String oldFilename = (String) view.getFTPTable().getValueAt(row, 1);
		String extension = "";
		if (oldFilename.contains(".")) extension = oldFilename.substring(oldFilename.lastIndexOf("."));
		newFilename+=extension;
		view.changeRemoteName(oldFilename, newFilename);
	}
	
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
	
	private void throwException(Exception e) {
		JOptionPane.showMessageDialog(view.getFrame(),
				e.getMessage(),
				"Problem!",
				JOptionPane.WARNING_MESSAGE);
		return ;
	}

}
