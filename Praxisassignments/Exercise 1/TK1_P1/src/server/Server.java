package server;

import impl.IGameClient;
import impl.IGameServer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
	transient Map<String, PlayerData>	players	= new HashMap<>();
	// Maintains the currentFly. See also #huntFly(String,int)
	transient int						currentFlyID;

	// Used for generating new FlyPositions
	private transient Image				img;
	private transient Random			rand	= new Random();
	private int							x;
	private int							y;

	public Server(int port) throws IOException {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		this.img = ImageIO.read(new File("." + File.separator + "fliege.jpg"));

		Registry registry = LocateRegistry.createRegistry(port);
		
		IGameServer stub = (IGameServer) UnicastRemoteObject.exportObject(this, 0);
		
		try {
			registry.bind("Server", stub);
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * +1 to counter the exlusive part in nextInt
		 */
		this.x = this.rand.nextInt(300 - this.img.getWidth(null) + 1);
		this.y = this.rand.nextInt(300 - this.img.getHeight(null) + 1);

		System.out.println("Server Ready");
	}

	@Override
	public void login(String playerName, IGameClient client) throws RemoteException {
		this.players.put(playerName, new PlayerData(client, 0));
		client.recieveFlyPosition(this.x, this.y, this.currentFlyID);
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
			/*
			 * +1 to counter the exlusive part in nextInt
			 */
			this.x = this.rand.nextInt(300 - this.img.getWidth(null) + 1);
			this.y = this.rand.nextInt(300 - this.img.getHeight(null) + 1);

			// Distribute new fly position to each player. Seperated from the
			// other for-loop to ensure in the best way possible that each
			// player receives the
			// fly at the same time.
			for (PlayerData player : this.players.values()) {
				player.client.recieveFlyPosition(this.x, this.y, this.currentFlyID);
			}
		}
	}

	@Override
	public Object[][] getPlayers() {
		Object[][] value = new Object[this.players.size()][2];

		int i = 0;
		for (Entry<String, PlayerData> e : this.players.entrySet()) {
			value[i++] = new Object[] { e.getKey(), e.getValue().score };
		}

		return value;
	}

	private class PlayerData {
		public IGameClient	client;
		public Integer		score;

		private PlayerData(IGameClient client, Integer score) {
			super();
			this.client = client;
			this.score = score;
		}

	}
}
