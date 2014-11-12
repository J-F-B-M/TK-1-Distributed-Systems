package client;

import impl.IGameClient;
import impl.IGameServer;

import java.awt.EventQueue;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Client implements IGameClient {

	public static void main(String[] args) throws IOException {
		Client client = new Client(args[0], args[1], 1099);

		System.out.println("Enter 's' (without Quotationmarks) to stop client.");

		while (System.in.read() != 's') {
			// Do nothing
		}
		System.exit(0);
	}

	// ---------------------------------------------------------
	// ---------------------------------------------------------
	// ---------------------------------------------------------

	transient Display										display;

	private transient int									flyID;

	private transient ConcurrentHashMap<String, Integer>	players;

	private transient IGameServer							server;

	private transient String								playerName;

	@SuppressWarnings("deprecation")
	public Client(String playerName, String url, int port) throws RemoteException {
		UnicastRemoteObject.exportObject(this, 0);

		this.players = new ConcurrentHashMap<>();
		this.playerName = playerName;

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// It *should* be safe to initialize the display at an
					// unknow point in the future. As it is an eventQUEUE,
					// and I queue all interactions
					// with display, I see no Problem.
					Client.this.display = new Display();
					Client.this.display.setVisible(true);
					Client.this.display.client = Client.this;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		try {

			this.server = (IGameServer) Naming.lookup("rmi://" + url + ":" + port + "/" + "Server");

			this.server.login(playerName, this);
			Object[][] players = this.server.getPlayers();

			for (Object[] objects : players) {
				if (objects[0] instanceof String && objects[1] instanceof Integer) {
					this.players.put((String) objects[0], (Integer) objects[1]);
				} else {
					throw new IllegalArgumentException("Received invalid connected player, received entry was: Player=" + objects[0] + ", Score=" + objects[1]);
				}
			}
		} catch (Exception e) {
			System.out.println("Client failed, caught exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void recieveFlyHunted(String playerName, int newPoints) throws RemoteException {
		this.players.put(playerName, newPoints);
		rebuildDisplay();
	}

	@Override
	public void recieveFlyPosition(final int x, final int y, int flyID) throws RemoteException {
		this.flyID = flyID;
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				Client.this.display.setFlyPosition(x, y);
			}
		});
	}

	@Override
	public void recievePlayerJoined(String playerName) throws RemoteException {
		// I don't care if anything was there before, as the server should take
		// care of it.
		this.players.put(playerName, 0);
		rebuildDisplay();
	}

	@Override
	public void recievePlayerLeft(String playerName) throws RemoteException {
		this.players.remove(playerName);
		rebuildDisplay();
	}

	public void notifyFlyCatched() {
		try {
			this.server.huntFly(this.playerName, this.flyID);
		} catch (RemoteException e) {
			System.out.println("Something went wrong when sending the FlyHuntedEvent to the server. Error: " + e.getMessage());
		}
	}

	private void rebuildDisplay() {
		final Object[][] value = new Object[this.players.size()][2];

		int i = 0;
		for (Entry<String, Integer> e : this.players.entrySet()) {
			value[i++] = new Object[] { e.getKey(), e.getValue() };
		}
		
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				// I have no idea if this will work...
				Client.this.display.updateTable(value);
			}
		});
	}

	public void shutdown() {
		try {
			this.server.logout(this.playerName);
		} catch (RemoteException e) {
			System.out.println("Logout from server failed. The server has to find out by himself that something went wrong. Error: " + e.getMessage());
		}
	}

}
