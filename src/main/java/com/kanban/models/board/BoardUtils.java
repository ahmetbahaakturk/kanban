package com.kanban.models.board;

import java.security.SecureRandom;
import java.util.Base64;

public class BoardUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final int MIN_LENGTH = 20;
    public static final int MAX_LENGTH = 40;

    private BoardUtils() {
    }

    public static String generatePublicId() {
        // Pick the publicId length randomly within the allowed bounds.
        int length = RANDOM.nextInt(MIN_LENGTH, MAX_LENGTH + 1);
        byte[] randomBytes = new byte[bytesNeededForBase64Length(length)];

        // Fill the byte array with secure random data.
        RANDOM.nextBytes(randomBytes);

        // Produce a URL-safe Base64 value without padding characters.
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes)
            .substring(0, length);
    }

    // Calculate the number of random bytes needed for the requested Base64 length.
    private static int bytesNeededForBase64Length(int length) {
        return (int) Math.ceil(length * 3 / 4.0);
    }
}
