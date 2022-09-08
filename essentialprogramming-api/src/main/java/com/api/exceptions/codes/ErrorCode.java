package com.api.exceptions.codes;


import com.util.exceptions.ErrorCodes;

/**
 * Error code enum.
 *
 */
public enum ErrorCode implements ErrorCodes.ErrorCode {


    USER_NOT_FOUND("MICRO-SERVICE-ERR1", "User not found"),
    USER_ALREADY_EXISTS("MICRO-SERVICE-ERR2", "Email not available"),
    UNABLE_TO_DECRYPT_API_KEY("MICRO-SERVICE-ERR3", "Unable to decrypt Datadog api key"),
    UNABLE_TO_DECRYPT_APPLICATION_KEY("MICRO-SERVICE-ERR4", "Unable to decrypt Datadog application key");

    private final String code;
    private final String description;


    /**
     * The standard constructor.
     *
     * @param code        the error code
     * @param description the error description
     */
    ErrorCode(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

}
