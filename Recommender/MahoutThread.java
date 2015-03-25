package Recommender;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import Recommender.ItemRecommend;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

public class MahoutThread implements Runnable {
	private static final Logger logger = Logger.getLogger(App.class.getName());
	protected Socket socket;
	protected Thread t;
	protected ItemRecommend Mahout;

	// Making the mahout thread is essentially going to be the data of reading a rec
	MahoutThread(Socket s, ItemRecommend recommender) {
		// we dont want to instantiate multiple copies of mahout so we pass it to the thread
		this.Mahout = recommender;
		this.socket = s;
	}
	@Override
	public void run() {
		try {
			PrintWriter pw = new PrintWriter(this.socket.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			String cmd = br.readLine();				
			String[] heard = cmd.split("\\s");
			
			if(heard[0].equals("uid")) {
				Long uid = Long.parseLong(heard[1]);		
				List<RecommendedItem> recommendations = this.Mahout.getRecommendations(uid, 30);
				
				// read off the recs and parse
				String recs = "";
				for(RecommendedItem recommendation: recommendations) {
					long itemID = recommendation.getItemID();
					recs = recs + itemID + ":" + recommendation.getValue() + " ";
				}
				recs = recs + "\n";
				pw.println(recs);
			} 
			pw.close();
			socket.close();
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString());
		} catch (TasteException e) {
			logger.log(Level.SEVERE, e.toString());
		}
	}
	
	public void start() {
		if (this.t == null) {
			t = new Thread(this, "recommender-thread");
			t.start();
		}
	}
}
