package io.fabric8.docker.dsl.container.annotations;

import io.fabric8.docker.dsl.annotations.NamedOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Only;
import io.sundr.dsl.annotations.Option;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@Option
@All({NamedOption.class})
@Only({NamedOption.class, ArchiveOption.class, UploadOption.class})
public @interface UploadOption {

}
