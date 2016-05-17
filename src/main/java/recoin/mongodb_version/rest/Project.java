package recoin.mongodb_version.rest;

import static spark.Spark.*;

import org.json.JSONObject;

public class Project {

	public static void main(String[] args) {
		port(1234);
		JSONObject json = new JSONObject();
		json.append("saud", "aljaloud");
		get("/Project","application/json", (request, response) -> json.toString());

	}
}
