package com.kanban.models.board;

public class BoardUtils {
    public static final int MIN_PUBLIC_ID_LENGTH = 4;
    public static final int MAX_PUBLIC_ID_LENGTH = 60;

    // Keeps publicId values safe to use inside URLs.
    public static final String URL_SAFE_PUBLIC_ID_REGEX = "^[A-Za-z0-9._~-]+$";

    // Prevents this utility class from being instantiated.
    private BoardUtils() {
    }
}
