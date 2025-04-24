package space.jamestang.ktimer.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Constant {

    @JvmStatic
    lateinit var logger: Logger
    @JvmStatic
    lateinit var mapper: ObjectMapper

    fun initialize(){
        initLogger()
        initMapper()
    }


    private fun initLogger(){
        logger = LoggerFactory.getLogger("KTimer")
    }

    private fun initMapper(){
        mapper = jacksonObjectMapper()
        mapper.apply {
            registerModules(JavaTimeModule())
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            enable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
            enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        }

    }
}