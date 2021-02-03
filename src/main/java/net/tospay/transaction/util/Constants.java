package net.tospay.transaction.util;

public class Constants {
    public static final Integer MAX_FETCH_LIMIT = 1000;
    public static final Integer MAX_STATEMENT_LIMIT = 100000;
    public static  boolean LICENSE_ACTIVE = true;
    public static final Double MAX_DISTANCE_LIMIT = 100d;

    public static class URL {
        public static final String API = "/api";
        public static final String API_VER = API + "/v1";
        public static final String TRANSFER = "/transfer";
        public static final String TRANSFER_STAGE = "/transfer/stage";
        public static final String TRANSFER_CONTINUE = "/transfer/continue";
        public static final String TRANSFER_COMMISSION = "/transfer/commission";
        public static final String TRANSFER_LOG = "/transfer/log";

        public static final String REVERSAl_MAKE = "/reversal/make";
        public static final String REVERSAl_CHECK = "/reversal/check";
        public static final String REVERSAl_FETCH = "/reversal/fetch";

        public static final String TRANSACTION_INIT_MAKE = "/transaction_init/make";
        public static final String TRANSACTION_INIT_CHECK = "/transaction_init/check";
        public static final String TRANSACTION_INIT_FETCH = "/transaction_init/fetch";

        public static final String TRANSACTION_CONFIG_MAKE = "/transaction_config/make";
        public static final String TRANSACTION_CONFIG_CHECK = "/transaction_config/check";
        public static final String TRANSACTION_CONFIG_FETCH = "/transaction_config/fetch";

        public static final String CALLBACK_MOBILE = "/mobile/callback";
        public static final String CALLBACK_CARD = "/card/callback";
        public static final String CALLBACK_BANK = "/bank/callback";

        public static final String TRANSACTIONS_FETCH = "/transactions/fetch";
        public static final String TRANSACTIONS_FETCH_LIKE = "/transactions/fetch_like";
        public static final String TRANSACTIONS_STATEMENT = "/transactions/statement";
        public static final String TRANSACTIONS_ID = "/transactions/fetch/id";
        public static final String TRANSACTIONS_STATUS_ID = "/transactions/status/id";

        public static final String PARTNER_TRANSACTIONS_FETCH = "/partner/transactions/fetch";
        public static final String PARTNER_TRANSACTIONS_ID = "/partner/transactions/fetch/id";
        public static final String PARTNER_TRANSACTIONS_STATUS_ID = "/partner/transactions/status/id";

        public static final String PARTNER_DASHBOARD_TRANSACTIONS_FETCH = "/partner/dashboard/transactions/fetch";
        public static final String PARTNER_DASHBOARD_TRANSACTIONS_FETCH_USER = "/partner/dashboard/transactions/fetch/user";
        public static final String PARTNER_DASHBOARD_TRANSACTIONS_ID = "/partner/dashboard/transactions/fetch/id";
        public static final String PARTNER_DASHBOARD_TRANSACTIONS_REVENUE = "/partner/dashboard/transaction/revenue";
        public static final String PARTNER_DASHBOARD_TRANSACTIONS_REVENUE_TABLE = "/partner/dashboard/transaction/revenue/table";


        public static final String ADMIN_TRANSACTIONS_FETCH = "/admin/transactions/fetch";
        public static final String ADMIN_TRANSACTIONS_STATEMENT = "/admin/transactions/statement";
        public static final String ADMIN_TRANSACTIONS_STATEMENT_DOC = "/admin/transactions/statement/doc";


        public static final String DASHBOARD_TRANSACTIONS_REVENUE = "/dashboard/transaction/revenue";

        public static final String LICENSE =  "/license";
        public static final String LICENSE_ENABLE =  "/license/enable";
        public static final String LICENSE_DISABLE =  "/license/disable";


        public static final String REPORT = "/report";
        public static final String REPORT_ALL = "/report/all";
    }

}
