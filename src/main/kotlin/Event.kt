import kotlin.time.ExperimentalTime

@ExperimentalTime
interface Event {
    val source: Driver
}
@ExperimentalTime
data class PageVisit(val url: String, override val source: Driver) : Event

data class GenericEvent(val message: String, override val source: Driver): Event

data class AccountCreated(val username: String, val password: String, val email: String,
                                 override val source: Driver
) : Event

data class StartupEvent(override val source: Driver) : Event