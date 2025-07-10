package com.han.oauth_practice.utils.data_store;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class GlobalDataStore {
    private final ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    public void put(String key, Object value) { map.put(key, value); }

    public Object get(String key) { return map.get(key); }

    public boolean contains(String key) { return map.containsKey(key); }
}
