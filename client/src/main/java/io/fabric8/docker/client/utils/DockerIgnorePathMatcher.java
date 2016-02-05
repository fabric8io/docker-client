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

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.StringTokenizer;

public class DockerIgnorePathMatcher implements PathMatcher {

    private static final String NON_SEPARATOR_CHARS = "[^ /\\\\\\]\\]]";
    private static final String STAR = "*";
    private static final String QUESTION_MARK = "?";
    private static final String END_BRACKET = "]";

    private final String[] patterns;

    public DockerIgnorePathMatcher(Collection<String> patterns) {
        this(patterns.toArray(new String[patterns.size()]));
    }

    public DockerIgnorePathMatcher(String... patterns) {
        this.patterns = new String[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            this.patterns[i] = toRegex(patterns[i]);
        }
    }

    @Override
    public boolean matches(Path path) {
        for (String p : patterns) {
            if (path.toString().matches(p)) {
                return true;
            }
        }
        return false;
    }

    private static String toRegex(String pattern) {
        return wildcardToRegex(wildcardToRegex(pattern, STAR, NON_SEPARATOR_CHARS), QUESTION_MARK, NON_SEPARATOR_CHARS);
    }

    private static String wildcardToRegex(String pattern, String wildcard, String regex) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(pattern, wildcard);
        String lastToken = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (lastToken != null && lastToken.endsWith(END_BRACKET)) {
                sb.append(token);
                sb.append(wildcard);
            } else if (tokenizer.hasMoreTokens() || pattern.endsWith(wildcard)) {
                sb.append(token);
                sb.append(regex);
                sb.append(wildcard);
            } else {
                sb.append(token);
            }
            lastToken = token;
        }
        return sb.toString();
    }
}
