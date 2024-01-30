package org.example;

//текст решения предполагает, что json гарантированно содержит все необходимые поля и их значения не null,
//не вводил проверку на всех этапах чтения данных, дабы сократить место
//разница в часовых поясах посчитана мануально конкретно для этого JSON файла, в реальной работе, конечно, нужно
//все считать автоматизированно

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File("src/main/resources/flights_and_forecast.json");
            JsonNode jsonData = objectMapper.readTree(jsonFile);

            JsonNode flights = jsonData.get("flights");
            JsonNode forecast = jsonData.get("forecast");

            for (JsonNode flight : flights) {
                String flightNo = flight.get("no").asText();
                String departureCity = flight.get("from").asText();
                String arrivalCity = flight.get("to").asText();
                int departureTime = flight.get("departure").asInt();
                int duration = flight.get("duration").asInt();

                int timeZoneDifference = getTimeZoneDifference(departureCity, arrivalCity);

                int adaptedDepartureTime = (departureTime + timeZoneDifference) % 24;
                int arrivalTime = (adaptedDepartureTime + duration) % 24;

                JsonNode departureWeather = forecast.get(departureCity).get(adaptedDepartureTime);

                int windSpeed = departureWeather.get("wind").asInt();
                int visibility = departureWeather.get("visibility").asInt();
                if (windSpeed <= 30 && visibility >= 200) {
                    JsonNode arrivalWeather = forecast.get(arrivalCity).get(arrivalTime);

                    int arrivalWindSpeed = arrivalWeather.get("wind").asInt();
                    int arrivalVisibility = arrivalWeather.get("visibility").asInt();

                    if (arrivalWindSpeed <= 30 && arrivalVisibility >= 200) {
                        System.out.println(flightNo + " | " + departureCity + " -> " + arrivalCity + " | по расписанию");
                    } else {
                        System.out.println(flightNo + " | " + departureCity + " -> " + arrivalCity + " | отменен");
                    }
                } else {
                    System.out.println(flightNo + " | " + departureCity + " -> " + arrivalCity + " | отменен");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getTimeZoneDifference(String departureCity, String arrivalCity) {
        if ("moscow".equals(departureCity)) {
            if ("omsk".equals(arrivalCity)) {
                return 3;
            } else if ("novosibirsk".equals(arrivalCity)) {
                return 4;
            }
        } else if ("omsk".equals(departureCity)) {
            if ("moscow".equals(arrivalCity)) {
                return -3;
            } else if ("novosibirsk".equals(arrivalCity)) {
                return 1;
            }
        } else if ("novosibirsk".equals(departureCity)) {
            if ("moscow".equals(arrivalCity)) {
                return -4;
            } else if ("omsk".equals(arrivalCity)) {
                return -1;
            }
        }
        return 0;
    }
}
