package com.github.yihtserns.javatuples.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.javatuples.Pair
import org.javatuples.Unit
import spock.lang.Specification

import java.time.Month

class JavatuplesModuleSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can de/serialize json to/from tuple"() {
        given:
        def json = toJson([(property): jsonValue])

        when:
        def wrapper = objectMapper.readValue(json, Wrapper)

        then:
        wrapper[property] == expectedJavaValue

        then:
        objectMapper.writeValueAsString(wrapper) == json

        where:
        property           | jsonValue                                         | expectedJavaValue
        "unitInteger"      | [1]                                               | Unit.with(1)
        "unitWildcard"     | [1]                                               | Unit.with(1)
        "unitWildcard"     | [1.1d]                                            | Unit.with(1.1d)
        "unitWildcard"     | ["1.1"]                                           | Unit.with("1.1")
        "unitWildcard"     | [true]                                            | Unit.with(true)
        "unitWildcard"     | [Month.JANUARY.name()]                            | Unit.with(Month.JANUARY.name())
        "unitUntyped"      | [1]                                               | Unit.with(1)
        "unitEnum"         | [Month.JANUARY.name()]                            | Unit.with(Month.JANUARY)
        "unitEnumNested"   | [[Month.JANUARY.name()]]                          | Unit.with(Unit.<Month> with(Month.JANUARY))
        "unitEnumList"     | [[Month.JANUARY.name()], [Month.FEBRUARY.name()]] | [Unit.with(Month.JANUARY), Unit.with(Month.FEBRUARY)]

        "pairInteger"      | [1, 2]                                            | Pair.with(1, 2)
        "pairIntegerEnum"  | [1, Month.FEBRUARY.name()]                        | Pair.with(1, Month.FEBRUARY)
        "pairWildcardEnum" | [1, Month.FEBRUARY.name()]                        | Pair.with(1, Month.FEBRUARY)
        "pairWildcardEnum" | [1.1d, Month.FEBRUARY.name()]                     | Pair.with(1.1d, Month.FEBRUARY)
        "pairWildcardEnum" | ["1.1", Month.FEBRUARY.name()]                    | Pair.with("1.1", Month.FEBRUARY)
        "pairWildcardEnum" | [true, Month.FEBRUARY.name()]                     | Pair.with(true, Month.FEBRUARY)
        "pairWildcardEnum" | [Month.JANUARY.name(), Month.FEBRUARY.name()]     | Pair.with(Month.JANUARY.name(), Month.FEBRUARY)
        "pairUntyped"      | [1, 2]                                            | Pair.with(1, 2)
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
    }
}
