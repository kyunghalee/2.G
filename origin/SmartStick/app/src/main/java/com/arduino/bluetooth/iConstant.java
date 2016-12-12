package com.arduino.bluetooth;

import java.util.UUID;

/**
 * 전역적으로 사용될 상수들의 집합
 */
public interface iConstant {

    /* app setting */
    String DEBUG_TAG = "arduino";	// 디버그 태그

    // 액티비티 요청값
    int REQUEST_ENABLE = 0;
    int SEARCH_DEVICE = 1;

    int ALL = 0;
    int RANDOM1 = 1;
    int RANDOM2 = 2;
    int EVEN = 3;
    int ODD = 4;
    int NORMAL = 5;
    // uuid 설정
    // RFCOMM Channel 통신을 위한 블루투스 표준 uuid값
    UUID BT_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    String DEVICE = "device";
    int MAX_CONNECT_COUNT = 6;

}
