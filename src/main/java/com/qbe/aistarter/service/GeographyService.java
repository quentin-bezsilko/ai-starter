package com.qbe.aistarter.service;

import java.util.List;

public interface GeographyService {

    String getCapital(String country);

    List<String> getEuropeanCapitals();

    List<String> getAmericanCapitals();

    List<String> getCities(String countryCode);
}
