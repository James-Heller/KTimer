package space.jamestang.ktimer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import space.jamestang.ktimer.core.KTimer

fun main() {

    val timer = KTimer()
    val mapper = jacksonObjectMapper()
    mapper.findAndRegisterModules()
    timer.messageEncoder = { message -> mapper.writeValueAsBytes(message) }
    timer.messageDecoder = { bytes -> mapper.readValue(bytes) }

    timer.strat()
}