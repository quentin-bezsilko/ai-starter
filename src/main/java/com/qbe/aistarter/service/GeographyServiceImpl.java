package com.qbe.aistarter.service;

import com.qbe.aistarter.repository.CityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeographyServiceImpl implements GeographyService {

    private final CityRepository cityRepository;

    @Override
    public String getCapital(String country) {
        log.info("Getting capital for country {}", country);
        return cityRepository
                .findCapitalByCountry(country)
                .orElseThrow(() -> new IllegalArgumentException("Unknown country : " + country));
    }

    @Override
    public List<String> getEuropeanCapitals() {
        log.info("Getting all European capitals");
        return cityRepository.findEuropeanCapitals();
    }

    @Override
    public List<String> getAmericanCapitals() {
        log.info("Getting all American capitals");
        return cityRepository.findAmericanCapitals();
    }

    @Override
    public List<String> getCities(String countryCode) {
        log.info("Getting cities for country code {}", countryCode);
        return cityRepository.findCitiesByCountryCode(countryCode);
    }
}
