package com.habitgame.app.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object DateUtils {
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun dayCloseDateTime(date: LocalDate, hour: Int, minute: Int): LocalDateTime =
        LocalDateTime.of(date, LocalTime.of(hour, minute))

    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun dayCloseBoundariesPassed(
        lastCloseDate: Long,
        dayCloseHour: Int,
        dayCloseMinute: Int
    ): List<Long> {
        val lastClosed = epochDayToLocalDate(lastCloseDate)
        val now = now()
        val today = LocalDate.now()
        val result = mutableListOf<Long>()

        var checkDate = lastClosed.plusDays(1)
        while (!checkDate.isAfter(today)) {
            val closeTime = dayCloseDateTime(checkDate, dayCloseHour, dayCloseMinute)
            if (now.isAfter(closeTime) || now.isEqual(closeTime)) {
                result.add(localDateToEpochDay(checkDate))
            } else {
                break
            }
            checkDate = checkDate.plusDays(1)
        }
        return result
    }

    fun currentDayForChallenge(lastCloseDate: Long, dayCloseHour: Int, dayCloseMinute: Int): Long {
        val boundaries = dayCloseBoundariesPassed(lastCloseDate, dayCloseHour, dayCloseMinute)
        return if (boundaries.isNotEmpty()) {
            boundaries.last() + 1
        } else {
            lastCloseDate + 1
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun formatDate(epochDay: Long): String {
        val date = epochDayToLocalDate(epochDay)
        return date.toString()
    }
}
