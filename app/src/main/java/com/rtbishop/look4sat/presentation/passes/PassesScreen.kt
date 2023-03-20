package com.rtbishop.look4sat.presentation.passes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rtbishop.look4sat.R
import com.rtbishop.look4sat.domain.NearEarthSatellite
import com.rtbishop.look4sat.model.DataState
import com.rtbishop.look4sat.model.OrbitalData
import com.rtbishop.look4sat.model.SatPass
import com.rtbishop.look4sat.presentation.*
import com.rtbishop.look4sat.presentation.dialogs.FilterDialog
import com.rtbishop.look4sat.presentation.pullRefresh.PullRefreshIndicator
import com.rtbishop.look4sat.presentation.pullRefresh.PullRefreshState
import com.rtbishop.look4sat.presentation.pullRefresh.pullRefresh
import com.rtbishop.look4sat.presentation.pullRefresh.rememberPullRefreshState
import java.text.SimpleDateFormat
import java.util.*

private val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

@Composable
fun PassesScreen(navToRadar: (Int, Long) -> Unit) {
    val viewModel = viewModel(PassesViewModel::class.java, factory = PassesViewModel.Factory)
    val state = viewModel.passes.collectAsState(initial = null)
    val timerText = viewModel.timerText.collectAsState(initial = null)
    val isRefreshing = state.value is DataState.Loading
    val refreshState = rememberPullRefreshState(refreshing = isRefreshing,
        onRefresh = { viewModel.calculatePasses() })

    val value = state.value
    var passes = emptyList<SatPass>()
    if (value is DataState.Success) passes = value.data

    val showDialog = rememberSaveable { mutableStateOf(false) }
    val toggleDialog = { showDialog.value = showDialog.value.not() }
    if (showDialog.value) {
        FilterDialog(viewModel.getHoursAhead(),
            viewModel.getMinElevation(),
            { toggleDialog() }) { hours, elev ->
            viewModel.calculatePasses(hours, elev)
        }
    }

    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ElevatedCard(modifier = Modifier.height(52.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(onClick = { toggleDialog() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.weight(1f)
                ) {
                    Text(text = timerText.value?.first ?: "Null")
                    Text(text = timerText.value?.second ?: "Null")
                }
                Text(
                    text = timerText.value?.third ?: "00:00:00",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 2.dp, end = 8.dp)
                )
            }
        }
        PassesCard(refreshState, isRefreshing, passes, navToRadar)
    }
}

@Preview(showBackground = true)
@Composable
private fun PassPreview() {
    val data = OrbitalData(
        "Satellite", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 45000, 0.0
    )
    val satellite = NearEarthSatellite(data)
    val pass = SatPass(1L, 25.0, 10L, 75.0, 850, 45.0, satellite, 0.5f)
    MainTheme { Pass(pass = pass, { _, _ -> }) }
}

@Composable
private fun Pass(pass: SatPass, navToRadar: (Int, Long) -> Unit, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier) {
        Surface(modifier = Modifier
            .padding(bottom = 2.dp)
            .clickable { navToRadar(pass.catNum, pass.aosTime) }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Id:${pass.catNum} - ",
                        modifier = Modifier.width(82.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pass.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_elevation),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${pass.maxElevation}°",
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.pass_aosAz, pass.aosAzimuth),
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(id = R.string.pass_altitude, pass.altitude),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.pass_losAz, pass.losAzimuth),
                        fontSize = 15.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val deepTime = stringResource(id = R.string.pass_placeholder)
                    Text(
                        text = if (pass.isDeepSpace) deepTime else sdf.format(Date(pass.aosTime)),
                        fontSize = 15.sp
                    )
                    LinearProgressIndicator(
                        progress = if (pass.isDeepSpace) 1f else pass.progress,
                        modifier = modifier
                            .fillMaxWidth(0.75f)
                            .padding(top = 3.dp),
                        trackColor = MaterialTheme.colorScheme.inverseSurface
                    )
                    Text(
                        text = if (pass.isDeepSpace) deepTime else sdf.format(Date(pass.losTime)),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PassesCard(
    refreshState: PullRefreshState,
    isRefreshing: Boolean,
    passes: List<SatPass>,
    navToRadar: (Int, Long) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.pullRefresh(refreshState)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = passes, key = { item -> item.catNum + item.aosTime }) { pass ->
                    Pass(pass, navToRadar, Modifier.animateItemPlacement())
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
