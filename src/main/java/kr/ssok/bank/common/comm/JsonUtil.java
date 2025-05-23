package kr.ssok.bank.common.comm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;

/**
 * JsonUtil 클래스
 *
 */
@NoArgsConstructor
public class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object object){
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("[JsonUtil] JSON 변환 실패", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) throws Exception {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("[JsonUtil] JSON 파싱 실패", e);
        }
    }
}

