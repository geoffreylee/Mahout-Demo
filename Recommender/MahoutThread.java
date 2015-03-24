package Recommender;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;

import Recommender.ItemRecommend;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

public class MahoutThread implements Callable<List<RecommendedItem>> {
	private static final Logger logger = Logger.getLogger(App.class.getName());
	protected String threadName;
	protected Thread t;
	protected ItemRecommend Mahout;
	protected Long uid;
	protected Integer n; 
	protected List<RecommendedItem> recs;

	// Making the mahout thread is essentially going to be the data of reading a rec
	MahoutThread(ItemRecommend recommender, Long userID, Integer num) {
		// we dont want to instantiate multiple copies of mahout so we pass it to the thread
		Mahout = recommender;
		uid = userID;
		n = num;
	}
	
	@Override
	// The main computation
	public List<RecommendedItem> call() {
		try {
			return Mahout.getRecommendations(uid, n);
		} catch (TasteException e) {
			logger.log(Level.SEVERE, e.toString());
			return new ArrayList<RecommendedItem>();
		}
	}
	
}
