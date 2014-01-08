/**
 * 
 */
package view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.MouseInfo;
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
import javax.swing.SwingWorker;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
import java.beans.ExceptionListener;

import javax.swing.BoxLayout;
import javax.xml.ws.handler.MessageContext;

/**
 * @author Jakub Fortunka
 *
 */
public class ClientMainFrame {

	private Connector connection = null;
	private JFrame frmFtpClient;
	private JTextField textLogin;
	private JPasswordField passwordField;
	private JTextField textHost;
	private JTextArea output;

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
	private JTextField textPort;

	private DragSource ds = new DragSource();

	private boolean dragLocal = false;
	private boolean dragFtp = false;

	//private Thread.UncaughtExceptionHandler h;

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
				connect();
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
					if (connection == null) throw new IOException("You haven't connected yet!");
					connection.disconnect();
					clearFTPFileList();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frmFtpClient,
							e.getMessage(),
							"Unknown Host!",
							JOptionPane.WARNING_MESSAGE);
					return ;
				}
			}
		});
		menu.add(btnDisconnect);

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
			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				//Point point = e.getLocation();
				//	Point overallPoint = e.getLocation();
				Point point = localTable.getMousePosition();
				int row = localTable.rowAtPoint(point);
				//System.out.println(localTable.getValueAt(localRow, 1));
				// Have to be row-1, because i'm adding ".." at the beginning of table
				if (row==0) throwException(new IOException("Can't move file to that folder!"));
				else {
					if (dragFtp) {
						getFileFromDrop(row);
					}
					else {
						// sprawdzić czy przenosimy do folderu!
						String filename = (String) localTable.getValueAt(localTable.getSelectedRow(), 1);
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
				clearDragState();
				super.drop(e);
			}
		});

		localTableScroll.setDropTarget(new DropTarget(){
			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				if (dragFtp) {
					getFileFromDrop(-1);
				}
				clearDragState();
				super.drop(e);
			}
		});

		localTable.setDragEnabled(true);

		//localTable.getdr


		//localTable.setDragEnabled(true);


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
			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				// handle drop outside current table (e.g. add row)
				if (dragLocal) {
					sendFileFromDrop();
				}
				else {
					//użyć changeRemoteFilename
					
				}
				clearDragState();
			}
		});

		ftpTableScroll.setDropTarget(new DropTarget() {
			@Override
			public synchronized void drop(DropTargetDropEvent e) {
				if (dragLocal) {
					sendFileFromDrop();
				}
				clearDragState();
				super.drop(e);
			}
		});

		ftpTable.setDragEnabled(true);

		/*Text Area*/
		JTextArea textArea = new JTextArea();
		//frmFtpClient.getContentPane().add(textArea, BorderLayout.SOUTH);
		output = textArea;

		textArea.setRows(10);

		JScrollPane scrollPane = new JScrollPane(output);
		frmFtpClient.getContentPane().add(scrollPane, BorderLayout.SOUTH);

		redirectSystemStreams();	

		frmFtpClient.setExtendedState(Frame.MAXIMIZED_BOTH); 

		/*h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
			}
		};*/


		/* try {
	    	PipedInputStream outPipe = new PipedInputStream();
	    	//PipedInputStream errOut = new PipedInputStream();
			System.setOut(new PrintStream(new PipedOutputStream(outPipe), true));
			//System.setErr(new PrintStream(new PipedOutputStream(outPipe), true));
			redirectStream(outPipe);
			//output.setRows(10);

		} catch (IOException e1) {
			redirectSystemStreams();
			e1.printStackTrace();

		}*/

		DragGestureRecognizer locaDraggin = ds.createDefaultDragGestureRecognizer(localTable, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
				dragLocal=true;
			} 
		});
		DragGestureRecognizer ftpDragging = ds.createDefaultDragGestureRecognizer(ftpTable, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
				dragFtp=true;
			}
		});
	}

	private void clearDragState() {
		dragLocal=false;
		dragFtp=false;
	}

	private void connect() {
		try {
			Connector c = new Connector(textHost.getText(), Integer.parseInt(textPort.getText()), textLogin.getText(), String.copyValueOf(passwordField.getPassword()));
			connection = c;
			//connection.connectToServer(textHost.getText(), Integer.parseInt(textPort.getText()), textLogin.getText(), String.copyValueOf(passwordField.getPassword()));
			ftpPath = connection.pwd();
			ftpFiles = connection.list();
			setFtpTableData(ftpFiles);
		} catch (UnknownHostException e) {
			throwException(e);
			//e.printStackTrace();
		} catch (IOException e) {
			throwException(e);
			return ;
		}
	}

	/*public void redirectStream (final InputStream out) {

	   // final JTextArea area = new JTextArea();



	    // handle "System.out"
	    new SwingWorker<Void, String>() {
	        @Override protected Void doInBackground() throws Exception {
	            Scanner s = new Scanner(out);
	            while (s.hasNextLine()) //publish(s.nextLine() + "\n");
	            	output.append(s.nextLine());
	            return null;
	        }
	        @Override protected void process(List<String> chunks) {
	            for (String line : chunks) output.append(line);
	        }
	    }.execute();

	  //  return area;
	}*/

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.append(text);
				//output.
				//output.setCaretPosition(output.getDocument().getLength());

				//output.repaint();
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
				//output.append(String.valueOf((char) b));
				//output.setCaretPosition(output.getDocument().getLength());
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
				//output.append(new String(b, off, len));
				//output.setCaretPosition(output.getDocument().getLength());
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		//PrintStream p = new PrintStream(out, true);
		//System.setOut(p);
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	/** Update the table on the EDT */
	private void setTableData(final File[] files) {
		/*File newFile = new File("..");
		File[] test = new File[files.length + 1];
		test[0]=newFile;
		int i=1;
		for (File f : files) {
			test[i] = f;
			i++;
		}*/
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
		String dirName = (String) ftpTable.getValueAt(row, 1);
		ftpPath = connection.pwd();
		if (dirName.equals("..")) {
			if (ftpPath.equals("/")) {
				ftpFiles = connection.list();
				setFtpTableData(ftpFiles);
			}
			else {
				if (ftpPath.lastIndexOf("/") == 0) {
					connection.cwd("/");
					ftpFiles=connection.list();
					setFtpTableData(ftpFiles);
				}
				else {
					ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
					connection.cwd(ftpPath);
					ftpFiles = connection.list();
					setFtpTableData(ftpFiles);
				}
			}
		}
		else {
			if (ftpPath.equals("/")) ftpPath = ftpPath + dirName;
			else ftpPath = ftpPath + "/" + dirName;
			connection.cwd(ftpPath);
			ftpFiles = connection.list();
			setFtpTableData(ftpFiles);	
		}
	}

	public void sendFile(File fileToServer) {
		try {
			boolean sendCompleted = connection.sendFile(fileToServer);
			if (!sendCompleted) {
				throw new IOException("There was a problem with sending your file");
			}
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	public void sendDirectory(File directoryToSend) {
		try {
			boolean sendCompleted = connection.sendDirectory(directoryToSend);
			if (!sendCompleted) {
				throw new IOException("There was a problem with sending your directory");
			}
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	public void getFile(File fileToComputer, String remotePath) {
		try {
			boolean getCompleted = connection.getFile(fileToComputer, remotePath);
			if (!getCompleted) {
				throw new IOException("There was problem with downloading your file");
			}
			refreshCurrentDirectory();
		} catch (IOException e) {
			throwException(e);
		}		
	}

	public void getDirectory(File directoryToGet, String remoteDirectoryname) {
		try {
			boolean getCompleted = connection.getDirectory(directoryToGet, remoteDirectoryname);
			if (!getCompleted) {
				throw new IOException("There was problem with downloading your directory");
			}
			refreshCurrentDirectory();
		} catch(IOException e) {
			throwException(e);
		}
	}

	public void deleteLocalFile(File fileToDelete) {
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

	public void createRemoteDirectory(String name) {
		try {
			boolean getCompleted = connection.makeDirectory(name);
			if (!getCompleted) {
				throw new IOException("There was a problem with making a directory");
			}
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}

	}

	public void createRemoteFile(String filename) {
		boolean getCompleted;
		try {
			getCompleted = connection.makeFile(filename);
			if (!getCompleted) {
				throw new IOException("There was a problem with making a file");
			}
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}		
	}

	public void deleteRemoteFile(String name, int fileIndex) {
		try {
			if (ftpFiles.get(fileIndex).isDirectory()) connection.removeDirectory(name);
			else connection.removeFile(name);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}
	}

	public void changeRemoteFilename(String oldFilename, String newFilename) {
		try {
			connection.changeName(oldFilename, newFilename);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}
	}

	public void changeRights(String filename, String rights) {
		try {
			connection.changeRights(filename, rights);
			refreshFTPDirectory();
		} catch (IOException e) {
			throwException(e);
		}
	}
	
	private void getFileFromDrop(int rowOfLocalTable) {
		int row = ftpTable.getSelectedRow();
		File fileToGet = null;
		String filename = (String) ftpTable.getValueAt(row, 1);
		if (rowOfLocalTable != -1) {
			String fileDragOn =(String) localTable.getValueAt(rowOfLocalTable, 1);
			File maybeDir = new File(fileDragOn);
			if (maybeDir.isDirectory()) 
				fileToGet = new File(fileRoot + File.separator + fileDragOn + File.separator + filename);
		}
		if (fileToGet == null) fileToGet = new File(fileRoot + File.separator + filename);
		if (ftpFiles.get(row).isDirectory()) getDirectory(fileToGet, filename);
		else getFile(fileToGet, filename);
	}
	
	private void sendFileFromDrop() {
		int row = localTable.getSelectedRow();
		String filename = (String) localTable.getValueAt(row, 1);
		File fileToServer = new File(fileRoot + File.separator + filename);
		if (fileToServer.isDirectory()) sendDirectory(fileToServer);
		else sendFile(fileToServer);
	}

	public void refreshCurrentDirectory() {
		subItems = fileRoot.listFiles();
		setTableData(subItems);
	}

	public void refreshFTPDirectory() throws IOException {
		ftpFiles = connection.list();
		setFtpTableData(ftpFiles);
	}

	public void clearFTPFileList() {
		ftpFiles = new ArrayList<FTPFile>();
		setFtpTableData(ftpFiles);
	}

	public Connector getConnector() {
		return connection;
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
