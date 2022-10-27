package com.github.yihtserns.javatuples.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.javatuples.Pair
import org.javatuples.Unit
import spock.lang.Specification

class JavatuplesModuleSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can de/serialize json to/from tuple"() {
        given:
        def json = toJson([(property): jsonValue])

        when:
        def wrapper = objectMapper.readValue(json, Wrapper)

        then:
        wrapper[property].toList() == expectedJavaValue

        then:
        objectMapper.writeValueAsString(wrapper) == json

        where:
        property      | jsonValue | expectedJavaValue
        "unitInteger" | [1]       | [1]
        "pairInteger" | [1, 2]    | [1, 2]
    }

    def "should throw when trying to deserialize invalid json to tuple"() {
        given:
        def json = toJson([(property): invalidValue])

        when:
        objectMapper.readValue(json, Wrapper)

        then:
        thrown(MismatchedInputException)

        where:
        property      | invalidValue
        "unitInteger" | [1, 2]
        "unitInteger" | []
        "unitInteger" | 1
        "unitInteger" | true
        "unitInteger" | ["a"]
        "unitInteger" | [a: 1]

        "pairInteger" | [1, 2, 3]
        "pairInteger" | [1]
        "pairInteger" | []
        "pairInteger" | 1
        "pairInteger" | true
        "pairInteger" | [1, "b"]
        "pairInteger" | ["a", 2]
        "pairInteger" | ["a", "b"]
        "pairInteger" | [a: 1]
    }

    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj)
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Wrapper {

        Unit<Integer> unitInteger
        Pair<Integer, Integer> pairInteger
    }
}
