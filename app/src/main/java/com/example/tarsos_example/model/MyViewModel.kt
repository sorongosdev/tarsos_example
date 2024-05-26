package com.example.tarsos_example.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarsos_example.consts.NoteTypes
import com.example.tarsos_example.consts.WavConsts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class MyViewModel : ViewModel() {

    /**파이썬으로부터 받은 사용자 연주 피드백 리스트*/
    private var _feedbackNoteList = MutableStateFlow(List(WavConsts.FEEDBACK_CHUNK_CNT + 1) { 0 })
    val feedbackNoteList: StateFlow<List<Int>> = _feedbackNoteList

    /**피드백리스트와 정답리스트를 비교하여 새로 그리기 위해 필요한 리스트*/
    private var _paintNoteList = MutableStateFlow(List(WavConsts.FEEDBACK_CHUNK_CNT + 1) { 0 })
    val paintNoteList: StateFlow<List<Int>> = _paintNoteList

    /**==============================================================악보, 코드*/
    /**보여주는 악보, 사용자가 친 것과 비교해서 정답인지 알려주기 위해 필요*/
    private var _shownNote1 = MutableStateFlow(listOf<Int>())
    val shownNote1: StateFlow<List<Int>> = _shownNote1

    private var _shownNote2 = MutableStateFlow(listOf<Int>())
    val shownNote2: StateFlow<List<Int>> = _shownNote2

    /**마디1에 보여주는 코드, 사용자가 친 것과 비교해서 정답인지 알려주기 위해 필요*/
    private var _shownChord1 = MutableStateFlow<String>("")
    val shownChord1: StateFlow<String> = _shownChord1

    /**마디2에 보여주는 코드, 사용자가 친 것과 비교해서 정답인지 알려주기 위해 필요*/
    private var _shownChord2 = MutableStateFlow<String>("")
    val shownChord2: StateFlow<String> = _shownChord2

    /**정답이 되는 노트*/
    private var _answerNote = MutableStateFlow(listOf<Int>())
    val answerNote: StateFlow<List<Int>> = _answerNote
    /**============================================================================*/

    /**========================================================================초 관련*/
    /**녹음이 시작하고 지난 시간, 소수점 첫째자리까지 표시*/
    private var _recordSecond = MutableStateFlow<Double>(0.0)
    val recordSecond: StateFlow<Double> = _recordSecond

    /**녹음이 시작하고 지난 시간, 소수점 첫째자리까지 표시*/
    private var _countDownSecond = MutableStateFlow<Int>(4)
    val countDownSecond: StateFlow<Int> = _countDownSecond

    /**프로세스바 유지 시간*/
    private var _barSecond = MutableStateFlow<Double>(0.0)
    val barSecond: StateFlow<Double> = _barSecond
    /**=============================================================================*/

    /**녹음중 유무*/
    private var _isRecording = MutableStateFlow<Boolean>(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    /**비프음 출력 유무*/
    private var _isBeeping = MutableStateFlow<Boolean>(false)
    val isBeeping: StateFlow<Boolean> = _isBeeping

    /****************************   함수들 **************************************/
    /**노트 타입별로 길이 4인 리스트를 받아 12개인 리스트로 반환해주는 함수*/
    private fun match_answer(note: List<Int>): List<Int> {
        return when (note) {
            NoteTypes.note_1111 -> NoteTypes.answer_note_1111
            NoteTypes.note_1011 -> NoteTypes.answer_note_1011
            NoteTypes.note_1010 -> NoteTypes.answer_note_1010
            else -> List(12) { 0 } // 해당하는 노트값이 없을 때 0으로 이루어진 리스트 반환
        }
    }

    /**사용자에게 보여줄 음표 리스트를 업데이트 해주는 함수*/
    fun updateNotes(note1: List<Int>, note2: List<Int>) {
        _shownNote1.value = note1
        _shownNote2.value = note2

        val draft_answer_note1 = match_answer(note1).toMutableList() // 4에 해당하는 인덱스를 12로 늘려줌
        val draft_answer_note2 = match_answer(note2)
        val halfFeedbackChunkCnt = WavConsts.HALF_FEEDBACK_CHUNK_CNT //36

        //정답 리스트를 위한 마디1 끝 값 1로 바꾸기
        draft_answer_note1[halfFeedbackChunkCnt - 1] = 1
        draft_answer_note1[halfFeedbackChunkCnt - 2] = 1
        draft_answer_note1[halfFeedbackChunkCnt - 3] = 1

        // note1과 note2에 해당하는 정답 리스트를 합쳐서 업데이트
        _answerNote.value = draft_answer_note1 + draft_answer_note2
        Log.d("answerNote", "${_answerNote.value}")
    }

    /**사용자에게 보여줄 코드를 업데이트 해주는 함수*/
    fun updateChords(chord1: String, chord2: String) {
        _shownChord1.value = chord1
        _shownChord2.value = chord2
    }

    /**연산 후 피드백 노트 리스트를 업데이트 해주는 함수*/
    fun updateFeedbackNoteList(feedbackNoteList: List<Int>) {
        _feedbackNoteList.value = feedbackNoteList

        Log.d("answerNote", "_feedbackNoteList.value ${_feedbackNoteList.value}")
        val updatedPaintNoteList = _paintNoteList.value.toMutableList()

        for (i in 0..<WavConsts.FEEDBACK_CHUNK_CNT) {
            if (feedbackNoteList[i + 1] != 0 && _answerNote.value[i] == 0) { // 피드백리스트의 값이 0이 아닌 정수라면 정답리스트의 원소를 1로 (오답)
                updatedPaintNoteList[i] = 1
            }

            if (feedbackNoteList[i + 1] != 0 && _answerNote.value[i] == 1) { // 정답리스트와 피드백 노트 리스트를 비교해서 둘다 1이라면 값을 2로 바꿈 (정답)
                updatedPaintNoteList[i] = 2
            }
        }
        for (i in 1..<WavConsts.FEEDBACK_CHUNK_CNT - 1) { // 1~70
            // 연속된 1이 있으면 그 3묶음은 오답이라고 판단하고 첫번째 위치에만 1을 남김
            if (updatedPaintNoteList[i - 1] == 1 && updatedPaintNoteList[i] == 1 && updatedPaintNoteList[i + 1] == 1) { // 3개 다 오답
                updatedPaintNoteList[i] = 0
                updatedPaintNoteList[i + 1] = 0
            }

            Log.d("answerNote", "_answerNote.value ${_answerNote.value}")
            /**========================================================================*/
            // 3묶음 중 하나라도 정답이면 3묶음 모두 지우고 정답 인 곳에 음표 그리기
            // 3묶음 중 정답과 가장 가까운 인덱스를 찾음
            /**======================================================================================================*/
            if (updatedPaintNoteList[i - 1] == 2 && updatedPaintNoteList[i] == 2 && updatedPaintNoteList[i + 1] == 2) { // 3개 다 정답
                val rightBoundIndex = WavConsts.FEEDBACK_CHUNK_CNT - 1
                //=============================================================================================좌에 치우쳐져 있을 때
                if (i - 1 == 0) { // 0부터 3개 연속 정답일 때
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                } else if (_answerNote.value[i - 2] == 0 && _answerNote.value[i + 2] == 1) { // 3묶음의 왼쪽 인덱스가 0보다 크면서, 정답 범위의 왼쪽에 있음
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                    updatedPaintNoteList[i + 2] = 2
                } else if (i - 3 >= 0 && _answerNote.value[i - 3] == 0 && _answerNote.value[i + 3] == 1) {
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                }
                //=======================================================================================================딱 중심
                else if (i - 4 >= 0 && _answerNote.value[i - 4] == 0 && _answerNote.value[i + 4] == 0) {
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i + 1] = 0
                }
                //=========================================================================================================우로 치우쳐짐
                else if (_answerNote.value[i] != 2 && i + 1 == rightBoundIndex) {
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                } else if (_answerNote.value[i] != 2 && _answerNote.value[i + 2] == 0 && _answerNote.value[i - 2] == 1) {
                    updatedPaintNoteList[i - 2] = 2
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                } else if (_answerNote.value[i] != 2 && i + 3 <= rightBoundIndex && _answerNote.value[i + 3] == 0 && _answerNote.value[i - 3] == 1) {
                    updatedPaintNoteList[i + 1] = 0
                    updatedPaintNoteList[i] = 0
                }
                //============================================================================================================
            } else if (updatedPaintNoteList[i - 1] == 2 && updatedPaintNoteList[i] == 1 && updatedPaintNoteList[i + 1] == 1) { // 앞에 한 개만 정답
                val shiftedAnswerIndex = (i - 1) - 3
                if (updatedPaintNoteList[shiftedAnswerIndex] == 2) { // 만약 원점이 이미 2라면
                    updatedPaintNoteList[i - 1] = 1 // 앞에 한 개를 오답으로 만들고 나머지를 0으로 만듦
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                } else { // 원점이 2가 아니라면
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                    updatedPaintNoteList[shiftedAnswerIndex] = 2
                }
            } else if (updatedPaintNoteList[i - 1] == 2 && updatedPaintNoteList[i] == 2 && updatedPaintNoteList[i + 1] == 1) { // 앞에 두 개 정답
                val shiftedAnswerIndex = (i - 1) - 2
                if (updatedPaintNoteList[shiftedAnswerIndex] == 2) { // 만약 원점이 이미 2라면
                    updatedPaintNoteList[i - 1] = 1 // 앞에 한 개를 오답으로 만들고 나머지를 0으로 만듦
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                } else { // 원점이 2가 아니라면
                    updatedPaintNoteList[i - 1] = 0
                    updatedPaintNoteList[i] = 0
                    updatedPaintNoteList[i + 1] = 0
                    updatedPaintNoteList[shiftedAnswerIndex] = 2
                }
            } else if (updatedPaintNoteList[i - 1] == 1 && updatedPaintNoteList[i] == 1 && updatedPaintNoteList[i + 1] == 2) { // 뒤에 한 개 정답
                val shiftedAnswerIndex = (i + 1) + 3
                updatedPaintNoteList[i - 1] = 0
                updatedPaintNoteList[i] = 0
                updatedPaintNoteList[i + 1] = 0
                updatedPaintNoteList[shiftedAnswerIndex] = 2
            } else if (updatedPaintNoteList[i - 1] == 1 && updatedPaintNoteList[i] == 2 && updatedPaintNoteList[i + 1] == 2) { // 뒤에 두 개만 정답
                val shiftedAnswerIndex = (i + 1) + 2
                updatedPaintNoteList[i - 1] = 0
                updatedPaintNoteList[i] = 0
                updatedPaintNoteList[i + 1] = 0
                updatedPaintNoteList[shiftedAnswerIndex] = 2
            }
            /**========================================================================*/

        }
        _paintNoteList.value = updatedPaintNoteList

        Log.d("answerNote", "_paintNoteList.value ${_paintNoteList.value}")
    }

    /**녹음 진행 정도(초)를 업데이트 해주는 함수*/
    fun updateRecordSecond(newSecond: Double) {
        _recordSecond.value = newSecond
    }

    fun updateBarSecond(newSecond: Double) {
        _barSecond.value = newSecond
    }

    /**카운트 다운(박자)를 업데이트 해주는 함수*/
    fun updateCountDownSecond(newSecond: Int) {
        _countDownSecond.value = newSecond
    }

    /**녹음중 유무 변수를 세팅*/
    fun updateRecordingState(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    /**녹음중 유무 변수를 세팅*/
    fun updateBeepingState(isBeeping: Boolean) {
        _isBeeping.value = isBeeping
    }

    /**init 버튼을 눌렀을 때, 초를 다시 세팅*/
    fun init() {
        _paintNoteList.value = List(WavConsts.FEEDBACK_CHUNK_CNT + 1) { 0 }
        _countDownSecond.value = 4
        _recordSecond.value = 0.0
        _barSecond.value = 0.0
    }
}
