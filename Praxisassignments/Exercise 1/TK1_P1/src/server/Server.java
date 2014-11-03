package server;

import java.rmi.RemoteException;

import impl.IGameClient;
import impl.IGameServer;
import impl.exception.PlayerAlreadyExistsException;

public class Server implements IGameServer, IGameClient {

	@Override
	public void recieveFlyHunted(String playerName, int newPoints) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void recieveFlyPosition(int x, int y) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void recievePlayerJoined(String playerName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void recievePlayerLeft(String playerName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void login(String playerName, IGameClient client) throws RemoteException, PlayerAlreadyExistsException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout(String playerName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void huntFly(String playerName, int flyID) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[][] getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

}
