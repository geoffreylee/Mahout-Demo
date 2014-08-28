/*  Copyright 2014 Tamber, Inc. 
	Developed by Geoffrey Lee

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

       	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

/* This app is a basic server side implementation of the Mahout User Based Recommendation Engine
 * It listens for API commands on port 24579 a command of the form "rec space_separated_ids" or "quit"
 */
package Recommender;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import Recommender.ItemRecommend; 

public class App {
	public static void main(String[] args) throws Exception {
		try {
			// Where Mahout will listen
			final int portNumber = 24579;
			ServerSocket serverSocket = new ServerSocket(portNumber);
	
			// Initialize; right now the format is for a csv training file though this could easily be made into a db
			ItemRecommend Mahout = new ItemRecommend("data/users.data.csv", 4, 100);
			
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
					List<RecommendedItem> recommendations = Mahout.getRecommendations(myId, 20);
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
			e.printStackTrace();
		}
	}
}
