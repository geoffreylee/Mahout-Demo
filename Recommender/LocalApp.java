/* This is a local implementation  without the sockets stuff
 * It listens on port 24579 for commands
 * Written by Geoffrey Lee (c) Tamber, Inc. 2014
 */

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
			ItemRecommend Mahout = new ItemRecommend("data/users.data.csv", 4, 100, "default");
			
			// Listen
			long myId = Mahout.getSessionId();
			List<Long> prefs = new ArrayList<Long>();
			List<Float> vals = new ArrayList<Float>();
			//System.out.println(args[0]);
			for(int j = 0; j < args.length; j++) {
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
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("try again");
		}
	}
}
