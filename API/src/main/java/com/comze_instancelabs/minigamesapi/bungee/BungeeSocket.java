package com.comze_instancelabs.minigamesapi.bungee;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaLogger;
import com.comze_instancelabs.minigamesapi.PluginInstance;

public class BungeeSocket {

	// Here sits the lovely MGLib server waiting for requests from our Lobby slaves >:D
	// Socket server moved to lobby, dis our slave now :(

	// We're gonna send simple strings like: sign:<minigame>:<arena>:<state>:<players>/<maxplayers>

	public static String signUpdateString(PluginInstance pli, Arena a) {
		if (a == null) {
			return "sign:" + pli.getPlugin().getName() + ":null:JOIN:0:0";
		}
		return "sign:" + pli.getPlugin().getName() + ":" + a.getInternalName() + ":" + a.getArenaState().toString() + ":" + a.getAllPlayers().size() + ":" + a.getMaxPlayers();
	}

	private static ArrayList<Integer> portsUp = new ArrayList<Integer>();
	private static boolean init = false;
	private static boolean initializing = false;

	public static void sendSignUpdate(final PluginInstance pli, final Arena a) {
		try {
			if (BungeeSocket.init) {
				for (int i : BungeeSocket.portsUp) {
					ArenaLogger.debug("Sending to port " + i);
					Socket socket = new Socket("127.0.0.1", i);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println(signUpdateString(pli, a));
					socket.close();
				}
				return;
			}
			// Of course we'll have lags at the first sign update as we check through 20 ports
			if (!BungeeSocket.initializing) {
				BungeeSocket.initializing = true;
				new Thread(new Runnable() {
					public void run() {
						for (int i = 13380; i < 13400; i++) {
							try {
								ArenaLogger.debug("Trying port " + i);
								Socket socket = new Socket("127.0.0.1", i);
								if (socket.isConnected()) {
									BungeeSocket.portsUp.add(i);
								}
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
								out.println(signUpdateString(pli, a));
								socket.close();
							} catch (Exception e) {
								ArenaLogger.debug("Could not connect to port " + i);
							}
						}
						BungeeSocket.init = true;
					}
				}).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}