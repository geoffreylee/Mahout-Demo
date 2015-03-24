package Recommender;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

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
		// Multithreading to support 8 cores
		ExecutorService executor = Executors.newFixedThreadPool(8);
		
		// Listen
		while(true) {	
			try {
				Socket socket = serverSocket.accept();
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String cmd = br.readLine();				
				String[] heard = cmd.split("\\s");
				
				if(heard[0].equals("uid")) {
					Long uid = Long.parseLong(heard[1]);
					
					// async submit task
					Future<List<RecommendedItem>> task = executor.submit(new MahoutThread(Mahout, uid, 30));
					List<RecommendedItem> recommendations = task.get();
					
					// read off the recs and parse
					String recs = "";
					for(RecommendedItem recommendation: recommendations) {
						long itemID = recommendation.getItemID();
						recs = recs + itemID + ":" + recommendation.getValue() + " ";
					}
					recs = recs + "\n";
					pw.println(recs);
				} else if(heard[0].equals("quit")) {
					//cleanup 
					pw.close();
					socket.close();
					serverSocket.close();
					System.exit(0);
				}
				pw.close();
				socket.close();
			} catch (NullPointerException e) {
				logger.log(Level.SEVERE, e.toString());
			}
		} 
	}

}
