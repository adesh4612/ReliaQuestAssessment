package com.example.rqchallenge.service;

public class Endpoint {
    public static final String ENDPOINT_URL = "https://dummy.restapiexample.com";
    public static final String API_VERSION_V1 = "v1";
    public static final String V1_ENDPOINT = getV1Endpoint();


    public static String getV1Endpoint() {
        return ENDPOINT_URL + "/api/" + API_VERSION_V1;
    }

}
