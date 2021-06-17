package com.example.jorge.androidapp.constantes;

public class KConstantesShareContent {


    public static final class KEYS {

        public static final String KEY_USERNAME = "key_username";

    }
    public static final class DEFAULT{
        public static final String UNKNOWN = "UNKNOWN";
        public static final String DEFAULT_DISPLAY_NAME ="Anonymous";
    }

    public static final class BUNDLE{
        public static final String ENDPOINT_DESTINATION = "ENDPOINT_DESTINATION";
        public static final String IS_REQUEST_CLIENT = "IS_REQUEST_CLIENT";
        public static final String IS_MULTI_FILE = "IS_REQUEST_FILE";
        public static final String PUNISHED = "PUNISH_";

    }

    public static final class ACTIVITY_RESULT_CODE{
        public static final int PICK_IMAGE_REQUEST_CODE = 1;
        public static final int PICKFILE_REQUEST_CODE = 2;
        public static final int MULTIFILE_REQUEST_CODE = 3;
        public static final int NOTIFICATION_REQUEST_CODE = 4;
    }
}
