package com.github.yihtserns.javatuples.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.javatuples.Decade
import org.javatuples.Ennead
import org.javatuples.Octet
import org.javatuples.Pair
import org.javatuples.Quartet
import org.javatuples.Quintet
import org.javatuples.Septet
import org.javatuples.Sextet
import org.javatuples.Triplet
import org.javatuples.Unit
import spock.lang.Specification

import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.time.temporal.ChronoUnit

import static java.time.DayOfWeek.THURSDAY
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY
import static java.time.Month.OCTOBER
import static java.time.temporal.ChronoUnit.DAYS
import static java.time.temporal.ChronoUnit.HOURS
import static java.time.temporal.ChronoUnit.MINUTES

class JavatuplesModuleSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can de/serialize json to/from tuple"() {
        given:
        def json = toJson([(property): jsonValue])

        when:
        def wrapper = objectMapper.readValue(json, Wrapper)

        then:
        wrapper[property].equals expectedJavaValue

        then:
        objectMapper.writeValueAsString(wrapper) == json

        where:
        property           | jsonValue                                                                                | expectedJavaValue
        "unitInteger"      | [1]                                                                                      | Unit.with(1)
        "unitWildcard"     | [1]                                                                                      | Unit.with(1)
        "unitWildcard"     | [1.1]                                                                                    | Unit.with(1.1d)
        "unitWildcard"     | ["1.1"]                                                                                  | Unit.with("1.1")
        "unitWildcard"     | [true]                                                                                   | Unit.with(true)
        "unitWildcard"     | [JANUARY.name()]                                                                         | Unit.with(JANUARY.name())
        "unitUntyped"      | [1]                                                                                      | Unit.with(1)
        "unitEnum"         | [JANUARY.name()]                                                                         | Unit.with(JANUARY)
        "unitEnumNested"   | [[JANUARY.name()]]                                                                       | Unit.with(Unit.<Month> with(JANUARY))
        "unitEnumList"     | [[JANUARY.name()], [FEBRUARY.name()]]                                                    | [Unit.with(JANUARY), Unit.with(FEBRUARY)]

        "pairInteger"      | [1, 2]                                                                                   | Pair.with(1, 2)
        "pairIntegerEnum"  | [1, FEBRUARY.name()]                                                                     | Pair.with(1, FEBRUARY)
        "pairWildcardEnum" | [1, FEBRUARY.name()]                                                                     | Pair.with(1, FEBRUARY)
        "pairWildcardEnum" | [1.1, FEBRUARY.name()]                                                                   | Pair.with(1.1d, FEBRUARY)
        "pairWildcardEnum" | ["1.1", FEBRUARY.name()]                                                                 | Pair.with("1.1", FEBRUARY)
        "pairWildcardEnum" | [true, FEBRUARY.name()]                                                                  | Pair.with(true, FEBRUARY)
        "pairWildcardEnum" | [JANUARY.name(), FEBRUARY.name()]                                                        | Pair.with(JANUARY.name(), FEBRUARY)
        "pairUntyped"      | [1, 2]                                                                                   | Pair.with(1, 2)

        "tripletDate"      | [27, OCTOBER.name(), 2022]                                                               | Triplet.with(27, OCTOBER, 2022)
        "quartetTime"      | [10, 32, 31.0, 0.401]                                                                    | Quartet.with(10, 32, 31f, 0.401)
        "quintetCron"      | [31, 32, 10, OCTOBER.name(), THURSDAY.name()]                                            | Quintet.with(31, 32, 10, OCTOBER, THURSDAY)
        "sextetChrono"     | [31, MINUTES.name(), 32, HOURS.name(), 10, DAYS.name()]                                  | Sextet.with(31, MINUTES, 32, HOURS, 10, DAYS)
        "septetAddress"    | ["Multimedia", "University", "Persiaran", "Multimedia", 63100, "Cyberjaya", "Malaysia"]  | Septet.with("Multimedia", "University", "Persiaran", "Multimedia", 63100, "Cyberjaya", "Malaysia")
        "octetIpAddress"   | [0xFE80, 0x0000, 0x0000, 0x0000, 0x0123, 0x4567, 0x89AB, 0xCDEF]                         | Octet.with(0xFE80, 0x0000, 0x0000, 0x0000, 0x0123, 0x4567, 0x89AB, 0xCDEF)
        "enneadLetters"    | ["一", "二", "三", "四", "五", "六", "七", "八", "九"]                                   | Ennead.with("一", "二", "三", "四", "五", "六", "七", "八", "九")
        "decadeDateTime"   | [2022, OCTOBER.name(), 27, THURSDAY.name(), 10, 32, 31.401, -3, 0.5, "America/St_Johns"] | Decade.with(Year.of(2022), OCTOBER, 27, THURSDAY, 10, 32, 31.401d, -3, 0.5f, ZoneId.of("America/St_Johns"))
    }

    def "should throw when trying to deserialize invalid json to tuple"() {
        given:
        def json = toJson([(property): invalidValue])

        when:
        objectMapper.readValue(json, Wrapper)

        then:
        thrown(MismatchedInputException)

        where:
        property          | invalidValue
        "unitInteger"     | [1, 2]
        "unitInteger"     | []
        "unitInteger"     | 1
        "unitInteger"     | true
        "unitInteger"     | ["a"]
        "unitInteger"     | [a: 1]
        "unitEnum"        | ["NON_EXISTENT"]

        "pairInteger"     | [1, 2, 3]
        "pairInteger"     | [1]
        "pairInteger"     | []
        "pairInteger"     | 1
        "pairInteger"     | true
        "pairInteger"     | [1, "b"]
        "pairInteger"     | ["a", 2]
        "pairInteger"     | ["a", "b"]
        "pairInteger"     | [a: 1]
        "pairIntegerEnum" | [1, "NON_EXISTENT"]
    }

    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj)
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Wrapper {

        Unit<Integer> unitInteger
        Unit<?> unitWildcard
        Unit unitUntyped
        Unit<Month> unitEnum
        Unit<Unit<Month>> unitEnumNested
        List<Unit<Month>> unitEnumList

        Pair<Integer, Integer> pairInteger
        Pair<Integer, Month> pairIntegerEnum
        Pair<?, Month> pairWildcardEnum
        Pair pairUntyped

        Triplet<Integer, Month, Integer> tripletDate
        Quartet<Integer, Integer, Float, BigDecimal> quartetTime
        Quintet<Integer, Integer, Integer, Month, DayOfWeek> quintetCron
        Sextet<Integer, ChronoUnit, Integer, ChronoUnit, Integer, ChronoUnit> sextetChrono
        Septet<String, String, String, String, Integer, String, String> septetAddress
        Octet<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> octetIpAddress
        Ennead<String, String, String, String, String, String, String, String, String> enneadLetters
        Decade<Year, Month, Integer, DayOfWeek, Integer, Integer, Double, Integer, Float, ZoneId> decadeDateTime
    }
}
