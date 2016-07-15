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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {

    private static final Pattern URL_PATTERN = Pattern.compile("(?<protocol>^\\w+:[/][/]?)[^ ]+");

    private URLUtils() {}

    public static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append("/");
            }
        }
        String joined = sb.toString();

        // And normalize it...
        return joined
                .replaceAll("/+", "/")
                .replaceAll("/\\?", "?")
                .replaceAll("/#", "#")
                .replaceAll(":/", "://");

    }

    public static String withProtocol(String url, String protocol) {
        Matcher m = URL_PATTERN.matcher(url);
        if (m.matches()) {
            String originalProtocol = m.group("protocol");
            return protocol + url.substring(originalProtocol.length());
        }
        throw new IllegalArgumentException();
    }
}
