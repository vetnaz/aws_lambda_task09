package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task09.api.OpenMeteoWeather;
import com.task09.model.Forecast;
import com.task09.model.WeatherRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<Object, Map<String, Object>> {
    private AmazonDynamoDB amazonDynamoDB;
    private static final String REGION = "eu-central-1";

    public Map<String, Object> handleRequest(Object request, Context context) {
        this.initDynamoDbClient();
        Gson gson = new Gson();
        OpenMeteoWeather openMeteoWeather = new OpenMeteoWeather();

        Forecast forecast = gson.fromJson(openMeteoWeather.callApi(), new TypeToken<Forecast>() {
        }.getType());
        WeatherRecord weatherRecord = new WeatherRecord();
        weatherRecord.setId(UUID.randomUUID().toString());
        weatherRecord.setForecast(forecast);

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
        mapper.save(weatherRecord);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "Successfully");
        return resultMap;
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }
}
