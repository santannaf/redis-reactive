package santannaf.demo.brc.demoredisreactive

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializer = Jackson2JsonRedisSerializer(String::class.java)
        val builder = RedisSerializationContext.newSerializationContext<String, String>(StringRedisSerializer())
        val context = builder.value(serializer).build()
        return ReactiveRedisTemplate(factory, context)
    }
}