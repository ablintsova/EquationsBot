import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.network.fold
import kotlin.math.roundToInt

private val problems = mutableListOf<Problem>() // задачи
private var currentProblemId = 0
private var currentAnswer = 0.0

fun main() {
    setProblems()
    equationsBot.startPolling()
}

// бот и описание его команд
private val equationsBot = bot {
    token = "1470123209:AAElzaYbNxiZ_5_0ikd8PNbQ3UWoFC7L2m0"
    timeout = 30
    dispatch {

        // самая первая команда при запуске бота
        command("start") {
            val keyboardMarkup = KeyboardReplyMarkup(keyboard = setCommandButtons())

            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = Texts.START + "\n\n" + Texts.HELP,
                parseMode = MARKDOWN,
                replyMarkup = keyboardMarkup)

            result.fold({
                println("start success")
            }, {
                println("start error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // отображение списка команд
        command("помощь") {
            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = Texts.HELP,
                parseMode = MARKDOWN)

            result.fold({
                println("help success")
            }, {
                println("help error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // вывод теоретических сведений о решениии уравнений
        command("теория") {

            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                parseMode = MARKDOWN,
                text = Texts.THEORY)

            result.fold({
                println("theory success")
            }, {
                println("theory error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // вывод примера на составление уравнения к задаче
        command("пример") {

            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                parseMode = MARKDOWN,
                text = Texts.EXAMPLE)

            result.fold({
                println("example success")
            }, {
                println("example error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // вывод случайной задачи
        command("задача") {
            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = getProblem(),
                parseMode = MARKDOWN)

            result.fold({
                println("problem success")
            }, {
                println("problem error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // вывод случайного уравнения
        command("уравнение") {
            val result = bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = getEquation())

            result.fold({
                println("equation success")
            }, {
                println("equation error")
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }

        // обработка ответа на задачу или уравнение от пользователя
        command("ответ") {
            val response =
                if (args.isNotEmpty() && args[0].toDouble() == currentAnswer) "Правильно!"
                else "Правильный ответ: $currentAnswer"
            val result = bot.sendMessage(chatId = message.chat.id, text = response)

            result.fold({
                println("answer success")
            }, {
                bot.sendMessage(message.chat.id, text = "Произошла ошибка!")
            })
        }
    }
}

// формирование вспомогательной клавиатуры с командами
fun setCommandButtons(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("/теория")),
        listOf(KeyboardButton("/пример")),
        listOf(KeyboardButton("/уравнение")),
        listOf(KeyboardButton("/задача")),
        listOf(KeyboardButton("/помощь"))
    )
}

// формирование массива с задачами
private fun setProblems() {
    var index = 0

    for (item in Texts.PROBLEMS) {
        val problem = Problem()
        problem.id = index
        problem.text = item.key
        problem.answer = item.value
        problems.add(index, problem)
        index++
    }
}

// выбор случайной задачи
private fun getProblem(): String {
    val ids = problems.map { it.id }
    currentProblemId = ids.random()
    currentAnswer = problems[currentProblemId].answer

    println(problems[currentProblemId].text)
    return problems[currentProblemId].text
}

// получение случайного уравнения
private fun getEquation(): String {
    var num1: Double = (-100..100).random().toDouble()
    val num2: Double = (-100..100).random().toDouble()
    val op = (1..4).random() // выбор операции
    val x = (1..2).random() // выбор позиции неизвестного операнда
    var equation = ""
    val res: Double

    when (op) {
        1 -> {
            res = num1 + num2
            equation = buildEquation(x, num1, num2, "+") + " = $res"
        }
        2 -> {
            res = num1 - num2
            equation = buildEquation(x, num1, num2, "-") + " = $res"
        }
        3 -> {
            res = num1 * num2
            equation = buildEquation(x, num1, num2, "*") + " = $res"
        }
        4 -> {
            res = (num1 / num2 * 1000.0).roundToInt() / 1000.0 //округляем до трёх знаков после запятой
            num1 = num2 * res // корректируем 1-ый операнд
            equation = buildEquation(x, num1, num2, "/") + " = $res"
        }
    }

    println(equation)

    return equation
}

// формирование строки с уравнением
private fun buildEquation(xPosition: Int, op1: Double, op2: Double, operation: String): String {
    return when (xPosition) {
        1 -> {
            currentAnswer = op1
            if (op2 < 0) {
                "x $operation ($op2)"
            } else {
                "x $operation $op2"
            }
        }
        2 -> {
            currentAnswer = op2
            "$op1 $operation x"
        }
        else -> "Ошибка при составлениии уравнения!"
    }
}

