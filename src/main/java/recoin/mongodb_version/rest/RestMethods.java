package recoin.mongodb_version.rest;

import spark.Request;

public class RestMethods {

	public static final int port = 3344;

	public static Integer setOffset(Request request) {
		Integer offset = 0;
		String offsetString = request.queryParams("offset");
		if (offsetString != null) {
			offset = Integer.valueOf(offsetString);
		}
		return offset;
	}

	public static Integer setLimit(Request request) {
		Integer limit = 200;
		String limitString = request.queryParams("limit");
		if (limitString != null) {
			limit = Integer.valueOf(limitString);
		}
		return limit;
	}
}
