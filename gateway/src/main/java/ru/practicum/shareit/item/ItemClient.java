package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.comment.CommentInnerDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long userId, ItemInnerDto itemInnerDto) {
        return post("", userId, itemInnerDto);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemInnerDto itemInnerDto) {
        return patch("/" + itemId, userId, itemInnerDto);
    }

    public ResponseEntity<Object> getItemById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItems(long userId, Integer from, Integer size) {
        if (from == null || size == null) {
            return get("", userId);
        } else {
            Map<String, Object> parameters = Map.of(
                    "from", from,
                    "size", size
            );
            return get("?from={from}&size={size}", userId, parameters);
        }
    }

    public ResponseEntity<Object> findItemsByNameOrDescription(long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters;
        if (from == null || size == null) {
            parameters = Map.of(
                    "text", text
            );
            return get("/search?text={text}", userId, parameters);
        } else {
            parameters = Map.of(
                    "text", text,
                    "from", from,
                    "size", size
            );
            return get("/search?text={text}&from={from}&size={size}", userId, parameters);
        }
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentInnerDto commentInnerDto) {
        return post("/" + itemId + "/comment", userId, commentInnerDto);
    }
}
