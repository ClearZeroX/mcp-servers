package com.opay.occ.mcp.businessdiagnostic.tools;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.opay.occ.mcp.businessdiagnostic.core.LogConfig;
import com.opay.occ.mcp.businessdiagnostic.core.LogEnv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Reads Apollo configuration with the apollo-client SDK, mirroring
 * {@code TestLogServiceImpl#getAllApolloConfigs} so the values match what the
 * running service actually sees.
 *
 * <p>Apollo Client keeps a process-wide client configuration, so credentials are
 * applied once before the first {@code ConfigService} call.</p>
 *
 * <p>TODO(multi-environment): support more than one environment at a time. The meta
 * server and app id are global to the SDK process, so multiple environments would
 * need dedicated Apollo client instances created from a custom client config.</p>
 */
public final class ApolloTools {

    private static final String COMMENT_UNAVAILABLE = "";

    private ApolloTools() {
    }

    /** Tool: get_apollo_configs */
    public static Map<String, Object> getConfigs(LogConfig config, String env, String namespace, String keyKeyword) {
        LogEnv logEnv = config.requireEnvironment(env);
        if (logEnv.getApolloAppId() == null || logEnv.getApolloMeta() == null) {
            throw new IllegalStateException(
                    "Apollo is not configured for environment: " + logEnv.getName());
        }
        applyCredentials(logEnv);

        List<String> namespaces = resolveNamespaces(logEnv, namespace);
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, String> sources = new LinkedHashMap<>();

        for (String current : namespaces) {
            try {
                Config apolloConfig = ConfigService.getConfig(current);
                if (apolloConfig == null) {
                    warnings.add("namespace not available: " + current);
                    continue;
                }
                ConfigSourceType sourceType = apolloConfig.getSourceType();
                sources.put(current, String.valueOf(sourceType));
                // Anything but REMOTE means the Apollo server was not reached and the values
                // come from a local fallback, which would silently look like "no config".
                if (sourceType != ConfigSourceType.REMOTE) {
                    warnings.add("namespace not served by Apollo (" + sourceType + "): " + current);
                }
                for (String key : new TreeSet<>(apolloConfig.getPropertyNames())) {
                    if (!matchesKey(key, keyKeyword)) {
                        continue;
                    }
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("namespace", current);
                    item.put("key", key);
                    item.put("value", apolloConfig.getProperty(key, ""));
                    // The runtime client does not expose configuration comments.
                    item.put("comment", COMMENT_UNAVAILABLE);
                    items.add(item);
                }
            } catch (Exception e) {
                warnings.add("namespace failed: " + current + " (" + e.getMessage() + ")");
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", true);
        response.put("env", logEnv.getName());
        response.put("appId", logEnv.getApolloAppId());
        response.put("meta", logEnv.getApolloMeta());
        response.put("namespaces", namespaces);
        response.put("sources", sources);
        response.put("items", items);
        response.put("itemCount", items.size());
        response.put("warnings", warnings);
        return response;
    }

    /** Applies apollo-client credentials as system properties. */
    static void applyCredentials(LogEnv env) {
        System.setProperty("app.id", env.getApolloAppId());
        System.setProperty("apollo.meta", env.getApolloMeta());
        String accessKey = env.getApolloAccessKey();
        if (accessKey != null) {
            System.setProperty("apollo.accesskey.secret", accessKey);
        }
    }

    static List<String> resolveNamespaces(LogEnv env, String namespace) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return new ArrayList<>(env.getApolloNamespaces());
        }
        return Collections.singletonList(namespace.trim());
    }

    static boolean matchesKey(String key, String keyKeyword) {
        if (keyKeyword == null || keyKeyword.trim().isEmpty()) {
            return true;
        }
        return key != null && key.toLowerCase().contains(keyKeyword.trim().toLowerCase());
    }
}
