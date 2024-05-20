/* **************
* CanvasDrawCanvasDraw.kt
*
* Canvas 를 이용해서 악보를 그려주는 부분
* *******************/
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.tarsos_example.model.MyViewModel
import com.example.tarsos_example.consts.ChordTypes
import kotlin.math.sqrt
import kotlin.random.Random

/**악보 전체 틀을 그려주는 함수*/
@Composable
fun DrawSheet(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val startY = 0f // 시트 그리기가 시작되는 Y 지점
        val endY = size.height // 시트 그리기가 끝나는 Y 지점
        for (i in 1..9) {
            if (i == 1 || i == 5 || i == 9) {
                val xOffset = i * (size.width / 10)
                drawLine(
                    color = Color.Black,
                    start = Offset(x = xOffset, y = startY),
                    end = Offset(x = xOffset, y = endY),
                    strokeWidth = Stroke.DefaultMiter
                )
            }
        }
        // 세로줄의 위에서 4/5 지점에 가로줄 그리기
        val threeQuartersY = startY + (endY - startY) * 4 / 5
        drawLine(
            color = Color.Black,
            start = Offset(x = 1 * (size.width / 10), y = threeQuartersY), // 가로줄 시작점 조정
            end = Offset(x = 9 * (size.width / 10), y = threeQuartersY), // 가로줄 끝점 조정
            strokeWidth = Stroke.DefaultMiter
        )
    }
}

/**noteType, 그리는 위치(1또는 2)를 받아 음표를 그려주는 함수*/
@Composable
fun DrawNotes(noteType: List<Int>, location: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val startX_measure = if (location == 1) {
            1 * (size.width / 10) // 첫번째 마디 시작점
        } else {
            5 * (size.width / 10) // 두번째 마디 시작점
        }
        val measure_width = 4 * (size.width / 10) // 한 마디 넓이
        val startY_tail = size.height * 1 / 5 // 꼬리가 시작되는 Y지점
        val endY_tail = size.height * 4 / 5 // 꼬리가 끝나는 Y지점
        val centerY = size.height * 4 / 5 // 선의 중심점
        val lineLength = sqrt(2f) * 25f // 45도 각도에서의 선 길이, 대각선 길이 계산

        // for 문 간소화
        listOf(1, 4, 7, 10).forEach { i ->
            val noteIndex = (i - 1) / 3 // { 0,1 / 1,4 / 2,7 / 3,10 } =======> { 3n+1 = i }
            if (noteType[noteIndex] == 1) {
                val xOffset = startX_measure + i * (measure_width / 13) // 음표가 그려지는 곳

                drawLine(
                    color = Color.Black,
                    start = Offset(x = xOffset + lineLength / 2, y = startY_tail),
                    end = Offset(x = xOffset + lineLength / 2, y = endY_tail - lineLength / 2),
                    strokeWidth = Stroke.DefaultMiter
                )

                // 45도 기울인 선을 그리기 위한 시작점과 끝점 계산
                val start = Offset(x = xOffset - lineLength / 2, y = centerY + lineLength / 2)
                val end = Offset(x = xOffset + lineLength / 2, y = centerY - lineLength / 2)
                drawLine(
                    color = Color.Black,
                    start = start,
                    end = end,
                    strokeWidth = Stroke.DefaultMiter
                )
            }
        }
    }
}

/**피드백 노트를 받아와 화면에 빨간색으로 보여주는 함수*/
@Composable
fun DrawFeedBackNotes(feedbackNoteList: List<Int>?, location: Int, modifier: Modifier) {
    if (!feedbackNoteList.isNullOrEmpty()) {
        Canvas(modifier = modifier) {
            val startX_measure = 1 * (size.width / 10) // 첫번째 마디 시작점
            val measure_width = 8 * (size.width / 10) // 두 마디 넓이
            val startY_tail = size.height * 1 / 5 // 꼬리가 시작되는 Y지점
            val endY_tail = size.height * 4 / 5 // 꼬리가 끝나는 Y지점
            val centerY = size.height * 4 / 5 // 선의 중심점
            val lineLength = sqrt(2f) * 25f // 45도 각도에서의 선 길이, 대각선 길이 계산

            for (i in 1..24) {
                if (feedbackNoteList[i] != 0) { // 받은 리스트의 값이 0이 아니면 친 곳
                    val xOffset = startX_measure + i * (measure_width / 26) // 음표가 그려지는 곳

                    drawLine(
                        color = Color.Red,
                        start = Offset(x = xOffset + lineLength / 2, y = startY_tail),
                        end = Offset(x = xOffset + lineLength / 2, y = endY_tail - lineLength / 2),
                        strokeWidth = 10f
                    )

                    // 45도 기울인 선을 그리기 위한 시작점과 끝점 계산
                    val start = Offset(x = xOffset - lineLength / 2, y = centerY + lineLength / 2)
                    val end = Offset(x = xOffset + lineLength / 2, y = centerY - lineLength / 2)
                    drawLine(
                        color = Color.Red,
                        start = start,
                        end = end,
                        strokeWidth = 20f
                    )
                }
            }
        }
    }
}

/**녹음 후 흐른 시간(초)을 받아와 진행바를 그려주는 함수*/
@Composable
fun DrawProcessBar(seconds: Double, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val startX_measure = 1 * (size.width / 10) // 첫번째 마디 시작점
        val measure_width = 8 * (size.width / 10) // 두 마디 넓이
        val startY = 0f // 진행바 그리기가 시작되는 Y 지점
        val endY = size.height // 진행바 그리기가 끝나는 Y 지점

        val process = seconds / 5.0 // 녹음 진행 후 얼마나 지났는지

        val xOffset = (startX_measure + process * measure_width).toFloat()

        drawLine(
            color = Color.Blue,
            start = Offset(x = xOffset, y = startY),
            end = Offset(x = xOffset, y = endY),
            strokeWidth = 10f
        )

    }
}

/**사용자에게 코드를 보여주는 함수*/
@Composable
fun ShowChords(viewModel: MyViewModel, modifier: Modifier) {
    /** viewModel의 recordSecond를 관찰*/
    val recordSecondState = viewModel.recordSecond.collectAsState() // 초
    val countDownSecondState = viewModel.countDownSecond.collectAsState() // 초
    val shownChordState1 = viewModel.shownChord1.collectAsState() // 마디1에 보여주는 코드
    val shownChordState2 = viewModel.shownChord2.collectAsState() // 마디2에 보여주는 코드
    val isRecordingState = viewModel.isRecording.collectAsState() // 마디2에 보여주는 코드

    if (!(isRecordingState.value) && countDownSecondState.value == 4) { // 녹음중이지 않으면서, 카운트다운되고 있지 않는 상태라면
        viewModel.updateChords(
            getRandomChord(viewModel),
            getRandomChord(viewModel)
        )
    }

    // shouldDrawText가 true일 때 Canvas 그리기
    Canvas(modifier = modifier) {
        val startX_measure1 = 1 * (size.width / 10) // 첫번째 마디 시작점
        val startX_measure2 = 5 * (size.width / 10) // 두번째 마디 시작점

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK // 텍스트 색상 설정
            textSize = 40f // 텍스트 크기 설정
        }

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                shownChordState1.value,
                startX_measure1, // x 좌표
                0f, // y 좌표
                paint // Paint 객체
            )
            canvas.nativeCanvas.drawText(
                shownChordState2.value,
                startX_measure2, // x 좌표
                0f, // y 좌표
                paint // Paint 객체
            )
        }
    }
}

/** 랜덤으로 코드를 가져오는 함수*/
fun getRandomChord(viewModel: MyViewModel): String {
    val randomInt = Random.nextInt(1, 19)

    val randomChord = ChordTypes.chords_numbers[randomInt] // 보여줄 코드를 인덱스를 통해 맵에서 찾아줌
    return randomChord!!
}