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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by iocanel on 7/15/16.
 */
public class URLUtilsTest {
    @Test
    public void testIssue65() throws Exception {
        String url = "tcp://some.ip.address:2376";
        String expectedResult = "https://some.ip.address:2376";

        String result = URLUtils.withProtocol(url, "https://");
        Assert.assertEquals(expectedResult, result);

        url = "tcp:some.ip.address:2376";
        result = URLUtils.withProtocol(url, "https://");
        Assert.assertEquals(expectedResult, result);
    }
}