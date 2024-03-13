package com.sciumo.jsonlogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonLogicTest {

    @Test
    public void testSimple() throws JsonProcessingException {
        String jsonString = "{\"==\": [{\"var\": \"a\"}, 1]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(true, result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(true, result);
    }

    @Test
    public void testComplex() throws JsonProcessingException {
        String jsonString = "{\"and\": [{\">\": [{\"var\": \"temp\"}, 110]}, {\"==\": [{\"var\": \"pie.filling\"}, \"apple\"]}]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("temp", 120);
        Map<String, Object> pie = new HashMap<>();
        pie.put("filling", "apple");
        data.put("pie", pie);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(true, result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(true, result);
    }

    @Test
    public void testMerge() throws JsonProcessingException {
        String jsonString = "{ \"join\": [\" \", { \"merge\": [{\"var\": \"a\"}, {\"var\": \"b\"}]}] }";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", "Hello");
        data.put("b", "world!");

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals("Hello world!", result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals("Hello world!", result);
    }

    @Test
    public void testCount() throws JsonProcessingException {
        String jsonString = "{\"count\": [{\"var\": \"a\"}]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        data.put("c", 3);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(1, result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(1, result);
    }

    @Test
    public void testIf() throws JsonProcessingException {
        String jsonString = "{\"if\": [{\"var\": \"a\"}, 1, 2]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", true);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(1, result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(1, result);
    }

    @Test
    public void testIfElse() throws JsonProcessingException {
        String jsonString = "{\"if\": [{\"var\": \"a\"}, 1, 2]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", false);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(2, result);

        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(2, result);
    }

    @Test
    public void testIfElseIf() throws JsonProcessingException {
        String jsonString = "{\"if\": [{\"var\": \"a\"}, 1, {\"if\": [{\"var\": \"b\"}, 2, 3]}]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", false);
        data.put("b", true);

        Object result = JsonLogic.apply(jsonLogic, data);
        assertEquals(2, result);
        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(2, result);

        data.put("b", false);

        result = JsonLogic.apply(jsonLogic, data);
        assertEquals(3, result);
        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(3, result);

        data.put("a", true);

        result = JsonLogic.apply(jsonLogic, data);
        assertEquals(1, result);
        result = JsonLogic.evaluate(jsonString, data);
        assertEquals(1, result);
    }

    @Test
    public void testMissing() throws JsonProcessingException {
        String jsonString = "{\"missing\":[\"a\", \"b\"]}";
        ObjectMapper objectMapper = new ObjectMapper();
        // Use TypeReference to specify the type of the deserialized object
        Map<String, Object> jsonLogic = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        data.put("c", 3);

        Object result = JsonLogic.apply(jsonLogic, data);
        // assert result is a collection and empty
        assertEquals(true, result instanceof Iterable);
        assertEquals(false, ((Iterable<?>) result).iterator().hasNext());
        data.remove("a");

        result = JsonLogic.apply(jsonLogic, data);
        assertEquals(true, result instanceof Iterable);
        assertEquals(true, ((Iterable<?>) result).iterator().hasNext());
        assertEquals("a", ((Iterable<?>) result).iterator().next());
    }
}