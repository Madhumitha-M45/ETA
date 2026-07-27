package config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongodbConnection {

	private static final String CONNECTION_STRING = "mongodb://192.168.1.172:27017";
    private static final String DATABASE_NAME = "bus_tracking_db";

    private static MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase("bus");
    }
}