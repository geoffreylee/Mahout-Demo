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

/* This is a local implementation  without the sockets stuff*/
package Recommender;
import java.util.ArrayList;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import Recommender.ItemRecommend; 

public class LocalApp {
	public static void main(String[] args) throws Exception {
		try {
			if(args.length == 0) {
				System.out.println("No tastes passed");
			}
			// Initialize; right now the format is for a csv training file though this could easily be made into a db
			ItemRecommend Mahout = new ItemRecommend("data/users.data.csv", 4, 100);
			
			// Listen
			long myId = Mahout.getSessionId();
			List<Long> prefs = new ArrayList<Long>();
			List<Float> vals = new ArrayList<Float>();
			for(int j = 1; j < args.length; j++) {
				prefs.add(Long.parseLong(args[j]));
						
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
			System.out.println(recs);
				
			
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}
}
