package com.abqwtb.model

import org.joda.time.LocalTime

class BusTrip {

    var scheduledTime: LocalTime? = null
    var route: Int = 0
    var secondsLate: Float = 0.toFloat()
    var busId: Int = 0

    override fun toString(): String {
        return scheduledTime!!.toString("h:mm aa")
    }
}
