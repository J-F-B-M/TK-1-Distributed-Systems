package client;

import impl.IGameClient;
import impl.IGameServer;

import java.awt.EventQueue;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
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

	private Display display;

	private int flyID;

	private ConcurrentHashMap<String, Integer> players;

	private IGameServer server;

	private String playerName;

	public Client(String playerName, String url, int port) {
		this.players = new ConcurrentHashMap<>();
		this.playerName = playerName;

		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						// It *should* be safe to initialize the display at an unknow point in the future. As it is an eventQUEUE, and I queue all interactions
						// with display, I see no Problem.
						Client.this.display = new Display();
						Client.this.display.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			this.server = (IGameServer) Naming.lookup("rmi://" + url + ":" + port + "/" + "ServerStub");

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
		// I don't care if anything was there before, as the server should take care of it.
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
		Set<Entry<String, Integer>> s = this.players.entrySet();
		final LinkedList<Object[]> dummy = new LinkedList<>();

		for (Entry<String, Integer> entry : s) {
			dummy.add(new Object[] { entry.getKey(), entry.getValue().toString() });
		}
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				// I have no idea if this will work...
				Client.this.display.updateTable((Object[][]) dummy.toArray());
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
