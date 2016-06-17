package io.fabric8.docker.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public interface DockerStreamData {

  StreamType streamType();

  int size();

  byte[] payload();

  enum StreamType {
    STDIN(0),
    STDOUT(1),
    STDERR(2);

    private static final Map<Integer, StreamType> lookup = new HashMap<>(3);

    static {
      for(StreamType s : EnumSet.allOf(StreamType.class)) {
        lookup.put(s.value, s);
      }
    }

    private final int value;

    StreamType(int streamType) {
      this.value = streamType;
    }

    public static StreamType lookup(int streamType) {
      StreamType type = lookup.get(streamType);
      if (type == null) {
        throw new IllegalArgumentException("Invalid stream type value: " + streamType);
      }
      return type;
    }
  }

}
