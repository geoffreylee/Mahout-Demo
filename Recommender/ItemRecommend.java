/*
 * Implementation of a Mahout User Based Recommender by Geoffrey Lee (c) Tamber, Inc. 2014
 * Used for speed comparison tests between Engie (TM) and Mahout
 */


package Recommender;
import java.io.File;
import java.io.IOException; 
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.PlusAnonymousConcurrentUserDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class ItemRecommend {
	private DataModel dm;
	private PlusAnonymousConcurrentUserDataModel tempdm;
	private UserSimilarity userSimilarity; 
	private UserNeighborhood neighborhood; 
	private GenericUserBasedRecommender recommender;
	private int concurrentUsers;
	private static final Logger logger = Logger.getLogger(ItemRecommend.class.getName());


	public ItemRecommend(String training, int nearestN, int concurrent, String similarity_model){
		try {
			logger.log(Level.INFO, "Initializing");
			concurrentUsers = concurrent;
			// set up to have the data model be read from a file though one could switch this to a db
			dm = new FileDataModel(new File(training));
			tempdm = new PlusAnonymousConcurrentUserDataModel(dm, concurrentUsers);

			
			//NB: DESPITE NAME USERSIMILARITY IS ACTUALLY TYPED ItemSimilarity
			if(similarity_model.equals("loglikelihood")) {
				userSimilarity = new LogLikelihoodSimilarity(tempdm);
			} else if (similarity_model.equals("pearson")) {
				userSimilarity = new PearsonCorrelationSimilarity(tempdm);
			} else if (similarity_model.equals("euclidean")) {
				userSimilarity = new EuclideanDistanceSimilarity(tempdm);
			} else if (similarity_model.equals("tanimoto")) {
				userSimilarity = new TanimotoCoefficientSimilarity(tempdm);
			} else if (similarity_model.equals("cosine")) {
				userSimilarity = new UncenteredCosineSimilarity(tempdm);
			} else {
				logger.log(Level.INFO, "Similarity model not recognized, defaulting to loglikelihood");
				userSimilarity = new LogLikelihoodSimilarity(tempdm);
			}
			neighborhood = new NearestNUserNeighborhood(nearestN, userSimilarity, tempdm);
			recommender = new GenericUserBasedRecommender(tempdm, neighborhood, userSimilarity);
			//recommender = new GenericItemBasedRecommender(tempdm, userSimilarity);
			logger.log(Level.INFO, "Mahout ready");
		} catch (TasteException e) {
			logger.log(Level.SEVERE, e.toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString());
		}
	}
	
	
	// Sets a temporary session ID for multi-thread safe writing
	public long getSessionId() throws IOException {
		PlusAnonymousConcurrentUserDataModel plusModel = (PlusAnonymousConcurrentUserDataModel) recommender.getDataModel();
		Long tempUserId = plusModel.takeAvailableUser();
		return tempUserId;
	}

	// Write a temporary set of user preferences
	public void writeTempUserPreferences(long id, List<Long> prefs, List<Float> vals) throws Exception{
		if(prefs.size() != vals.size()) {
			throw new Exception("List of item IDs and preference values not the same");
		}
		
		PreferenceArray tempPrefs = new GenericUserPreferenceArray(prefs.size());
		for(int i = 0; i < prefs.size(); i++) {
			tempPrefs.setUserID(i, id);
			tempPrefs.setItemID(i, prefs.get(i));
			// N.B. For the Tamber comparison, we used the float 4.0 since that is what we passed to our backend
			tempPrefs.setValue(i, vals.get(i));
		}
		tempdm.setTempPrefs(tempPrefs, id);
	}
	
	public void deleteTempUserPreferences(long id) {
		tempdm.releaseUser(id);
	}
	
	public List<RecommendedItem> getRecommendations(long userId, int howMany) throws TasteException {
		List<RecommendedItem> recommendations = recommender.recommend(userId, howMany);
		return recommendations;
	}
}
