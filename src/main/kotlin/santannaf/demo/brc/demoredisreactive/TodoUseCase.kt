package santannaf.demo.brc.demoredisreactive

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Named
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.publisher.Mono
import java.util.*

@Named
class TodoUseCase(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val mapper: ObjectMapper
) {
    fun doSomething(): Mono<String> {
        return Mono.fromCallable {
            UUID.randomUUID().toString()
        }.flatMap(this::doOperation)
    }

    fun getSomething(): Mono<String> = Mono.defer { reactiveRedisTemplate.opsForValue()["key"] }

    fun doOperation(value: String): Mono<String> {
        return Mono.defer {
            reactiveRedisTemplate.opsForValue().set("key", value)
        }
            .doOnNext { println(it) }
            .thenReturn(value)
    }
}
