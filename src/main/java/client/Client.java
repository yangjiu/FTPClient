package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

	private Connector connection = null;
	private Thread.UncaughtExceptionHandler h = null;

	String[] commandsList = {"ftp - connect to server. Needed informations: hostname, username, password (port is optional)",
			"ls - list files",
			"bye, quit - disconnects from server",
			"help - list all commands",
			"get - download file from server to current directory; Syntax: get filename",
			"put - upload file to server. Syntax: put filename",
			"mkdir - make directory on server. Syntax: mkdir dirname",
			"pwd - get working directory",
			"rmdir - delete directory from server. Syntax: rmdir dirname"};

	public Client(Thread.UncaughtExceptionHandler h) {
		this.h = h;
	}

	public void connectToServer(String username, String password, String hostname, int port)
			throws UnknownHostException, IOException {
		connection = new Connector(h);
		connection.connectToServer(hostname, port, username, password);
	}

	public void disconnectFromServer() throws IOException {
		connection.disconnect();
	}

	public String pwd() throws IOException {
		return connection.pwd();
	}

	public ArrayList<FTPFile> list() throws IOException {
		return connection.list();
	}

	public boolean cwd(String directory) throws IOException {
		return connection.cwd(directory);
	}

	public void sendFile(File fileToServer) throws IOException {
		boolean sendCompleted = connection.sendFile(fileToServer);
		if (!sendCompleted) {
			throw new IOException("There was a problem with sending your file");
		}
	}

	public void sendDirectory(File directoryToSend) throws IOException {
		boolean sendCompleted = connection.sendDirectory(directoryToSend);
		if (!sendCompleted) {
			throw new IOException("There was a problem with sending your directory");
		}
	}

	public void getFile(File fileToComputer, String remotePath) throws IOException {

		boolean getCompleted = connection.getFile(fileToComputer, remotePath);
		if (!getCompleted) {
			throw new IOException("There was problem with downloading your file");
		}
	}

	public void getDirectory(File directoryToGet, String remoteDirectoryname) throws IOException {

		boolean getCompleted = connection.getDirectory(directoryToGet, remoteDirectoryname);
		if (!getCompleted) {
			throw new IOException("There was problem with downloading your directory");
		}
	}

	public void createRemoteDirectory(String name) throws IOException {

		boolean getCompleted = connection.makeDirectory(name);
		if (!getCompleted) {
			throw new IOException("There was a problem with making a directory");
		}
		//		refreshFTPDirectory();

	}

	public void createRemoteFile(String filename) throws IOException {
		boolean getCompleted;
		getCompleted = connection.makeFile(filename);
		if (!getCompleted) {
			throw new IOException("There was a problem with making a file");
		}
		//			refreshFTPDirectory();

	}

	public void deleteRemoteFile(String name, boolean isDirectory)
			throws IOException {
		if (isDirectory) connection.removeDirectory(name);
		else connection.removeFile(name);
		//		refreshFTPDirectory();

	}

	public void changeRemoteFilename(String oldFilename, String newFilename) throws IOException {
		connection.changeName(oldFilename, newFilename);
		//	refreshFTPDirectory();

	}

	public void changeRights(String filename, String rights) throws IOException {
		connection.changeRights(filename, rights);
		//		refreshFTPDirectory();
	}

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
			/*case "ftp" :
			if (valueAfterCommand.contains(" ")) {
				int port = Integer.parseInt(valueAfterCommand.substring(valueAfterCommand.indexOf(" ") + 1));
				String host = valueAfterCommand.substring(0,valueAfterCommand.indexOf(" "));
				connection.startConnectingToServer(host, port);
			}
			else {
				connection.startConnectingToServer(valueAfterCommand, 21);
			}
			break;*/
		default :
			System.out.println("I don't know that command. Write help for commands list");
		}
	}

	public void ftpCommand(String host, int port, String username, String password) throws UnknownHostException, IOException {
		if (port == -1) port = 21;
		connectToServer(username, password, host, port);
	}

	private String formatInsertedCommand(String comm) {
		return comm.substring(0,comm.indexOf(" "));
	}

	private void listOfAllAvailableCommands() {
		for (String s : commandsList) {
			System.out.println(s);
		}
	}
	
	public void setConnectionToNull() {
		this.connection = null;
	}

	public boolean isConnected() {
		if (connection == null) return false;
		else return true;
	}
}
