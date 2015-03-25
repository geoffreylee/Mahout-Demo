package Recommender;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import Recommender.ItemRecommend; 
import Recommender.MahoutThread;

public class MultithreadedApp {
	private static final Logger logger = Logger.getLogger(App.class.getName());
	public static void main(String[] args) throws Exception {
		// Where Mahout will listen
		final int portNumber = 24579;
		ServerSocket serverSocket = new ServerSocket(portNumber);
		// Initialize; right now the format is for a csv training file though this could easily be made into a db
		ItemRecommend Mahout = new ItemRecommend("data/users.data.csv", 100, 100, "loglikelihood");
			
		// Listen
		while(true) {	
			logger.log(Level.INFO, "Mahout listening");
			try {
				Socket socket = serverSocket.accept();
				// start a new thread to handle everything
				new Thread(new MahoutThread(socket, Mahout)).start();
			} finally {
				serverSocket.close();
			}
		}
	}
}
