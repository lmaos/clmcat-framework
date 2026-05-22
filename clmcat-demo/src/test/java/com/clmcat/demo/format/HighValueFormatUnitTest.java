package com.clmcat.demo.format;

import com.clmcat.basics.commons.format.HighValueFormat;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighValueFormatUnitTest {

    @Test
    void shouldSupportCustomAroundForIndexedArgs() {
        HighValueFormat format = new HighValueFormat("{?}");

        String value = format.format("{0}-{1}", "A", "B");

        assertEquals("A-B", value);
    }

    @Test
    void shouldReadNamedValueFromMapArgument() {
        HighValueFormat format = new HighValueFormat("{?}");
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Tom");

        String value = format.format("hello {name}", user);

        assertEquals("hello Tom", value);
    }

    @Test
    void shouldReadNamedValueFromObjectArgument() {
        HighValueFormat format = new HighValueFormat("{?}");

        String value = format.format("hello {name}", new User("Tom", true, null));

        assertEquals("hello Tom", value);
    }

    @Test
    void shouldResolveNestedPathFromObjectAndMap() {
        HighValueFormat format = new HighValueFormat("{?}");
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", new User("Tom", true, null));

        String value = format.format("{user.name}", payload);

        assertEquals("Tom", value);
    }

    @Test
    void shouldResolveNamedHighValueBeforeArgZeroFallback() {
        HighValueFormat format = new HighValueFormat("{?}");

        String value = format.format("{user.name}-{1.name}",
                HighValueFormat.HighValue.valueOf("user", new User("Named", true, null)),
                new User("Indexed", true, null));

        assertEquals("Named-Indexed", value);
    }

    @Test
    void shouldSupportBooleanGetter() {
        HighValueFormat format = new HighValueFormat("{?}");

        String value = format.format("{active}", new User("Tom", true, null));

        assertEquals("true", value);
    }

    @Test
    void shouldReuseHighValuesWithoutMutatingIndexedArguments() {
        HighValueFormat format = new HighValueFormat("{?}");
        HighValueFormat.HighValues values = HighValueFormat.HighValues.start("name", "Tom");

        String first = format.format("{name}-{1}", values, "A");
        String second = format.format("{name}-{1}", values, "B");

        assertEquals("Tom-A", first);
        assertEquals("Tom-B", second);
    }

    @Test
    void shouldResolveNamedValueInsideHighValuesContainer() {
        HighValueFormat format = new HighValueFormat("{?}");
        HighValueFormat.HighValues values = HighValueFormat.HighValues.start("user", new User("Tom", true, null));

        String value = format.format("{user.name}", values);

        assertEquals("Tom", value);
    }

    @Test
    void shouldClearParseCacheWhenPrefixAndSuffixChange() {
        HighValueFormat format = new HighValueFormat();
        assertEquals("${0}", format.format("${0}", (Object[]) null));

        format.format("${0}", "A");
        format.setPrefix("{");
        format.setSuffix("}");

        String value = format.format("{0}", "B");

        assertEquals("B", value);
    }

    private static class User {
        private final String name;
        private final boolean active;
        private final User parent;

        private User(String name, boolean active, User parent) {
            this.name = name;
            this.active = active;
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }

        public User getParent() {
            return parent;
        }
    }
}
