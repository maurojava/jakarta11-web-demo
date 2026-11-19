package com.example;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class PayaraMicroIntegrationIT {
private static final String APP_CONTEXT = "/jakarta11-web-demo/";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    public static MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:10.11")
            .withNetwork(NETWORK)
            .withNetworkAliases("mariadb")
            .withDatabaseName("dukes_test")
            .withUsername("duke")
            .withPassword("secretpassword");

    @Container
    public static GenericContainer<?> PAYARA_MICRO = new GenericContainer<>(DockerImageName.parse("payara/micro:7.2026.7-jdk25"))
            .withNetwork(NETWORK)
            .withExposedPorts(8080)
            .dependsOn(MARIA_DB)
            .withEnv("DB_HOST", "mariadb")
            .withEnv("DB_PORT", "3306")
            .withEnv("DB_NAME", "dukes_test")
            .withEnv("DB_USER", "duke")
            .withEnv("DB_PASSWORD", "secretpassword")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("target/jakarta11-web-demo.war")),
                    "/opt/payara/deployments/jakarta11-web-demo.war"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(findMariaDbDriverPath()),
                    "/opt/payara/libs/mariadb-java-client.jar"
            )
            .withCommand("--addlibs /opt/payara/libs/mariadb-java-client.jar --deploy /opt/payara/deployments/jakarta11-web-demo.war")
           // .waitingFor(Wait.forLogMessage(".*Payara Micro .* ready in .*\\n", 1));
.waitingFor(
    Wait.forHttp(APP_CONTEXT)
        .forPort(8080)
        .forStatusCode(200)
        .withStartupTimeout(Duration.ofMinutes(3))
);

    @Test
    public void testPayaraMicroContainerIsRunning() {
        assertTrue(PAYARA_MICRO.isRunning());
    }

    @Test
    public void testAppIsResponding() {
        int mappedPort = PAYARA_MICRO.getMappedPort(8080);
        String host = PAYARA_MICRO.getHost();

        try {
            // Chiama la pagina index.html dell'applicazione usando il nome corretto del WAR
            URI uri = URI.create("http://" + host + ":" + mappedPort + "/jakarta11-web-demo/");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verifica che l'app risponda con 200 OK
            assertEquals(200, response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("Impossibile connettersi all'endpoint di test", e);
        }
    }

    private static Path findMariaDbDriverPath() {
        try {
            return Paths.get(org.mariadb.jdbc.Driver.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (Exception e) {
            throw new RuntimeException("Impossibile trovare il driver MariaDB", e);
        }
    }
}
