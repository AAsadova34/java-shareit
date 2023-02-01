package ru.practicum.shareit.log;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@UtilityClass
@Slf4j
public class Logger {
    public static void logWarnException(Throwable e) {
        log.warn(e.getClass().getSimpleName(), e);
    }

    public static void logRequest(HttpMethod method, String uri, String headers, String body) {
        log.info("Endpoint request received: '{} {}'. Headers: '{}'. Request body: '{}'", method, uri, headers, body);
    }

    public static void logStorageChanges(String action, String object) {
        log.info("'{}': '{}'", action, object);
    }
}
