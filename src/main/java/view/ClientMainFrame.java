/**
 * 
 */
package view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;

import client.*;

import javax.swing.JFrame;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.JTable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;

/**
 * @author Jakub Fortunka
 *
 */
public class ClientMainFrame {

	private Client serverConnect = null;

	private JFrame frmFtpClient;

	private JTextField textLogin;
	private JPasswordField passwordField;
	private JTextField textHost;
	private JTextArea output;
	private JTextField textPort;

	private LocalPopupMenu localPopupMenu;
	private FTPPopupMenu ftpPopupMenu;

	private JTable localTable;

	private JTable ftpTable;

	/** Table model for File[]. */
	private FileTableModel fileTableModel;
	private ListSelectionListener listSelectionListener;
	private boolean cellSizesSet = false;

	/* Table model for FTPFile[] */
	private FTPFileTableModel ftpFileTableModel;
	private ListSelectionListener ftpSelectionListener;
	private boolean ftpCellSizesSet = false;

	private File fileRoot = null;
	private File[] subItems = null;

	private ArrayList<FTPFile> ftpFiles = null;
	private String ftpPath;

	private DragSource ds = new DragSource();

	private boolean dragLocal;

	private Thread.UncaughtExceptionHandler handler;
	private JTextField commandField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMainFrame window = new ClientMainFrame();
					window.frmFtpClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientMainFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFtpClient = new JFrame();
		frmFtpClient.setTitle("FTP Client");
		frmFtpClient.setBounds(100, 100, 766, 550);
		frmFtpClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFtpClient.getContentPane().setLayout(new BorderLayout(4, 4));

		handler = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				//System.out.println(th + " Handler works: " + ex);
				JOptionPane.showMessageDialog(frmFtpClient,
						ex.getMessage(),
						"Problem!",
						JOptionPane.WARNING_MESSAGE);
				clearFTPFileList();
				serverConnect.setConnectionToNull();
				return ;
			}
		};

		serverConnect = new Client(handler);


		JPanel menu = new JPanel();
		frmFtpClient.getContentPane().add(menu, BorderLayout.NORTH);

		JLabel lblLogin = new JLabel("Login");
		menu.add(lblLogin);

		textLogin = new JTextField();
		textLogin.setText("a4417886");
		textLogin.setToolTipText("");
		menu.add(textLogin);
		textLogin.setColumns(10);

		JLabel lblHaslo = new JLabel("Haslo");
		menu.add(lblHaslo);

		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		menu.add(passwordField);

		JLabel lblHost = new JLabel("host");
		menu.add(lblHost);

		textHost = new JTextField();
		textHost.setText("fortunka.hostoi.com");
		textHost.setToolTipText("");
		menu.add(textHost);
		textHost.setColumns(10);

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread() {
					@Override
					public void run() {
						try {
							serverConnect.connectToServer(textLogin.getText(), String.copyValueOf(passwordField.getPassword()), textHost.getText(), Integer.parseInt(textPort.getText()));
							ftpPath = serverConnect.pwd();
							ftpFiles = serverConnect.list();
							setFtpTableData(ftpFiles);
						} catch (NumberFormatException | IOException e) {
							throwException(e);
						}
					}
				}.start();
				/*try {
					serverConnect.connectToServer(textLogin.getText(), String.copyValueOf(passwordField.getPassword()), textHost.getText(), Integer.parseInt(textPort.getText()));
					ftpPath = serverConnect.pwd();
					ftpFiles = serverConnect.list();
					setFtpTableData(ftpFiles);
				} catch (NumberFormatException | IOException e) {
					throwException(e);
				}*/
			}
		});

		JLabel lblportLabel = new JLabel("Port");
		menu.add(lblportLabel);

		textPort = new JTextField();
		textPort.setText("21");
		menu.add(textPort);
		textPort.setColumns(10);
		menu.add(btnConnect);
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!serverConnect.isConnected()) throw new IOException("You haven't connected yet!");
					serverConnect.disconnectFromServer();
					clearFTPFileList();
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		menu.add(btnDisconnect);

		JLabel lblCommand = new JLabel("COMMAND");
		menu.add(lblCommand);

		commandField = new JTextField();
		commandField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String command = commandField.getText();
					if (command.startsWith("ftp")) {
						int port = -1;
						if (!textPort.getText().isEmpty()) port = Integer.parseInt(textPort.getText());
						serverConnect.ftpCommand(textHost.getText(), port, textLogin.getText(), String.copyValueOf(passwordField.getPassword()));
					}
					else executeCommand(commandField.getText());
					commandField.setText("");
				} catch (IOException e) {
					throwException(e);
				}
			}
		});
		menu.add(commandField);
		commandField.setColumns(15);

		/* Local Table */

		JPanel mainPanel = new JPanel();
		frmFtpClient.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		JPanel localView = new JPanel();
		mainPanel.add(localView);
		localView.setLayout(new BorderLayout(0, 0));

		localTable = new JTable();
		localTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					localDoubleClick(e);
				}
			}
		});



		localTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		localTable.setAutoCreateRowSorter(true);
		localTable.setShowVerticalLines(false);
		localTable.getSelectionModel().addListSelectionListener(listSelectionListener);

		JScrollPane localTableScroll = new JScrollPane(localTable);
		localView.add(localTableScroll);

		localPopupMenu = new LocalPopupMenu(this);

		localPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (localPopupMenu.isMouseOnTable()) {
					Point point = localTable.getMousePosition();
					int currentRow = localTable.rowAtPoint(point);
					localTable.setRowSelectionInterval(currentRow, currentRow);
				}
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		localTableScroll.setComponentPopupMenu(localPopupMenu);
		localTable.setComponentPopupMenu(localPopupMenu);
		JPanel FTPView = new JPanel();
		mainPanel.add(FTPView);
		FTPView.setLayout(new BorderLayout(0,0));

		localTable.setDropTarget(new DropTarget(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6822838109327672265L;

			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				Point point = localTable.getMousePosition();
				int row = localTable.rowAtPoint(point);
				// Have to be row-1, because i'm adding ".." at the beginning of table
				if (row==0) throwException(new IOException("Can't move file to that folder!"));
				else {
					if (!dragLocal) {
						try {
							getFileByDrop(row);
						} catch (IOException e1) {
							throwException(e1);
						}
					}
					else {
						moveLocalFile(row);
					}
				}
				clearDragState();
				super.drop(e);
			}
		});

		localTableScroll.setDropTarget(new DropTarget(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6620072049922656420L;

			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				if (!dragLocal) {
					try {
						getFileByDrop(-1);
					} catch (IOException e1) {
						throwException(e1);
					}
				}
				clearDragState();
				super.drop(e);
			}
		});

		localTable.setDragEnabled(true);

		/* FTP Table */

		ftpTable = new JTable();
		ftpTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					try {
						ftpDoubleClick(e);
					} catch (IOException e1) {
						throwException(e1);
					}
				}
			}
		});
		ftpTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ftpTable.setAutoCreateRowSorter(true);
		ftpTable.setShowVerticalLines(false);
		ftpTable.getSelectionModel().addListSelectionListener(ftpSelectionListener);


		JScrollPane ftpTableScroll = new JScrollPane(ftpTable);
		FTPView.add(ftpTableScroll);

		ftpPopupMenu = new FTPPopupMenu(this);
		ftpTableScroll.setComponentPopupMenu(ftpPopupMenu);
		ftpTable.setComponentPopupMenu(ftpPopupMenu);

		ftpPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (ftpPopupMenu.isMouseOnTable()) {
					Point point = ftpTable.getMousePosition();
					int currentRow = ftpTable.rowAtPoint(point);
					ftpTable.setRowSelectionInterval(currentRow, currentRow);
				}
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		fileRoot = new File(System.getProperty("user.dir"));
		//fileRoot = new File("C:/");
		subItems = fileRoot.listFiles();
		setTableData(subItems);

		//ftpTable.setDragEnabled(true);

		listSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				localTable.getSelectionModel().getLeadSelectionIndex();
			}
		};

		ftpSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ftpTable.getSelectionModel().getLeadSelectionIndex();
			}
		};

		ftpTable.setDropTarget(new DropTarget() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6884829170676684030L;

			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				Point point = ftpTable.getMousePosition();
				int row = ftpTable.rowAtPoint(point);
				if (dragLocal) {
					try {
						sendFileByDrop(row);
					} catch (IOException e1) {
						throwException(e1);
					}
				}
				else {
					moveServerFile(row);
					clearDragState();
				}
			}});

		ftpTableScroll.setDropTarget(new DropTarget() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1358091544559844033L;

			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				if (dragLocal) {
					try {
						sendFileByDrop(-1);
					} catch (IOException e1) {
						throwException(e1);
					}
				}
				clearDragState();
				super.drop(e);
			}
		});

		ftpTable.setDragEnabled(true);

		/*Text Area*/
		JTextArea textArea = new JTextArea();
		output = textArea;

		textArea.setRows(10);

		JScrollPane scrollPane = new JScrollPane(output);
		frmFtpClient.getContentPane().add(scrollPane, BorderLayout.SOUTH);

		redirectSystemStreams();	

		frmFtpClient.setExtendedState(Frame.MAXIMIZED_BOTH); 

		ds.createDefaultDragGestureRecognizer(localTable, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
				dragLocal=true;
			} 
		});
	}

	private void clearDragState() {
		dragLocal=false;
	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	/** Update the table on the EDT */
	private void setTableData(final File[] files) {
		setDataInTable(files, null);
	}

	private void setFtpTableData(final ArrayList<FTPFile> files) {
		setDataInTable(null, files);
	}

	private void setDataInTable(final File[] files, final ArrayList<FTPFile> ftpFiles) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (ftpFiles == null) {
					if (fileTableModel==null) {
						fileTableModel = new FileTableModel();
						localTable.setModel(fileTableModel);
					}
					localTable.getSelectionModel().removeListSelectionListener(listSelectionListener);
					fileTableModel.setFiles(files);
					localTable.getSelectionModel().addListSelectionListener(listSelectionListener);
					if (!cellSizesSet) {
						setColumnWidth(0,-1,true);
						setColumnWidth(4,-1, true);
						setColumnWidth(5,-1, true);
						setColumnWidth(6,-1, true);
						setColumnWidth(7,-1, true);
						setColumnWidth(8,-1, true);
						cellSizesSet = true;
					}
				}
				else {
					if (ftpFileTableModel==null) {
						ftpFileTableModel = new FTPFileTableModel();
						ftpTable.setModel(ftpFileTableModel);
					}
					ftpTable.getSelectionModel().removeListSelectionListener(ftpSelectionListener);
					ftpFileTableModel.setFiles(ftpFiles);
					localTable.getSelectionModel().addListSelectionListener(ftpSelectionListener);
					if (!ftpCellSizesSet) {
						setColumnWidth(0,-1,false);
						ftpCellSizesSet = true;
					}
				}
			}
		});
	}

	private void setColumnWidth(int column, int width, boolean localeList) {
		TableColumn tableColumn = null;
		if (localeList) tableColumn = localTable.getColumnModel().getColumn(column);
		else tableColumn = ftpTable.getColumnModel().getColumn(column);
		if (width<0) {
			// use the preferred width of the header..
			JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
			Dimension preferred = label.getPreferredSize();
			// altered 10->14 as per camickr comment.
			width = (int)preferred.getWidth()+14;
		}
		tableColumn.setPreferredWidth(width);
		tableColumn.setMaxWidth(width);
		tableColumn.setMinWidth(width);
	}

	private void localDoubleClick(MouseEvent e) {
		JTable target = (JTable)e.getSource();
		int row = target.getSelectedRow();
		String dirName = (String) localTable.getValueAt(row, 1);
		if (dirName.equals("..")) {
			File[] f = File.listRoots();
			int a = fileRoot.getAbsolutePath().lastIndexOf(File.separator);
			String path = fileRoot.getAbsolutePath().substring(0, a);
			if (path.endsWith(":")) path+=File.separator;
			boolean root=false;
			for (int i=0;i<f.length;i++) {
				if (f[i].getAbsolutePath().equals(path)) root=true;
			}
			if (root) {
				fileRoot = new File(path);
				setTableData(f);
			}
			else {
				fileRoot=new File(path);
				subItems = fileRoot.listFiles();
				setTableData(subItems);
			}
		}
		else {
			String path = null;
			if (dirName.contains(":")) path = dirName.substring(dirName.lastIndexOf(":")-1,dirName.lastIndexOf(":")+1) + File.separator;
			else path = fileRoot.getAbsolutePath() + File.separator + dirName;
			fileRoot=new File(path);
			subItems = fileRoot.listFiles();
			setTableData(subItems);
		}
	}

	private void ftpDoubleClick(MouseEvent e) throws IOException {
		JTable target = (JTable)e.getSource();
		int row = target.getSelectedRow();
		final String dirName = (String) ftpTable.getValueAt(row, 1);
		new Thread() {
			@Override
			public void run() {
				try {
					ftpPath = serverConnect.pwd();
					if (dirName.equals("..")) {
						if (ftpPath.equals("/")) {
							ftpFiles = serverConnect.list();
							setFtpTableData(ftpFiles);
						}
						else {
							if (ftpPath.lastIndexOf("/") == 0) {
								serverConnect.cwd("/");
								ftpFiles=serverConnect.list();
								setFtpTableData(ftpFiles);
							}
							else {
								ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
								serverConnect.cwd(ftpPath);
								ftpFiles = serverConnect.list();
								setFtpTableData(ftpFiles);
							}
						}
					}
					else {
						if (ftpPath.equals("/")) ftpPath = ftpPath + dirName;
						else ftpPath = ftpPath + "/" + dirName;
						serverConnect.cwd(ftpPath);
						ftpFiles = serverConnect.list();
						setFtpTableData(ftpFiles);	
					}
				} catch(IOException e) {
					throwException(e);
				}
			}
		}.start();
	}

	private void getFileByDrop(int rowOfLocalTable) throws IOException {
		int row = ftpTable.getSelectedRow();
		File fileToGet = null;
		String filename = (String) ftpTable.getValueAt(row, 1);
		if (rowOfLocalTable != -1) {
			String fileDragOn =(String) localTable.getValueAt(rowOfLocalTable, 1);
			File maybeDir = new File(fileDragOn);
			if (maybeDir.isDirectory()) fileToGet = new File(fileRoot + File.separator + fileDragOn + File.separator + filename);
		}
		if (fileToGet == null) fileToGet = new File(fileRoot + File.separator + filename);
		getFileOrDirectory(fileToGet, filename, ftpFiles.get(row).isDirectory());
		refreshCurrentDirectory();
	}

	private void sendFileByDrop(int rowOfFtpTable) throws IOException {
		int row = localTable.getSelectedRow();
		String filename = (String) localTable.getValueAt(row, 1);
		File fileToServer = new File(fileRoot + File.separator + filename);
		sendToServer(fileToServer, fileToServer.isDirectory());
		refreshFTPDirectory();
	}

	public void getFileOrDirectory(final File fileToGet, final String filename,final boolean isDirectory) {
		new Thread() {
			@Override
			public void run() {
				try {
					if (isDirectory) serverConnect.getDirectory(fileToGet,filename);
					else serverConnect.getFile(fileToGet, filename);
					refreshCurrentDirectory();
				} catch (IOException e) {
					try {
						serverConnect.sendCommand("ABOR", fileRoot.getAbsolutePath());
					} catch (IOException e1) {
						throwException(e1);
					}
					throwException(e);
				}
			}
		}.start();
	}

	public void sendToServer(final File fileToSend, final boolean isDirectory) {
		new Thread() {
			@Override
			public void run() {
				try {
					if (isDirectory) serverConnect.sendDirectory(fileToSend);
					else serverConnect.sendFile(fileToSend);
					refreshFTPDirectory();
				} catch (IOException e) {
					throwException(e);
				}
			}
		}.start();
	}

	public void createFileOrDirectoryOnServer(String name,boolean createDirectory) {
		try {
			if (createDirectory) serverConnect.createRemoteDirectory(name);
			else serverConnect.createRemoteFile(name);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	public void deleteFileOrDirectoryFromServer(final String filename, final boolean isDirectory) {
		new Thread() {
			@Override
			public void run() {
				try {
					serverConnect.deleteRemoteFile(filename, isDirectory);
					refreshFTPDirectory();
				} catch (IOException e) {
					throwException(e);
				}
			}
		}.start();
	}

	public void changeNameOnServer(String oldFilename, String newFilename) {
		try {
			serverConnect.changeRemoteFilename(oldFilename, newFilename);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	public void changeRights(String filename, String rights) {
		try {
			serverConnect.changeRights(filename, rights);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}
	}

	private void moveLocalFile(int row) {
		String filename = (String) localTable.getValueAt(localTable.getSelectedRow(), 1);
		String dragOn = (String) localTable.getValueAt(row, 1);
		if (filename.equals(dragOn)) return ;
		File maybeDir = new File(fileRoot + File.separator + dragOn);
		if (maybeDir.isDirectory()) {
			Path oldPath = Paths.get(fileRoot + File.separator + filename);
			Path newPath = Paths.get(fileRoot + File.separator + localTable.getValueAt(row, 1) + File.separator + filename);
			try {
				Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				throwException(e1);
			}
			refreshCurrentDirectory();
		}
	}

	private void moveServerFile(int row) {
		String filename = (String) ftpTable.getValueAt(ftpTable.getSelectedRow(), 1);
		String maybeDir = (String)ftpTable.getValueAt(row, 1);
		if (filename.equals(maybeDir)) return ;
		boolean isDirectory = false;
		for (FTPFile f : ftpFiles) {
			if (f.getFilename().equals(maybeDir) && f.isDirectory()) isDirectory=true;
		}
		if (isDirectory) {
			try {
				serverConnect.changeRemoteFilename(filename, maybeDir + "/" + filename);
				refreshFTPDirectory();
			} catch (IOException e1) {
				throwException(e1);
			}
		}
	}

	private void executeCommand(String command) throws IOException {
		if (command.endsWith(" ")) command = command.trim();
		serverConnect.sendCommand(command, fileRoot.getAbsolutePath());
	}

	public void refreshCurrentDirectory() {
		subItems = fileRoot.listFiles();
		setTableData(subItems);
	}

	public void refreshFTPDirectory() throws IOException {
		ftpFiles = serverConnect.list();
		setFtpTableData(ftpFiles);
	}

	public void clearFTPFileList() {
		ftpFiles = new ArrayList<FTPFile>();
		setFtpTableData(ftpFiles);
	}

	public JTable getLocalTable() {
		return localTable;
	}

	public JFrame getFrame() {
		return frmFtpClient;
	}

	public File getFileroot() {
		return fileRoot;
	}

	public JTable getFTPTable() {
		return ftpTable;
	}

	public ArrayList<FTPFile> getFTPFiles() {
		return ftpFiles;
	}

	private void throwException(Exception e) {
		//	if (connection != null) connection.cancelNOOPDeamon();
		JOptionPane.showMessageDialog(frmFtpClient,
				e.getMessage(),
				"Problem!",
				JOptionPane.WARNING_MESSAGE);
		return ;
	}
}
