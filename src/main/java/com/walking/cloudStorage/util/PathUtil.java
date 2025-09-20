package com.walking.cloudStorage.util;

import com.walking.cloudStorage.domain.exception.BadRequestException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {

    public static boolean isDirectory(String path) {
        return path != null && path.endsWith("/");
    }

    public static String userRoot(Long userId) {
        return "user-%d-files/".formatted(userId);
    }

    public static String parentOf(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    public static String nameOf(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String buildObjectName(String path, Long userId) {
        return userRoot(userId).concat(path);
    }

    public static void pathNonEmpty(String path, String parameterName) {
        if (path == null || path.isBlank()) {
            throw new BadRequestException("%s cannot be empty".formatted(parameterName));
        }
    }

    public static void pathNonEmpty(String path) {
        pathNonEmpty(path, "Path");
    }

    public static void pathWithoutLeadingSlash(String path, String parameterName) {
        if (path != null && path.startsWith("/")) {
            throw new BadRequestException("%s cannot start with a slash".formatted(parameterName));
        }
    }

    public static void pathWithoutLeadingSlash(String path) {
        pathWithoutLeadingSlash(path, "Path");
    }

    public static void pathWithTrailingSlash(String path) {
        if (path == null || !path.endsWith("/")) {
            throw new BadRequestException("Path must end with a slash");
        }
    }

    public static void pathEmptyOrWithTrailingSlash(String path) {
        if (path == null) return;

        if (!path.isEmpty() && !path.endsWith("/")) {
            throw new BadRequestException("Path must end with a slash when specifying a directory");
        }
    }

    public static void fromAndToNonEqual(String from, String to) {
        if (from.equals(to)) {
            throw new BadRequestException("Source and destination paths cannot be identical");
        }
    }
}
