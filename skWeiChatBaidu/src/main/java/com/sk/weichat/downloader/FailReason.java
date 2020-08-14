
package com.sk.weichat.downloader;

public class FailReason {

    private final FailType type;

    private final Throwable cause;

    public FailReason(FailType type, Throwable cause) {
        this.type = type;
        this.cause = cause;
    }

    /**
     * @return {@linkplain FailType Fail type}
     */
    public FailType getType() {
        return type;
    }

    /**
     * @return Thrown exception/error, can be <b>null</b>
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Presents type of fail while image loading
     */
    public static enum FailType {
        /**
         * Input/output error. Can be caused by network communication fail or
         * error while caching image on file system.
         */
        IO_ERROR,

        /* URI为空，不可用 */
        URI_EMPTY,

        /**
         * Unknown error was occurred while loading image
         */
        UNKNOWN
    }
}