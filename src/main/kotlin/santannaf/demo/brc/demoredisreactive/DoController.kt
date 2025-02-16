package santannaf.demo.brc.demoredisreactive

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["/do/something"])
class DoController(
    private val useCase: TodoUseCase
) {
    @PostMapping
    fun doSomething(): Mono<String> {
        return useCase.doSomething()
    }

    @GetMapping
    fun getSomething(): Mono<String> = useCase.getSomething()
}