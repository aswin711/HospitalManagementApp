package com.synnefx.cqms.event.core;

/**
 * Bootstrap constants
 */
public final class Constants {
    private Constants() {
    }

    public static final String APP_ID = "events";

    public static final class Auth {
        private Auth() {
        }

        /**
         * Account type id
         */
        public static final String BOOTSTRAP_ACCOUNT_TYPE = "com.synnefx.cqms.event";

        /**
         * Account name
         */
        public static final String BOOTSTRAP_ACCOUNT_NAME = "CQMS Events";

        /**
         * Provider id
         */
        public static final String BOOTSTRAP_PROVIDER_AUTHORITY = "com.synnefx.cqms.event.sync";

        /**
         * Auth token type
         */
        public static final String AUTHTOKEN_TYPE = BOOTSTRAP_ACCOUNT_TYPE;
    }

    /**
     * All HTTP is done through a REST style API built for demonstration purposes on Parse.com
     * Thanks to the nice people at Parse for creating such a nice system for us to use for bootstrap!
     */
    public static final class Http {
        private Http() {
        }


        /**
         * Base URL for all requests
         */
        public static final String URL_BASE = "https://app.cqms.synnefx.co.in/api/";
        // public static final String URL_BASE =  "http://192.168.0.100:8085/api/";

        /**
         * Authentication URL
         */
        // public static final String URL_AUTH_FRAG = "1/login";
        public static final String URL_AUTH_FRAG = "auth/login";
        public static final String URL_AUTH = URL_BASE + URL_AUTH_FRAG;

        public static final String URL_GCM_REG = "user/gcmreg";
        public static final String URL_DEVICE_REG = "user/devicereg";

        public static final String URL_APP_VERSION = "support/getLatestVersion";

        /**
         * List Users URL
         */
        public static final String URL_USER_PROFILE = "user/getUser";
        public static final String URL_USERS_FRAG = "1/users";
        public static final String URL_USERS = URL_BASE + URL_USERS_FRAG;


        /**
         * List Service types URL
         */
        public static final String URL_IMPORT_SERVICES = "import/servicetypes";

        public static final String URL_IMPORT_SPECIALTY = "import/speciality";

        public static final String URL_IMPORT_INCIDENTTYPES = "import/incidenttype";

        /**
         * List Units URL
         */
        public static final String URL_IMPORT_UNITS = "import/units";

        public static final String URL_IMPORT_CASESHETT_QUESTIONS = "import/casesheetform";

        public static final String URL_PUSH_CASESHEET_AUDIT = "casesheet/pushOpenCaseSheet";

        public static final String URL_PULL_CASESHEET_AUDIT = "casesheet/pullOpenCaseSheet";

        public static final String URL_PUSH_HH_AUDIT = "hhaudit/pushHHAuditSession";

        public static final String URL_PULL_HH_AUDIT = "hhaudit/pullHHAuditSession";

        //Incident report
        public static final String URL_PUSH_INCIDENT = "eventreport/pushIncident";

        public static final String URL_PULL_INCIDENT = "eventreport/pushIncident";


        public static final String URL_PUSH_MEDICATION_ERROR = "eventreport/pushMedicationError";

        public static final String URL_PULL_MEDICATION_ERROR = "eventreport/pushMedicationError";


        public static final String URL_PUSH_DRUGREACTION_ERROR = "eventreport/pushAdverseDrugReactionr";

        public static final String URL_PULL_DRUGREACTION_ERROR = "eventreport/pushAdverseDrugReaction";


        /**
         * List News URL
         */
        public static final String URL_NEWS_FRAG = "1/classes/News";
        public static final String URL_NEWS = URL_BASE + URL_NEWS_FRAG;


        /**
         * List Checkin's URL
         */
        public static final String URL_CHECKINS_FRAG = "1/classes/Locations";
        public static final String URL_CHECKINS = URL_BASE + URL_CHECKINS_FRAG;

        /**
         * PARAMS for auth
         */
        public static final String PARAM_USERNAME = "username";
        public static final String PARAM_PASSWORD = "password";


        public static final String PARSE_APP_ID = "zHb2bVia6kgilYRWWdmTiEJooYA17NnkBSUVsr4H";
        public static final String PARSE_REST_API_KEY = "N2kCY1T3t3Jfhf9zpJ5MCURn3b25UpACILhnf5u9";
        public static final String HEADER_PARSE_REST_API_KEY = "X-Parse-REST-API-Key";
        public static final String HEADER_PARSE_APP_ID = "X-Parse-Application-Id";

        public static final String HEADER_DEVICE_ID = "Device-ID";
        public static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
        public static final String CONTENT_TYPE_JSON = "application/json;versions=1";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String SESSION_TOKEN = "sessionToken";


    }


    public static final class Extra {
        private Extra() {
        }

        public static final String NEWS_ITEM = "session_item";
        public static final String HH_SESSION_ITEM = "hh_session_item";
        public static final String HH_SESSION_REF = "hh_session_ref";
        public static final String HH_SESSION_STATUS = "hh_session_status";
        public static final String HH_SESSION_ADD_OBSERVATION = "hh_sessionadd_observation";
        public static final String USER = "user";

        public static final String CASESHEET_ITEM = "casesheet_item";
        public static final String CASESHEET_REF = "casesheet_ref";

        public static final String INCIDENT_ITEM = "incident_item";
        public static final String INCIDENT_REF = "incident_ref";

    }

    public static final class Intent {
        private Intent() {
        }

        /**
         * Action prefix for all intents created
         */
        public static final String INTENT_PREFIX = "com.synnefx.cqms.";

    }

    public static class Notification {
        private Notification() {
        }

        public static final int TIMER_NOTIFICATION_ID = 1;
        public static final int DEFAULT_NOTIFICATION_ID = 1000; // Why 1000? Why not? :)
        public static final int UPLOAD_NOTIFICATION_ID = 1010;
        public static final int IMPORT_NOTIFICATION_ID = 1020;
        public static final int IMPORT_UNIT_NOTIFICATION_ID = 1021;
        public static final int IMPORT_SPECIALTY_NOTIFICATION_ID = 1023;
        public static final int IMPORT_INCIDENTTYPE_NOTIFICATION_ID = 1025;
    }

    public static class Common {
        private Common() {
        }

        public static final String DATE_DISPLAY_FORMAT = "EEE, d MMM yyyy";
        public static final String DATE_TIME_DISPLAY_FORMAT = "EEE, d MMM yyyy HH:mm aa";
        public static final String DATE_TIME_APP_SERVER_FORMAT = "dd/MMM/yyyy hh:mm a";
        public static final String DATE_INTERNAL_FORMAT = "MM/dd/yyyy";

        public static final String BLANK = " ";
        public static final String EMPTY = " ";
        public static final String NEW_LINE = "\n";
        public static final String YESTERDAY = "Yesterday";
    }
}


