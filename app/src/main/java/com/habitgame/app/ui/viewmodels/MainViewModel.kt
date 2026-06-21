package com.habitgame.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.habitgame.app.HabitGameApp
import com.habitgame.app.data.model.*
import com.habitgame.app.domain.DateUtils
import com.habitgame.app.domain.GameEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as HabitGameApp).database
    private val engine = GameEngine(
        db.challengeDao(), db.commitmentDao(), db.temptationDao(),
        db.dailyLogDao(), db.dailyLogEntryDao(), db.todayActionDao()
    )

    private val _hasActiveChallenge = MutableStateFlow<Boolean?>(null)
    val hasActiveChallenge: StateFlow<Boolean?> = _hasActiveChallenge

    private val _challenge = MutableStateFlow<Challenge?>(null)
    val challenge: StateFlow<Challenge?> = _challenge

    private val _commitments = MutableStateFlow<List<Commitment>>(emptyList())
    val commitments: StateFlow<List<Commitment>> = _commitments

    private val _temptations = MutableStateFlow<List<Temptation>>(emptyList())
    val temptations: StateFlow<List<Temptation>> = _temptations

    private val _todayActions = MutableStateFlow<List<TodayAction>>(emptyList())
    val todayActions: StateFlow<List<TodayAction>> = _todayActions

    private val _dailyPoints = MutableStateFlow(0)
    val dailyPoints: StateFlow<Int> = _dailyPoints

    private val _todayDate = MutableStateFlow(0L)
    val todayDate: StateFlow<Long> = _todayDate

    private val _closedLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val closedLogs: StateFlow<List<DailyLog>> = _closedLogs

    private val _logEntries = MutableStateFlow<List<DailyLogEntry>>(emptyList())
    val logEntries: StateFlow<List<DailyLogEntry>> = _logEntries

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        loadChallenge()
    }

    fun loadChallenge() {
        viewModelScope.launch {
            try {
                val active = db.challengeDao().getActiveChallenge()
                if (active != null) {
                    val reconciled = engine.reconcileDayCloses(active)
                    _challenge.value = reconciled
                    _hasActiveChallenge.value = true
                    loadChallengeData(reconciled)
                } else {
                    _hasActiveChallenge.value = false
                    _isReady.value = true
                }
            } catch (e: Exception) {
                _hasActiveChallenge.value = false
                _isReady.value = true
            }
        }
    }

    private suspend fun loadChallengeData(challenge: Challenge) {
        try {
            _commitments.value = db.commitmentDao().getActive(challenge.id)
            _temptations.value = db.temptationDao().getActive(challenge.id)

            val today = DateUtils.currentDayForChallenge(
                challenge.lastCloseDate, challenge.dayCloseHour, challenge.dayCloseMinute
            )
            _todayDate.value = today
            refreshTodayActions(challenge.id, today)

            _closedLogs.value = db.dailyLogDao().getClosedLogs(challenge.id)
            _isReady.value = true
        } catch (e: Exception) {
            _isReady.value = true
        }
    }

    private suspend fun refreshTodayActions(challengeId: Int, today: Long) {
        try {
            val actions = db.todayActionDao().getActions(challengeId, today)
            _todayActions.value = actions
            _dailyPoints.value = engine.computeDailyPoints(actions)
        } catch (_: Exception) { }
    }

    fun createChallenge(
        addictionName: String,
        penaltyPercent: Int,
        dayCloseHour: Int,
        dayCloseMinute: Int,
        commitments: List<Pair<String, Triple<Int, CommitmentType, Int>>>,
        temptations: List<Triple<String, Int, Int>>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val today = DateUtils.todayEpochDay()
                val challengeId = db.challengeDao().insert(
                    Challenge(
                        addictionName = addictionName,
                        addictionPenaltyPercent = penaltyPercent,
                        dayCloseHour = dayCloseHour,
                        dayCloseMinute = dayCloseMinute,
                        startDate = today,
                        totalScore = 0,
                        lastClosedTotal = 0,
                        lastCloseDate = today - 1
                    )
                ).toInt()

                db.commitmentDao().insertAll(
                    commitments.map { (name, config) ->
                        Commitment(
                            challengeId = challengeId,
                            name = name,
                            pointValue = config.first,
                            type = config.second,
                            maxCount = config.third
                        )
                    }
                )

                db.temptationDao().insertAll(
                    temptations.map { (name, threshold, penalty) ->
                        Temptation(
                            challengeId = challengeId,
                            name = name,
                            unlockThreshold = threshold,
                            slipPenalty = penalty
                        )
                    }
                )

                val active = db.challengeDao().getActiveChallenge()
                if (active != null) {
                    val reconciled = engine.reconcileDayCloses(active)
                    _challenge.value = reconciled
                    _hasActiveChallenge.value = true
                    loadChallengeData(reconciled)
                }

                onComplete()
            } catch (_: Exception) { }
        }
    }

    fun toggleCommitment(commitment: Commitment) {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.toggleCommitment(c, commitment, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun setCommitmentCount(commitment: Commitment, count: Int) {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.setCommitmentCount(c, commitment, count, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun recordTemptationSlip(temptation: Temptation) {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.recordTemptationSlip(c, temptation, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun removeTemptationSlip(temptation: Temptation) {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.removeTemptationSlip(c, temptation, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun recordAddictionSlip() {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.recordAddictionSlip(c, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun removeAddictionSlip() {
        val c = _challenge.value ?: return
        val today = _todayDate.value
        viewModelScope.launch {
            try {
                engine.removeAddictionSlip(c, today)
                refreshTodayActions(c.id, today)
            } catch (_: Exception) { }
        }
    }

    fun loadLogEntries(logId: Int) {
        viewModelScope.launch {
            try {
                _logEntries.value = db.dailyLogEntryDao().getEntriesForLog(logId)
            } catch (_: Exception) { }
        }
    }

    fun updatePenaltyPercent(newPercent: Int) {
        val c = _challenge.value ?: return
        viewModelScope.launch {
            try {
                val updated = c.copy(addictionPenaltyPercent = newPercent)
                db.challengeDao().update(updated)
                _challenge.value = updated
            } catch (_: Exception) { }
        }
    }

    fun updateDayCloseTime(hour: Int, minute: Int) {
        val c = _challenge.value ?: return
        viewModelScope.launch {
            try {
                val updated = c.copy(dayCloseHour = hour, dayCloseMinute = minute)
                db.challengeDao().update(updated)
                _challenge.value = updated
            } catch (_: Exception) { }
        }
    }

    fun addCommitment(name: String, pointValue: Int, type: CommitmentType, maxCount: Int) {
        val c = _challenge.value ?: return
        viewModelScope.launch {
            try {
                db.commitmentDao().insert(
                    Commitment(
                        challengeId = c.id,
                        name = name,
                        pointValue = pointValue,
                        type = type,
                        maxCount = maxCount
                    )
                )
                _commitments.value = db.commitmentDao().getActive(c.id)
            } catch (_: Exception) { }
        }
    }

    fun deactivateCommitment(commitment: Commitment) {
        viewModelScope.launch {
            try {
                db.commitmentDao().update(commitment.copy(active = false))
                val c = _challenge.value ?: return@launch
                _commitments.value = db.commitmentDao().getActive(c.id)
            } catch (_: Exception) { }
        }
    }

    fun addTemptation(name: String, threshold: Int, penalty: Int) {
        val c = _challenge.value ?: return
        viewModelScope.launch {
            try {
                db.temptationDao().insert(
                    Temptation(
                        challengeId = c.id,
                        name = name,
                        unlockThreshold = threshold,
                        slipPenalty = penalty
                    )
                )
                _temptations.value = db.temptationDao().getActive(c.id)
            } catch (_: Exception) { }
        }
    }

    fun deactivateTemptation(temptation: Temptation) {
        viewModelScope.launch {
            try {
                db.temptationDao().update(temptation.copy(active = false))
                val c = _challenge.value ?: return@launch
                _temptations.value = db.temptationDao().getActive(c.id)
            } catch (_: Exception) { }
        }
    }

    fun endChallenge() {
        val c = _challenge.value ?: return
        viewModelScope.launch {
            try {
                db.challengeDao().update(c.copy(status = ChallengeStatus.ENDED))
                _challenge.value = null
                _hasActiveChallenge.value = false
                _commitments.value = emptyList()
                _temptations.value = emptyList()
                _todayActions.value = emptyList()
                _dailyPoints.value = 0
            } catch (_: Exception) { }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MainViewModel(application)
            }
        }
    }
}
