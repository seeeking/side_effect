package com.processor.uniquename;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author dwang
 * @since 08.11.18
 */
public class GroupedNameContext {
    private final Map<GroupedName, Element> map = new HashMap<>();

    public void insertClassOrThrow(String group, Element element) throws DuplicateGroupNameException {
        final GroupedName groupedName = new GroupedName(group, element.getSimpleName().toString());

        if (map.get(groupedName) != null && !map.get(groupedName).equals(element)) {
            throw new DuplicateGroupNameException(String.format("Duplicated class name : %s - %s",
                    getCanonicalName(element),
                    getCanonicalName(map.get(groupedName))
            ));
        } else {
            map.put(groupedName, element);
        }
    }

    public void clear() {
        map.clear();
    }

    private String getCanonicalName(Element element) {
        return element.getEnclosingElement().getSimpleName().toString() + '.' + element.getSimpleName().toString();
    }

    public static class GroupedName {
        private final String group;
        private final String name;

        public GroupedName(String group, String name) {
            this.group = group;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupedName that = (GroupedName) o;
            return Objects.equals(group, that.group)
                    && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, name);
        }
    }

    public class DuplicateGroupNameException extends Exception {
        public DuplicateGroupNameException(String message) {
            super(message);
        }
    }
}
