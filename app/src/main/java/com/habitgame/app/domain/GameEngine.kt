package com.habitgame.app.domain

import com.habitgame.app.data.dao.*
import com.habitgame.app.data.model.*
import kotlin.math.max
import kotlin.math.roundToInt

class GameEngine(
    private val challengeDao: ChallengeDao,
    private val commitmentDao: CommitmentDao,
    private val temptationDao: TemptationDao,
    private val dailyLogDao: DailyLogDao,
    private val dailyLogEntryDao: DailyLogEntryDao,
    private val todayActionDao: TodayActionDao
) {
    suspend fun reconcileDayCloses(challenge: Challenge): Challenge {
        var current = challenge
        val boundaries = DateUtils.dayCloseBoundariesPassed(
            current.lastCloseDate, current.dayCloseHour, current.dayCloseMinute
        )

        for (boundaryDate in boundaries) {
            current = closeSingleDay(current, boundaryDate)
        }
        return current
    }

    private suspend fun closeSingleDay(challenge: Challenge, date: Long): Challenge {
        val actions = todayActionDao.getActions(challenge.id, date)

        val dailyPoints = actions.sumOf { it.pointDelta }
        val newTotal = max(0, challenge.totalScore + dailyPoints)

        val logId = dailyLogDao.insert(
            DailyLog(
                challengeId = challenge.id,
                date = date,
                dailyPoints = dailyPoints,
                totalAfterClose = newTotal,
                closed = true
            )
        )

        val entries = actions.map { action ->
            DailyLogEntry(
                dailyLogId = logId.toInt(),
                entryType = when (action.actionType) {
                    ActionType.COMMITMENT -> EntryType.COMMITMENT_DONE
                    ActionType.TEMPTATION_SLIP -> EntryType.TEMPTATION_SLIP
                    ActionType.ADDICTION_SLIP -> EntryType.ADDICTION_SLIP
                },
                refName = action.refName,
                pointDelta = action.pointDelta
            )
        }
        if (entries.isNotEmpty()) {
            dailyLogEntryDao.insertAll(entries)
        }

        todayActionDao.clearDay(challenge.id, date)

        val updated = challenge.copy(
            totalScore = newTotal,
            lastClosedTotal = newTotal,
            lastCloseDate = date
        )
        challengeDao.update(updated)
        return updated
    }

    suspend fun toggleCommitment(challenge: Challenge, commitment: Commitment, today: Long) {
        val existing = todayActionDao.findAction(
            challenge.id, today, ActionType.COMMITMENT, commitment.id
        )
        if (existing != null) {
            todayActionDao.delete(existing)
        } else {
            todayActionDao.insert(
                TodayAction(
                    challengeId = challenge.id,
                    actionType = ActionType.COMMITMENT,
                    refId = commitment.id,
                    refName = commitment.name,
                    pointDelta = commitment.pointValue,
                    count = 1,
                    date = today
                )
            )
        }
    }

    suspend fun setCommitmentCount(
        challenge: Challenge,
        commitment: Commitment,
        count: Int,
        today: Long
    ) {
        val clampedCount = count.coerceIn(0, commitment.maxCount)
        val existing = todayActionDao.findAction(
            challenge.id, today, ActionType.COMMITMENT, commitment.id
        )
        if (clampedCount == 0) {
            if (existing != null) todayActionDao.delete(existing)
        } else {
            val delta = commitment.pointValue * clampedCount
            if (existing != null) {
                todayActionDao.update(existing.copy(pointDelta = delta, count = clampedCount))
            } else {
                todayActionDao.insert(
                    TodayAction(
                        challengeId = challenge.id,
                        actionType = ActionType.COMMITMENT,
                        refId = commitment.id,
                        refName = commitment.name,
                        pointDelta = delta,
                        count = clampedCount,
                        date = today
                    )
                )
            }
        }
    }

    suspend fun recordTemptationSlip(challenge: Challenge, temptation: Temptation, today: Long) {
        todayActionDao.insert(
            TodayAction(
                challengeId = challenge.id,
                actionType = ActionType.TEMPTATION_SLIP,
                refId = temptation.id,
                refName = temptation.name,
                pointDelta = -temptation.slipPenalty,
                date = today
            )
        )
    }

    suspend fun removeTemptationSlip(challenge: Challenge, temptation: Temptation, today: Long) {
        val slips = todayActionDao.getActions(challenge.id, today).filter {
            it.actionType == ActionType.TEMPTATION_SLIP && it.refId == temptation.id
        }
        if (slips.isNotEmpty()) {
            todayActionDao.delete(slips.last())
        }
    }

    suspend fun recordAddictionSlip(challenge: Challenge, today: Long) {
        val penalty = (challenge.addictionPenaltyPercent / 100.0 * challenge.lastClosedTotal).roundToInt()
        todayActionDao.insert(
            TodayAction(
                challengeId = challenge.id,
                actionType = ActionType.ADDICTION_SLIP,
                refId = 0,
                refName = challenge.addictionName,
                pointDelta = -penalty,
                date = today
            )
        )
    }

    suspend fun removeAddictionSlip(challenge: Challenge, today: Long) {
        val slips = todayActionDao.getActions(challenge.id, today).filter {
            it.actionType == ActionType.ADDICTION_SLIP
        }
        if (slips.isNotEmpty()) {
            todayActionDao.delete(slips.last())
        }
    }

    fun computeDailyPoints(actions: List<TodayAction>): Int = actions.sumOf { it.pointDelta }

    fun isTemptationUnlocked(temptation: Temptation, totalScore: Int): Boolean =
        totalScore >= temptation.unlockThreshold
}
