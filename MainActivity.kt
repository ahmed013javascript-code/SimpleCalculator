package com.example.simplecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScientificCalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun ScientificCalculatorScreen() {
    var num1 by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("النتيجة ستظهر هنا") }

    // قائمة تفاعلية لحفظ سجل العمليات السابقة
    val historyList = remember { mutableStateListOf<String>() }

    // دالة مساعدة لتحديث النتيجة وإضافة العملية للسجل في نفس الوقت
    fun runBinaryOp(op: String) {
        val calcResult = calculate(num1, num2, op)
        result = calcResult.first
        if (calcResult.second != null) {
            historyList.add(0, calcResult.second!!) // إضافة العملية في أعلى القائمة
        }
    }

    fun runSingleOp(op: String) {
        val calcResult = calculateSingle(num1, op)
        result = calcResult.first
        if (calcResult.second != null) {
            historyList.add(0, calcResult.second!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "آلة أحمد العلمية",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = num1,
            onValueChange = { num1 = it },
            label = { Text("الرقم الأول (أو الزاوية)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = num2,
            onValueChange = { num2 = it },
            label = { Text("الرقم الثاني") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // الصف الأول: العمليات الأساسية + زر المسح C
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { runBinaryOp("+") }) { Text("+", fontSize = 18.sp) }
            Button(onClick = { runBinaryOp("-") }) { Text("-", fontSize = 18.sp) }
            Button(onClick = { runBinaryOp("*") }) { Text("×", fontSize = 18.sp) }
            Button(onClick = { runBinaryOp("/") }) { Text("÷", fontSize = 18.sp) }
            Button(
                onClick = {
                    num1 = ""
                    num2 = ""
                    result = "تم المسح"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("C", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // الصف الثاني: العمليات العلمية
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = { runBinaryOp("^") }) { Text("xʸ") }
            OutlinedButton(onClick = { runSingleOp("sqrt") }) { Text("√x") }
            OutlinedButton(onClick = { runSingleOp("%") }) { Text("%") }
            OutlinedButton(onClick = { runSingleOp("sin") }) { Text("sin") }
            OutlinedButton(onClick = { runSingleOp("cos") }) { Text("cos") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = result,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider() // خط فاصل أنيق بين الحاسبة والسجل

        // رأس قسم السجل مع زر مسح السجل
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل العمليات (${historyList.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (historyList.isNotEmpty()) {
                TextButton(onClick = { historyList.clear() }) {
                    Text("مسح السجل", color = Color(0xFFD32F2F))
                }
            }
        }

        // قائمة السجل القابلة للتمرير
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(historyList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = item,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
    }
}

// ترجع هذه الدالة زوجًا (Pair): النص المعروض للنتيجة + نص العملية لحفظه في السجل
fun calculate(n1: String, n2: String, op: String): Pair<String, String?> {
    val number1 = n1.toDoubleOrNull()
    val number2 = n2.toDoubleOrNull()

    if (number1 == null || number2 == null) {
        return Pair("أدخل الرقم الأول والثاني", null)
    }

    val symbol = when (op) {
        "*" -> "×"
        "/" -> "÷"
        else -> op
    }

    val ans = when (op) {
        "+" -> number1 + number2
        "-" -> number1 - number2
        "*" -> number1 * number2
        "/" -> if (number2 != 0.0) number1 / number2 else return Pair("لا يمكن القسمة على صفر!", null)
        "^" -> number1.pow(number2)
        else -> return Pair("خطأ", null)
    }

    val formattedAns = formatNumber(ans)
    val record = "${formatNumber(number1)} $symbol ${formatNumber(number2)} = $formattedAns"
    return Pair("النتيجة: $formattedAns", record)
}

fun calculateSingle(n1: String, op: String): Pair<String, String?> {
    val number1 = n1.toDoubleOrNull() ?: return Pair("أدخل الرقم الأول", null)

    val (ans, recordPrefix) = when (op) {
        "sqrt" -> if (number1 >= 0) Pair(sqrt(number1), "√${formatNumber(number1)}") else return Pair("لا يوجد جذر لعدد سالب", null)
        "%" -> Pair(number1 / 100.0, "${formatNumber(number1)}%")
        "sin" -> Pair(sin(Math.toRadians(number1)), "sin(${formatNumber(number1)}°)")
        "cos" -> Pair(cos(Math.toRadians(number1)), "cos(${formatNumber(number1)}°)")
        else -> return Pair("خطأ", null)
    }

    val formattedAns = formatNumber(ans)
    val record = "$recordPrefix = $formattedAns"
    return Pair("النتيجة: $formattedAns", record)
}




