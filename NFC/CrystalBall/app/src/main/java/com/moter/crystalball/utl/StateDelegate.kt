package com.moter.crystalball.utl

import kotlinx.coroutines.flow.StateFlow

interface StateDelegate<State> {

    val stateFlow: StateFlow<State>

    val state: State

    fun updateState(reduce: State.() -> State)
}