package server;

import impl.IGameClient;
import impl.IGameServer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

public class Server implements IGameServer {

	public static void main(String[] args) throws IOException {
		Server server = new Server(1099);

		System.out.println("Enter 's' (without Quotationmarks) to stop server.");

		while (System.in.read() != 's') {
			// Do nothing
		}
		System.exit(0);
	}

	// ---------------------------------------------------------
	// ---------------------------------------------------------
	// ---------------------------------------------------------

	// Manages the players.
	Map<String, PlayerData> players = new HashMap<>();
	// Maintains the currentFly. See also #huntFly(String,int)
	int currentFlyID;

	// Used for generating new FlyPositions
	private Image img;
	private Random rand = new Random();

	public Server(int port) throws IOException {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		this.img = ImageIO.read(new File("." + File.pathSeparator + "fliege-t20678.jpg"));

		Registry registry = null;

		try {
			registry = LocateRegistry.getRegistry(port);
		} catch (RemoteException e) {
			// By Methodcontract, this should never ever happen.
			System.out.println("Unable to locate RMI-Registry. Serverinitialization failed. Error: " + e.getMessage());
			System.exit(1);
		}

		boolean bound = false;
		for (int i = 0; !bound && i < 2; i++) {
			try {
				registry.rebind("ServerStub", this);
				bound = true;
				System.out.println("ServerStub bound to registry, port " + port + ".");
			} catch (RemoteException e) {
				System.out.println("Rebinding " + "ServerStub" + " failed, " + "retrying ...");
				try {
					registry = LocateRegistry.createRegistry(port);
				} catch (RemoteException e1) {
					System.out.println("Creating of RMI-Registry failed. Serverinitialization failed. Error: " + e.getMessage());
					System.exit(1);
				}
				System.out.println("Registry started on port " + port + ".");
			}
		}
	}

	@Override
	public void login(String playerName, IGameClient client) throws RemoteException {
		this.players.put(playerName, new PlayerData(client, 0));
		for (PlayerData player : this.players.values()) {
			player.client.recievePlayerJoined(playerName);
		}
	}

	@Override
	public void logout(String playerName) throws RemoteException {
		this.players.remove(playerName);
		for (PlayerData player : this.players.values()) {
			player.client.recievePlayerLeft(playerName);
		}
	}

	@Override
	public void huntFly(String playerName, int flyID) throws RemoteException {
		if (flyID == this.currentFlyID) {
			// Avoid another player claiming the same point
			++this.currentFlyID;

			// increase the score
			PlayerData data = this.players.get(playerName);
			data.score += 1;

			// notify each player about the score-change
			for (PlayerData player : this.players.values()) {
				player.client.recieveFlyHunted(playerName, data.score);
			}

			// compute new fly position
			int x = this.rand.nextInt(300 - this.img.getWidth(null) + 1 /* +1 to counter the exlusive part in nextInt */);
			int y = this.rand.nextInt(300 - this.img.getHeight(null) + 1 /* +1 to counter the exlusive part in nextInt */);

			// Distribute new fly position to each player. Seperated from the other for-loop to ensure in the best way possible that each player receives the
			// fly at the same time.
			for (PlayerData player : this.players.values()) {
				player.client.recieveFlyPosition(x, y, this.currentFlyID);
			}
		}
	}

	@Override
	public Object[][] getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	private class PlayerData {
		public IGameClient client;
		public Integer score;

		private PlayerData(IGameClient client, Integer score) {
			super();
			this.client = client;
			this.score = score;
		}

	}
}
