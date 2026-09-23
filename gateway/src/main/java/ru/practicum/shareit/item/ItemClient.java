package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getAll() {
        return get("/all");
    }

    public ResponseEntity<Object> getByUserId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getById(long userId, long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> addComment(long userId, long id, CommentDto dto) {
        return post("/" + id + "/comment", userId, dto);
    }

    public ResponseEntity<Object> updateItem(long userId, long id, UpdateItemDto dto) {
        return patch("/" + id, userId, dto);
    }

    public ResponseEntity<Object> deleteItem(long userId, long id) {
        return delete("/" + id, userId);
    }
}
