/* This is the actual server side api
 * It listens on port 24579 for commands
 * Written by Geoffrey Lee (c) Tamber, Inc. 2014
 */

package Recommender;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import Recommender.ItemRecommend; 

public class App {
	private static final Logger logger = Logger.getLogger(App.class.getName());
	public static void main(String[] args) throws Exception {
		try {
			// Where Mahout will listen
			final int portNumber = 24579;
			ServerSocket serverSocket = new ServerSocket(portNumber);
	
			// Initialize; right now the format is for a csv training file though this could easily be made into a db
			// 100 NN
			ItemRecommend Mahout = new ItemRecommend("data/users.data.csv", 100, 100);
			
			// Listen
			while(true) {
				Socket socket = serverSocket.accept();
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String cmd = br.readLine();				
				String[] heard = cmd.split("\\s");

				// The API commands
				if(heard[0].equals("rec")) {
					long myId = Mahout.getSessionId();
					List<Long> prefs = new ArrayList<Long>();
					List<Float> vals = new ArrayList<Float>();
					for(int j = 1; j < heard.length; j++) {
						prefs.add(Long.parseLong(heard[j]));
						
						// we hard code the value of 4.0 for each preference because this is what we do for the Engie recs
						// only did this for fairness of comparison
						vals.add(new Float(4.0));
						Mahout.writeTempUserPreferences(myId, prefs, vals);
					}
					List<RecommendedItem> recommendations = Mahout.getRecommendations(myId, 50);
					String recs = "";
					for(RecommendedItem recommendation : recommendations) {
						long itemID = recommendation.getItemID();
						recs = recs + itemID + ":"+ recommendation.getValue() + " ";
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
			}
		} catch (TasteException e) {
			//e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
		} catch (NullPointerException e) {
			//e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
		}
	}
}
