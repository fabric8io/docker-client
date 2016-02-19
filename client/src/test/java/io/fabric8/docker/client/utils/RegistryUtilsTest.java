/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.client.utils;

import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.AuthConfigBuilder;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegistryUtilsTest {

    @Test
    public void testGetConfigFromImage() {
        AuthConfig reg1 = new AuthConfigBuilder()
                .withServeraddress("https://index.reg1.io/v1")
                .withAuth("reg1")
                .build();

        AuthConfig reg2 = new AuthConfigBuilder()
                .withServeraddress("https://reg2.io/v1")
                .withAuth("reg2")
                .build();

        AuthConfig reg3 = new AuthConfigBuilder()
                .withServeraddress("reg3.io")
                .withAuth("reg3")
                .build();

        Config config = new ConfigBuilder()
                .addToAuthConfigs(reg1.getServeraddress(), reg1)
                .addToAuthConfigs(reg2.getServeraddress(), reg2)
                .addToAuthConfigs(reg3.getServeraddress(), reg3)
                .build();

        AuthConfig auth1 = RegistryUtils.getConfigForImage("reg1.io/my/image", config);
        assertNotNull(auth1);
        assertEquals(reg1, auth1);
    }

}