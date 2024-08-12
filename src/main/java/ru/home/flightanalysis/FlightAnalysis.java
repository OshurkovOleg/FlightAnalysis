package ru.home.flightanalysis;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightAnalysis {

    private static final Map<String, Integer> flightTimesMin = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String TICKETS_JSON_FILE = "/tickets.json";
    public static final String AVERAGE_PRICE = "Средняя цена: ";
    public static final String MEDIAN = "Медиана: ";
    public static final String BETWEEN_PRICE_AVG_MEDIAN = "Разница между средней ценой и медианой: ";
    public static final String VVO = "VVO";
    public static final String TLV = "TLV";
    public static final String FILE_NOT_FOUND = "Не удалось найти файл: ";
    public static final String FILE_NOT_READER = "Ошибка при чтении файла: ";
    public static final String CARRIER = "Перевозчик: ";
    public static final String MIN_TIME = " имеет минимальное время: ";


    public static void main(String[] args) {
        TicketRequest tickets;

        try (InputStream input = FlightAnalysis.class.getResourceAsStream(TICKETS_JSON_FILE)) {
            if (input == null) {
                throw new RuntimeException(FILE_NOT_FOUND + TICKETS_JSON_FILE);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                tickets = objectMapper.readValue(reader, TicketRequest.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(FILE_NOT_READER + e.getMessage(), e);
        }

        List<TicketRequest.Ticket> filteredTickets = getFilterList(tickets);
        showMinFlightTime(filteredTickets);
        viewDiffBetweenAveragePriceAndMedianFlights(filteredTickets);
    }

    static List<TicketRequest.Ticket> getFilterList(TicketRequest tickets) {
        return tickets.getTickets().stream()
                .filter(t -> t.getOrigin().equals(VVO) && t.getDestination().equals(TLV))
                .collect(Collectors.toList());
    }
    
    static void showMinFlightTime(List<TicketRequest.Ticket> tickets) {

        for (TicketRequest.Ticket ticket : tickets) {
            int flightTime = getMinFlightTime(ticket.getDeparture_date(), ticket.getDeparture_time(),
                    ticket.getArrival_date(), ticket.getArrival_time());

            if (flightTimesMin.containsKey(ticket.getCarrier())) {
                flightTimesMin.put(ticket.getCarrier(), Math.min(flightTimesMin.get(ticket.getCarrier()), flightTime));
            } else {
                flightTimesMin.put(ticket.getCarrier(), flightTime);
            }
        }

        for (Map.Entry<String, Integer> stringIntegerEntry : flightTimesMin.entrySet()) {
            System.out.println(CARRIER + stringIntegerEntry.getKey() +
                    MIN_TIME + stringIntegerEntry.getValue());
        }
    }

    private static int getMinFlightTime(String departureDate, String departureTime, String arrivalDate, String arrivalTime) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.yy H:mm")
                .toFormatter();
        LocalDateTime departureDateTime = LocalDateTime.parse(departureDate + " " + departureTime, formatter);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDate + " " + arrivalTime, formatter);
        return (int) ChronoUnit.MINUTES.between(departureDateTime, arrivalDateTime);
    }

    private static void viewDiffBetweenAveragePriceAndMedianFlights(List<TicketRequest.Ticket> tickets) {

        List<Integer> prices = getPricesTickets(tickets);
        Collections.sort(prices);

        int size = prices.size();
        double medianPrice;
        if (size % 2 == 0) {
            medianPrice = (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            medianPrice = prices.get(size / 2);
        }

        double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double difference = averagePrice - medianPrice;

        System.out.println(AVERAGE_PRICE + averagePrice);
        System.out.println(MEDIAN + medianPrice);
        System.out.println(BETWEEN_PRICE_AVG_MEDIAN + difference + "\n");
    }

    static List<Integer> getPricesTickets(List<TicketRequest.Ticket> tickets) {
        return tickets.stream().map(TicketRequest.Ticket::getPrice).collect(Collectors.toList());
    }

}