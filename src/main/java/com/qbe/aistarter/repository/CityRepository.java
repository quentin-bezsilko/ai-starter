package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.CityEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CityRepository extends JpaRepository<CityEntity, Long> {

    @Query(
            """
            select c.name
            from CityEntity c
            join c.country co
            where lower(co.name) = lower(:country)
              and c.capital = true
            """)
    Optional<String> findCapitalByCountry(@Param("country") String country);

    @Query(
            """
            select c.name
            from CityEntity c
            join c.country co
            join co.continent continent
            where continent.code = 'EU'
              and c.capital = true
            order by c.name
            """)
    List<String> findEuropeanCapitals();

    @Query(
            """
            select c.name
            from CityEntity c
            join c.country co
            join co.continent continent
            where continent.code in ('NA','SA')
              and c.capital = true
            order by c.name
            """)
    List<String> findAmericanCapitals();

    @Query(
            """
            select c.name
            from CityEntity c
            join c.country co
            where upper(co.isoCode) = upper(:countryCode)
            order by c.name
            """)
    List<String> findCitiesByCountryCode(@Param("countryCode") String countryCode);

    @Query("""
       select c
       from CityEntity c
       where lower(c.name) = lower(:city)
       """)
    Optional<CityEntity> findByName(String city);
}
