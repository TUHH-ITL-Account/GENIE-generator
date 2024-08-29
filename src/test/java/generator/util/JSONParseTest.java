package generator.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class JSONParseTest {

  static final String refString = "{\"topic_actual\":\"Stetigförderer\",\"difficulty\":5,\"exercise_type_recommended\":[\"multiple_choice\"]}";
  // use LinkedHashMap because it has an ordering
  static final Map<String, Object> refMap = new LinkedHashMap<>();

  static {
    refMap.put("topic_actual", "Stetigförderer");
    refMap.put("difficulty", 5);
    List<String> exTypes = new ArrayList<>();
    exTypes.add("multiple_choice");
    refMap.put("exercise_type_recommended", exTypes);
  }

  private Map<String, Object> convertJson2Map(String input) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(input, new TypeReference<Map<String, Object>>() {
    });
  }

  private String convertMapToJson(Map<String, Object> input) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(input);
  }

  @Test
  public void testStringToMap() throws JsonProcessingException {
    Map<String, Object> map = convertJson2Map(refString);
    assertThat("topic_actual is as expected.", map.get("topic_actual"),
        is(refMap.get("topic_actual")));
    assertThat("difficulty is as expected.", map.get("difficulty"), is(refMap.get("difficulty")));
    assertThat("exercise_type_recommended is List type",
        map.get("exercise_type_recommended") instanceof List);
    assertThat("exercise_type_recommended is List type",
        ((List<String>) map.get("exercise_type_recommended")).get(0), is("multiple_choice"));
  }

  @Test
  public void testMapToString() throws JsonProcessingException {
    String string = convertMapToJson(refMap);
    assertThat("string is as expected.", string, equalToCompressingWhiteSpace(refString));
  }

  @Test
  public void testEmptyString() throws JsonProcessingException {
    String jsonString = "{}";
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map;
    map = objectMapper.readValue(jsonString, new TypeReference<>() {
    });
    assertThat("empty map works", map.isEmpty());
  }

  @Test
  public void testSetToJson() throws JsonProcessingException {
    Set<String> test = Set.of("a","b","c");
    ObjectMapper objectMapper = new ObjectMapper();
    String result = objectMapper.writeValueAsString(test);
    assertThat("Set is there and has expected length", result.length(), is(13));
  }
}
