package com.sciumo.jsonlogic;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/***
 * This class is a simple implementation of the JsonLogic language.
 * It is a simple way to execute logic in JSON format.
 * The main method is apply, which receives a JSON object and a data object.
 * The JSON object is a logic tree, and the data object is the data to be used in the logic.
 * The apply method returns the result of the logic.
 * The logic tree is a JSON object, with a single key, which is the operator.
 * The value is an array of arguments to the operator.
 * The arguments can be other logic trees, or data references.
 * The data reference is a string, with the format "varName.subVarName.0.1".
 */
public class JsonLogic {
    private static final Map<String, Function<List<Object>, Object>> OPERATIONS = new HashMap<>();

    static {
        OPERATIONS.put("==", args -> softEquals(args.get(0), args.get(1)));
        OPERATIONS.put("===", args -> hardEquals(args.get(0), args.get(1)));
        OPERATIONS.put("!=", args -> !softEquals(args.get(0), args.get(1)));
        OPERATIONS.put("!==", args -> !hardEquals(args.get(0), args.get(1)));
        OPERATIONS.put(">", args -> less(args.get(1), args.get(0)));
        OPERATIONS.put(">=", args -> less(args.get(1), args.get(0)) || softEquals(args.get(0), args.get(1)));
        OPERATIONS.put("<", args -> less(args.get(0), args.get(1)));
        OPERATIONS.put("<=", args -> lessOrEqual(args.get(0), args.get(1)));
        OPERATIONS.put("!", args -> !toBool(args.get(0)));
        OPERATIONS.put("!!", args -> toBool(args.get(0)));
        OPERATIONS.put("%", args -> toDouble(args.get(0)) % toDouble(args.get(1)));
        OPERATIONS.put("and", args -> args.stream().allMatch(JsonLogic::toBool));
        OPERATIONS.put("or", args -> args.stream().anyMatch(JsonLogic::toBool));
        OPERATIONS.put("?:", args -> toBool(args.get(0)) ? args.get(1) : args.get(2));
        OPERATIONS.put("if", JsonLogic::ifOperator);
        OPERATIONS.put("in", args -> contains(args.get(1), args.get(0)));
        OPERATIONS.put("cat", args -> args.stream().map(String::valueOf).reduce("", String::concat));
        OPERATIONS.put("+", args -> args.stream().mapToDouble(JsonLogic::toDouble).sum());
        OPERATIONS.put("*", args -> args.stream().mapToDouble(JsonLogic::toDouble).reduce(1, (a, b) -> a * b));
        OPERATIONS.put("-", args -> args.size() == 1 ? -toDouble(args.get(0)) : toDouble(args.get(0)) - toDouble(args.get(1)));
        OPERATIONS.put("/", args -> args.size() == 1 ? toDouble(args.get(0)) : toDouble(args.get(0)) / toDouble(args.get(1)));
        OPERATIONS.put("min", args -> args.stream().mapToDouble(JsonLogic::toDouble).min().orElse(Double.NaN));
        OPERATIONS.put("max", args -> args.stream().mapToDouble(JsonLogic::toDouble).max().orElse(Double.NaN));
        OPERATIONS.put("merge", JsonLogic::merge);
        OPERATIONS.put("join", JsonLogic::join );
        OPERATIONS.put("count", args -> (int) args.stream().filter(JsonLogic::toBool).count());
    }

    private static boolean softEquals(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return String.valueOf(a).equals(String.valueOf(b));
        }
        if (a instanceof Boolean || b instanceof Boolean) {
            return toBool(a) == toBool(b);
        }
        return Objects.equals(a, b);
    }

    private static boolean hardEquals(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getClass() == b.getClass() && Objects.equals(a, b);
    }

    private static boolean less(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() < ((Number) b).doubleValue();
        }
        return String.valueOf(a).compareTo(String.valueOf(b)) < 0;
    }

    private static boolean lessOrEqual(Object a, Object b) {
        return less(a, b) || softEquals(a, b);
    }

    private static boolean toBool(Object a) {
        if (a instanceof Boolean) {
            return (Boolean) a;
        }
        return a != null;
    }

    private static double toDouble(Object a) {
        if (a instanceof Number) {
            return ((Number) a).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(a));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static boolean contains(Object container, Object item) {
        if (container instanceof String && item instanceof String) {
            return ((String) container).contains((String) item);
        }
        if (container instanceof Collection) {
            return ((Collection<?>) container).contains(item);
        }
        return false;
    }

    private static Object join(List<Object> args) {
        if (args.size() < 2) {
            return "";
        }
        String separator = String.valueOf(args.get(0));
        Object arg2 = args.get(1);
        if( arg2 instanceof Collection) {
            return ((Collection<?>) arg2).stream().map(String::valueOf).collect(Collectors.joining(separator));
        }
        return args.stream().skip(1).map(String::valueOf).collect(Collectors.joining(separator));
    }

    private static Object merge(List<Object> args) {
        List<Object> result = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof Collection) {
                result.addAll((Collection<?>) arg);
            } else {
                result.add(arg);
            }
        }
        return result;
    }

    private static Object ifOperator(List<Object> args) {
        for (int i = 0; i < args.size() - 1; i += 2) {
            if (toBool(args.get(i))) {
                return args.get(i + 1);
            }
        }
        if (args.size() % 2 == 1) {
            return args.get(args.size() - 1);
        }
        return null;
    }

    private static Object getVar(Map<String, Object> data, String varName, Object notFound) {
        String[] keys = varName.split("\\.");
        Object value = data;
        for (String key : keys) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(key);
            } else if (value instanceof List) {
                try {
                    int index = Integer.parseInt(key);
                    value = ((List<?>) value).get(index);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    return notFound;
                }
            } else {
                return notFound;
            }
        }
        return value;
    }

    private static List<String> missing(Map<String, Object> data, List<String> varNames) {
        List<String> missing = new ArrayList<>();
        for (String varName : varNames) {
            if (getVar(data, varName, null) == null) {
                missing.add(varName);
            }
        }
        return missing;
    }

    private static List<String> missingSome(Map<String, Object> data, int minRequired, List<String> varNames) {
        if (minRequired < 1) {
            return Collections.emptyList();
        }
        int found = 0;
        List<String> missing = new ArrayList<>();
        for (String varName : varNames) {
            if (getVar(data, varName, null) == null) {
                missing.add(varName);
            } else {
                found++;
                if (found >= minRequired) {
                    return Collections.emptyList();
                }
            }
        }
        return missing;
    }

    @SuppressWarnings("unchecked")
    public static Object apply(Map<String, Object> tests, Map<String, Object> data) {
        if (tests == null || !(tests instanceof Map)) {
            return tests;
        }

        String operator = (String) tests.keySet().iterator().next();
        Object operfunc = tests.get(operator);
        List<Object> args = new ArrayList<>();

        //Easy syntax for unary operators, like {"var": "x"} instead of strict{"var": ["x"]}
        if (!(operfunc instanceof List)) {
            operfunc = Collections.singletonList(operfunc);
        }

        for (Object value : (List<?>) operfunc) {
            if( value instanceof Map) {
                args.add(apply((Map<String, Object>) value, data));
            } else {
                args.add(value);
            }
        }

        if ("var".equals(operator)) {
            return getVar(data, (String) args.get(0), null);
        }
        if ("missing".equals(operator)) {
            // convert and verify args is a list of strings
            List<String> varNames = args.stream().map(String::valueOf).collect(Collectors.toList());
            return missing(data, varNames);
        }
        if ("missing_some".equals(operator)) {
            return missingSome(data, ((Number) args.get(0)).intValue(), (List<String>) args.get(1));
        }

        Function<List<Object>, Object> operation = OPERATIONS.get(operator);
        if (operation == null) {
            throw new IllegalArgumentException("Unrecognized operation: " + operator);
        }

        return operation.apply(args);
    }

    public static Object evaluate(String jsonLogic, Map<String, Object> data) throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> logic = objectMapper.readValue(jsonLogic, new TypeReference<Map<String, Object>>() {});

        return apply(logic, data);
    }
}