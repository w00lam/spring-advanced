package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class WeatherClientTest {

    private RestTemplate restTemplate;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);
        given(builder.build()).willReturn(restTemplate);
        weatherClient = new WeatherClient(builder);
    }

    @Test
    void getTodayWeather_오늘_날씨를_반환한다() {
        // given
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] response = {
                new WeatherDto("01-01", "Snow"),
                new WeatherDto(today, "Sunny")
        };
        given(restTemplate.getForEntity(isA(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // when
        String weather = weatherClient.getTodayWeather();

        // then
        assertEquals("Sunny", weather);
    }

    @Test
    void getTodayWeather_응답_상태가_OK가_아니면_예외가_발생한다() {
        // given
        given(restTemplate.getForEntity(isA(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(new WeatherDto[0], HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then
        assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
    }

    @Test
    void getTodayWeather_응답_body가_null이면_예외가_발생한다() {
        // given
        given(restTemplate.getForEntity(isA(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // when & then
        assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
    }

    @Test
    void getTodayWeather_응답_body가_비어있으면_예외가_발생한다() {
        // given
        given(restTemplate.getForEntity(isA(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(new WeatherDto[0], HttpStatus.OK));

        // when & then
        assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
    }

    @Test
    void getTodayWeather_오늘_날씨가_없으면_예외가_발생한다() {
        // given
        WeatherDto[] response = {
                new WeatherDto("01-01", "Snow")
        };
        given(restTemplate.getForEntity(isA(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // when & then
        assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
    }
}
