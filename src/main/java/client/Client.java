package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

	/**
	 * object of {@see Connector} class, which is used to work with server.
	 */
	private Connector connection = null;
	/**
	 * Exception handler for timer Thread in {@link Connector}.
	 */
	private Thread.UncaughtExceptionHandler h = null;

	/**
	 * list of commands that can be used (more advanced version). Is directed to System.out by {@link Client.listOfAllAvailableCommands}.
	 */
	String[] commandsList = {"ftp - connect to server. Needed informations: hostname, username, password (port is optional)",
			"ls - list files",
			"bye, quit - disconnects from server",
			"help - list all commands",
			"get - download file from server to current directory; Syntax: get filename",
			"put - upload file to server. Syntax: put filename",
			"mkdir - make directory on server. Syntax: mkdir dirname",
			"pwd - get working directory",
	"rmdir - delete directory from server. Syntax: rmdir dirname"};

	/**
	 * Constructor
	 * 
	 * @param h Exception handler for timer Thread
	 */
	public Client(Thread.UncaughtExceptionHandler h) {
		this.h = h;
	}

	/**
	 * Method connects to the server using Connector class, to which we are passing credentials
	 * 
	 * @param username users login
	 * @param password users password
	 * @param hostname hostname
	 * @param port server port number
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connectToServer(String username, String password, String hostname, int port)
			throws UnknownHostException, IOException {
		connection = new Connector(h);
		connection.connectToServer(hostname, port, username, password);
	}

	/**
	 * Disconnects from server
	 * 
	 * @throws IOException
	 */
	public void disconnectFromServer() throws IOException {
		connection.disconnect();
	}

	/**
	 * Method gets currently working directory
	 * 
	 * @return currently working directory
	 * @throws IOException
	 */
	public String pwd() throws IOException {
		return connection.pwd();
	}

	/**
	 * Gets list of files in current directory
	 * 
	 * @return list of {@link FTPFile} which represents files on server 
	 * @throws IOException
	 */
	public ArrayList<FTPFile> list() throws IOException {
		return connection.list();
	}

	/**
	 * Changes working directory on server to want we want
	 * 
	 * @param directory name of directory to which we want to go (can be full path)
	 * @return true if everything went well; false otherwise
	 * @throws IOException
	 */
	public boolean cwd(String directory) throws IOException {
		return connection.cwd(directory);
	}

	/**
	 * Sends file to the server
	 * 
	 * @param fileToServer File which we want to send
	 * @throws IOException
	 */
	public void sendFile(File fileToServer) throws IOException {
		boolean sendCompleted = connection.sendFile(fileToServer);
		if (!sendCompleted) {
			throw new IOException("There was a problem with sending your file");
		}
	}

	/**
	 * Sends directory to the server
	 * 
	 * @param directoryToSend directory which we want to send
	 * @throws IOException
	 */
	public void sendDirectory(File directoryToSend) throws IOException {
		boolean sendCompleted = connection.sendDirectory(directoryToSend);
		if (!sendCompleted) {
			throw new IOException("There was a problem with sending your directory");
		}
	}

	/**
	 * Retrieves file from the server
	 * 
	 * @param fileToComputer File object which represents file we want to get on local computer
	 * @param remotePath name (or full path) of the file we want to get
	 * @throws IOException
	 */
	public void getFile(File fileToComputer, String remotePath) throws IOException {
		boolean getCompleted = connection.getFile(fileToComputer, remotePath);
		if (!getCompleted) {
			throw new IOException("There was problem with downloading your file");
		}
	}

	/**
	 * Retrieves directory from the server
	 * 
	 * @param directoryToGet File object which represents directory we want to get on local computer
	 * @param remoteDirectoryname name (of full path) of the directory we want to get
	 * @throws IOException
	 */
	public void getDirectory(File directoryToGet, String remoteDirectoryname) throws IOException {
		directoryToGet.mkdirs();
		boolean getCompleted = connection.getDirectory(directoryToGet, remoteDirectoryname);
		if (!getCompleted) {
			throw new IOException("There was problem with downloading your directory");
		}
	}

	/**
	 * Creates empty directory on the server
	 * 
	 * @param name name of the directory we want to create
	 * @throws IOException
	 */
	public void createRemoteDirectory(String name) throws IOException {
		boolean getCompleted = connection.makeDirectory(name);
		if (!getCompleted) {
			throw new IOException("There was a problem with making a directory");
		}
	}

	/**
	 * Creates empty file on the server
	 * 
	 * @param filename name of the file we want to create
	 * @throws IOException
	 */
	public void createRemoteFile(String filename) throws IOException {
		boolean getCompleted;
		getCompleted = connection.makeFile(filename);
		if (!getCompleted) {
			throw new IOException("There was a problem with making a file");
		}
	}

	/**
	 * Removes file (or directory) from the server
	 * 
	 * @param name name of the file (or directory) we want to remove from server
	 * @param isDirectory true if we want to delete directory; false otherwise
	 * @throws IOException
	 */
	public void deleteRemoteFile(String name, boolean isDirectory) throws IOException {
		if (isDirectory) connection.removeDirectory(name);
		else connection.removeFile(name);
	}

	/**
	 * Changes name of the file
	 * 
	 * @param oldFilename old name of file
	 * @param newFilename new name of file
	 * @throws IOException
	 */
	public void changeRemoteFilename(String oldFilename, String newFilename) throws IOException {
		connection.changeName(oldFilename, newFilename);
	}

	/**
	 * Changes rights of the file
	 * 
	 * @param filename name (or full path) of the file
	 * @param rights rights to which we want change (must be numeric like 777 or 644 etc.)
	 * @throws IOException
	 */
	public void changeRights(String filename, String rights) throws IOException {
		connection.changeRights(filename, rights);
	}

	/**
	 * Executes passed command
	 * 
	 * @param command command line
	 * @param localWorkingPath currently working local path (is used in sending/geting files)
	 * @throws IOException
	 */
	public void sendCommand(String command, String localWorkingPath) throws IOException {
		String com = command;
		if (command.contains(" ")) com = formatInsertedCommand(command);
		String valueAfterCommand = null;
		//TODO
		if (command.length() > 4) valueAfterCommand = command.substring(command.indexOf(" ") + 1);
		switch(com) {
		case "USER" :
			connection.sendUserCommand(valueAfterCommand);
			break;
		case "PASS" :
			connection.sendPassCommand(valueAfterCommand);
			break;
		case "QUIT" :
			connection.disconnect();
			break;
		case "NOOP" :
			connection.noop();
			break;
		case "PASV" :
			connection.pasv();
			break;
		case "STOR" :
			File storFile = new File(localWorkingPath + File.separator + valueAfterCommand);
			connection.sendStorCommand(storFile, true);
			break;
		case "RETR" :
			File retrFile = new File(localWorkingPath + File.separator + valueAfterCommand);
			connection.sendRetrCommand(retrFile, valueAfterCommand);
			break;
		case "APPE" :
			File appeFile = new File(localWorkingPath + File.separator + valueAfterCommand);
			connection.sendStorCommand(appeFile, false);
			break;
		case "ABOR" :
			connection.abort();
			break;
		case "DELE" :
			connection.removeFile(valueAfterCommand);
			break;
		case "RMD" :
			connection.removeDirectory(valueAfterCommand);
			break;
		case "MKD" :
			connection.makeDirectory(valueAfterCommand);
			break;
		case "PWD" :
			connection.pwd();
			break;
		case "LIST" :
			connection.sendListLine();
			break;
		case "CWD" :
			connection.cwd(valueAfterCommand);
			break;
		case "CHMOD" :
			String filename = valueAfterCommand.substring(0,valueAfterCommand.indexOf(" "));
			String rights = valueAfterCommand.substring(valueAfterCommand.indexOf(" "));
			connection.changeRights(filename, rights);
			break;
		case "get" :
			File getFile = new File (localWorkingPath + File.separator + valueAfterCommand);
			connection.getFile(getFile, valueAfterCommand);
			break;
		case "put" :
			File putFile = new File(localWorkingPath + File.separator + valueAfterCommand);
			connection.sendFile(putFile);
			break;
		case "ls" :
			connection.list();
			break;
			/*case "user" :
			//TODO
			break;*/
		case "quit" :
		case "bye" :
			connection.disconnect();
			break;
		case "help" :
			listOfAllAvailableCommands();
			break;
		case "mkdir" :
			connection.makeDirectory(valueAfterCommand);
			break;
		case "pwd" :
			connection.pwd();
			break;
		case "rmdir" :
			connection.removeDirectory(valueAfterCommand);
			break;
		case "cd" :
			connection.cwd(valueAfterCommand);
			connection.list();
			break;
		default :
			System.out.println("I don't know that command. Write help for commands list");
		}
	}

	/**
	 * connects with server with passed credentials. Is used to simulate ftp command
	 * 
	 * @param host hostname
	 * @param port port of the server
	 * @param username username
	 * @param password password
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void ftpCommand(String host, int port, String username, String password) throws UnknownHostException, IOException {
		if (port == -1) port = 21;
		connectToServer(username, password, host, port);
	}

	/**
	 * gets command name from raw command line
	 * 
	 * @param comm command line
	 * @return name of command 
	 */
	private String formatInsertedCommand(String comm) {
		return comm.substring(0,comm.indexOf(" "));
	}

	/**
	 * prints on System.out all commands that can be used
	 */
	private void listOfAllAvailableCommands() {
		for (String s : commandsList) {
			System.out.println(s);
		}
	}

	/**
	 * Sets field connection to null
	 */
	public void setConnectionToNull() {
		this.connection = null;
	}

	/**
	 * Checks if we are connected to the FTP server
	 * 
	 * @return true if we are connected to the server; false otherwise
	 */
	public boolean isConnected() {
		if (connection == null) return false;
		else return true;
	}
}
