package com.qbe.aistarter.tool;

import com.qbe.aistarter.service.GeographyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeographyTools {

    private final GeographyService geographyService;

    @Tool(
            description =
                    """
                    Returns the capital city name of a country.

                    Input: a country name in English.
                    Output: the capital city name.

                    Examples:
                    - Spain -> Madrid
                    - France -> Paris
                    - Germany -> Berlin

                    Use this tool whenever the user asks for the capital of a country.
                    """)
    public String getCapital(String country) {
        return geographyService.getCapital(country);
    }

    @Tool(
            description =
                    """
                    Returns the list of all European capital cities available in the database.

                    Use this tool when the user asks for:
                    - European capitals
                    - capitals of Europe
                    - a list of capital cities in Europe
                    """)
    public List<String> getEuropeanCapitals() {
        return geographyService.getEuropeanCapitals();
    }

    @Tool(
            description =
                    """
                    Returns the list of all North American and South American capital cities available in the database.

                    Use this tool when the user asks for:
                    - American capitals
                    - capitals in North America
                    - capitals in South America
                    - capitals of the Americas
                    """)
    public List<String> getAmericanCapitals() {
        return geographyService.getAmericanCapitals();
    }

    @Tool(
            description =
                    """
                    Returns all cities for a given ISO country code.

                    Input: a two-letter country code.
                    Output: a list of city names.

                    Examples:
                    - FR -> French cities
                    - ES -> Spanish cities
                    - DE -> German cities

                    Use this tool whenever the user asks for cities belonging to a specific country.
                    """)
    public List<String> getCities(String countryCode) {
        return geographyService.getCities(countryCode);
    }
}
