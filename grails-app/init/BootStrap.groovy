import org.joshuacoles.injector.Injector

class BootStrap {

    def init = { servletContext ->
        Injector.inject()
    }

    def destroy = {
    }
}
