package impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IGameClient extends Remote {
	void recieveFlyHunted(String playerName, int newPoints) throws RemoteException;

	void recieveFlyPosition(int x, int y) throws RemoteException;

	// I AM aware of the fact that I can replace these two methods with clever recieveFlyHunted-calls. I just don't like it.

	void recievePlayerJoined(String playerName) throws RemoteException;

	void recievePlayerLeft(String playerName) throws RemoteException;
}
