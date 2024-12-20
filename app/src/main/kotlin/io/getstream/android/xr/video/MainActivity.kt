package io.getstream.android.xr.video

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.controls.actions.FlipCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.LeaveCallAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.RealtimeConnection
import io.getstream.video.android.core.StreamVideo

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val client = StreamVideo.instance()
            val call = client.call(type = "default", id = "GzGQPrISLSHk")

            LaunchCallPermissions(
                call = call,
                onAllPermissionsGranted = {
                    // all permissions are granted so that we can join the call.
                    val result = call.join(create = true)
                    result.onError {
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                }
            )

            VideoTheme {
                val connection by call.state.connection.collectAsStateWithLifecycle()
                val session = LocalSession.current

                if (connection == RealtimeConnection.Connected) {
                    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                        Subspace {
                            SpatialVideoContent(
                                call = call,
                                onRequestHomeSpaceMode = { session?.requestHomeSpaceMode() }
                            )
                        }
                    } else {
                        VideoContent(
                            call = call,
                            onRequestFullSpaceMode = { session?.requestFullSpaceMode() }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VideoTheme.colors.baseSheetPrimary)
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 120.dp),
                            text = "Joining a video call..",
                            color = Color.White,
                            fontSize = 32.sp,
                        )

                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VideoTheme.colors.basePrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpatialVideoContent(call: Call, onRequestHomeSpaceMode: () -> Unit) {
    SpatialPanel(SubspaceModifier.width(1280.dp).height(800.dp).resizable().movable()) {
        Surface {
            MainContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                call = call,
            )
        }
        Orbiter(
            position = OrbiterEdge.Top,
            offset = EdgeOffset.inner(offset = 14.dp),
            alignment = Alignment.End,
            shape = SpatialRoundedCornerShape(CornerSize(28.dp))
        ) {
            HomeSpaceModeIconButton(
                onClick = onRequestHomeSpaceMode
            )
        }

        Orbiter(
            position = OrbiterEdge.Bottom,
            offset = EdgeOffset.inner(offset = 14.dp),
            alignment = Alignment.CenterHorizontally,
        ) {
            HomeSpaceModeCallControls(call = call)
        }
    }
}

@Composable
fun VideoContent(call: Call, onRequestFullSpaceMode: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MainContent(
            modifier = Modifier.padding(48.dp),
            call = call,
        )

        if (LocalHasXrSpatialFeature.current) {
            FullSpaceModeIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                onClick = onRequestFullSpaceMode,
            )
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier, call: Call) {
    CallContent(
        modifier = modifier.fillMaxSize(),
        call = call
    )
}

@Composable
fun FullSpaceModeIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Icon(
        modifier = modifier
            .size(32.dp)
            .clickable { onClick.invoke() },
        painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
        tint = Color.White,
        contentDescription = stringResource(R.string.switch_to_full_space_mode)
    )
}

@Composable
fun HomeSpaceModeIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier.size(56.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@Composable
fun HomeSpaceModeCallControls(modifier: Modifier = Modifier, call: Call) {
    // observe the current devices states
    val isCameraEnabled by call.camera.isEnabled.collectAsStateWithLifecycle()
    val isMicrophoneEnabled by call.microphone.isEnabled.collectAsStateWithLifecycle()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleCameraAction(
            isCameraEnabled = isCameraEnabled,
            onCallAction = { call.camera.setEnabled(it.isEnabled) }
        )

        ToggleMicrophoneAction(
            isMicrophoneEnabled = isMicrophoneEnabled,
            onCallAction = { call.microphone.setEnabled(it.isEnabled) }
        )

        FlipCameraAction(
            onCallAction = { call.camera.flip() }
        )

        LeaveCallAction(
            onCallAction = { call.leave() }
        )
    }
}
