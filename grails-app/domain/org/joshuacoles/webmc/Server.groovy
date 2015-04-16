package org.joshuacoles.webmc

import org.aspectj.weaver.World

class Server {

    String name
    int port
    boolean online
    String pathToRoot, _core

    static hasMany = [plugins: String]

    static constraints = {
    }

//    File getRoot() {new File(pathToRoot)}
//    Sponge getCore() {(pathToRoot)}
}
