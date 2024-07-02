package com.monthlyib.server.constant;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TutoringTime {

    ONE(9,0),
    TWO(9,30),
    THREE(10,0),
    FOUR(10,30),
    FIVE(11,0),
    SIX(11,30),
    SEVEN(12,0),
    EIGHT(12,30),
    NINE(13,0),
    TEN(13,30),
    ELEVEN(14,0),
    TWELVE(14,30),
    THIRTEEN(15,0),
    FOURTEEN(15,30),
    FIFTEEN(16,0),
    SIXTEEN(16,30),
    SEVENTEEN(17,0),
    EIGHTEEN(17,30),
    NINETEEN(18,0),
    TWENTY(18,30),
    TWENTY_ONE(19,0),
    TWENTY_TWO(19,30),
    TWENTY_THREE(20,0),
    TWENTY_FOUR(20,30),
    TWENTY_FIVE(21,0),
    TWENTY_SIX(21,30),
    TWENTY_SEVEN(22,0),
    ;

    private final int hour;

    private final int minute;
}
