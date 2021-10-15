/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2021 Arty Bishop (bishop.arty@gmail.com)
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
package com.rtbishop.look4sat.framework.local

import com.rtbishop.look4sat.data.LocalDataSource
import com.rtbishop.look4sat.domain.model.SatEntry
import com.rtbishop.look4sat.domain.model.SatItem
import com.rtbishop.look4sat.domain.predict.Satellite
import com.rtbishop.look4sat.domain.model.Transmitter
import com.rtbishop.look4sat.framework.DataMapper
import com.rtbishop.look4sat.framework.model.DataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSource(
    private val satelliteDao: SatelliteDao,
    private val sourcesDao: SourcesDao
) : LocalDataSource {

    override fun getEntriesWithModes(): Flow<List<SatItem>> {
        return satelliteDao.getSatItems()
            .map { satItems -> DataMapper.satItemsToDomainItems(satItems) }
    }

    override suspend fun getSources(): List<String> {
        return sourcesDao.getSources()
    }

    override suspend fun getSelectedSatellites(): List<Satellite> {
        return satelliteDao.getSelectedSatellites()
    }

    override suspend fun updateEntries(entries: List<SatEntry>) {
        val satEntries = entries.map { entry -> DataMapper.domainEntryToSatEntry(entry) }
        satelliteDao.updateEntries(satEntries)
    }

    override suspend fun updateEntriesSelection(catNums: List<Int>, isSelected: Boolean) {
        satelliteDao.updateEntriesSelection(catNums, isSelected)
    }

    override suspend fun updateSources(sources: List<String>) {
        sourcesDao.deleteSources()
        sourcesDao.setSources(sources.map { DataSource(it) })
    }

    override fun getTransmitters(catNum: Int): Flow<List<Transmitter>> {
        return satelliteDao.getSatTransmitters(catNum)
            .map { satTransList -> DataMapper.satTransListToDomainTransList(satTransList) }
    }

    override suspend fun updateTransmitters(transmitters: List<Transmitter>) {
        val satTransList = DataMapper.domainTransListToSatTransList(transmitters)
        satelliteDao.updateTransmitters(satTransList)
    }
}