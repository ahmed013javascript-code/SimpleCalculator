package com.example.simplecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.*

val BgDark = Color(0xFF0B0E17)
val TopBarBg = Color(0xFF131826)
val SciBtnBg = Color(0xFF1F1C3D)
val SciText = Color(0xFF9F96FF)
val AcBtnBg = Color(0xFF3A1219)
val AcText = Color(0xFFFF6B6B)
val ActionBtnBg = Color(0xFF232938)
val NumBtnBg = Color(0xFF161B28)
val OpBtnBg = Color(0xFF132247)
val OpText = Color(0xFF5C9DFF)
val EqualBtnBg = Color(0xFF2563EB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = BgDark)) {
                Surface(modifier = Modifier.fillMaxSize(), color = BgDark) {
                    ModernCalculatorApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCalculatorApp() {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var clearOnNextDigit by remember { mutableStateOf(false) }

    val historyList = remember { mutableStateListOf<String>() }
    var showHistorySheet by remember { mutableStateOf(false) }

    fun formatNum(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }

    fun onDigitClick(digit: String) {
        if (clearOnNextDigit || display == "0" || display == "Error") {
            display = if (digit == ".") "0." else digit
            clearOnNextDigit = false
        } else {
            if (digit == "." && display.contains(".")) return
            display += digit
        }
    }

    fun onOperatorClick(op: String) {
        val current = display.toDoubleOrNull() ?: return
        firstOperand = current
        pendingOp = op
        expression = "${formatNum(current)} $op"
        clearOnNextDigit = true
    }

    fun onEqualClick() {
        val op = pendingOp ?: return
        val num1 = firstOperand ?: return
        val num2 = display.toDoubleOrNull() ?: return

        val res = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            "xʸ" -> num1.pow(num2)
            else -> num2
        }

        val formattedRes = formatNum(res)
        val fullRecord = "${formatNum(num1)} ${if (op == "xʸ") "^" else op} ${formatNum(num2)} = $formattedRes"
        if (formattedRes != "Error") {
            historyList.add(0, fullRecord)
        }

        expression = "${formatNum(num1)} ${if (op == "xʸ") "^" else op} ${formatNum(num2)} ="
        display = formattedRes
        firstOperand = null
        pendingOp = null
        clearOnNextDigit = true
    }

    fun onScientificClick(func: String) {
        val current = display.toDoubleOrNull() ?: return
        val (res, label) = when (func) {
            "sin" -> Pair(sin(Math.toRadians(current)), "sin(${formatNum(current)}°)")
            "cos" -> Pair(cos(Math.toRadians(current)), "cos(${formatNum(current)}°)")
            "√x" -> Pair(if (current >= 0) sqrt(current) else Double.NaN, "√(${formatNum(current)})")
            "%" -> Pair(current / 100.0, "${formatNum(current)}%")
            else -> Pair(current, "")
        }

        val formattedRes = formatNum(res)
        if (formattedRes != "Error") {
            historyList.add(0, "$label = $formattedRes")
        }
        expression = label
        display = formattedRes
        clearOnNextDigit = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // الشريط العلوي: Ahmed's Calc + زر History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TopBarBg)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("±", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ahmed's Calc",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { showHistorySheet = true },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1A2234),
                    contentColor = Color(0xFFCBD5E1)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text("↺  History", fontSize = 13.sp)
            }
        }

        // منطقة عرض المعادلة والنتيجة
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                color = Color(0xFF64748B),
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = display,
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // لوحة الأزرار
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcButton("sin", SciBtnBg, SciText, Modifier.weight(1f)) { onScientificClick("sin") }
                CalcButton("cos", SciBtnBg, SciText, Modifier.weight(1f)) { onScientificClick("cos") }
                CalcButton("√x", SciBtnBg, SciText, Modifier.weight(1f)) { onScientificClick("√x") }
                CalcButton("xʸ", SciBtnBg, SciText, Modifier.weight(1f)) { onOperatorClick("xʸ") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcButton("AC", AcBtnBg, AcText, Modifier.weight(1f)) {
                    display = "0"
                    expression = ""
                    firstOperand = null
                    pendingOp = null
                }
                CalcButton("⌫", ActionBtnBg, Color.White, Modifier.weight(1f)) {
                    display = if (display.length > 1 && display != "Error") {
                        display.dropLast(1)
                    } else {
                        "0"
                    }
                }
                CalcButton("%", ActionBtnBg, Color.White, Modifier.weight(1f)) { onScientificClick("%") }
                CalcButton("÷", OpBtnBg, OpText, Modifier.weight(1f)) { onOperatorClick("÷") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcButton("7", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("7") }
                CalcButton("8", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("8") }
                CalcButton("9", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("9") }
                CalcButton("×", OpBtnBg, OpText, Modifier.weight(1f)) { onOperatorClick("×") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcButton("4", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("4") }
                CalcButton("5", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("5") }
                CalcButton("6", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("6") }
                CalcButton("-", OpBtnBg, OpText, Modifier.weight(1f)) { onOperatorClick("-") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                CalcButton("1", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("1") }
                CalcButton("2", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("2") }
                CalcButton("3", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick("3") }
                CalcButton("+", OpBtnBg, OpText, Modifier.weight(1f)) { onOperatorClick("+") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcButton("0", NumBtnBg, Color.White, Modifier.weight(2.1f)) { onDigitClick("0") }
                CalcButton(".", NumBtnBg, Color.White, Modifier.weight(1f)) { onDigitClick(".") }
                CalcButton("=", EqualBtnBg, Color.White, Modifier.weight(1f)) { onEqualClick() }
            }
        }
    }
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = TopBarBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .heightIn(min = 250.dp, max = 450.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculation History",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (historyList.isNotEmpty()) {
                        TextButton(onClick = { historyList.clear() }) {
                            Text("Clear", color = AcText)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (historyList.isEmpty()) {
                    Text(
                        text = "No calculations yet",
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn {
                        items(historyList) { item ->
                            Text(
                                text = item,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            )
                            HorizontalDivider(color = Color(0xFF1E293B))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CalcButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
