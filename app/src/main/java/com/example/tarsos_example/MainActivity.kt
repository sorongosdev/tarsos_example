package com.example.tarsos_example

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tarsos_example.ui.theme.Tarsos_exampleTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import be.tarsos.dsp.io.TarsosDSPAudioFormat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.nio.ByteOrder
import android.Manifest

var tarsosDSPAudioFormat: TarsosDSPAudioFormat? = null

class MainActivity : ComponentActivity() {
    private lateinit var audioProcessorHandler: AudioProcessorHandler
    private val pitchTextViewValue = mutableStateOf("0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청
        requestRecordAudioPermission()

        setContent {
            Tarsos_exampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityUI("Android")
                    SetupTarsosDSP()
                }
            }
        }

        // AudioProcessorHandler 인스턴스 생성
        audioProcessorHandler = AudioProcessorHandler(applicationContext)
        audioProcessorHandler.pitchTextViewValue.observe(this) { value ->
            pitchTextViewValue.value = value  // UI 업데이트 로직
        }
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    @Composable
    fun MainActivityUI(name: String, modifier: Modifier = Modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(507.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pitch:",
                    modifier = Modifier.align(Alignment.TopStart)
                )
                Text(
                    text = pitchTextViewValue.value,  // pitchTextViewValue 바인딩,
                    fontSize = 50.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "입력된 코드",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            Button(
                onClick = { audioProcessorHandler.SetupAudioProcessing() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "녹음")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { audioProcessorHandler.stopAudioProcessing() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "중지")
            }
        }
    }
}



@Composable
fun SetupTarsosDSP() {
    LaunchedEffect(Unit) {
        tarsosDSPAudioFormat = TarsosDSPAudioFormat(
            TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
            22050f,
            2 * 8,
            1,
            2 * 1,
            22050f,
            ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Tarsos_exampleTheme {
//        MainActivityUI("Android")
    }
}

