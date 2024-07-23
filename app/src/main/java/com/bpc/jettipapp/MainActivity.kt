package com.bpc.jettipapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpc.jettipapp.components.InputField
import com.bpc.jettipapp.ui.theme.JetTipAppTheme
import com.bpc.jettipapp.util.calculateTotalPerPerson
import com.bpc.jettipapp.util.calculateTotalTip
import com.bpc.jettipapp.widgets.RoundIconButton
import java.text.NumberFormat
import java.text.NumberFormat.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            myApp {
                //TopHeader()
                MainContent()
            }
        }
    }
}

@Composable
fun myApp(content: @Composable () -> Unit) {
    JetTipAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Surface(modifier = Modifier
                .padding(innerPadding), color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}


@Composable
fun TopHeader(totalPerPerson: Double = 0.0){
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(15.dp)
        .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFFE9D7F7)
        //.clip(shape = RoundedCornerShape(corner = CornerSize(12.dp)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(text = "Total por Pessoa",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
                )
            Text(text = "R$ $total",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold)

        }
    }
}


@Preview
@Composable
fun MainContent(){
    BillForm(){ billAmt ->
        Log.d("AMT", "MainContent: $billAmt")}

}

@Composable
fun BillForm(modifier: Modifier = Modifier,
             onValueChange: (String) -> Unit = {}){
    val totalBillState = remember {
        mutableStateOf(value = "")

    }
    val splitByState = remember {
        mutableStateOf(1)
    }
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val sliderPositionState = remember {
        mutableStateOf(0f)
    }
    val tipPercentage = (sliderPositionState.value * 100).toInt()

    val range = IntRange(start = 1, endInclusive = 100)

    val tipAmountState = remember {
        mutableStateOf(0.0)
    }

    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        TopHeader(totalPerPerson = totalPerPersonState.value)

        Surface(modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ){
            Column(modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                InputField(
                    valueState = totalBillState,
                    labelId = "Insira o Valor",
                    enabled = true,
                    isSingleLine = true,
                    onAction = KeyboardActions {
                        if (!validState) return@KeyboardActions
                        onValueChange(totalBillState.value.trim())

                        keyboardController?.hide()
                    }
                )
                //if (validState) {
                Row (modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.Start){
                    Text(text = "Dividir por: ",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(120.dp))
                    Row(modifier = Modifier.padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.End){
                        RoundIconButton(
                            imageVector = Icons.Default.Remove,
                            onClick = {
                                splitByState.value =
                                    if (splitByState.value > 1) splitByState.value - 1 else 1
                                totalPerPersonState.value = calculateTotalPerPerson(totalBill = totalBillState.value.toDoubleOrNull() ?: 0.0,
                                    splitBy = splitByState.value,
                                    tipPercentage = tipPercentage)

                            })

                        Text(text="${splitByState.value}",
                            modifier = Modifier
                                .align(alignment = Alignment.CenterVertically)
                                .padding(start = 9.dp, end = 9.dp))
                        RoundIconButton(
                            imageVector = Icons.Default.Add,
                            onClick = {
                                if (splitByState.value < range.last) splitByState.value = splitByState.value + 1
                                totalPerPersonState.value = calculateTotalPerPerson(totalBill = totalBillState.value.toDoubleOrNull() ?: 0.0,
                                    splitBy = splitByState.value,
                                    tipPercentage = tipPercentage)
                            })

                    }

                }

                Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)){
                    Text(text = "Gorjeta:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(180.dp))

                    Text(text = "R$ ${tipAmountState.value}", modifier = Modifier.align(alignment = Alignment.CenterVertically),)
                }

                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "$tipPercentage %",  fontWeight = FontWeight. Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    //Slider
                    Slider(value = sliderPositionState.value,
                        onValueChange = {newVal ->
                            sliderPositionState.value = newVal
                            val tipPercentage = (sliderPositionState.value * 100).toInt()
                            tipAmountState.value = calculateTotalTip(
                                totalBill = totalBillState.value.toDoubleOrNull() ?: 0.0,
                                tipPercentage = tipPercentage
                            )
                            totalPerPersonState.value = calculateTotalPerPerson(totalBill = totalBillState.value.toDoubleOrNull() ?: 0.0,
                                splitBy = splitByState.value,
                                tipPercentage = tipPercentage)

                        }, modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        steps = 9)


                }
            }
        }

    }

}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetTipAppTheme {
        myApp {
            //TopHeader()
            MainContent()
        }
    }
}