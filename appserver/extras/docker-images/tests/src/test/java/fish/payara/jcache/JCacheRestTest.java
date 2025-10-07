/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2025 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/main/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fish.payara.jcache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import java.time.Duration;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCacheRestTest {

    private static GenericContainer<?>[] nodes = new GenericContainer<?>[3];
    private static Network network;

    @BeforeAll
    public static void setUp() throws Exception {
        System.out.println("\n=== Starting Docker Network ===");
        network = Network.newNetwork();
        System.out.println("Created network with ID: " + network.getId());

        DockerImageName payaraImg = DockerImageName.parse("nexus.dev.payara.fish:5000/payara/micro");
        System.out.println("\n=== Starting Payara Micro Containers ===");

        for (int instanceIndex = 0; instanceIndex < 3; instanceIndex++) {
            System.out.println("\n--- Starting Node " + instanceIndex + " ---");
            System.out.println("Creating container with image: " + payaraImg);

            // Create final copy of instanceIndex for use in lambda
            final int nodeIndex = instanceIndex;

            nodes[instanceIndex] = new GenericContainer<>(payaraImg)
                    .withNetwork(network)
                    .withNetworkAliases("node" + instanceIndex)
                    .withExposedPorts(8080)
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("jcache-rest.war"),
                            "/opt/payara/deployments/jcache-rest.war"
                    )
                    .withCommand(
                            "--deploymentDir",
                            "/opt/payara/deployments",
                            "--autoBindHttp"
                    )
                    .waitingFor(
                            Wait.forLogMessage(".*(Payara Micro.*ready|Deployed: jcache-rest).*\\n", 1)
                                    .withStartupTimeout(Duration.ofMinutes(2))
                    )
                    .withLogConsumer(outputFrame -> {
                        String logLine = outputFrame.getUtf8String().trim();
                        System.out.println(String.format("[Container %d] %s", nodeIndex, logLine));

                        // Log specific deployment messages
                        if (logLine.contains("Deployed: jcache-rest") ||
                            logLine.contains("jcache-rest.war") ||
                            logLine.contains("Exception") ||
                            logLine.contains("Error") ||
                            logLine.contains("WARN") ||
                            logLine.contains("deploy")) {
                            System.out.println(String.format("[Container %d - DEPLOYMENT] %s", nodeIndex, logLine));
                        }
                    });

            System.out.println("Starting container...");
            nodes[instanceIndex].start();

            // Print container info
            System.out.println("Container started with ID: " + nodes[instanceIndex].getContainerId());
            System.out.println("HTTP Port: " + nodes[instanceIndex].getMappedPort(8080));
            System.out.println("Node " + instanceIndex + " base URL: " + baseUrl(nodes[instanceIndex]));
        }

        System.out.println("\n=== All containers started successfully ===");
        System.out.println("Node 0: " + baseUrl(nodes[0]));
        System.out.println("Node 1: " + baseUrl(nodes[1]));
        System.out.println("Node 2: " + baseUrl(nodes[2]));
    }

    @Test
    public void testJCacheCluster() throws Exception {
        System.out.println("STARTING JCACHE CLUSTER TEST...");

        // Test 1: Store on node 1 and verify on all nodes
        System.out.println("TEST 1: Single Node Write");
        String key1 = "foo";
        String value1 = "bar";

        System.out.println("Storing: " + key1 + " = " + value1 + " on node 1");
        put(nodes[0], key1, value1);

        // Verify on all nodes
        System.out.println("Verifying: key '" + key1 + "' on all nodes");
        for (int instanceIndex = 0; instanceIndex < nodes.length; instanceIndex++) {
            String nodeName = String.format("Node %d", instanceIndex + 1);
            String actualValue = get(nodes[instanceIndex], key1);
            boolean success = value1.equals(actualValue);
            System.out.println("  " + (success ? "✓" : "✗") + " Verified: " + key1 + " = " + actualValue + " (expected: " + value1 + ")");
            assertEquals(actualValue, value1,
                    String.format("Value mismatch for key '%s' on %s", key1, nodeName));
        }

        // Test 2: Store on node 2 and verify on all nodes
        System.out.println("TEST 2: Second Node Write");
        String key2 = "baz";
        String value2 = "qux";

        System.out.println("Storing: " + key2 + " = " + value2 + " on node 2");
        put(nodes[1], key2, value2);

        // Verify on all nodes
        System.out.println("Verifying: key '" + key2 + "' on all nodes");
        for (int instanceIndex = 0; instanceIndex < nodes.length; instanceIndex++) {
            String nodeName = String.format("Node %d", instanceIndex + 1);
            String actualValue = get(nodes[instanceIndex], key2);
            boolean success = value2.equals(actualValue);
            System.out.println("  " + (success ? "✓" : "✗") + " Verified: " + key2 + " = " + actualValue + " (expected: " + value2 + ")");
            assertEquals(actualValue, value2,
                    String.format("Value mismatch for key '%s' on %s", key2, nodeName));
        }

        // Test 3: Store on node 3 and verify on all nodes
        System.out.println("TEST 3: Third Node Write");
        String key3 = "hello";
        String value3 = "world";

        System.out.println("Storing: " + key3 + " = " + value3 + " on node 3");
        put(nodes[2], key3, value3);

        // Verify on all nodes
        System.out.println("Verifying: key '" + key3 + "' on all nodes");
        for (int instanceIndex = 0; instanceIndex < nodes.length; instanceIndex++) {
            String nodeName = String.format("Node %d", instanceIndex + 1);
            String actualValue = get(nodes[instanceIndex], key3);
            boolean success = value3.equals(actualValue);
            System.out.println("  " + (success ? "✓" : "✗") + " Verified: " + key3 + " = " + actualValue + " (expected: " + value3 + ")");
            assertEquals(actualValue, value3,
                    String.format("Value mismatch for key '%s' on %s", key3, nodeName));
        }
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("STOPPING DOWN JCACHE CLUSTER TEST...");

        for (GenericContainer<?> node : nodes) {
            if (node != null && node.isRunning()) {
                node.close();
            }
        }

        if (network != null) {
            network.close();
        }
    }

    private static String baseUrl(GenericContainer<?> node) {
        return "http://" + node.getHost() + ":" + node.getMappedPort(8080);
    }

    private static void put(GenericContainer<?> node, String key, String value) throws Exception {
        String url = baseUrl(node) + "/jcache-rest/webresources/cache?key=" + key;
        System.out.println("\n[PUT] Node: " + node.getContainerInfo().getName() +
                " | URL: " + url +
                " | Key: " + key +
                " | Value: " + value);

        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            String jsonInput = "\"" + value + "\"";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int status = connection.getResponseCode();
            System.out.println("[PUT] Status: " + status);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String get(GenericContainer<?> node, String key) throws Exception {
        String url = baseUrl(node) + "/jcache-rest/webresources/cache?key=" + key;
        System.out.println("\n[GET] Node: " + node.getContainerInfo().getName() +
                " | URL: " + url +
                " | Key: " + key);

        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            
            int status = connection.getResponseCode();
            String responseBody;
            
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                responseBody = response.toString();
            }

            // Parse the JSON string to remove the extra quotes
            String result = responseBody;
            if (responseBody != null && responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                result = responseBody.substring(1, responseBody.length() - 1);
            }

            System.out.println("[GET] Status: " + status +
                    " | Raw response: " + responseBody +
                    " | Parsed value: " + result);
            return result;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
