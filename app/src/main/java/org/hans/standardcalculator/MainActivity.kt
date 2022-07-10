package org.hans.standardcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import org.hans.standardcalculator.db.HistoryDao
import org.hans.standardcalculator.db.HistoryDatabase
import org.hans.standardcalculator.db.HistoryMemo

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById(R.id.expressionTextView)
    }
    private val resultTextView: TextView by lazy {
        findViewById(R.id.resultTextView)
    }
    private val historyLayout: View by lazy {
        findViewById(R.id.historyLayout)
    }
    private val historyLinearLayout: LinearLayout by lazy {
        findViewById(R.id.historyLinearLayout)
    }

    private var isOperator = false
    private var hasOperator = false

    private val historyDao: HistoryDao by lazy {
        val historyDatabase: HistoryDatabase = HistoryDatabase.getInstance(applicationContext)
        historyDatabase.historyDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun buttonClicked(v: View) {
        when(v.id) {
            R.id.buttonZero -> numberButtonClicked("0")
            R.id.buttonOne -> numberButtonClicked("1")
            R.id.buttonTwo -> numberButtonClicked("2")
            R.id.buttonThree -> numberButtonClicked("3")
            R.id.buttonFour -> numberButtonClicked("4")
            R.id.buttonFive -> numberButtonClicked("5")
            R.id.buttonSix -> numberButtonClicked("6")
            R.id.buttonSeven -> numberButtonClicked("7")
            R.id.buttonEight -> numberButtonClicked("8")
            R.id.buttonNine -> numberButtonClicked("9")

            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMultiply -> operatorButtonClicked("×")
            R.id.buttonDivision -> operatorButtonClicked("÷")
            R.id.buttonPercent -> operatorButtonClicked("%")
        }
    }

    private fun numberButtonClicked(number: String) {
        // 연산자를 사용 후 왔으면 띄어쓰기 추가
        if (isOperator) {
            expressionTextView.append(" ")
        }

        isOperator = false

        val expressionTexts = expressionTextView.text.split(" ")
        // 예외처리
        // 설명 : split으로 구분된 숫자 배열 중 마지막 값이 15자리 넘으면 예외처리
        if (expressionTexts.isNotEmpty() && expressionTexts.last().length >= 15) {
            Toast.makeText(this, "15자리 까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()

            return
        // 설명 : split으로 구분된 숫자 배열 중 마지막 값의 처음값이 0이 오면 예외처리
        } else if (expressionTexts.last().isEmpty() && number == "0") {
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()

            return
        }

        expressionTextView.append(number)
        resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        // 예외처리
        // 설명 : 맨 처음에 연산자가 올 수 없도록 예외처리
        if (expressionTextView.text.isEmpty()) {
            return
        }

        when {
            isOperator -> {
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1) + operator
            }
            hasOperator -> {
                Toast.makeText(this, "연산자는 한번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()

                return
            }
            else -> {
                expressionTextView.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.textGreen)),
            expressionTextView.text.length - 1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        expressionTextView.text = ssb

        isOperator = true
        hasOperator = true
    }

    // 실시간으로 계산하는 메서드
    private fun calculateExpression(): String {
        val expressionTexts = expressionTextView.text.split(" ")

        // 예외처리
        // 설명 : 연산자가 없거나 (피연산자, 연산자, 피연산자)로 구성되지 않으면 공백으로 예외처리
        if (!hasOperator || expressionTexts.size != 3) {
            return ""
        // 설명 : 피연산자들이 숫자가 아니면 공백으로 예외처리
        } else if (!expressionTexts[0].isNumber() || !expressionTexts[2].isNumber()) {
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when (op) {
                "+" -> (exp1 + exp2).toString()
                "-" -> (exp1 - exp2).toString()
                "÷" -> (exp1 / exp2).toString()
                "×" -> (exp1 * exp2).toString()
                "%" -> (exp1 % exp2).toString()
                else -> ""
        }
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTextView.text.split(" ")

        // 예외처리
        // 설명 : expressionTextView가 비어있거나 피연산자만 있으면 결과가 안나오도록 예외처리
       if (expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        // 예외처리
        // 설명 : (피연산자, 연산자)로만 아직 구성중이면 결과가 안나오도록 예외처리
        if (expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "아직 완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()

            return
        }

        // 예외처리
        // 설명 : 피연산자들이 숫자가 아니면 결과가 안나오도록 예외처리
       if (!expressionTexts[0].isNumber() || !expressionTexts[2].isNumber()) {
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()

            return
        }

        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        // DB에 데이터를 Insert하는 기능
        Thread(Runnable {
            historyDao.insertHistory(HistoryMemo(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTextView.text = resultText

        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(v: View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()

        // DB에서 데이터를 가져와서 View에 붙여주는 기능
        Thread(Runnable {
            historyDao.getAll().reversed().forEach {
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    val expressionTextView = historyView.findViewById<TextView>(R.id.expressionTextView)
                    val resultTextView = historyView.findViewById<TextView>(R.id.resultTextView)

                    expressionTextView.text = it.expression
                    resultTextView.text = " = ${it.result}"

                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()
    }
    fun historyCloseButtonClicked(v: View) {
        historyLayout.isVisible = false
    }

    fun historyClearButtonClicked(v: View) {
        // DB에서 데이터를 가져와서 붙여준 View를 지워주는 기능
        historyLinearLayout.removeAllViews()

        // DB에 데이터를 지워주는 기능
        Thread(Runnable {
            historyDao.deleteAll()
        }).start()
    }

    // 문자를 숫자로 변환해서 숫자인지 확인하는 메서드
    // 설명 : 숫자로 변경시 문자열이 포함되있으면 NumberFormatException 예외처리
   fun String.isNumber(): Boolean {
        return try {
            this.toBigInteger()

            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}