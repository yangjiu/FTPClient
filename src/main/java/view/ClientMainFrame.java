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
 * Class in which is initialized main window of the client application
 * 
 * @author Jakub Fortunka
 *
 */
public class ClientMainFrame {

	/**
	 * Object of class that is responsible for contact with server
	 */
	private Client serverConnect = null;

	/**
	 * main frame
	 */
	private JFrame frmFtpClient;

	/**
	 * text field in which user is inserting login for server
	 */
	private JTextField textLogin;
	/**
	 * password field for password for the server
	 */
	private JPasswordField passwordField;
	/**
	 * text field for hostname of server
	 */
	private JTextField textHost;
	/**
	 * text area which works as System.out
	 */
	private JTextArea output;
	/**
	 * text field for inserting port number
	 */
	private JTextField textPort;

	/**
	 * popup menu for table which represents local files
	 */
	private LocalPopupMenu localPopupMenu;
	/**
	 * popup menu for table representing files on FTP Server
	 */
	private FTPPopupMenu ftpPopupMenu;

	/**
	 * JTable which is responsible for represnting local files
	 */
	private JTable localTable;

	/**
	 * JTable representing files from FTP Server
	 */
	private JTable ftpTable;
	/**
	 * Table model for localTable
	 */
	private FileTableModel fileTableModel;
	/**
	 * list selection listener for local table
	 */
	private ListSelectionListener listSelectionListener;
	/**
	 * true if columns for local table already have set sizes
	 */
	private boolean cellSizesSet = false;

	
	/**
	 * Table model for FTP table
	 */
	private FTPFileTableModel ftpFileTableModel;
	/**
	 * list selection listener for ftpTable {@link ListSelectionListener}
	 */
	private ListSelectionListener ftpSelectionListener;
	/**
	 * true if columns for ftp table have 
	 */
	private boolean ftpCellSizesSet = false;

	/**
	 * {@link File} which represents currently working directory
	 */
	private File fileRoot = null;
	/**
	 * list of {@link File}[] with all the files in the currently directory
	 */
	private File[] subItems = null;

	/**
	 * List of files in directory in which we are currently working
	 */
	private ArrayList<FTPFile> ftpFiles = null;
	/**
	 * Path of the directory in which we currently are on the FTP Sever
	 */
	private String ftpPath;

	/**
	 * Drag source for both tables
	 */
	private DragSource ds = new DragSource();

	/**
	 * true if source of drag is localTable
	 */
	private boolean dragLocal;

	/**
	 * Handler for {@link RuntimeException} from threads in {@link Connector}
	 */
	private Thread.UncaughtExceptionHandler handler;
	/**
	 * text field in which user can types commands
	 */
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
	 * Sets up listeners, exception handlers, and DragGestureListener
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
				new Thread() {
					@Override
					public void run() {
						try {
							if (!serverConnect.isConnected()) throw new IOException("You haven't connected yet!");
							serverConnect.disconnectFromServer();
							clearFTPFileList();
						} catch (IOException e) {
							throwException(e);
						}
					}
				}.start();
				
			}
		});
		menu.add(btnDisconnect);

		JLabel lblCommand = new JLabel("COMMAND");
		menu.add(lblCommand);

		commandField = new JTextField();
		commandField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread() {
					@Override
					public void run() {
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
				};
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

	/**
	 * sets {@link view.ClientMainFrame#dragLocal} to false
	 */
	private void clearDragState() {
		dragLocal=false;
	}

	/**
	 * Method that is responsible for appending text in our JTextArea
	 * 
	 * @param text whats will be appended in {@link view.ClientMainFrame#output}
	 */
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.append(text);
			}
		});
	}

	/**
	 * method that redirects System.out to {@link view.ClientMainFrame#output}
	 */
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

	/**
	 * methods that sets {@link view.ClientMainFrame#localTable} with read files from directory
	 * 
	 * @param files files which will be "made" into rows of JTable
	 */
	private void setTableData(final File[] files) {
		setDataInTable(files, null);
	}

	/**
	 * @param files ArrayList of {@link client.FTPFile} objects which represents files on FTP Server
	 */
	private void setFtpTableData(final ArrayList<FTPFile> files) {
		setDataInTable(null, files);
	}

	/**
	 * Method that sets data in chosen table
	 * 
	 * @param files if null, method is entering data for {@link view.ClientMainFrame#ftpTable}
	 * @param ftpFiles if null method is entering data for {@link view.ClientMainFrame#localTable}
	 */
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

	/**
	 * method that sets column width for chosen column
	 * 
	 * @param column number of column for which we want to set width
	 * @param width width to what we want resize column
	 * @param localeList true if we are sizing {@link view.ClientMainFrame#localTable}
	 */
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

	/**
	 * Method which is responsible for action when there is double click of mouse on {@link view.ClientMainFrame#localTable}
	 * 
	 * @param e {@link MouseEvent} object representing event that has place
	 */
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

	/**
	 * Method which is responsible for action when there is double click of mouse on {@link view.ClientMainFrame#ftpTable}
	 * 
	 * @param e {@link MouseEvent} object representing event that has place
	 */
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

	/**
	 * Method that is responsible for action when file (or directory) is dropped from ftpTable to localTable
	 * 
	 * @param rowOfLocalTable row of localTable at which file was dropped
	 * @throws IOException
	 */
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

	/**
	 * Method that is responsible for action when file (or directory) is dropped from localTable to ftpTable
	 * 
	 * @param rowOfFtpTable row of ftpTable at which file was dropped
	 * @throws IOException
	 */
	private void sendFileByDrop(int rowOfFtpTable) throws IOException {
		int row = localTable.getSelectedRow();
		String filename = (String) localTable.getValueAt(row, 1);
		File fileToServer = new File(fileRoot + File.separator + filename);
		sendToServer(fileToServer, fileToServer.isDirectory());
		refreshFTPDirectory();
	}

	/**
	 * method responsible for action when we want to download file/directory from server
	 * 
	 * @param fileToGet {@link File} object representing file to which we want to save file 
	 * @param filename name of file we want to get from server
	 * @param isDirectory true if we are getting directory
	 */
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

	/**
	 * method responsible for sending file to server (is using {@link Client} class)
	 * 
	 * @param fileToSend File we want to send
	 * @param isDirectory true if we are sending directory
	 */
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

	/**
	 * Method that creates file/directory on server (uses {@link Client} class)
	 * 
	 * @param name name of file/directory we want to create
	 * @param createDirectory true if we are creating directory
	 */
	public void createFileOrDirectoryOnServer(String name,boolean createDirectory) {
		try {
			if (createDirectory) serverConnect.createRemoteDirectory(name);
			else serverConnect.createRemoteFile(name);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	/**
	 * method which is responsible for removing file/directory from server
	 * 
	 * @param filename name of file/directory we want to remove
	 * @param isDirectory true if we want to remove directory
	 */
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

	/**
	 * methods that can change name of file/directory on the FTP Server
	 * 
	 * @param oldFilename old name of file
	 * @param newFilename name of file we want to change to
	 */
	public void changeNameOnServer(String oldFilename, String newFilename) {
		try {
			serverConnect.changeRemoteFilename(oldFilename, newFilename);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	/**
	 * method that is responsible for changing rights of file
	 * 
	 * @param filename name of file which rights we want to change
	 * @param rights to what rights we want to change
	 */
	public void changeRights(String filename, String rights) {
		try {
			serverConnect.changeRights(filename, rights);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}
	}

	/**
	 * method responsible for moving file on local computer
	 * 
	 * @param row row of localTable at which we dropped the file
	 */
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

	/**
	 * method responsible for moving file on FTP Server
	 * 
	 * @param row row of ftpTable at which we dropped the file
	 */
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

	/**
	 * method which makes sure program will execute the command user enters in {@link view.ClientMainFrame#commandField}
	 * 
	 * @param command line of command
	 * @throws IOException
	 */
	private void executeCommand(String command) throws IOException {
		if (command.endsWith(" ")) command = command.trim();
		serverConnect.sendCommand(command, fileRoot.getAbsolutePath());
	}

	/**
	 * method refresh localTable
	 */
	public void refreshCurrentDirectory() {
		subItems = fileRoot.listFiles();
		setTableData(subItems);
	}

	/**
	 * method refresh ftpTable
	 * 
	 * @throws IOException
	 */
	public void refreshFTPDirectory() throws IOException {
		ftpFiles = serverConnect.list();
		setFtpTableData(ftpFiles);
	}

	/**
	 * method that clears ftpTable (is executed after disconnection)
	 */
	public void clearFTPFileList() {
		ftpFiles = new ArrayList<FTPFile>();
		setFtpTableData(ftpFiles);
	}

	/**
	 * @return the localTable
	 */
	public JTable getLocalTable() {
		return localTable;
	}

	/**
	 * @return the frmFtpClient
	 */
	public JFrame getFrame() {
		return frmFtpClient;
	}

	/**
	 * @return the ftpTable
	 */
	public JTable getFTPTable() {
		return ftpTable;
	}

	/**
	 * @return the fileRoot
	 */
	public File getFileroot() {
		return fileRoot;
	}

	/**
	 * @return the ftpFiles
	 */
	public ArrayList<FTPFile> getFTPFiles() {
		return ftpFiles;
	}

	/**
	 * shows JOptionPane with exception message
	 * 
	 * @param e exception which message will be shown
	 */
	private void throwException(Exception e) {
		//	if (connection != null) connection.cancelNOOPDeamon();
		JOptionPane.showMessageDialog(frmFtpClient,
				e.getMessage(),
				"Problem!",
				JOptionPane.WARNING_MESSAGE);
		e.printStackTrace();
		return ;
	}
}
