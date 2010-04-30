/*
 * $Header: $
 */

package soccerscope.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPConnection {
	private final static int BUFFER_SIZE = 2048;
	private byte[] buffer;

	private InetAddress address;
	private int port;
	private DatagramSocket socket;

	public UDPConnection(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		try {
			socket = new DatagramSocket();
		} catch (SocketException se) {
			System.err.println(se);
		}
		buffer = new byte[BUFFER_SIZE];
	}

	public UDPConnection(String hostname, int port) {
		InetAddress address;
		try {
			address = InetAddress.getByName(hostname);
			this.address = address;
			this.port = port;
			try {
				socket = new DatagramSocket();
			} catch (SocketException se) {
				System.err.println(se);
			}
		} catch (UnknownHostException uhe) {
			System.err.println(uhe);
		}
		buffer = new byte[BUFFER_SIZE];
	}

	public void send(byte[] msg) {
		// Initilize the packet with data and address
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address,
				port);
		// send the packet through socket.
		try {
			socket.send(packet);
		} catch (IOException ie) {
			System.err.println(ie);
		}
	}

	public void send(String msgString) {
		// Convert the message to an array of bytes
		byte[] message = msgString.getBytes();
		//int msglen = msgString.length();
		send(message);
	}

	public DatagramPacket receive() throws IOException {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		// Wait to receive a datagram
		try {
			socket.receive(packet);
		} catch (IOException ie) {
			System.err.println(ie);
		}
		return packet;
	}

	public String getHostName() {
		return address.getHostName();
	}

	public int getPort() {
		return port;
	}
}
