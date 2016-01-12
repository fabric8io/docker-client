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

package io.fabric8.docker.dsl.misc;

import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.annotations.ListOption;
import io.fabric8.docker.dsl.misc.annotations.EventsOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Terminal;

@Dsl
@InterfaceName("MiscOperation")
public interface MiscDSL {

    @EntryPoint
    @Terminal
    Info info();

    @EntryPoint
    @Terminal
    InlineAuth auth();

    @EntryPoint
    @Terminal
    Version version();

    @EntryPoint
    @Terminal
    Boolean ping();

    @EntryPoint
    @EventsOption
    void events();

    @All(EventsOption.class)
    void since(String since);

    @All(EventsOption.class)
    void until(String until);

    @All({EventsOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All(EventsOption.class)
    OutputHandle list();
}
