/**
 * 
 */
package view;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Jakub Fortunka
 *
 */
public class LocalPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8410567121993033604L;
	
	private JMenuItem mntmSendFile;
	private JMenuItem mntmNewDirectory;
	private JMenuItem mntmDelete;
	private JMenuItem mntmChangeName;
	private JMenuItem mntmMakeFile;	
	
	private ClientMainFrame view=null;
	
	/**
	 * 
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
	
	
	private void send() throws IOException {
		int row = view.getLocalTable().getSelectedRow();
		String filename = (String) view.getLocalTable().getValueAt(row, 1);
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File fileToSend = new File(path + filename);
		if (fileToSend.isFile()) view.sendFile(fileToSend);
		else if (fileToSend.isDirectory()) view.sendDirectory(fileToSend);
	}
	
	private void makeFile() throws IOException {
		mk(false);
		//System.out.println("Test");
		
	}
	
	private void makeDirectory() throws IOException {
		mk(true);
	}
	
	private void mk(boolean createDirectory) throws IOException {
		String filename = JOptionPane.showInputDialog("Enter a filename");
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File newfile = new File(path + filename);
		if (!createDirectory) newfile.createNewFile();
		else newfile.mkdir();
		view.refreshCurrentDirectory();
	}
	
	private void deleteFile() throws IOException {
		int row = view.getLocalTable().getSelectedRow();
		String filename = (String) view.getLocalTable().getValueAt(row, 1);
		String path = view.getFileroot().getAbsolutePath() + File.separator;
		File fileToDelete = new File(path + filename);
		view.deleteLocalFile(fileToDelete);
		view.refreshCurrentDirectory();
	}
	
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
		view.refreshCurrentDirectory();
		
	}
	
	

	private void throwException(Exception e) {
		JOptionPane.showMessageDialog(view.getFrame(),
				e.getMessage(),
				"Problem!",
				JOptionPane.WARNING_MESSAGE);
		return ;
	}
}
