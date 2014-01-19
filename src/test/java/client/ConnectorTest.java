/**
 * 
 */
package client;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import exception.ConnectionException;

/**
 * @author Jakub Fortunka
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class ConnectorTest {

	private String correctHostname = "fortunka.hostoi.com";
	private String correctUsername = "a4417886";
	private String correctPassword = "Euler272";
	
	/**
	 * Test method for {@link client.Connector#connectToServer(String, int, String, String)} (bad host)
	 * @throws ConnectionException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	@Test(expected=UnknownHostException.class)
	public void testConnectingToServerUnknownHost() throws UnknownHostException, IOException, ConnectionException {
		Connector c = new Connector();
		c.connectToServer("nieistniejacy adres", 1, "user", "pass");
		fail();
	}
	
	/**
	 * Test method for {@link client.Connector#connectToServer(String, int, String, String)} (bad username)
	 * @throws ConnectionException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	@Test(expected=ConnectionException.class)
	public void testConnectingToServerBadUsername() throws UnknownHostException, IOException, ConnectionException {
		Connector c = new Connector();
		c.connectToServer(correctHostname, 21, "user", "pass");
		fail();
	}
	
	/**
	 * Test method for {@link client.Connector#connectToServer(String, int, String, String)} (bad password)
	 * @throws ConnectionException 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	@Test(expected=ConnectionException.class)
	public void testConnectingToServerBadPassword() throws UnknownHostException, IOException, ConnectionException {
		Connector c = new Connector();
		c.connectToServer(correctHostname, 21, correctUsername, "pass");
		fail();
	}
	
	/**
	 * Test method for {@link client.Connector#connectToServer(String, int, String, String)}
	 */
	@Test
	public void testConnectingToServerCorrect() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#pwd()}
	 */
	@Test
	public void testPwd() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			assertNotNull(c.pwd());
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#cwd()}
	 */
	@Test
	public void testCwd() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			assertTrue(c.cwd("public_html"));
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#list()}
	 */
	@Test
	public void testList() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			c.list();
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#sendFile(File, boolean)}
	 */
	@Test
	public void testFileOperation1() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			File f = new File("tmp");
			f.createNewFile();
			PrintWriter out = new PrintWriter("tmp");
			out.println("Test 1 2 3");
			out.close();
			c.sendFile(f, true);
			f.delete();
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#getFile(File, String)}
	 */
	@Test
	public void testFileOperation2() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			File f = new File("tmp");
			c.getFile(f, "tmp");
			if (f.length() == 0) fail();
			f.delete();
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#changeName(String, String)}
	 */
	@Test
	public void testFileOperation3() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			if (!c.changeName("tmp", "tmp2")) fail();
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#changeRights(String, String)}
	 */
	@Test
	public void testFileOperation4() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			if (!c.changeRights("tmp2", "777")) fail();
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	/**
	 * Test for {@link client.Connector#removeFile(String)}
	 */
	@Test
	public void testFileOperation5() {
		Connector c = new Connector();
		try {
			c.connectToServer(correctHostname, 21, correctUsername, correctPassword);
			c.removeFile("tmp2");
		} catch (IOException | ConnectionException e) {
			e.printStackTrace();
			fail();
		} finally {
			try {
				c.disconnect();
			} catch (IOException | ConnectionException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	

}
