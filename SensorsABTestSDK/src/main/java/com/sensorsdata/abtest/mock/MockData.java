package com.sensorsdata.abtest.mock;

public class MockData {
    public static final String RESPONSE = "{\n" +
            "    \"status\": \"SUCCESS\",\n" +
            "    \"fuzzy_experiments\":\n" +
            "    [\n" +
            "        \"aa\",\n" +
            "        \"device_test\",\n" +
            "        \"javasdk_custom_properties\",\n" +
            "        \"device_code_user_filter\",\n" +
            "        \"cqs_device\"\n" +
            "    ],\n" +
            "    \"results\":\n" +
            "    [\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1025\",\n" +
            "            \"abtest_experiment_group_id\": \"0\",\n" +
            "            \"is_control_group\": true,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"TIME_PIECE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"local_time_device\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"1\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10250100\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"UNSTICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1026\",\n" +
            "            \"abtest_experiment_group_id\": \"0\",\n" +
            "            \"is_control_group\": true,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"TIME_PIECE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"local_time_custom\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"1\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10260100\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"UNSTICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1030\",\n" +
            "            \"abtest_experiment_group_id\": \"0\",\n" +
            "            \"is_control_group\": true,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"sdk_cacheable_true\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"1\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10300100\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"STICKINESS\",\n" +
            "            \"cacheable\": true\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1015\",\n" +
            "            \"abtest_experiment_group_id\": \"3\",\n" +
            "            \"is_control_group\": false,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"cqs_color\",\n" +
            "                    \"type\": \"STRING\",\n" +
            "                    \"value\": \"green\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"cqs_index\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"3\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10150103\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"DEVICE\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"STICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1032\",\n" +
            "            \"abtest_experiment_group_id\": \"0\",\n" +
            "            \"is_control_group\": true,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"javasdk_unstickiness\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"1\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10320500\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"5\",\n" +
            "            \"stickiness\": \"STICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1016\",\n" +
            "            \"abtest_experiment_group_id\": \"1\",\n" +
            "            \"is_control_group\": false,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"li\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"2\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"name\",\n" +
            "                    \"type\": \"STRING\",\n" +
            "                    \"value\": \"xiaohong\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10161001\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"10\",\n" +
            "            \"stickiness\": \"UNSTICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        },\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1035\",\n" +
            "            \"abtest_experiment_group_id\": \"2\",\n" +
            "            \"is_control_group\": false,\n" +
            "            \"is_white_list\": false,\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"int\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"3\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"str\",\n" +
            "                    \"type\": \"STRING\",\n" +
            "                    \"value\": \"sy2\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"bl\",\n" +
            "                    \"type\": \"BOOLEAN\",\n" +
            "                    \"value\": \"false\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"js\",\n" +
            "                    \"type\": \"JSON\",\n" +
            "                    \"value\": \"{\\\"json3\\\":789}\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"10350102\",\n" +
            "            \"subject_id\": \"1ab777146148bed1\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"STICKINESS\",\n" +
            "            \"cacheable\": false\n" +
            "        }\n" +
            "    ],\n" +
            "    \"track_config\":\n" +
            "    {\n" +
            "        \"item_switch\": false,\n" +
            "        \"trigger_switch\": true,\n" +
            "        \"property_set_switch\": false\n" +
            "        \"trigger_content_ext\":\n" +
            "        [\n" +
            "            \"abtest_experiment_result_id\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"out_list\":\n" +
            "    []\n" +
            "}";
}
