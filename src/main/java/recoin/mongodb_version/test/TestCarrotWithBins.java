package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.ProcessingResult;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;

public class TestCarrotWithBins {
	final static Logger logger = Logger.getLogger(TestCarrotWithBins.class);

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		Config.reload();
		HashSet<org.bson.Document> docs = getNominatedbins("zika");
		System.out.println("Size " + docs.size());
		for (org.bson.Document document : docs) {
			System.out.println("Json " + document.toString());
			break;
		}

	}

	public static void test1() {

		PropertyConfigurator.configure("log4j.properties");
		Config.reload();
		HashSet<org.bson.Document> bins = MongodbMethods
				.getTweetsFromBinInMongoDB("zika");
		logger.debug("Bins " + bins.size());
		ArrayList<org.carrot2.core.Document> documents = new ArrayList<org.carrot2.core.Document>();
		for (org.bson.Document bin : bins) {
			ObjectId _id = bin.getObjectId("_id");
			documents.add(new org.carrot2.core.Document(bin.getString("text")).setField("id",
					bin));
		}

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("BisectingKMeansClusteringAlgorithm.clusterCount", 100);
		attributes.put("documents", documents);
		Controller controller = ControllerFactory.createSimple();
		ProcessingResult byDomainClusters = controller.process(attributes,
				BisectingKMeansClusteringAlgorithm.class);
		List<Cluster> clustersByDomain = byDomainClusters.getClusters();
		logger.debug("Clusterssss  " + clustersByDomain.size());
		// ConsoleFormatter.displayClusters(clustersByDomain);
		int clusterNo = 1;
		for (Cluster cluster : clustersByDomain) {
			logger.debug("===============");
			logger.debug("Cluster: " + clusterNo);
			clusterNo++;
			logger.debug("label: " + cluster.getLabel());
			List<org.carrot2.core.Document> docs = cluster.getAllDocuments();
			for (org.carrot2.core.Document document : docs) {
				logger.debug("Doc " + document.getTitle());
				// logger.debug("field " + document.getField("id"));
			}
		}

	}

	public static HashSet<org.bson.Document> getNominatedbins(
			String collectionName) {
		HashSet<org.bson.Document> returnedBins = new HashSet<org.bson.Document>();
		HashSet<org.bson.Document> bins = MongodbMethods
				.getTweetsFromBinInMongoDB(collectionName);
		logger.debug("Bins size is: " + bins.size());
		ArrayList<org.carrot2.core.Document> documents = new ArrayList<org.carrot2.core.Document>();
		for (org.bson.Document bin : bins) {
			documents.add(new org.carrot2.core.Document(bin.getString("text")).setField("id",
					bin));
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("BisectingKMeansClusteringAlgorithm.clusterCount", 100);
		attributes.put("documents", documents);
		Controller controller = ControllerFactory.createSimple();
		ProcessingResult byDomainClusters = controller.process(attributes,
				BisectingKMeansClusteringAlgorithm.class);
		List<Cluster> clustersByDomain = byDomainClusters.getClusters();
		logger.debug("Clusters size is: " + clustersByDomain.size());
		int clusterNo = 1;
		for (Cluster cluster : clustersByDomain) {
			logger.debug("===============");
			logger.debug("Cluster number: " + clusterNo);
			clusterNo++;
			logger.debug("Cluster label is: " + cluster.getLabel());
			List<org.carrot2.core.Document> docs = cluster.getAllDocuments();
			org.bson.Document chosenDoc = docs.get(0).getField("id");
			returnedBins.add(chosenDoc);
			for (org.carrot2.core.Document document : docs) {
				logger.debug("bin text: " + document.getTitle());
			}
		}
		logger.debug("===============");
		return returnedBins;
	}
}
