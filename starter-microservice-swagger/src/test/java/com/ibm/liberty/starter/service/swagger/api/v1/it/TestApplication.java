/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.liberty.starter.service.swagger.api.v1.it;

import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import com.ibm.liberty.starter.api.v1.model.provider.Dependency;
import com.ibm.liberty.starter.api.v1.model.provider.Provider;
import com.ibm.liberty.starter.api.v1.model.provider.Sample;
import com.ibm.liberty.starter.api.v1.model.provider.ServerConfig;

/**
 * Test the deployed service responds as expected
 * 
 */
public class TestApplication extends EndpointTest {

    @Before
    public void checkSetup() {
        checkAvailability("/api/v1/provider/");
    }

    @Test
    public void testProvider() throws Exception {
        Provider provider = testEndpoint("/api/v1/provider/", Provider.class);
        assertNotNull("No response from API for provider", provider);
        assertTrue("Description was not found.", provider.getDescription().contains("<h2>Swagger</h2>"));
        Dependency[] dependencies = provider.getDependencies();
        boolean providedDependency = false;
        boolean runtimeDependency = false;
        for (Dependency dependency : dependencies) {
            if (Dependency.Scope.PROVIDED.equals(dependency.getScope())) {
                assertTrue("groupId incorrect.", "net.wasdev.wlp.starters.swagger".equals(dependency.getGroupId()));
                assertTrue("artifactId incorrect.", "provided-pom".equals(dependency.getArtifactId()));
                assertTrue("version incorrect.", "0.0.3".equals(dependency.getVersion()));
                providedDependency = true;
            }
            if (Dependency.Scope.RUNTIME.equals(dependency.getScope())) {
                assertTrue("groupId incorrect.", "net.wasdev.wlp.starters.swagger".equals(dependency.getGroupId()));
                assertTrue("artifactId incorrect.", "runtime-pom".equals(dependency.getArtifactId()));
                assertTrue("version incorrect.", "0.0.3".equals(dependency.getVersion()));
                runtimeDependency = true;
            }
        }
        assertTrue("Provided dependencies were specified.", providedDependency);
        assertTrue("Runtime dependencies were specified.", runtimeDependency);
    }

    @Test
    public void testConfig() throws Exception {
        ServerConfig config = testEndpoint("/api/v1/provider/config", ServerConfig.class);
        assertNotNull("No response from API for configuration", config);
        String actual = config.getTags()[0].getTags()[0].getValue();
        String expected = "apiDiscovery-1.0";
        assertEquals("Incorrect feature specified", expected, actual);
    }

    @Test
    public void testSamples() throws Exception {
        Sample sample = testEndpoint("/api/v1/provider/samples", Sample.class);
        assertNotNull("No response from API for sample", sample);
        assertNotNull("Expected locations", sample.getLocations());
        assertEquals("Expected no samples.", 0, sample.getLocations().length);
    }
    
    @Test
    public void testFeaturesInstall() throws Exception {
        String actual = testEndpoint("/api/v1/provider/features/install");
        assertNotNull("No response from API for features/install", actual);
        String expected = "apiDiscovery-1.0";
        assertEquals("Incorrect feature to install was specified : " + actual, expected, actual);
    }
    
    @Test
    public void testPrepareDynamicPackages() throws Exception {
    	String serverOutputDir = new File("./build/test/wlp/servers/StarterServer").getCanonicalPath().replace('\\', '/');
    	System.out.println("serverOutputDir=" + serverOutputDir);
    	String uuid = UUID.randomUUID().toString();
    	String swaggerTechDirPath = serverOutputDir + "/workarea/appAccelerator/" + uuid + "/swagger";
    	File swaggerFile = new File(swaggerTechDirPath + "/server/src/sampleSwagger.json");
    	createSampleSwagger(swaggerFile);
    	assertTrue("Swagger file doesn't exist : " + swaggerFile.getCanonicalPath(), swaggerFile.exists());
    	String actual = testEndpoint("/api/v1/provider/packages/prepare?path=" + swaggerTechDirPath + "&options=server");
    	assertNotNull("No response from API for packages/prepare", actual);
        assertEquals("Response doesn't match : " + actual, "success", actual);
        String packagedFilePath = swaggerTechDirPath + "/package/src/sampleSwagger.json";
        assertTrue("Swagger file was not packaged successfully : " + packagedFilePath, new File(packagedFilePath).exists());
    }
    
    private void createSampleSwagger(File file) throws Exception{
    	String swaggerContent = "{\"swagger\": \"2.0\",\"info\": {\"description\": \"Info APIs for Collective\",\"version\": \"1.0.0\"},\"basePath\": \"/\","
        		+ "\"paths\": {\"/ibm/api/root1/v1/info\": {\"get\": {\"summary\": \"Retrieve collective's core information\","
        		+ "\"description\": \"Returns a JSON with core information about collective\",\"operationId\": \"getInfo\",\"produces\": "
        		+ "[\"application/json\"],\"responses\": {\"200\": {\"description\": \"successful operation\","
        		+ "\"schema\": {\"$ref\": \"#/definitions/CollectiveInfo\"}},\"404\": {\"description\": \"Invalid path\"}}}}},\"definitions\": {"
        		+ "\"CollectiveInfo\": {\"properties\": {\"name\": {\"type\": \"string\",\"description\": \"Name of the collective\"}}}}}";
    	FileUtils.writeStringToFile(file, swaggerContent);
    }

}
