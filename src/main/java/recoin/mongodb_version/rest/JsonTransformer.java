package recoin.mongodb_version.rest;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    

    @Override
    public String render(Object model) {
        return model.toString();
    }
}