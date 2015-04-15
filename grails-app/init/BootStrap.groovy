import org.spongepowered.api.world.difficulty.Difficulties
import webmc.Injector

class BootStrap {

    def init = { servletContext ->
        Injector.inject()
        println Difficulties.EASY
    }

    def destroy = {
    }
}
