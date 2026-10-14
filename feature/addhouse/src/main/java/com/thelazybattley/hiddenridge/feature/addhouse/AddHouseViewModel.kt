package com.thelazybattley.hiddenridge.feature.addhouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.model.HouseDraft
import com.thelazybattley.hiddenridge.core.domain.usecase.AddHouseUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.HouseCatalog
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveHouseCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddHouseViewModel @Inject constructor(
    observeHouseCatalog: ObserveHouseCatalogUseCase,
    private val addHouse: AddHouseUseCase,
) : ViewModel() {

    private val form = MutableStateFlow(AddHouseForm())
    private val added = MutableStateFlow<AddHouseUiState.Added?>(null)
    private val catalog: StateFlow<HouseCatalog?> = observeHouseCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)

    internal val uiState: StateFlow<AddHouseUiState> = combine(catalog, form, added) { catalog, form, added ->
        when {
            added != null -> added
            catalog == null -> AddHouseUiState.Loading
            else -> AddHouseUiState.Editing(form.toUi(catalog))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AddHouseUiState.Loading,
    )

    internal fun onAction(action: AddHouseAction) {
        when (action) {
            // A new type or a different one brings its own suggested name and sleeps.
            is AddHouseAction.SelectType -> form.update { it.copy(typeId = action.typeId, isNewType = false, name = null, sleeps = null) }
            AddHouseAction.SelectNewType -> form.update { it.copy(isNewType = true, name = null, sleeps = null) }
            is AddHouseAction.SetNewTypeName -> form.update { it.copy(newTypeName = action.name) }
            is AddHouseAction.SetNewTypeIcon -> form.update { it.copy(newTypeIcon = action.icon) }
            is AddHouseAction.SetName -> form.update { it.copy(name = action.name) }
            AddHouseAction.AddSleeps -> changeSleeps(+1)
            AddHouseAction.RemoveSleeps -> changeSleeps(-1)
            AddHouseAction.OpenDatePicker -> form.update { it.copy(isDatePickerOpen = true) }
            AddHouseAction.CloseDatePicker -> form.update { it.copy(isDatePickerOpen = false) }
            is AddHouseAction.SetBookableFrom -> form.update { it.copy(bookableFrom = action.date, isDatePickerOpen = false) }
            is AddHouseAction.SetNote -> form.update { it.copy(note = action.note) }
            AddHouseAction.Save -> save()
        }
    }

    private fun changeSleeps(by: Int) {
        val catalog = catalog.value ?: return
        form.update { it.copy(sleeps = (it.toDraft(catalog).sleeps + by).coerceIn(HouseDraft.SLEEPS_RANGE)) }
    }

    private fun save() {
        val catalog = catalog.value ?: return
        val current = form.value
        if (current.isSaving) return
        form.update { it.copy(isSaving = true, showProblems = true) }
        viewModelScope.launch {
            when (val result = addHouse(current.toDraft(catalog))) {
                is AddHouseUseCase.Result.Added -> added.value = AddHouseUiState.Added(
                    houseName = result.house.name,
                    newGroupName = result.house.type.pluralName.takeIf { result.isNewType },
                )
                // The problems show on the form now that showProblems is on.
                is AddHouseUseCase.Result.Invalid -> form.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
