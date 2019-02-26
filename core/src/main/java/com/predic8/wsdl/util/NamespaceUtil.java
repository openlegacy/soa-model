package com.predic8.wsdl.util;

import com.predic8.schema.Schema;
import com.predic8.soamodel.XMLElement;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vlad Gorshkov
 */
public class NamespaceUtil {

    private static final Pattern SEED = Pattern.compile("[0-9]$");

    private static final Map<XMLElement, Stack<Map<String, String>>> STACK_MAP = new HashMap<>();

    /**
     * Verifies element namespaces and returns next possible entry to avoid override
     * */
    public static Map.Entry<String, String> getNamespace(XMLElement element, String prefix, String namespace) {
        Map<String, String> refs = getReferenceMap(element, true, false);
        if (refs != null) {
            String ns = element.getNamespaces().get(prefix);
            if (ns != null && !Objects.equals(ns, namespace)) {
                String next = getNextPrefix(element.getNamespaces().keySet(), prefix);
                if (!next.equals(prefix)) {
                    refs.put(prefix, next);
                    prefix = next;
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(prefix, namespace);
    }

    /**
     * Returns new prefix value from stack of current loading session
     * */
    public static String getPrefix(XMLElement element, String prefix) {
        return getReferenceMap(element).getOrDefault(prefix, prefix);
    }

    private static Map<String, String> getReferenceMap(XMLElement element) {
        return getReferenceMap(element, false, true);
    }

    private static Map<String, String> getReferenceMap(XMLElement element, boolean returnNull, boolean useParents) {
        Map<String, String> result = returnNull ? null : Collections.emptyMap();
        if (STACK_MAP.containsKey(element)) {
            Stack<Map<String, String>> stack = STACK_MAP.get(element);
            if (!stack.isEmpty()) {
                result = stack.get(stack.size() -1);
            }
        } else if (element != null && useParents) {
            result = getReferenceMap(element.getParent(), returnNull, useParents);
        }
        return result;
    }

    /**
     * Pushes previous schema namespace references (or creates new if previous missed) in stack. Must be called BEFORE schema.parse
     * */
    public static void push(Schema schema) {
        if (STACK_MAP.containsKey(schema)) {
            STACK_MAP.get(schema).push(new HashMap<>());
        } else {
            Stack<Map<String, String>> stack = new Stack<>();
            stack.push(new HashMap<>());
            STACK_MAP.put(schema, stack);
        }
    }

    /**
     * Pops current schema namespace references from stack. Must be called AFTER schema.parse
     * */
    public static void pop(Schema schema) {
        if (STACK_MAP.containsKey(schema)) {
            STACK_MAP.get(schema).pop();
        }
    }

    /**
     * Clears namespace references
     * */
    public static void clear() {
        STACK_MAP.clear();
    }


    private static String getNextPrefix(Collection<String> prefixes, String prefix) {
        if (isEmpty(prefixes) || StringUtils.isEmpty(prefix)) {
            return prefix;
        }
        Integer seed = getSeed(prefix);
        String pfx = seed == null ? prefix : StringUtils.substringBeforeLast(prefix, String.valueOf(seed));
        String searchPattern = String.format("^%s$", seed != null ? String.format("%s[0-9]", pfx) : pfx);
        boolean changed = false;
        for (String p : prefixes) {
            if (p == null) {
                continue;
            }
            if (!p.matches(searchPattern)) {
                continue;
            }
            Integer sd = getSeed(p);
            if (sd == null) {
                continue;
            }
            if (seed == null || sd >= seed) {
                changed = true;
                seed = sd;
            }
        }
        if (seed == null) {
            changed = true;
            seed = -1;
        }
        if (changed) {
            seed++;
        }
        return String.format("%s%d", pfx, seed);
    }

    private static Integer getSeed(String value) {
        Matcher matcher = SEED.matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    private static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }
}
