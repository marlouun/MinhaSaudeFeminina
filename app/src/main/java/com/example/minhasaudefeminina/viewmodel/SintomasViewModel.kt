package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.repository.SymptomRepository
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CalendarDayType {
    MENSTRUACAO,
    PREVISAO_MENSTRUACAO,
    SINTOMA,
    HOJE
}

sealed interface SalvarState {
    data object Idle : SalvarState
    data object Carregando : SalvarState
    data class Sucesso(val message: String) : SalvarState
    data class Erro(val mensagem: String) : SalvarState
}

data class CycleSummary(
    val nextExpectedDate: LocalDate? = null,
    val lateDays: Int = 0,
    val estimatedCycleDays: Int = 28,
    val basedOnHistory: Boolean = false,
    val detectedCycles: Int = 0
)

class SintomasViewModel(
    private val repository: SymptomRepository,
    private val userId: String
) : ViewModel() {
    val registrosSintomas: StateFlow<List<RegistroSintoma>> = repository.observeRecords(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mesExibido = MutableStateFlow(YearMonth.now())
    val mesExibido: StateFlow<YearMonth> = _mesExibido.asStateFlow()

    private val _salvarState = MutableStateFlow<SalvarState>(SalvarState.Idle)
    val salvarState: StateFlow<SalvarState> = _salvarState.asStateFlow()

    val cycleSummary: StateFlow<CycleSummary> = registrosSintomas
        .map(::calculateCycleSummary)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CycleSummary())

    fun mesAnterior() = _mesExibido.update { it.minusMonths(1) }
    fun mesProximo() = _mesExibido.update { it.plusMonths(1) }

    fun recordForId(recordId: String?): RegistroSintoma? =
        recordId?.let { id -> registrosSintomas.value.firstOrNull { it.id == id } }

    fun getTiposParaDia(data: LocalDate): List<CalendarDayType> {
        val result = mutableListOf<CalendarDayType>()
        if (data == LocalDate.now()) result += CalendarDayType.HOJE
        val records = registrosSintomas.value.filter { it.localDate() == data }
        if (records.any { it.tipo == SintomaTipo.MENSTRUACAO }) result += CalendarDayType.MENSTRUACAO
        if (records.any { it.tipo != SintomaTipo.MENSTRUACAO }) result += CalendarDayType.SINTOMA
        if (cycleSummary.value.nextExpectedDate == data && CalendarDayType.MENSTRUACAO !in result) {
            result += CalendarDayType.PREVISAO_MENSTRUACAO
        }
        return result
    }

    fun saveRecord(
        recordId: String?,
        date: LocalDate,
        type: SintomaTipo?,
        intensity: Int,
        notes: String
    ) {
        if (type == null) {
            _salvarState.value = SalvarState.Erro("Selecione um sintoma.")
            return
        }
        if (date.isAfter(LocalDate.now())) {
            _salvarState.value = SalvarState.Erro("Não é possível registrar um sintoma no futuro.")
            return
        }
        val existing = recordForId(recordId)
        val now = System.currentTimeMillis()
        val record = RegistroSintoma(
            id = existing?.id ?: UUID.randomUUID().toString(),
            usuarioId = userId,
            dataTimestamp = date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            tipo = type,
            intensidade = intensity,
            notas = notes.trim().takeIf(String::isNotEmpty),
            criadoEm = existing?.criadoEm ?: now,
            atualizadoEm = now
        )
        viewModelScope.launch {
            _salvarState.value = SalvarState.Carregando
            runCatching { repository.saveRecord(record) }
                .onSuccess {
                    _salvarState.value = SalvarState.Sucesso(
                        if (existing == null) "Registro salvo com sucesso." else "Registro atualizado com sucesso."
                    )
                }
                .onFailure { error ->
                    _salvarState.value = SalvarState.Erro(error.message ?: "Não foi possível salvar o registro.")
                }
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            _salvarState.value = SalvarState.Carregando
            runCatching { repository.deleteRecord(userId, recordId) }
                .onSuccess { _salvarState.value = SalvarState.Sucesso("Registro excluído.") }
                .onFailure { error ->
                    _salvarState.value = SalvarState.Erro(error.message ?: "Não foi possível excluir o registro.")
                }
        }
    }

    fun resetSaveState() {
        _salvarState.value = SalvarState.Idle
    }

    private fun calculateCycleSummary(records: List<RegistroSintoma>): CycleSummary {
        val periodDays = records
            .asSequence()
            .filter { it.tipo == SintomaTipo.MENSTRUACAO }
            .map { it.localDate() }
            .distinct()
            .sorted()
            .toList()

        if (periodDays.isEmpty()) return CycleSummary()

        // Varios registros em dias seguidos pertencem a uma mesma menstruacao.
        // Consideramos um novo ciclo quando existe um intervalo de pelo menos 14 dias.
        val cycleStarts = mutableListOf(periodDays.first())
        periodDays.drop(1).forEach { date ->
            if (ChronoUnit.DAYS.between(cycleStarts.last(), date) >= 14) {
                cycleStarts += date
            }
        }

        val intervals = cycleStarts.zipWithNext { previous, current ->
            ChronoUnit.DAYS.between(previous, current).toInt()
        }.filter { it in 15..60 }

        val basedOnHistory = intervals.isNotEmpty()
        val estimatedCycleDays = if (basedOnHistory) {
            intervals.average().roundToInt().coerceIn(15, 60)
        } else {
            28
        }

        val lastPeriodStart = cycleStarts.last()
        val expected = lastPeriodStart.plusDays(estimatedCycleDays.toLong())
        val today = LocalDate.now()
        val late = if (today.isAfter(expected)) ChronoUnit.DAYS.between(expected, today).toInt() else 0

        return CycleSummary(
            nextExpectedDate = expected,
            lateDays = late,
            estimatedCycleDays = estimatedCycleDays,
            basedOnHistory = basedOnHistory,
            detectedCycles = cycleStarts.size
        )
    }

    companion object {
        fun factory(repository: SymptomRepository, userId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { SintomasViewModel(repository, userId) }
        }
    }
}

fun RegistroSintoma.localDate(): LocalDate = Instant.ofEpochMilli(dataTimestamp)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
