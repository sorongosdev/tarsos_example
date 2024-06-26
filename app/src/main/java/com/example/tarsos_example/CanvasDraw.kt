/* **************
* CanvasDrawCanvasDraw.kt
*
* Canvas 를 이용해서 악보를 그려주는 부분
* *******************/
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.tarsos_example.consts.AnswerTypes
import com.example.tarsos_example.model.MyViewModel
import com.example.tarsos_example.consts.ChordTypes
import com.example.tarsos_example.consts.NoteTypes
import com.example.tarsos_example.consts.WavConsts
import java.time.Instant
import java.util.Date
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

/**악보 구분선 그리는 함수*/
@Composable
fun DrawDiv(modifier: Modifier = Modifier) {
    val CHUNK_CNT = WavConsts.CHUNK_CNT
    Canvas(modifier = modifier) {
        val startX_measure = size.width / 10 // 첫번째 마디 시작점

        val measure_width = 8 * (size.width / 10) // 두 마디 넓이
        val startY = size.height * 3 / 5 // div가 시작되는 Y지점
        val endY = size.height * 4 / 5 // 꼬리가 끝나는 Y지점


        for (i in 1..CHUNK_CNT) {
            if (NoteTypes.note_feedback[i] == 1) { // 첫번째 마디 그리기
                val xOffset1 = startX_measure + i * (measure_width / (CHUNK_CNT + 1)) // 음표가 그려지는 곳
                drawLine(
                    color = Color.Green,
                    start = Offset(x = xOffset1, y = startY),
                    end = Offset(x = xOffset1, y = endY),
                    strokeWidth = Stroke.DefaultMiter
                )
            }
        }
    }
}

/**악보를 그려주는 함수*/
@Composable
fun NewDrawNotes(viewModel: MyViewModel, modifier: Modifier = Modifier) {
    val countDownSecondState = viewModel.countDownSecond.collectAsState() // 초
    val isRecordingState = viewModel.isRecording.collectAsState() // 마디2에 보여주는 코드

    if (!(isRecordingState.value) && countDownSecondState.value == 4) { // 녹음중이지 않으면서, 카운트다운되고 있지 않는 상태라면
        viewModel.updateNotes(
            viewModel.getRandomNote(),
            viewModel.getRandomNote()
        )
    }
    Canvas(modifier = modifier) {
        val startX_measure1 = 1 * (size.width / 10) // 첫번째 마디 시작점
        val startX_measure2 = 5 * (size.width / 10) // 두번째 마디 시작점

        val measure_width = 4 * (size.width / 10) // 한 마디 넓이
        val startY_tail = size.height * 1 / 5 // 꼬리가 시작되는 Y지점
        val endY_tail = size.height * 4 / 5 // 꼬리가 끝나는 Y지점
        val centerY = size.height * 4 / 5 // 선의 중심점
        val lineLength = sqrt(2f) * 25f // 45도 각도에서의 선 길이, 대각선 길이 계산

        // 마디1 그리기
        listOf(2, 8, 14, 20).forEach { i ->
            val noteIndex =
                (i - 2) / 6 // { 0,2 / 1,8 / 2,14 / 3,20 }  =====> {6n+2 = i} ===> (i-2)/6
            if (viewModel.shownNote1.value[noteIndex] == 1) {
                val xOffset1 = startX_measure1 + i * (measure_width / 25)

                drawLine(
                    color = Color.Black,
                    start = Offset(x = xOffset1 + lineLength / 2, y = startY_tail),
                    end = Offset(x = xOffset1 + lineLength / 2, y = endY_tail - lineLength / 2),
                    strokeWidth = Stroke.DefaultMiter
                )

                // 45도 기울인 선을 그리기 위한 시작점과 끝점 계산
                val start = Offset(x = xOffset1 - lineLength / 2, y = centerY + lineLength / 2)
                val end = Offset(x = xOffset1 + lineLength / 2, y = centerY - lineLength / 2)
                drawLine(
                    color = Color.Black,
                    start = start,
                    end = end,
                    strokeWidth = Stroke.DefaultMiter
                )
            }
        }
        // 마디2 그리기
        listOf(1, 7, 13, 19).forEach { i ->
            val noteIndex =
                (i - 1) / 6 // { 0,1 / 1,7 / 2,13 / 3,19 }  =====> {6n+1 = i} ===> (i-1)/6
            if (viewModel.shownNote2.value[noteIndex] == 1) {
                val xOffset1 = startX_measure2 + i * (measure_width / 25)

                drawLine(
                    color = Color.Black,
                    start = Offset(x = xOffset1 + lineLength / 2, y = startY_tail),
                    end = Offset(x = xOffset1 + lineLength / 2, y = endY_tail - lineLength / 2),
                    strokeWidth = Stroke.DefaultMiter
                )

                // 45도 기울인 선을 그리기 위한 시작점과 끝점 계산
                val start = Offset(x = xOffset1 - lineLength / 2, y = centerY + lineLength / 2)
                val end = Offset(x = xOffset1 + lineLength / 2, y = centerY - lineLength / 2)
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

/**코드 텍스트를 보여주는 함수*/
@Composable
fun ShowChords(viewModel: MyViewModel, modifier: Modifier) {
    val countDownSecondState = viewModel.countDownSecond.collectAsState() // 초
    val shownChordState1 = viewModel.shownChord1.collectAsState() // 마디1에 보여주는 코드
    val shownChordState2 = viewModel.shownChord2.collectAsState() // 마디2에 보여주는 코드
    val isRecordingState = viewModel.isRecording.collectAsState() // 마디2에 보여주는 코드

    if (!(isRecordingState.value) && countDownSecondState.value == 4) { // 녹음중이지 않으면서, 카운트다운되고 있지 않는 상태라면
        viewModel.updateChords(
            viewModel.getRandomChord(),
            viewModel.getRandomChord()
        )
    }

    // shouldDrawText가 true일 때 Canvas 그리기
    Canvas(modifier = modifier) {
        val startX_measure1 = 1 * (size.width / 10) // 첫번째 마디 시작점
        val startX_measure2 = 5 * (size.width / 10) // 두번째 마디 시작점

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK // 텍스트 색상 설정
            textSize = 120f // 텍스트 크기 설정
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

/**타브 악보 그리는 함수*/
@Composable
fun ShowTabNote(viewModel: MyViewModel, modifier: Modifier) {
    val shownChordState1 = viewModel.shownChord1.collectAsState() // 마디1에 보여주는 코드
    val shownChordState2 = viewModel.shownChord2.collectAsState() // 마디2에 보여주는 코드

    Canvas(modifier = modifier) {
        val startX_measure1 = 1 * (size.width / 10) // 첫번째 마디 시작점
        val startX_measure2 = 5 * (size.width / 10) // 두번째 마디 시작점
        val tab_width = 3 * (size.width / 10) // 타브 악보 넓이
        val tab_height = size.height // 타브 악보 높이

        val startY = 0f // 시작되는 Y지점
        val endY = size.height // 끝나는 Y지점
        /**===========================================================첫 번째 마디에 대한 사각형 그리기*/
        drawRect(
            color = Color.Black,
            topLeft = Offset(startX_measure1, startY),
            size = Size(tab_width, endY),
            style = Stroke(width = 5f)
        )
        /**================================================================================마디1 세로줄*/
        for (i in 1..3) {
            drawLine(
                color = Color.Black,
                start = Offset(x = (startX_measure1 + i * 0.25 * tab_width).toFloat(), y = startY),
                end = Offset(x = (startX_measure1 + i * 0.25 * tab_width).toFloat(), y = endY),
                strokeWidth = Stroke.DefaultMiter
            )
        }
        /**================================================================================마디1 가로줄*/
        for (i in 1..4) {
            drawLine(
                color = Color.Black,
                start = Offset(
                    x = startX_measure1,
                    y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                ),
                end = Offset(
                    x = startX_measure1 + tab_width,
                    y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                ),
                strokeWidth = Stroke.DefaultMiter
            )
        }
        /**============================================================ 두 번째 마디에 대한 사각형 그리기*/
        drawRect(
            color = Color.Black,
            topLeft = Offset(startX_measure2, startY),
            size = Size(tab_width, endY),
            style = Stroke(width = 5f)
        )
        /**=======================================================================마디2 세로줄*/
        for (i in 1..3) {
            drawLine(
                color = Color.Black,
                start = Offset(x = (startX_measure2 + i * 0.25 * tab_width).toFloat(), y = startY),
                end = Offset(x = (startX_measure2 + i * 0.25 * tab_width).toFloat(), y = endY),
                strokeWidth = Stroke.DefaultMiter
            )
        }
        /**============================================================================마디2 가로줄*/
        for (i in 1..4) {
            drawLine(
                color = Color.Black,
                start = Offset(
                    x = startX_measure2,
                    y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                ),
                end = Offset(
                    x = startX_measure2 + tab_width,
                    y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                ),
                strokeWidth = Stroke.DefaultMiter
            )
        }
        /**=========================================================================코드1 그리기*/
        val tab_note1 = ChordTypes.chords_int_tab_map[shownChordState1.value] ?: listOf()
        val tab_note2 = ChordTypes.chords_int_tab_map[shownChordState2.value] ?: listOf()

        for (i in 0..5) { // 첫 프랫
            if (tab_note1[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure1 + (1.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
            if (tab_note2[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure2 + (1.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + (i.toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
        }
        for (i in 6..11) { // 두번째 프랫
            if (tab_note1[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure1 + (3.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 6).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
            if (tab_note2[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure2 + (3.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 6).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }

        }
        for (i in 12..17) { // 세번째 프랫
            if (tab_note1[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure1 + (5.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 12).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
            if (tab_note2[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure2 + (5.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 12).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
        }
        for (i in 18..23) { // 세번째 프랫
            if (tab_note1[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure1 + (7.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 18).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
            if (tab_note2[i] == 1) {
                drawCircle(
                    color = Color.Black, // 원의 색상
                    center = Offset(
                        (startX_measure2 + (7.0 / 8.0) * tab_width).toFloat(),
                        y = (startY + ((i - 18).toFloat() / 5.0) * tab_height).toFloat()
                    ), // 원의 중심 좌표
                    radius = 20f, // 원의 반지름
                    style = Fill // 원을 채워서 그리기 (기본값이므로 생략 가능)
                )
            }
        }
        /**=========================================================================코드2 그리기*/

    }
}

/**피드백리스트와 정답리스트를 기반으로 다시 노트를 그림*/
@Composable
fun DrawPaintNotes(paintNoteList: List<Int>?, modifier: Modifier) {
    val feedbackChunkCnt = WavConsts.FEEDBACK_CHUNK_CNT
    val halfFeedbackChunkCnt = feedbackChunkCnt / 2
    if (!paintNoteList.isNullOrEmpty()) {
        Canvas(modifier = modifier) {
            val startX_measure1 = 1 * (size.width / 10) // 첫번째 마디 시작점
            val startX_measure2 = 5 * (size.width / 10) // 두번째 마디 시작점
            val measure_width = 4 * (size.width / 10) // 한 마디 넓이
            val startY_tail = size.height * 1 / 5 // 꼬리가 시작되는 Y지점
            val endY_tail = size.height * 4 / 5 // 꼬리가 끝나는 Y지점
            val centerY = size.height * 4 / 5 // 선의 중심점
            val lineLength = sqrt(2f) * 25f // 45도 각도에서의 선 길이, 대각선 길이 계산

            var startX = startX_measure1
            var xOffset = startX

            for (i in 0..feedbackChunkCnt) { // 0~72
                if (i <= (halfFeedbackChunkCnt)) { // 마디1 인덱스 설정
                    startX = startX_measure1
                    xOffset = startX + (i + 3) * (measure_width / (halfFeedbackChunkCnt + 1))

                } else { // 마디2 X인덱스 설정
                    startX = startX_measure2
                    xOffset =
                        (startX + ((i + 1.5) - halfFeedbackChunkCnt) * (measure_width / (halfFeedbackChunkCnt + 1))).toFloat()
                }

                val selected_color = AnswerTypes.answerCodeMap[paintNoteList[i]]
                val selected_stroke = 10f

//                val selected_stroke = if (paintNoteList[i] == 3) answer_stroke else normal_stroke
//                val selected_color = if (paintNoteList[i] == 3) answer_color else normal_color

                if (paintNoteList[i] != 0) { // 첫번째 마디 그리기
                    if (selected_color != null) {
                        drawLine(
                            color = selected_color,
                            start = Offset(x = xOffset + lineLength / 2, y = startY_tail),
                            end = Offset(
                                x = xOffset + lineLength / 2,
                                y = endY_tail - lineLength / 2
                            ),
                            strokeWidth = selected_stroke
                        )
                    }

                    // 45도 기울인 선을 그리기 위한 시작점과 끝점 계산
                    val start = Offset(x = xOffset - lineLength / 2, y = centerY + lineLength / 2)
                    val end = Offset(x = xOffset + lineLength / 2, y = centerY - lineLength / 2)
                    if (selected_color != null) {
                        drawLine(
                            color = selected_color,
                            start = start,
                            end = end,
                            strokeWidth = selected_stroke
                        )
                    }
                }
            }


        }
    }
}

/**녹음 후 흐른 시간(초)을 받아와 진행바를 그려주는 함수*/
@Composable
fun DrawProcessBar(seconds: Double, modifier: Modifier) {
    Log.d("startBar", "DrawProcessBar")
    Canvas(modifier = modifier) {
        val startX_measure = 1 * (size.width / 10) // 첫번째 마디 시작점
        val measure_width = 8 * (size.width / 10) // 두 마디 넓이

        val startY = 0f // 진행바 그리기가 시작되는 Y 지점
        val endY = size.height // 진행바 그리기가 끝나는 Y 지점

        val process = seconds / (WavConsts.BAR_PERIOD / 1000.0) // 녹음 진행 후 얼마나 지났는지

        val xOffset = (startX_measure + process * measure_width).toFloat()

        drawLine(
            color = Color.Blue,
            start = Offset(x = xOffset, y = startY),
            end = Offset(x = xOffset, y = endY),
            strokeWidth = 10f
        )
    }
}
