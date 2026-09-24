package com.opay.occ.mcp.businessdiagnostic.tools;
import com.opay.occ.mcp.businessdiagnostic.core.Masking;
import com.opay.occ.mcp.businessdiagnostic.core.Profile;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only Mongo diagnostic operations.
 */
public final class MongoTools {
    private MongoTools() {
    }

    public static Map<String, Object> query(
            Profile profile,
            String collection,
            Map<String, Object> filter,
            Map<String, Object> projection,
            Map<String, Object> sort,
            int limit,
            int skip
    ) {
        requireAllowedCollection(profile, collection);
        MongoBuilder.validatePagination(limit, skip);
        Document bsonFilter = MongoBuilder.buildFilter(filter);
        Document bsonProjection = MongoBuilder.buildProjection(projection);
        Document bsonSort = MongoBuilder.buildSort(sort);

        try (MongoClient client = createClient(profile)) {
            MongoDatabase database = client.getDatabase(profile.getDatabase());
            List<Map<String, Object>> rows = new ArrayList<>();
            database.getCollection(collection).find(bsonFilter)
                    .projection(bsonProjection)
                    .sort(bsonSort)
                    .skip(skip)
                    .limit(limit)
                    .map(document -> (Map<String, Object>) Masking.maskRow(document, profile))
                    .into(rows);
            Map<String, Object> result = envelope(profile, collection);
            result.put("rows", rows);
            result.put("limit", limit);
            result.put("skip", skip);
            return result;
        }
    }

    public static Map<String, Object> count(
            Profile profile,
            String collection,
            Map<String, Object> filter
    ) {
        requireAllowedCollection(profile, collection);
        Document bsonFilter = MongoBuilder.buildFilter(filter);
        try (MongoClient client = createClient(profile)) {
            MongoDatabase database = client.getDatabase(profile.getDatabase());
            long count = database.getCollection(collection).countDocuments(bsonFilter);
            Map<String, Object> result = envelope(profile, collection);
            result.put("count", count);
            return result;
        }
    }

    public static Map<String, Object> explain(
            Profile profile,
            String collection,
            Map<String, Object> filter,
            Map<String, Object> projection,
            Map<String, Object> sort,
            int limit,
            int skip
    ) {
        requireAllowedCollection(profile, collection);
        MongoBuilder.validatePagination(limit, skip);
        Document bsonFilter = MongoBuilder.buildFilter(filter);
        Document bsonProjection = MongoBuilder.buildProjection(projection);
        Document bsonSort = MongoBuilder.buildSort(sort);

        Document find = new Document("find", collection)
                .append("filter", bsonFilter)
                .append("projection", bsonProjection)
                .append("sort", bsonSort)
                .append("skip", skip)
                .append("limit", limit);
        Document command = new Document("explain", find).append("verbosity", "executionStats");
        try (MongoClient client = createClient(profile)) {
            MongoDatabase database = client.getDatabase(profile.getDatabase());
            Document plan = database.runCommand(command);
            Map<String, Object> result = envelope(profile, collection);
            result.put("plan", Masking.maskRow(plan, profile));
            return result;
        }
    }

    private static MongoClient createClient(Profile profile) {
        String uri = profile.getUri();
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalStateException("Missing Mongo URI environment variable");
        }
        return MongoClients.create(uri);
    }

    private static void requireAllowedCollection(Profile profile, String collection) {
        if (!profile.isCollectionAllowed(collection)) {
            throw new IllegalArgumentException("Collection is not allowed by profile: " + collection);
        }
    }

    private static Map<String, Object> envelope(Profile profile, String collection) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("profile", profile.getName());
        result.put("collection", collection);
        return result;
    }
}
