package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

	private Connector connection = null;

	public Client() {

	}

	public void connectToServer(String username, String password, String hostname, int port)
			throws UnknownHostException, IOException {
		Connector c = new Connector(hostname, port, username, password);
		connection = c;
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

	public boolean isConnected() {
		if (connection == null) return false;
		else return true;
	}
}
