package com.github.yihtserns.javatuples.jackson

import com.fasterxml.jackson.databind.ObjectMapper
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
        wrapper.unitInteger.value0 == 1

        then:
        objectMapper.writeValueAsString(wrapper) == json
    }

    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj)
    }

    static class Wrapper {

        Unit<Integer> unitInteger
    }
}
