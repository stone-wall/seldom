import kotlin.time.ExperimentalTime

@ExperimentalTime
interface Event {
    val source: Driver
}
@ExperimentalTime
data class PageVisit(val url: String, override val source: Driver) : Event

data class TwitterAccountCreated(val username: String, val password: String, val email: String,
                                 override val source: Driver
) : Event