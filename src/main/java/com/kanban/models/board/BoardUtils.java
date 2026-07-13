package com.kanban.models.board;

public class BoardUtils {
    public static final int MIN_PUBLIC_ID_LENGTH = 4;
    public static final int MAX_PUBLIC_ID_LENGTH = 60;

    // URL safe publicId oluşturmak için gerekli regex yapısı
    public static final String URL_SAFE_PUBLIC_ID_REGEX = "^[A-Za-z0-9._~-]+$";
}
