package client;

import impl.IGameClient;

import java.awt.EventQueue;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Client implements IGameClient {

	private Display display;

	private ConcurrentHashMap<String, Integer> players;

	public Client(String url, int port) {
		this.players = new ConcurrentHashMap<>();

	}

	@Override
	public void recieveFlyHunted(String playerName, int newPoints) throws RemoteException {
		this.players.put(playerName, newPoints);
		rebuildDisplay();
	}

	@Override
	public void recieveFlyPosition(final int x, final int y) throws RemoteException {
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

}
