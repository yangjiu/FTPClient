/**
 * 
 */
package view;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;


/**
 * Class for fancy PopupMenu showed when right mouse button is clicked on JTable representing local files
 * 
 * @author Jakub Fortunka
 *
 */
public class LocalPopupMenu extends JPopupMenu {

	/**
	 * needed for eventual serialization
	 */
	private static final long serialVersionUID = -8410567121993033604L;
	
	/**
	 * elements of PopupMenu
	 */
	private JMenuItem mntmSendFile,mntmNewDirectory,mntmDelete,mntmChangeName,mntmMakeFile;
	
	/**
	 * main class of gui
	 */
	private ClientMainFrame view=null;
	
	/**
	 * Initialize popupMenu
	 */	
	public LocalPopupMenu(ClientMainFrame view) {
		this.view = view;
		//frame = this.view.getFrame();
		mntmSendFile = new JMenuItem("Send");
		mntmSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					send();
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		add(mntmSendFile);
		
		mntmMakeFile = new JMenuItem("Make File");
		mntmMakeFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					makeFile();
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		add(mntmMakeFile);
		
		mntmNewDirectory = new JMenuItem("Make Directory");
		mntmNewDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					makeDirectory();
				} catch (IOException e1) {
					throwException(e1);
				}
			}
		});
		add(mntmNewDirectory);
		mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					deleteFile();
				} catch (IOException e1) {
					throwException(e1);
				}
			}
		});
		add(mntmDelete);
		
		mntmChangeName = new JMenuItem("Change name");
		mntmChangeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					changeName();
				} catch (IOException e1) {
					throwException(e1);
				}
			}
		});
		add(mntmChangeName);
		//this.c=view.getConnector();
		//this.table=this.view.getLocalTable();
		//this.fileRoot = this.view.getFileroot();
		
	}
	
	/** 
	 * is needed to make some of menu items unable to click when it is needed (mouse clicked on JScrollPane)
	 * 
	 * @see javax.swing.JPopupMenu#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (this.getInvoker() instanceof JScrollPane) {
			mntmChangeName.setEnabled(false);
			mntmDelete.setEnabled(false);
			mntmSendFile.setEnabled(false);
		}
		else if (view.getLocalTable().getValueAt(view.getLocalTable().getSelectedRow(), 1).equals("..")) {
			mntmChangeName.setEnabled(false);
			mntmDelete.setEnabled(false);
			mntmSendFile.setEnabled(false);
		}
		else {
			mntmChangeName.setEnabled(true);
			mntmDelete.setEnabled(true);
			mntmSendFile.setEnabled(true);
		}			
	}
	
	public void setVisible2(boolean b, int x, int y) {
		super.setLocation(x, y);
		this.setVisible(b);
	}
	
	/**
	 * Excutes method in main class which is responsible for sending file to server, to which this method passes currently chosen file/directory
	 * 
	 * @throws IOException
	 */
	private void send() throws IOException {
		int row = view.getLocalTable().getSelectedRow();
		String filename = (String) view.getLocalTable().getValueAt(row, 1);
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File fileToSend = new File(path + filename);
		boolean isDirectory = false;
		if (fileToSend.isDirectory()) isDirectory = true;
		view.sendToServer(fileToSend, isDirectory);
	}
	
	/**
	 * makes local file
	 * 
	 * @throws IOException
	 */
	private void makeFile() throws IOException {
		mk(false);		
	}
	
	/**
	 * makes local directory
	 * 
	 * @throws IOException
	 */
	private void makeDirectory() throws IOException {
		mk(true);
	}
	
	/**
	 * manages of making file/directory on local machine. Shows InputDialog for name.
	 * 
	 * @param createDirectory
	 * @throws IOException
	 */
	private void mk(boolean createDirectory) throws IOException {
		String filename = JOptionPane.showInputDialog("Enter a filename");
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File newfile = new File(path + filename);
		if (!createDirectory) newfile.createNewFile();
		else newfile.mkdir();
		view.refreshCurrentDirectory();
	}
	
	/**
	 * deletes file from local machine. Executes {@link view.LocalPopupMenu#deleteLocalFile(File)}.
	 * 
	 * @throws IOException
	 */
	private void deleteFile() throws IOException {
		int row = view.getLocalTable().getSelectedRow();
		String filename = (String) view.getLocalTable().getValueAt(row, 1);
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File fileToDelete = new File(path + filename);
		deleteLocalFile(fileToDelete);
		view.refreshCurrentDirectory();
	}
	
	/**
	 * Method for deleting local file/directory.
	 * 
	 * @param fileToDelete
	 */
	private void deleteLocalFile(File fileToDelete) {
		if (fileToDelete.isDirectory()) {
			if (fileToDelete.list().length==0) fileToDelete.delete();
			else {
				File[] files = fileToDelete.listFiles();
				for (File f : files) {
					if (f.isDirectory()) deleteLocalFile(f);
					else f.delete();
				}
				if (fileToDelete.list().length==0) fileToDelete.delete();
			}
		}
		else fileToDelete.delete();
	}
	
	/**
	 * changes name of local file/directory
	 * 
	 * @throws IOException
	 */
	private void changeName() throws IOException {
		String newFilename = JOptionPane.showInputDialog("Enter a new filename: ");
		int row = view.getLocalTable().getSelectedRow();
		String oldFilename = (String) view.getLocalTable().getValueAt(row, 1);
		String extension=null;
		if (oldFilename.contains(".")) extension = oldFilename.substring(oldFilename.indexOf("."),oldFilename.length());
		if (extension != null) newFilename+=extension;
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File f = new File (path + oldFilename);
		if (!f.renameTo(new File(path + newFilename))) {
			throw new IOException("Can't rename this file (i don't know why...)");
		}		
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
	 * Checks if mouse is on JTable when this PopupMenu will be showed
	 * 
	 * @return true if this PopupMenu will be shown on JTable; false otherwise
	 */
	public boolean isMouseOnTable() {
		if (this.getInvoker() instanceof JTable) return true;
		else return false;
	}
}
