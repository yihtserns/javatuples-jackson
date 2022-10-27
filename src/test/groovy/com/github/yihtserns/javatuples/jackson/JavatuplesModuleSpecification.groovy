package com.github.yihtserns.javatuples.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.javatuples.Pair
import org.javatuples.Unit
import spock.lang.Specification

class JavatuplesModuleSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can de/serialize Unit"() {
        given:
        def json = toJson([unitInteger: [1]])

        when:
        def wrapper = objectMapper.readValue(json, Wrapper)

        then:
        wrapper.unitInteger.toList() == [1]

        then:
        objectMapper.writeValueAsString(wrapper) == json
    }

    def "can de/serialize Pair"() {
        given:
        def json = toJson([pairInteger: [1, 2]])

        when:
        def wrapper = objectMapper.readValue(json, Wrapper)

        then:
        wrapper.pairInteger.toList() == [1, 2]

        then:
        objectMapper.writeValueAsString(wrapper) == json
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
