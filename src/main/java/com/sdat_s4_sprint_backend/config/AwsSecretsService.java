package com.sdat_s4_sprint_backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Service to fetch database credentials from AWS Secrets Manager.
 * The secret should be stored in JSON format with:
 * {
 *   "username": "...",
 *   "password": "...",
 *   "host": "...",
 *   "port": 3306,
 *   "dbname": "..."
 * }
 */
@Service
public class AwsSecretsService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public DbCredentials getDbCredentials() {
        // TODO: You can move secret name & region to application.properties if you want to make it dynamic
        String secretName = "FlightDb"; // AWS Secret name
        Region region = Region.of("us-east-2"); // AWS Region where your secret is stored

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        String secretString = getSecretValueResponse.secretString();

        try {
            JsonNode jsonNode = mapper.readTree(secretString);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            String host = jsonNode.get("host").asText();
            int port = jsonNode.get("port").asInt();
            String dbname = jsonNode.get("dbname").asText();

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, dbname
            );

            return new DbCredentials(jdbcUrl, username, password);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing DB secret", e);
        }
    }

    public record DbCredentials(String jdbcUrl, String username, String password) {}
}
