/**
 * 
 */
package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.StringTokenizer;

import sun.misc.IOUtils;

/**
 * Class that is responsible for connection and functions working with FTP server
 * 
 * @author Jakub Fortunka
 *
 */
public class Connector {

	private Socket server = null;
	private PrintWriter write = null;
	private BufferedReader read = null;

	private Thread.UncaughtExceptionHandler handler;

	private Thread timer = null;
	private boolean killTimer = false;

	Socket dataSocket = null;

	BufferedInputStream input = null;
	BufferedOutputStream output = null;

	private boolean usingMyOwnSuperServer = false;

	/**
	 * @param add name of host
	 * @param port port at which server is working
	 * @throws UnknownHostException exception is thrown if passed hostname is invalid
	 * @throws IOException
	 */
	public Connector() throws UnknownHostException, IOException {
	}

	public Connector(Thread.UncaughtExceptionHandler h) throws UnknownHostException, IOException {
		handler = h;		
	}

	public synchronized void connectToServer(String host, int port, String user, String pass) throws UnknownHostException, IOException {
		startConnectingToServer(host, port);

		timer = newTimerThread();
		timer.start();

		sendUserCommand(user);
		sendPassCommand(pass);
	}

	public void startConnectingToServer(String host, int port) throws IOException {
		server = new Socket(host,port);
		write = new PrintWriter(server.getOutputStream(), true);
		read = new BufferedReader(new InputStreamReader(server.getInputStream()));
		String response = null;
		response = getAllResponses("220", read.readLine());
		if (!response.startsWith("220 ")) {
			throw new IOException("Unknown response from FTP Server: " + response);
		}
	}

	public void sendUserCommand(String user) throws IOException {
		sendLine("USER " + user);
		String response = getAllResponses("331", read.readLine());
		if (!response.startsWith("331 ")) {
			if (timer != null) cancelNOOPDeamon();
			throw new IOException("There is a problem with username:  "	+ response);
		}
	}

	public void sendPassCommand(String pass) throws IOException {
		sendLine("PASS " + pass);
		String response = getAllResponses("230", read.readLine());
		if (!response.startsWith("230 ")) {
			if (timer != null) cancelNOOPDeamon();
			throw new IOException("There is a problem with password: " + response);
		}
		if (timer==null) {
			timer = newTimerThread();
			timer.start();
		}
	}

	public synchronized void disconnect() throws IOException {
		try {
			if (!server.isConnected()) {
				throw new IOException("Can't disconnect from server - you haven't connected yet!");
			}
			sendLine("QUIT");
			getAllResponses("221", read.readLine());
		} finally {
			cancelNOOPDeamon();
			read.close();
			server.close();
		}
	}

	/**
	 * @return name of directory in which we are
	 * @throws IOException
	 */
	public synchronized String pwd() throws IOException {
		sendLine("PWD");
		String dir = null;
		String response = read.readLine();
		response = getAllResponses("257", response);
		if (response.startsWith("257 ")) {
			int firstQuote = response.indexOf('\"');
			int secondQuote = response.indexOf('\"', firstQuote + 1);
			if (secondQuote > 0) {
				dir = response.substring(firstQuote + 1, secondQuote);
			}
		}
		return dir;
	}

	/**
	 * Changes the working directory (like cd). Returns true if successful.
	 * 
	 * @param dir String in which is directorypath to which we want to go.
	 */
	public synchronized boolean cwd(String dir) throws IOException {
		sendLine("CWD " + dir);
		String response = read.readLine();
		response = getAllResponses("250", response);
		return (response.startsWith("250 "));
	}

	/**
	 * Lists current directory. Returns ArrayList of {@link FTPFile} in which is stored information about content of current directory.
	 * 
	 * @return ArrayList<{@link FTPFile}> in which are the information about files in the current directory on the server
	 * @throws IOException
	 */
	public synchronized ArrayList<FTPFile> list() throws IOException {
		//	Object[] o = pasv();
		//	String ip=(String)o[0];
		//	int port = (int)o[1];
		//	dataSocket = new Socket(ip,port);
		if (!pasv()) throw new IOException("Problem with PASV");
		return sendListLine();
	}

	public synchronized ArrayList<FTPFile> sendListLine() throws IOException {
		sendLine("LIST");
		BufferedReader readList = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
		String response = getAllResponses("150", read.readLine());
		String file = null;
		ArrayList<FTPFile> files = new ArrayList<FTPFile>();
		if (response.startsWith("150 ")) {
			while ((file=readList.readLine()) != null) {
				System.out.println(file);
				if (!(file.endsWith(".") && file.substring(file.lastIndexOf(".")-1, file.lastIndexOf(".")).equals(" "))) files.add(new FTPFile(file));
			}
			dataSocket.close();
		}
		else {
			dataSocket.close();
			throw new IOException("List Problem" + response);
		}
		response = read.readLine();
		response = getAllResponses("226", response);
		if (!response.startsWith("226 ")) {
			throw new IOException("List Problem" + response);
		}
		return files;
	}

	/**
	 * @param file file which will be sended to server
	 * @param isStor true if sending STOR; false if sending APPE
	 * @return true if everything went well
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public boolean sendStorCommand(File file, boolean isStor) throws IOException, FileNotFoundException {
		String filename = file.getName();
		if (isStor) sendLine("STOR " + filename);
		else sendLine("APPE " + filename);
		if (!CanMoveFile()) {
			dataSocket.close();
			throw new IOException("Can't send/download a file");
		}
		input = new BufferedInputStream(new FileInputStream(file));
		output = new BufferedOutputStream(dataSocket.getOutputStream());
		boolean status = moveFile();
		
	}

	/**
	 * @param file file in which will be saved file from server
	 * @param filename name of the file on server (which we want download)
	 * @return true if everything well as expected
	 * @throws IOException
	 */
	public boolean sendRetrCommand(File file, String filename) throws IOException {
		sendLine("RETR " + filename);
		if (!CanMoveFile()) {
			dataSocket.close();
			throw new IOException("Can't send/download a file");
		}
		input = new BufferedInputStream(dataSocket.getInputStream());
		output = new BufferedOutputStream(new FileOutputStream(file));
		return moveFile();
	}

	public boolean sendAppeCommand(File file) throws FileNotFoundException, IOException {
		return sendStorCommand(file, false);
	}

	/**
	 * @param file file we want to send to the server
	 * @return true if everything is fine (sending was completed)
	 * @throws IOException
	 */
	public synchronized boolean sendFile(File file) throws IOException {
		if (pasv()) return sendStorCommand(file, true);
		else return false;
	}

	/**
	 * @param file file at computer in which we want to save file from server
	 * @param filename full path to the file which we want to download from server
	 * @return true if everything is fine (sending was completed)
	 * @throws IOException
	 */
	public synchronized boolean getFile(File file, String filename) throws IOException {
		if (pasv()) return sendRetrCommand(file, filename);
		else return false;
	}

	private boolean moveFile() throws IOException {
		//TODO when sending, it deletes file from computer...
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		output.flush();
		output.close();
		input.close();
		dataSocket.close();
		return transferingFileCompleted();
	}

	private boolean CanMoveFile() throws IOException {
		String response = getAllResponses("150", read.readLine());
		if (!response.startsWith ("150 ")) return false;
		else return true;
	}

	private boolean transferingFileCompleted() throws IOException {
		String response = getAllResponses("226", read.readLine());
		if (response.startsWith("226 ")) return true;
		else return false;
	}

	public synchronized boolean sendDirectory(File directoryToSend) throws IOException {
		File[] list = directoryToSend.listFiles();
		ArrayList<File> directoriesList = new ArrayList<File>();
		String directoryName = directoryToSend.getName();
		boolean status = true;
		if (!cwd(directoryName)) {
			makeDirectory(directoryName);
			cwd(directoryName);
		}
		for (File f : list) {
			if (f.isFile()) sendFile(f);
			if (f.isDirectory()) directoriesList.add(f);
		}
		for (File directory : directoriesList) {
			status = sendDirectory(directory);
		}
		cwd("..");
		return status;
	}

	public synchronized boolean getDirectory(File localDirectory, String directorypath) throws IOException {
		if(!localDirectory.exists()) localDirectory.mkdir();
		boolean status = true;
		cwd(directorypath);
		String localFilepath = localDirectory.getAbsolutePath() + File.separator;
		ArrayList<FTPFile> list = list();
		ArrayList<FTPFile> directories = new ArrayList<FTPFile>();
		for (FTPFile f : list) {
			if (f.isDirectory()) directories.add(f);
			else {
				File newFile = new File(localFilepath + f.getFilename());
				status = getFile(newFile, f.getFilename());
			}
		}
		for (FTPFile d : directories) {
			if (!d.getFilename().equals("..")) getDirectory(new File(localFilepath + d.getFilename()), d.getFilename());
		}
		return status;
	}

	/**
	 * Enter binary mode for sending binary files.
	 */
	public synchronized boolean bin() throws IOException {
		sendLine("TYPE I");
		String response = read.readLine();
		response = getAllResponses("200", response);
		return (response.startsWith("200 "));
	}

	/**
	 * Enter ASCII mode for sending text files. This is usually the default
	 * mode. Make sure you use binary mode if you are sending images or
	 * other binary data, as ASCII mode is likely to corrupt them.
	 */
	public synchronized boolean ascii() throws IOException {
		sendLine("TYPE A");
		String response = read.readLine();
		response = getAllResponses("200", response);
		return (response.startsWith("200 "));
	}


	/**
	 * @return if dataSocket connected to server - true; else - false
	 * @throws IOException
	 */
	public synchronized boolean pasv() throws IOException {
		sendLine("PASV");
		String response = read.readLine();
		response = getAllResponses("227", response);
		if (!response.startsWith("227 ")) {
			throw new IOException("Can't access passive mode "
					+ response);
		}

		String ip = null;
		int port = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);
		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
						+ tokenizer.nextToken() + "." + tokenizer.nextToken();
				port = Integer.parseInt(tokenizer.nextToken()) * 256
						+ Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException("Received bad data link connection "
						+ response);
			}
		}
		dataSocket = new Socket(ip,port);
		if (dataSocket.isConnected()) return true;
		else return false;
		//	Object[] o = new Object[2];
		//	o[0]=ip;
		//	o[1]=port;
		//	return o;
	}

	public synchronized boolean makeDirectory(String dirpath) throws IOException {
		sendLine("MKD " + dirpath);
		String response = getAllResponses("257", read.readLine());
		if (!response.startsWith("257 ")) {
			throw new IOException("There is a problem with making directory" + response);
		}
		else return true;
	}

	public synchronized boolean removeDirectory(String dirname) throws IOException {
		cwd(dirname);
		ArrayList<FTPFile> list = list();
		ArrayList<FTPFile> directoriesList = new ArrayList<FTPFile>();
		for (FTPFile f : list) {
			if (f.isDirectory()) directoriesList.add(f);
			else removeFile(f.getFilename());
		}
		for (FTPFile directory : directoriesList) {
			if (!(directory.getFilename().equals(".."))) removeDirectory(directory.getFilename());
		}
		list = list();
		directoriesList.clear();
		for (FTPFile f : list) {
			if (f.isDirectory()) directoriesList.add(f);
			else removeFile(f.getFilename());
		}
		if (directoriesList.size()==1) {
			cwd("..");
			sendLine("RMD " + dirname);
			String response = getAllResponses("250", read.readLine());
			return response.startsWith("250 ");
		}
		return true;
	}

	public synchronized boolean removeFile(String filename) throws IOException {
		sendLine("DELE " + filename);
		String response = getAllResponses("250", read.readLine());
		if (!response.startsWith("250 ")) {
			throw new IOException("There is a problem with removing file" + response);
		}
		else return true;
	}

	public synchronized boolean makeFile(String filename) throws IOException {
		File f = new File(filename);
		f.createNewFile();
		boolean test = sendFile(f);
		f.delete();
		return test;
	}

	public synchronized boolean changeName(String oldFilename, String newFilename) throws IOException {
		sendLine("RNFR " + oldFilename);
		String response = getAllResponses("350", read.readLine());
		if (!response.startsWith("350 ")) {
			throw new IOException("There is a problem with renaming file " + response);
		}
		sendLine("RNTO " + newFilename);
		response = getAllResponses("250", read.readLine());
		if (!response.startsWith("250 ")) {
			throw new IOException("There is a problem with renaming file " + response);
		}
		return true;
	}

	public synchronized boolean changeRights(String filename, String rights) throws IOException {
		//TODO
		if (usingMyOwnSuperServer) sendLine("CHMOD " + filename + " " + rights);
		else sendLine("SITE CHMOD " + rights + " " + filename);
		String response = getAllResponses("200", read.readLine());
		if (!response.startsWith("200 ")) {
			throw new IOException("There is a problem with changing rights to file/directory " + response);
		}
		return true;
	}

	public synchronized boolean noop() throws IOException {
		sendLine("NOOP");
		if (getAllResponses("200 ", read.readLine()).startsWith("200 ")) return true;
		else return false;
	}

	public synchronized void abort() throws IOException {
		sendLine("ABOR");
		output.close();
		input.close();
		dataSocket.close();
	}

	/**
	 * Sends a raw command to the FTP server.
	 * 
	 * @param line line that will be send to server
	 * @throws IOException
	 */
	private synchronized void sendLine(String line) throws IOException {
		if (server == null || server.isClosed()) {
			throw new IOException("There is no connection to any FTP server or you've been inactive for too long. If so, please connect again");
		}
		write.write(line + "\r\n");
		write.flush();
		if (!line.startsWith("PASS ")) System.out.println(line);
		else System.out.println("PASS *****");
		if (!line.equals("NOOP")) resetNoopTimer();
	}

	private String getAllResponses(String code, String response) throws IOException {
		String r = response;
		while (r.startsWith(code + "-")) {
			System.out.println(r);
			System.out.flush();
			r=read.readLine();
		}
		System.out.println(r);
		System.out.flush();
		return r;
	}

	private synchronized Thread newTimerThread() {
		return new Thread("Timer-Thread") {
			@Override
			public void run() {
				try {
					sleep(1000*30);
					for (int i=0;i<5;i++) {
						if (killTimer) return ;
						noop();
						sleep(1000*30);
					}
					disconnect();
				} catch (InterruptedException e) {
					if (killTimer) return ;
					else {
						timer = newTimerThread();
						timer.setUncaughtExceptionHandler(handler);
						timer.start();
					}
				} catch (IOException e) {
					throw new RuntimeException("Server disconnected!");
				} catch (NullPointerException e) {
					throw new RuntimeException("Server disconnected!");
				}
			}
		};
	}
	private synchronized void resetNoopTimer() {
		timer.interrupt();
	}

	public synchronized void cancelNOOPDeamon() {
		killTimer=true;
	}

}
