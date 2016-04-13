package sociam.pybossa.mongodb;

import org.bson.types.ObjectId;

public class GetCounterFrom_ID {
	
	public static void main(String[] args) {
		ObjectId _id = new ObjectId("570e52c40a3fead1da655110");
		System.out.println(_id.getCounter());
	}

}
