package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.synthetic.ByUrlClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;

public class TestCarrot2 {

	public static void main(String[] args) {

		String text1 = "play saud football";
		String text2 = "programming code java";
		String text3 = "aaaa dfgsd wewer wer wer ";
		String text4 = "code programming code";
		String text5 = "sdfsdf sdfsdf sdf ";
		ArrayList<Document> documents = new ArrayList<Document>();
		documents.add(new Document(text1).setField("id", 1));
		documents.add(new Document(text2).setField("id", 2));
		documents.add(new Document(text3).setField("id", 3));
		documents.add(new Document(text4).setField("id", 4));
		documents.add(new Document(text5).setField("id", 5));

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("BisectingKMeansClusteringAlgorithm.clusterCount", 3);
		attributes.put("documents", documents);
		Controller controller = ControllerFactory.createSimple();
		ProcessingResult byDomainClusters = controller.process(attributes,
				BisectingKMeansClusteringAlgorithm.class);
		List<Cluster> clustersByDomain = byDomainClusters.getClusters();
		System.out.println("Clusterssss  " + clustersByDomain.size());
//		ConsoleFormatter.displayClusters(clustersByDomain);
		for (Cluster cluster : clustersByDomain) {
			System.out.println("label " + cluster.getLabel());
			List<Document> docs = cluster.getAllDocuments();
			for (Document document : docs) {
				System.out.println("Doc " + document.getTitle());
				System.out.println("field " + document.getField("id"));
			}
		}

	}
}
