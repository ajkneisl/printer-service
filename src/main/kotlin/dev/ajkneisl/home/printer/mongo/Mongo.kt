package dev.ajkneisl.home.printer.mongo

import org.litote.kmongo.KMongo

object Mongo {
    val CLIENT by lazy {
        KMongo.createClient("mongodb+srv://ajkneisl:${System.getenv("MONGO_PW")}@ajknpr.hscnn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority")
    }
}