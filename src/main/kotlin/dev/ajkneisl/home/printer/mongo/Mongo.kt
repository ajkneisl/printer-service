package dev.ajkneisl.home.printer.mongo

import org.litote.kmongo.KMongo

object Mongo {
    val CLIENT by lazy {
        KMongo.createClient(System.getenv("MONGO_URI"))
    }
}