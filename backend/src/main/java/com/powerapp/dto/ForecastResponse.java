package com.powerapp.dto;

public class ForecastResponse {
    public String scope;
    public Double forecast;
    public String method;

    public ForecastResponse(String scope, Double forecast, String method) {
        this.scope = scope;
        this.forecast = forecast;
        this.method = method;
    }
}
