/**
 * 
 */
package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

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

	private Timer timer = null;
	private TimerTask task = null;

	/**
	 * @param add name of host
	 * @param port port at which server is working
	 * @throws UnknownHostException exception is thrown if passed hostname is invalid
	 * @throws IOException
	 */
	public Connector(String hostName, int port) throws UnknownHostException, IOException {
		connectToServer(hostName, port);
	}

	public Connector(String hostName, int port, String user, String pass) throws UnknownHostException, IOException {
		connectToServer(hostName, port, user, pass);
	}

	private synchronized void connectToServer(String host, int port) throws UnknownHostException, IOException {
		connectToServer(host,port,"anonymous","anonymous");
	}

	private synchronized void connectToServer(String host, int port, String user, String pass) throws UnknownHostException, IOException {
		server = new Socket(host,port);
		write = new PrintWriter(server.getOutputStream(), true);
		read = new BufferedReader(new InputStreamReader(server.getInputStream()));

		timer = new Timer();
		task = createNewTimerTask();
		timer.schedule(task, 60*1000);
		
		//server.setSoTimeout(1000*2);
		
		String response = read.readLine();
		response = getAllResponses("220", response);

		//System.out.println(read.readLine());

		if (!response.startsWith("220 ")) {
			cancelNOOPDeamon();
			throw new IOException(
					"Unknown response from FTP Server: "
							+ response);
		}

		sendLine("USER " + user);
		response = read.readLine();
		response = getAllResponses("331", response);
		//System.out.println(response);

		if (!response.startsWith("331 ")) {
			cancelNOOPDeamon();
			throw new IOException(
					"There is a problem with username:  "
							+ response);
		}
		//System.out.println("PASS " + pass);
		sendLine("PASS " + pass);
		response = read.readLine();
		response = getAllResponses("230", response);
		//System.out.println(response);

		if (!response.startsWith("230 ")) {
			cancelNOOPDeamon();
			throw new IOException("There is a problem with password: " + response);
		}
	}

	public synchronized void disconnect() throws IOException {
		try {
			if (!server.isConnected()) {
				cancelNOOPDeamon();
				throw new IOException("Can't disconnect from server - you haven't connected yet!");
			}
			sendLine("QUIT");
			getAllResponses("221", read.readLine());
			
			//System.out.println(read.readLine());
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
		Object[] o = pasv();
		String ip=(String)o[0];
		int port = (int)o[1];
		//System.out.println(ip + " " + port);
		Socket listConnection = new Socket(ip,port);
		BufferedReader readList = new BufferedReader(new InputStreamReader(listConnection.getInputStream()));
		sendLine("LIST");
		String response = read.readLine();
		response = getAllResponses("150", response);
		String file = null;
		ArrayList<FTPFile> files = new ArrayList<FTPFile>();
		if (response.startsWith("150 ")) {
			//String test = readList.readLine();
			while ((file=readList.readLine()) != null) {
				System.out.println(file);
				if (!(file.endsWith(".") && file.substring(file.lastIndexOf(".")-1, file.lastIndexOf(".")).equals(" "))) files.add(new FTPFile(file));
			}
			listConnection.close();
		}
		else {
			listConnection.close();
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
	 * Sends a raw command to the FTP server.
	 * 
	 * @param line line that will be send to server
	 * @throws IOException
	 */
	private void sendLine(String line) throws IOException {
		if (server == null || server.isClosed()) {
			throw new IOException("There is no connection to any FTP server or you've been inactive for too long. If so, please connect again");
		}
		write.write(line + "\r\n");
		write.flush();
		if (!line.startsWith("PASS ")) System.out.println(line);
		/*
		 * Marnotrawienie zasobów na parê krzy¿yków...
		 * else {
			String[] pass = line.split(" ");
			String passCoded = "";
			for (int i=0;i<pass[1].length();i++) passCoded+="*";
			System.out.println("PASS " + passCoded);
		}*/
		else System.out.println("PASS *****");
		if (!line.equals("NOOP")) resetNoopTimer();
	}

	/**
	 * @param file file we want to send to the server
	 * @return true if everything is fine (sending was completed)
	 * @throws IOException
	 */
	public synchronized boolean sendFile(File file) throws IOException {
		if (file.isDirectory()) {
			throw new IOException("SimpleFTP cannot upload a directory.");
		}
		String filename = file.getName();
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream output = null;
		return moveFile(input,output, filename);
	}

	/**
	 * @param file file at computer in which we want to save file from server
	 * @param filename full path to the file which we want to download from server
	 * @return true if everything is fine (sending was completed)
	 * @throws IOException
	 */
	public synchronized boolean getFile(File file, String filename) throws IOException {
		if (file.isDirectory()) {
			throw new IOException("SimpleFTP cannot upload a directory.");
		}
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
		BufferedInputStream input = null;
		return moveFile(input, output, filename);
	}

	/**
	 * Sends a file to be stored on the FTP server. Returns true if the file
	 * transfer was successful. The file is sent in passive mode to avoid NAT or
	 * firewall problems at the client end.
	 */
	private synchronized boolean moveFile(BufferedInputStream input, BufferedOutputStream output, String filename) throws IOException {		
		Object[] o = pasv();
		String ip = (String)o[0];
		int port = (int)o[1];

		if (output == null) sendLine("STOR " + filename);
		else sendLine("RETR " + filename);

		Socket dataSocket = new Socket(ip, port);

		String response = getAllResponses("150", read.readLine());
		if (!response.startsWith ("150 ")) {
			//if (!response.startsWith("150 ")) {
			dataSocket.close();
			//input.close();
			output.close();
			input.close();
			throw new IOException("SimpleFTP was not allowed to send the file: " + response);
		}
		if (output == null) output = new BufferedOutputStream(dataSocket.getOutputStream());
		else input = new BufferedInputStream(dataSocket.getInputStream());

		moveFile(input,output);
		output.close();
		input.close();
		response = read.readLine();
		response = getAllResponses("226", response);

		dataSocket.close();
		return response.startsWith("226 ");
	}

	private void moveFile(BufferedInputStream input, BufferedOutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		output.flush();
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
	 * @return array of Object: [0] is String-ip, [1] is int-port
	 * @throws IOException
	 */
	public synchronized Object[] pasv() throws IOException {
		sendLine("PASV");
		String response = read.readLine();
		response = getAllResponses("227", response);
		if (!response.startsWith("227 ")) {
			throw new IOException("SimpleFTP could not request passive mode: "
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
				throw new IOException("SimpleFTP received bad data link information: "
						+ response);
			}
		}
		Object[] o = new Object[2];
		o[0]=ip;
		o[1]=port;
		return o;
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
		sendLine("SITE CHMOD " + rights + " " + filename);
		String response = getAllResponses("200", read.readLine());
		if (!response.startsWith("200 ")) {
			throw new IOException("There is a problem with changing rights to file/directory " + response);
		}
		return true;
	}

	public synchronized boolean noop() throws IOException {
		//if (read.read() == -1) throw new IOException("Server disconnected");
		sendLine("NOOP");
		if (getAllResponses("200 ", read.readLine()).startsWith("200 ")) return true;
		else return false;
	}

	private String getAllResponses(String code, String response) throws IOException {
		String r = response;
		while (r.startsWith(code + "-")) {
			System.out.println(r);
			r=read.readLine();
		}
		System.out.println(r);
		return r;
	}
	
	private TimerTask createNewTimerTask() {
		return new TimerTask() {
			int noopCounter = 0;
			@Override
			public void run() {
				if (noopCounter==6) {
					try {
						disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						if (!noop()) {
							cancelNOOPDeamon();
							throw new RuntimeException("Server disconnected");
						}
						noopCounter++;
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage());
					}
				}
			}
		};
	}
	
	private void resetNoopTimer() {
		timer.cancel();
		task = createNewTimerTask();
		timer = new Timer();
		timer.scheduleAtFixedRate(task, 30*1000, 30*1000);
	}
	
	public void cancelNOOPDeamon() {
		if (timer != null ) {
			timer.cancel();
			timer = null;
		}
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

}
