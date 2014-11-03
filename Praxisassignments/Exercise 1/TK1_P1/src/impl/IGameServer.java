package impl;

import impl.exception.PlayerAlreadyExistsException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.swing.JTable;

public interface IGameServer extends Remote {
	/**
	 * Connects a new player to the game. The player should initially start with 0 Points and can connect any time to the game. Adding a player with an already
	 * existing player to the game should fail (we don't care for logins, so first-come-first-serve. This method will also notify all connected IGameClients
	 * that a new Player has joined. A new player can receive the whole player list via {@link #getPlayers()}.
	 * 
	 * @param playerName
	 *            The name by which the player wants to be recognized.
	 * @param client
	 * @throws RemoteException
	 */
	void login(String playerName, IGameClient client) throws RemoteException, PlayerAlreadyExistsException;

	/**
	 * Disconnects a player from the game. This method should notify all connected players of this disconnect, so they can update their displays.
	 * 
	 * @param playerName
	 *            The player to disconnect.
	 * @throws RemoteException
	 */
	void logout(String playerName) throws RemoteException;

	/**
	 * Notifies the server that playerName has catched the fly with the given ID. The ID is necessary to prevent delayed messages interferring with the gameflow
	 * (e.g. a second player hits the fly before he is notified that a new fly exists, giving him a point as well).
	 * 
	 * @param playerName
	 *            The scoring player
	 * @param flyID
	 *            The fly that was slain
	 * @throws RemoteException
	 */
	void huntFly(String playerName, int flyID) throws RemoteException;

	/**
	 * Returns all currently connected players together with their points, in a form suitable for a {@link JTable}. This method is especially for clients
	 * joining late. Normally clients should update themselves when being notified.
	 * 
	 * @return
	 */
	String[][] getPlayers();

}
