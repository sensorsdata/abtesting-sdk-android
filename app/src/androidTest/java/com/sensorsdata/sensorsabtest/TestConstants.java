package com.sensorsdata.sensorsabtest;

public interface TestConstants {
    String SA_SERVER_URL = "http://10.129.20.65:8106/sa?project=test_091";
    String AB_DISPATCH_SERVER_URL = "http://10.129.20.96:8202/api/v2/abtest/online/results?project-key=6654CD0907B729D4DAE175BA30E11143E52822A9";
    String MOCK_DATA = "{\n" +
            "    \"status\": \"SUCCESS\",\n" +
            "    \"results\":\n" +
            "    [\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"334\",\n" +
            "            \"is_white_list\": false,\n" +
            "            \"abtest_experiment_group_id\": \"1\",\n" +
            "            \"abtest_experiment_result_id\": \"3340101\",\n" +
            "            \"experiment_type\": \"CODE\",\n" +
            "            \"subject_id\": \"4i90jfsdrlkfsj402932\",\n" +
            "            \"subject_name\": \"user/device/custom\",\n" +
            "            \"abtest_experiment_version\": \"1\",\n" +
            "            \"stickiness\": \"STICKINESS\",\n" +
            "             \"variables\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"name\": \"test_int\",\n" +
            "                    \"type\": \"INTEGER\",\n" +
            "                    \"value\": \"111\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"test_str\",\n" +
            "                    \"type\": \"STRING\",\n" +
            "                    \"value\": \"Hello\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"test_bool\",\n" +
            "                    \"type\": \"BOOLEAN\",\n" +
            "                    \"value\": \"true\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"test_json\",\n" +
            "                    \"type\": \"JSON\",\n" +
            "                    \"value\": \"{\\\"json_key\\\": \\\"json_value\\\"}\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "        \n" +
            "    ],\n" +
            "    \"out_list\":\n" +
            "    [\n" +
            "        {\n" +
            "            \"abtest_experiment_id\": \"1006\",\n" +
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
            "                    \"name\": \"ming\",\n" +
            "                    \"type\": \"String\",\n" +
            "                    \"value\": \"xiaoming\"\n" +
            "                },\n" +
            "            ],\n" +
            "            \"abtest_experiment_result_id\": \"-1\",\n" +
            "            \"subject_id\": \"a8a98fe7481fb309\",\n" +
            "            \"subject_name\": \"USER\",\n" +
            "            \"abtest_experiment_version\": \"14\",\n" +
            "            \"stickiness\": \"UNSTICKINESS\"\n" +
            "        }  \n" +
            "        \n" +
            "    ],\n" +
            "    \"fuzzy_experiments\":\n" +
            "    [\n" +
            "        \"TeamEntranceType\",\n" +
            "        \"goodImageConfig\"\n" +
            "    ],\n" +
            "    \"track_config\":\n" +
            "    {\n" +
            "        \"item_switch\": false,\n" +
            "        \"trigger_switch\": true,\n" +
            "        \"property_set_switch\": false,\n" +
            "        \"trigger_content_ext\":\n" +
            "        [\n" +
            "            \"abtest_experiment_version\",\n" +
            "            \"abtest_experiment_result_id\"\n" +
            "        ],\n" +
            "        \"item_content\":\n" +
            "        {\n" +
            "            \"item_name\": \"experimenet_trigger\",\n" +
            "            \"content\":\n" +
            "            [\n" +
            "                \"abtest_experiment_id\",\n" +
            "                \"abtest_experiment_group_id\",\n" +
            "                \"abtest_experiment_result_id\",\n" +
            "                \"abtest_experiment_version\"\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
}
