/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2022 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rtbishop.look4sat.presentation.settingsScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rtbishop.look4sat.domain.IDataRepository
import com.rtbishop.look4sat.domain.ILocationManager
import com.rtbishop.look4sat.domain.ISettingsManager
import com.rtbishop.look4sat.domain.model.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val locationManager: ILocationManager,
    private val repository: IDataRepository,
    private val settings: ISettingsManager
) : ViewModel() {

    private val defaultLocationSettings = LocationSettings(false,
        settings.getLastUpdateTime(),
        locationManager.getStationPosition().lat,
        locationManager.getStationPosition().lon,
        settings.loadStationLocator(),
        { locationManager.setPositionFromGps() },
        { lat, lon -> locationManager.setStationPosition(lat, lon) },
        { locationManager.setPositionFromQth(it) })
    var locationSettings = mutableStateOf(defaultLocationSettings)
        private set

    private val defaultDataSettings = DataSettings(false,
        settings.getLastUpdateTime(),
        0,
        0,
        { repository.updateFromWeb() },
        { repository.updateFromFile(it) },
        { repository.clearAllData() })
    var dataSettings = mutableStateOf(defaultDataSettings)
        private set

    private val defaultOtherSettings = OtherSettings(settings.isUtcEnabled(),
        settings.isUpdateEnabled(),
        settings.isSweepEnabled(),
        settings.isSensorEnabled(),
        { setUtc(it) },
        { setUpdate(it) },
        { setSweep(it) },
        { setSensor(it) })
    var otherSettings = mutableStateOf(defaultOtherSettings)
        private set

    init {
        viewModelScope.launch {
            repository.updateState.collect {
                val isUpdating = it is DataState.Loading
                dataSettings.value = dataSettings.value.copy(
                    getUpdating = isUpdating, getLastUpdated = settings.getLastUpdateTime()
                )
            }
        }
        viewModelScope.launch {
            combine(repository.getEntriesTotal(), repository.getRadiosTotal()) { sats, radios ->
                dataSettings.value =
                    dataSettings.value.copy(getSatellites = sats, getRadios = radios)
            }.collect {

            }
        }
    }

    private fun setUtc(value: Boolean) {
        settings.setUtcState(value)
        otherSettings.value = otherSettings.value.copy(getUtc = value)
    }

    private fun setUpdate(value: Boolean) {
        settings.setUpdateState(value)
        otherSettings.value = otherSettings.value.copy(getUpdate = value)
    }

    private fun setSweep(value: Boolean) {
        settings.setSweepState(value)
        otherSettings.value = otherSettings.value.copy(getSweep = value)
    }

    private fun setSensor(value: Boolean) {
        settings.setSensorState(value)
        otherSettings.value = otherSettings.value.copy(getSensor = value)
    }

//    fun getRotatorEnabled(): Boolean = settings.getRotatorEnabled()
//    fun setRotatorEnabled(value: Boolean) = settings.setRotatorEnabled(value)
//    fun getRotatorServer(): String = settings.getRotatorServer()
//    fun setRotatorServer(value: String) = settings.setRotatorServer(value)
//    fun getRotatorPort(): String = settings.getRotatorPort()
//    fun setRotatorPort(value: String) = settings.setRotatorPort(value)
//    fun getBTEnabled(): Boolean = settings.getBTEnabled()
//    fun setBTEnabled(value: Boolean) = settings.setBTEnabled(value)
//    fun getBTFormat(): String = settings.getBTFormat()
//    fun setBTFormat(value: String) = settings.setBTFormat(value)
//    fun getBTDeviceName(): String = settings.getBTDeviceName()
//    fun setBTDeviceName(value: String) = settings.setBTDeviceName(value)
//    fun getBTDeviceAddr(): String = settings.getBTDeviceAddr()
//    fun setBTDeviceAddr(value: String) = settings.setBTDeviceAddr(value)
}
