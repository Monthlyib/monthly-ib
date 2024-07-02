package com.monthlyib.server.constant;

import lombok.Getter;

@Getter
public enum AwsProperty {

    // todo 디렉토리 이름 수정 필요
    MONTHLYIB_IMAGE("monthly-ib/image/"),
    MONTHLYIB_PDF("monthly-ib/pdf/"),
    BOARD_FILE("board/file/"),
    NEWS_FILE("news/file/"),
    VIDEO_LESSONS_THUMBNAIL("video-lessons/thumbnail/"),
    STORAGE("storage/"),
    USER_IMAGE("user/image/"),

    ZIP_DIR_NAME("zip/");

    private final String name;

    AwsProperty(String name) {
        this.name = name;
    }
}
