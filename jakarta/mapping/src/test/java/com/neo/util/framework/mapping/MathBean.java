package com.neo.util.framework.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.json.JsonUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.util.Map;

@Named("mathBean")  // Makes it available in EL as #{mathBean}
@RequestScoped  // Ensures it's a singleton in the app
public class MathBean {

    private JsonNode node = JsonUtil.misingNode();

    public MathBean() {
        System.out.println("AA");
    }

    public int add(int a, int b) {
        return a + b;
    }

    public Map<String, String> getTest() {
        return Map.of("idk", "5");
    }

    public JsonNode getA() {
        return node;
    }
}
