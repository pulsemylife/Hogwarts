package ru.mavelle.hogwarts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.store by preferencesDataStore("hogwarts_world")
private val parchment = Color(0xFFE7DCC5)
private val muted = Color(0xFFB7AA91)
private val gold = Color(0xFFC5A66A)
private val ink = Color(0xFF171613)
private val panel = Color(0xFF24221D)
private val green = Color(0xFF39483C)
private val line = Color(0xFF38342B)

data class Student(
    val id:String, val name:String, val house:String, val initial:String,
    val bio:String, val voice:String, val status:String
)
data class Message(val from:String, val text:String, val time:String = "")
data class Room(val id:String, val title:String, val members:List<String>, val description:String, val group:Boolean = false)

private val students = listOf(
    Student("draco","Драко Малфой","Слизерин","Д","Самоуверенный, язвительный, наблюдательный.","Сухая ирония, точные замечания, сдержанность.","В гостиной Слизерина"),
    Student("theo","Теодор Нотт","Слизерин","Т","Немногословный, вдумчивый, внимательный к деталям.","Спокойный, интеллектуальный, лаконичный.","В библиотеке"),
    Student("blaise","Блейз Забини","Слизерин","Б","Уверенный, светский, проницательный.","Элегантная ирония, наблюдательность.","На уроке зельеварения"),
    Student("hermione","Гермиона Грейнджер","Гриффиндор","Г","Эрудированная, принципиальная, рассудительная.","Аргументы, факты, ясные объяснения.","В библиотеке"),
    Student("harry","Гарри Поттер","Гриффиндор","Г","Смелый, прямой, иногда упрямый.","Просто и честно, без пафоса.","На тренировке по квиддичу"),
    Student("ron","Рон Уизли","Гриффиндор","Р","Верный друзьям, эмоциональный, с юмором.","Живой разговорный стиль, самоирония.","В Большом зале"),
    Student("pansy","Пэнси Паркинсон","Слизерин","П","Острая на язык, наблюдательная.","Меткие замечания, светская манера.","В гостиной Слизерина"),
    Student("luna","Луна Лавгуд","Когтевран","Л","Необычная, спокойная, открытая новому.","Мягко и образно, неожиданные ассоциации.","У теплиц"),
    Student("cedric","Седрик Диггори","Пуффендуй","С","Доброжелательный, честный, справедливый.","Уважительный, спокойный, поддерживающий.","Во дворе"),
    Student("fred","Фред Уизли","Гриффиндор","Ф","Изобретательный, озорной, энергичный.","Быстрые шутки, выдумки и дружеские подколы.","В коридоре"),
    Student("george","Джордж Уизли","Гриффиндор","Дж","Сообразительный, находчивый, с тонким юмором.","Ироничный, любит развивать шутки брата.","В коридоре")
)


private val teachers = listOf(
    Student("dumbledore","Альбус Дамблдор","Преподаватель","А","Мудрый директор Хогвартса, внимательный к людям и склонный говорить загадками.","Спокойная речь, мягкий юмор, неожиданные вопросы.","В кабинете директора"),
    Student("mcgonagall","Минерва Макгонагалл","Преподаватель","М","Строгая, справедливая и требовательная преподавательница трансфигурации.","Точная, сдержанная, иногда сухо-ироничная.","В кабинете трансфигурации"),
    Student("snape","Северус Снегг","Преподаватель","С","Проницательный мастер зелий, резкий и требовательный к ученикам.","Холодные замечания, сарказм, лаконичность.","В подземельях"),
    Student("hagrid","Рубеус Хагрид","Преподаватель","Х","Добродушный хранитель ключей и лесничий, заботится о магических существах.","Тёплая, простая и эмоциональная речь.","У хижины"),
    Student("flitwick","Филиус Флитвик","Преподаватель","Ф","Доброжелательный преподаватель заклинаний, ценит старание и точность.","Воодушевлённо, вежливо, с академическим интересом.","В классе заклинаний"),
    Student("sprout","Помона Спраут","Преподаватель","П","Заботливая профессор травологии, терпеливая и практичная.","Доброжелательно, обстоятельно, с любовью к растениям.","В теплице"),
    Student("lupin","Римус Люпин","Преподаватель","Р","Тактичный и внимательный преподаватель защиты от тёмных искусств.","Спокойно, уважительно, поддерживающе.","В кабинете ЗОТИ"),
    Student("trelawney","Сивилла Трелони","Преподаватель","Т","Эксцентричная преподавательница прорицаний, говорит загадочно и образно.","Таинственные намёки и театральные предсказания.","В башне прорицаний")
)
private val allPeople = students + teachers

private val rooms = listOf(
    Room("great_hall","Большой зал", students.map{it.id}, "Общий школьный чат", true),
    Room("gryffindor","Гостиная Гриффиндора", listOf("harry","ron","hermione","fred","george"), "Новости и разговоры факультета", true),
    Room("slytherin","Гостиная Слизерина", listOf("draco","theo","blaise","pansy"), "Разговоры змеиного факультета", true),
    Room("library","Библиотека после отбоя", listOf("theo","hermione","luna"), "Тихий уголок для тех, кто не спит", true),
    Room("twins","Уизли: семейный заговор", listOf("fred","george","ron"), "Планы, шутки и подозрительные идеи", true)
)


data class PlayerProfile(val name:String = "Виктория", val house:String = "Слизерин", val year:String = "5 курс")
data class Relationship(val trust:Int = 0, val respect:Int = 0, val rivalry:Int = 0) {
    fun label():String = when {
        trust >= 4 -> "Доверяет тебе"
        respect >= 4 -> "Уважает тебя"
        rivalry >= 4 -> "Соперничество"
        trust <= -3 -> "Настороженность"
        else -> "Знакомство"
    }
}

private val houseOptions = listOf("Гриффиндор","Слизерин","Когтевран","Пуффендуй")
private val yearOptions = listOf("1 курс","2 курс","3 курс","4 курс","5 курс","6 курс","7 курс")

private val relationshipNotes = mapOf(
    "draco" to "Относится настороженно, но уважает уверенность и острый ум.",
    "theo" to "Ценит спокойные беседы и постепенно начинает доверять.",
    "blaise" to "Считает тебя интересным собеседником; любит тонкую иронию.",
    "hermione" to "Уважает любознательность и честность.",
    "harry" to "Готов поддержать, если дело касается справедливости.",
    "ron" to "Быстро переходит на дружеский тон, особенно за едой.",
    "pansy" to "Присматривается и проверяет, умеешь ли ты держать удар.",
    "luna" to "Принимает тебя без лишних вопросов.",
    "cedric" to "Относится доброжелательно и с уважением.",
    "fred" to "Уже подозревает, что ты подходящий сообщник для шалостей.",
    "george" to "Считает, что у тебя есть потенциал для хорошей затеи."
)


private data class OwlArrival(val studentId:String, val message:String, val headline:String)

private fun nextOwlArrival():OwlArrival {
    return listOf(
        OwlArrival("draco","Ты собираешься сегодня появиться в библиотеке или предпочитаешь, чтобы я лично прислал приглашение? — Д.","Драко ищет тебя"),
        OwlArrival("theo","Я нашёл упоминание о старом проходе рядом с библиотекой. Если тебе интересны такие вещи, загляни после ужина.","Записка от Теодора"),
        OwlArrival("blaise","В гостиной сегодня будет интересный разговор. Постарайся не опаздывать — самое любопытное обычно происходит в начале.","Блейз приглашает на встречу"),
        OwlArrival("hermione","Я собираю небольшую компанию для поиска сведений о старой школьной легенде. Ты с нами?","Гермиона предлагает расследование"),
        OwlArrival("harry","Слушай, у нас тут кое-что странное происходит возле внутреннего двора. Можешь подойти?","Гарри заметил странность"),
        OwlArrival("ron","Ты не поверишь, что только что случилось в Большом зале. Приходи, пока Фред с Джорджем не устроили вторую часть представления.","Рон зовёт в Большой зал"),
        OwlArrival("pansy","Небольшой совет: сегодня вечером будь внимательнее к разговорам за столом Слизерина. Кажется, назревает кое-что занятное.","Пэнси предупреждает"),
        OwlArrival("luna","Я оставила для тебя записку возле окна в библиотеке. Она, кажется, сама туда прилетела. Не опаздывай.","Луна оставила загадку"),
        OwlArrival("cedric","Мы собираемся на дружескую тренировку во дворе. Будет здорово, если присоединишься.","Седрик приглашает на тренировку"),
        OwlArrival("fred","У нас с Джорджем появилась идея. Нам нужен человек с хорошим чувством юмора и крепкими нервами. Заинтересована?","Фред ищет сообщницу"),
        OwlArrival("george","Фред говорит, что у него план. Я говорю, что плану нужен здравомыслящий наблюдатель. Это ты?","Джордж просит помощи")
    ).random()
}

private fun generateWorldMoment(profile:PlayerProfile):String {
    val moments = listOf(
        "В Большом зале Фред и Джордж спорят, кто придумал более удачную шутку. Рон просит их хотя бы не проверять её на нём.",
        "В библиотеке Гермиона пытается убедить Теодора, что старинная книга должна вернуться на своё место.",
        "В гостиной Слизерина Блейз заметил, что Драко сегодня необычно задумчив.",
        "Луна оставила на доске объявлений загадочную заметку о странном свечении возле теплиц.",
        "Седрик предложил ученикам разных факультетов объединиться для дружеской тренировки.",
        "Пэнси принесла в гостиную свежую школьную сплетню, но пока не раскрывает источник.",
        "На лестнице кто-то оставил свёрток с запиской: «Не открывать до полуночи»."
    )
    return moments.random()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HogwartsApp(this) }
    }
}

@Composable
fun HogwartsApp(context: Context) {
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var tab by remember { mutableStateOf("chats") }
    var customRooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var chatData by remember { mutableStateOf<Map<String,List<Message>>>(emptyMap()) }
    var events by remember { mutableStateOf(listOf(
        "Сегодня вечером — собрание клуба любителей зельеварения.",
        "На доске объявлений появилось загадочное приглашение.",
        "Команда Гриффиндора готовится к тренировке по квиддичу."
    )) }
    var profile by remember { mutableStateOf(PlayerProfile()) }
    var relationships by remember { mutableStateOf<Map<String,Relationship>>(allPeople.associate { it.id to Relationship() }) }
    val scope = rememberCoroutineScope()
    val key = stringPreferencesKey("world_data")
    LaunchedEffect(Unit) {
        context.store.data.collect { prefs ->
            try {
                val root = JSONObject(prefs[key] ?: "{}")
                val loaded = mutableMapOf<String,List<Message>>()
                val chats = root.optJSONObject("chats") ?: JSONObject()
                chats.keys().forEach { id ->
                    val arr = chats.optJSONArray(id) ?: JSONArray()
                    loaded[id] = (0 until arr.length()).map { i ->
                        val m = arr.getJSONObject(i)
                        Message(m.optString("from"), m.optString("text"), m.optString("time"))
                    }
                }
                chatData = loaded
                val custom = root.optJSONArray("customRooms") ?: JSONArray()
                customRooms = (0 until custom.length()).map { i ->
                    val r = custom.getJSONObject(i)
                    Room(r.getString("id"),r.getString("title"),r.getString("members").split(","),
                        r.optString("description"),true)
                }
                val ev = root.optJSONArray("events")
                if (ev != null && ev.length() > 0) events = (0 until ev.length()).map { ev.getString(it) }
                val pr = root.optJSONObject("profile")
                if (pr != null) profile = PlayerProfile(pr.optString("name","Виктория"),
                    pr.optString("house","Слизерин"),pr.optString("year","5 курс"))
                val rel = root.optJSONObject("relationships")
                if (rel != null) {
                    val restored = mutableMapOf<String,Relationship>()
                    rel.keys().forEach { id ->
                        val item = rel.getJSONObject(id)
                        restored[id] = Relationship(item.optInt("trust"),item.optInt("respect"),item.optInt("rivalry"))
                    }
                    relationships = allPeople.associate { it.id to (restored[it.id] ?: Relationship()) }
                }
                // On each app opening the castle has a new small development.
                val lastVisit = root.optLong("lastVisit",0L)
                val now = System.currentTimeMillis()
                if (lastVisit == 0L || now-lastVisit > 45*60*1000L) {
                    val arrival = nextOwlArrival()
                    val oldList = loaded[arrival.studentId] ?: emptyList()
                    val stamp = SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())
                    chatData = loaded + (arrival.studentId to (oldList + Message(arrival.studentId,arrival.message,stamp)))
                    events = events + arrival.headline + ": " + arrival.message + " " + generateWorldMoment(profile)
                }
            } catch (_:Exception) { }
        }
    }
    fun persist(nextChats:Map<String,List<Message>> = chatData, nextRooms:List<Room> = customRooms,
                nextEvents:List<String> = events, nextProfile:PlayerProfile = profile,
                nextRelationships:Map<String,Relationship> = relationships) {
        chatData = nextChats; customRooms = nextRooms; events = nextEvents; profile = nextProfile
        relationships = nextRelationships
        scope.launch {
            val root = JSONObject()
            val chats = JSONObject()
            nextChats.forEach { (id,list) ->
                val arr = JSONArray()
                list.forEach { m -> arr.put(JSONObject().put("from",m.from).put("text",m.text).put("time",m.time)) }
                chats.put(id,arr)
            }
            root.put("chats",chats)
            val cr = JSONArray()
            nextRooms.forEach { r -> cr.put(JSONObject().put("id",r.id).put("title",r.title)
                .put("members",r.members.joinToString(",")).put("description",r.description)) }
            root.put("customRooms",cr)
            root.put("events",JSONArray(nextEvents))
            root.put("profile",JSONObject().put("name",nextProfile.name).put("house",nextProfile.house).put("year",nextProfile.year))
            val rel = JSONObject()
            nextRelationships.forEach { (id,value) ->
                rel.put(id,JSONObject().put("trust",value.trust).put("respect",value.respect).put("rivalry",value.rivalry))
            }
            root.put("relationships",rel)
            root.put("lastVisit",System.currentTimeMillis())
            context.store.edit { it[key] = root.toString() }
        }
    }
    MaterialTheme(colorScheme=darkColorScheme(background=ink,surface=panel,primary=gold,onPrimary=ink,
        onBackground=parchment,onSurface=parchment,secondary=muted)) {
        Surface(Modifier.fillMaxSize(),color=ink) {
            if (selectedRoom != null) {
                val room = selectedRoom!!
                RoomScreen(room,chatData[room.id] ?: emptyList(), onBack={selectedRoom=null},
                    onSend={text ->
                        val stamp = SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())
                        val old = chatData[room.id] ?: emptyList()
                        val updated = old + Message("me",text,stamp)
                        val replies:List<Pair<String,String>> = if(room.group) WorldEngine.groupReply(room,text,updated.size)
                            else {
                                val person=allPeople.firstOrNull{it.id==room.members.first()}
                                if(person==null) emptyList() else listOf(person.id to CharacterEngine.reply(person,text,updated.size))
                            }
                        persist(chatData + (room.id to (updated + replies.map { Message(it.first,it.second,stamp) })))
                    }, onClear={persist(chatData + (room.id to emptyList()))})
            } else {
                MainHub(tab, {tab=it}, chatData, customRooms, events, profile, relationships, {selectedRoom=it},
                    {title,members,description ->
                        val r=Room("custom_"+System.currentTimeMillis(),title,members,description,true)
                        persist(nextRooms=customRooms+r)
                    }, {persist(nextEvents=events+it)},
                    {next -> persist(nextProfile=next)},
                    {id,delta -> 
                        val old=relationships[id] ?: Relationship()
                        val updated=old.copy(trust=(old.trust+delta.first).coerceIn(-5,5),
                            respect=(old.respect+delta.second).coerceIn(-5,5),
                            rivalry=(old.rivalry+delta.third).coerceIn(0,5))
                        persist(nextRelationships=relationships+(id to updated),
                            nextEvents=events+"Твои отношения с ${allPeople.first{it.id==id}.name}: ${updated.label().lowercase()}.")
                    })
            }
        }
    }
}

@Composable
fun MainHub(tab:String,onTab:(String)->Unit,chats:Map<String,List<Message>>,custom:List<Room>,
            events:List<String>,profile:PlayerProfile,relationships:Map<String,Relationship>,open:(Room)->Unit,
            create:(String,List<String>,String)->Unit,addEvent:(String)->Unit,
            saveProfile:(PlayerProfile)->Unit,changeRelationship:(String,Triple<Int,Int,Int>)->Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(Color(0xFF1D1B17)).padding(20.dp)) {
            Text("✦ HOGWARTS",color=gold,fontSize=12.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Твоя совятня",color=parchment,fontSize=27.sp,fontWeight=FontWeight.SemiBold)
            Text("DARK ACADEMIA · ШКОЛА МАГИИ",color=muted,fontSize=10.sp,letterSpacing=2.sp)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=10.dp, vertical=8.dp), horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("chats" to "Чаты","people" to "Ученики","teachers" to "Учителя","story" to "Сюжет","events" to "События","profile" to "Профиль").forEach { (id,label) ->
                Text(label,color=if(tab==id) gold else muted,fontWeight=if(tab==id) FontWeight.Bold else FontWeight.Normal,
                    modifier=Modifier.clickable{onTab(id)}.padding(10.dp))
            }
        }
        when(tab) {
            "chats" -> {
                LazyColumn(contentPadding=PaddingValues(bottom=20.dp)) {
                    item { SectionLabel("ОБЩИЕ ЧАТЫ") }
                    items(rooms+custom) { r -> RoomRow(r,chats[r.id]?.lastOrNull()?.text ?: r.description){open(r)} }
                    item { SectionLabel("ЛИЧНЫЕ СОВЯТНИ") }
                    items(allPeople) { s ->
                        val r=Room(s.id,s.name,listOf(s.id),s.bio,false)
                        RoomRow(r,chats[s.id]?.lastOrNull()?.text ?: s.status){open(r)}
                    }
                }
                Button(onClick={onTab("create")},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp),
                    colors=ButtonDefaults.buttonColors(containerColor=gold)) { Text("＋ Создать групповой чат",color=ink) }
            }
            "people" -> LazyColumn { items(students) { s ->
                Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                    Avatar(s.initial)
                    Column(Modifier.weight(1f).padding(start=12.dp)) {
                        Text(s.name,color=parchment,fontSize=16.sp)
                        Text(s.house+" · "+s.status,color=muted,fontSize=11.sp)
                        Text(s.bio,color=muted,fontSize=12.sp)
                    }
                    Text("Написать",color=gold,modifier=Modifier.clickable{open(Room(s.id,s.name,listOf(s.id),s.bio,false))})
                }
                HorizontalDivider(color=line,thickness=.5.dp)
            } }
            "teachers" -> LazyColumn {
                item { SectionLabel("ПРЕПОДАВАТЕЛЬСКИЙ СОСТАВ") }
                items(teachers) { teacher ->
                    Row(Modifier.fillMaxWidth().clickable { open(Room(teacher.id,teacher.name,listOf(teacher.id),teacher.bio,false)) }
                        .padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                        Avatar(teacher.initial)
                        Column(Modifier.weight(1f).padding(start=12.dp)) {
                            Text(teacher.name,color=parchment,fontSize=15.sp)
                            Text(teacher.status,color=gold,fontSize=11.sp)
                            Text(teacher.bio,color=muted,fontSize=12.sp)
                        }
                        Text("Написать",color=gold,fontSize=11.sp)
                    }
                    HorizontalDivider(color=line,thickness=.5.dp)
                }
            }
            "story" -> StoryScreen(profile,events,relationships,open,changeRelationship,addEvent)
            "events" -> {
                LazyColumn {
                    item { SectionLabel("ШКОЛЬНЫЕ ОБЪЯВЛЕНИЯ И СЛУХИ") }
                    items(events.reversed()) { e ->
                        Text("✦  $e",color=parchment,fontSize=14.sp,modifier=Modifier.fillMaxWidth()
                            .padding(horizontal=18.dp,vertical=14.dp).background(panel,RoundedCornerShape(12.dp)).padding(14.dp))
                    }
                    item { Button(onClick={addEvent("Новое событие: ${listOf("В Большом зале назначена встреча.","В библиотеке нашли старую записку.","Завтра состоится факультетская тренировка.").random()}")},modifier=Modifier.padding(16.dp),
                        colors=ButtonDefaults.buttonColors(containerColor=gold)){Text("Добавить событие",color=ink)} }
                }
            }
            "profile" -> ProfileScreen(profile,relationships,saveProfile)
            "create" -> CreateRoomScreen(onCancel={onTab("chats")},onCreate={title,members,desc->create(title,members,desc);onTab("chats")})
            else -> CreateRoomScreen(onCancel={onTab("chats")},onCreate={title,members,desc->create(title,members,desc);onTab("chats")})
        }
    }
}

@Composable
fun SectionLabel(text:String) {
    Text(text,color=gold,fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(start=20.dp,top=14.dp,bottom=8.dp))
}
@Composable
fun Avatar(initial:String) {
    Box(Modifier.size(46.dp).background(green,CircleShape),contentAlignment=Alignment.Center) {
        Text(initial,color=gold,fontSize=21.sp,fontWeight=FontWeight.Medium)
    }
}
@Composable
fun RoomRow(room:Room,preview:String,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clickable{onClick()}.padding(horizontal=18.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
        Avatar(if(room.group) "✦" else allPeople.find{it.id==room.members.firstOrNull()}?.initial ?: "✦")
        Column(Modifier.weight(1f).padding(start=13.dp)) {
            Text(room.title,color=parchment,fontSize=15.sp,fontWeight=FontWeight.Medium)
            Text(preview,color=muted,fontSize=12.sp,maxLines=1)
        }
        Text(if(room.group) "ГРУППА" else "ЛИЧНО",color=gold,fontSize=9.sp)
    }
    HorizontalDivider(color=line,thickness=.5.dp)
}




@Composable
fun StoryScreen(profile:PlayerProfile,events:List<String>,relationships:Map<String,Relationship>,
                open:(Room)->Unit,changeRelationship:(String,Triple<Int,Int,Int>)->Unit,
                addEvent:(String)->Unit) {
    data class Quest(val id:String,val person:String,val title:String,val intro:String,
                     val choices:List<Triple<String,Triple<Int,Int,Int>>>)
    val quests = listOf(
        Quest("dumbledore_trial","dumbledore","Письмо директора",
            "Дамблдор просит тебя разобраться, почему из школьного архива исчезла страница старой летописи. Он не торопит тебя с выводами.",
            listOf(
                Triple("Изучить записи и проверить каждую зацепку",Triple(2,2,0)),
                Triple("Сначала поговорить с библиотекарем и свидетелями",Triple(2,1,0)),
                Triple("Предложить директору собственную версию",Triple(0,2,1))
            )),
        Quest("mcgonagall_test","mcgonagall","Испытание Макгонагалл",
            "Профессор поручает тебе восстановить порядок в классе после необычного происшествия. Важно не только исправить последствия, но и понять, что пошло не так.",
            listOf(
                Triple("Действовать по инструкции и не торопиться",Triple(1,2,0)),
                Triple("Объединить однокурсников и распределить задачи",Triple(2,1,0)),
                Triple("Самостоятельно найти нестандартное решение",Triple(0,2,1))
            )),
        Quest("snape_potion","snape","Заметки в подземельях",
            "Снегг обнаружил в лабораторном журнале противоречивые записи. Он поручает тебе сверить их с учебником и объяснить, где допущена ошибка. Никаких экспериментов без его разрешения.",
            listOf(
                Triple("Точно сверить записи и указать на ошибку",Triple(1,2,0)),
                Triple("Задать уточняющие вопросы перед выводом",Triple(2,1,0)),
                Triple("Защитить свою гипотезу аргументами",Triple(0,2,1))
            )),
        Quest("hagrid_creature","hagrid","Тайна у Запретного леса",
            "Хагрид просит помочь выяснить, кому принадлежат странные следы у опушки. Он напоминает: сначала наблюдение и осторожность, никаких самостоятельных вылазок в лес.",
            listOf(
                Triple("Помочь Хагриду собрать сведения у хижины",Triple(2,1,0)),
                Triple("Найти описание следов в библиотеке",Triple(1,2,0)),
                Triple("Предложить дождаться сопровождения преподавателя",Triple(2,2,0))
            )),
        Quest("lupin_defence","lupin","Урок Люпина",
            "Люпин предлагает разобрать учебную ситуацию по защите от тёмных искусств. Он оценивает не силу заклинания, а самообладание и умение поддержать товарищей.",
            listOf(
                Triple("Поддержать растерявшегося однокурсника",Triple(2,2,0)),
                Triple("Спокойно разобрать ситуацию по шагам",Triple(1,2,0)),
                Triple("Взять на себя роль координатора группы",Triple(2,1,0))
            )),
        Quest("trelawney_omen","trelawney","Предсказание в башне",
            "Трелони замечает необычный символ на старой карте звёздного неба. Она уверена, что это знак, но предлагает тебе самой проверить значение символа.",
            listOf(
                Triple("Сопоставить символ с книгами по астрономии",Triple(1,2,0)),
                Triple("Выслушать Трелони и записать все детали",Triple(2,1,0)),
                Triple("Предложить несколько возможных трактовок",Triple(0,2,1))
            ))
    )
    val studentStories = listOf(
        Triple("theo","Тайна старого прохода","Теодор нашёл в каталоге странную ссылку на закрытое крыло."),
        Triple("draco","Разговор в подземельях","Драко оставил записку о странных разговорах в гостиной Слизерина."),
        Triple("hermione","Школьная загадка","Гермиона собирает сведения о загадочном символе на полях старых книг."),
        Triple("fred","Подозрительный план","Фред и Джордж ищут добровольца, который поможет проверить их новую, но безопасную задумку.")
    )
    var expanded by remember { mutableStateOf<String?>(null) }
    val completed = events.filter { it.startsWith("Завершено задание: ") }
        .map { it.removePrefix("Завершено задание: ").substringBefore(" —") }.toSet()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("ТВОЯ ИСТОРИЯ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        Text("${profile.name}, ${profile.house}",color=parchment,fontSize=23.sp,fontWeight=FontWeight.SemiBold)
        Text("Главы, поручения и решения, которые меняют твою репутацию в Хогвартсе.",color=muted,fontSize=12.sp)
        Spacer(Modifier.height(16.dp))
        Text("ПОРУЧЕНИЯ ПРЕПОДАВАТЕЛЕЙ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        quests.forEach { quest ->
            val rel = relationships[quest.person] ?: Relationship()
            val done = quest.id in completed
            Column(Modifier.fillMaxWidth().padding(vertical=6.dp).background(panel,RoundedCornerShape(14.dp)).padding(14.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(quest.title,color=gold,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
                        Text(allPeople.first { it.id==quest.person }.name,color=muted,fontSize=11.sp)
                    }
                    Text(if(done) "ЗАВЕРШЕНО" else "ГЛАВА",color=if(done) Color(0xFF9EB59A) else gold,fontSize=9.sp)
                }
                Spacer(Modifier.height(7.dp))
                Text(quest.intro,color=parchment,fontSize=13.sp)
                Text("Отношения: ${rel.label()}",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=7.dp))
                if (!done && expanded==quest.id) {
                    Text("Твой выбор:",color=gold,fontSize=12.sp,modifier=Modifier.padding(top=9.dp))
                    quest.choices.forEach { (label,delta) ->
                        Text("• $label",color=parchment,fontSize=12.sp,
                            modifier=Modifier.fillMaxWidth().clickable {
                                val nextTrust=(rel.trust+delta.first).coerceIn(-5,5)
                                val nextRespect=(rel.respect+delta.second).coerceIn(-5,5)
                                val ending = when {
                                    nextTrust>=3 && nextRespect>=3 -> "Преподаватель отмечает твою надёжность и приглашает к следующему делу."
                                    nextRespect>=3 -> "Преподаватель признаёт твою самостоятельность и внимательность."
                                    else -> "Поручение завершено. Преподаватель пока присматривается к твоим решениям."
                                }
                                changeRelationship(quest.person,delta)
                                addEvent("Завершено задание: ${quest.id} — ${quest.title}. $ending")
                                expanded=null
                            }.padding(vertical=8.dp))
                    }
                } else if (!done) {
                    Text("ПРИНЯТЬ ПОРУЧЕНИЕ  ›",color=gold,fontSize=9.sp,letterSpacing=1.sp,
                        modifier=Modifier.clickable { expanded=quest.id }.padding(top=10.dp))
                } else {
                    Text("Это дело уже записано в твоём журнале.",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=8.dp))
                }
                Text("Открыть переписку",color=muted,fontSize=11.sp,
                    modifier=Modifier.clickable {
                        val person=allPeople.first { it.id==quest.person }
                        open(Room(person.id,person.name,listOf(person.id),person.bio,false))
                    }.padding(top=8.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("ИСТОРИИ УЧЕНИКОВ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        studentStories.forEach { (id,title,description) ->
            val rel=relationships[id] ?: Relationship()
            Column(Modifier.fillMaxWidth().padding(vertical=6.dp).background(panel,RoundedCornerShape(14.dp)).padding(14.dp)) {
                Text(title,color=gold,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
                Text(description,color=parchment,fontSize=13.sp,modifier=Modifier.padding(top=5.dp))
                Text("Отношения: ${rel.label()}",color=muted,fontSize=11.sp,modifier=Modifier.padding(top=7.dp))
                Text("Открыть переписку  ›",color=gold,fontSize=10.sp,
                    modifier=Modifier.clickable {
                        val person=allPeople.first { it.id==id }
                        open(Room(id,person.name,listOf(id),person.bio,false))
                    }.padding(top=8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("ЖУРНАЛ ПРИКЛЮЧЕНИЙ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        val records=events.filter { it.startsWith("Завершено задание: ") }
        if(records.isEmpty()) Text("Здесь появятся итоги твоих поручений.",color=muted,fontSize=12.sp,
            modifier=Modifier.padding(vertical=8.dp))
        records.reversed().forEach { record ->
            Text("✦  ${record.substringAfter(" — ")}",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=7.dp))
            HorizontalDivider(color=line,thickness=.5.dp)
        }
        Text("ПОСЛЕДНИЕ СОБЫТИЯ",color=gold,fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(top=16.dp))
        events.filterNot { it.startsWith("Завершено задание: ") }.takeLast(5).reversed().forEach { e ->
            Text("✦  $e",color=muted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
            HorizontalDivider(color=line,thickness=.5.dp)
        }
    }
}

@Composable
fun ProfileScreen(profile:PlayerProfile,relationships:Map<String,Relationship>,onSave:(PlayerProfile)->Unit) {
    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var house by remember(profile.house) { mutableStateOf(profile.house) }
    var year by remember(profile.year) { mutableStateOf(profile.year) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("ТВОЙ ПРОФИЛЬ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        Spacer(Modifier.height(8.dp))
        Text(name.ifBlank{"Новая ученица"},color=parchment,fontSize=26.sp,fontWeight=FontWeight.SemiBold)
        Text("$house · $year",color=muted,fontSize=13.sp)
        Spacer(Modifier.height(20.dp))
        Text("Имя",color=gold,fontSize=12.sp)
        DarkField(name,{name=it},"Как тебя зовут?")
        Spacer(Modifier.height(14.dp))
        Text("Факультет",color=gold,fontSize=12.sp)
        houseOptions.forEach { option ->
            Row(Modifier.fillMaxWidth().clickable{house=option}.padding(vertical=5.dp),verticalAlignment=Alignment.CenterVertically) {
                RadioButton(selected=house==option,onClick={house=option},colors=RadioButtonDefaults.colors(selectedColor=gold))
                Text(option,color=parchment)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Курс",color=gold,fontSize=12.sp)
        yearOptions.forEach { option ->
            Row(Modifier.fillMaxWidth().clickable{year=option}.padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically) {
                RadioButton(selected=year==option,onClick={year=option},colors=RadioButtonDefaults.colors(selectedColor=gold))
                Text(option,color=parchment)
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick={onSave(PlayerProfile(name.ifBlank{"Ученица"},house,year))},
            colors=ButtonDefaults.buttonColors(containerColor=gold)) { Text("Сохранить профиль",color=ink) }
        Spacer(Modifier.height(18.dp))
        Text("ТВОИ ОТНОШЕНИЯ С УЧЕНИКАМИ",color=gold,fontSize=10.sp,letterSpacing=2.sp)
        allPeople.forEach { s ->
            val rel=relationships[s.id] ?: Relationship()
            Column(Modifier.fillMaxWidth().padding(vertical=8.dp)) {
                Text(s.name+" · "+rel.label(),color=parchment,fontSize=14.sp,fontWeight=FontWeight.Medium)
                Text(relationshipNotes[s.id] ?: s.bio,color=muted,fontSize=12.sp)
                Text("Доверие ${rel.trust} · Уважение ${rel.respect} · Соперничество ${rel.rivalry}",
                    color=gold,fontSize=10.sp)
            }
            HorizontalDivider(color=line,thickness=.5.dp)
        }
        Text("Отношения — сюжетные заметки этой версии, а не динамическая психологическая модель.",color=muted,fontSize=10.sp)
    }
}

@Composable
fun CreateRoomScreen(onCancel:()->Unit,onCreate:(String,List<String>,String)->Unit) {
    var title by remember{mutableStateOf("")}
    var desc by remember{mutableStateOf("Новый разговор учеников")}
    val chosen= remember { mutableStateListOf<String>() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Новая совятня",color=parchment,fontSize=24.sp)
        Text("Выбери участников группового чата",color=muted,fontSize=12.sp)
        Spacer(Modifier.height(14.dp))
        DarkField(title,{title=it},"Название чата")
        Spacer(Modifier.height(8.dp))
        DarkField(desc,{desc=it},"Описание")
        allPeople.forEach { s ->
            Row(Modifier.fillMaxWidth().clickable{if(chosen.contains(s.id))chosen.remove(s.id) else chosen.add(s.id)}.padding(8.dp),
                verticalAlignment=Alignment.CenterVertically) {
                Checkbox(checked=chosen.contains(s.id),onCheckedChange={if(it)chosen.add(s.id) else chosen.remove(s.id)},
                    colors=CheckboxDefaults.colors(checkedColor=gold))
                Text(s.name,color=parchment)
                Spacer(Modifier.weight(1f)); Text(s.house,color=muted,fontSize=10.sp)
            }
        }
        Button(onClick={if(title.isNotBlank()&&chosen.isNotEmpty())onCreate(title,chosen.toList(),desc)},
            enabled=title.isNotBlank()&&chosen.isNotEmpty(),colors=ButtonDefaults.buttonColors(containerColor=gold)) {
            Text("Создать чат",color=ink)
        }
        TextButton(onClick=onCancel){Text("Отмена",color=muted)}
    }
}
@Composable
fun DarkField(value:String,onValue:(String)->Unit,hint:String) {
    BasicTextField(value,onValue,modifier=Modifier.fillMaxWidth().background(panel,RoundedCornerShape(10.dp)).padding(14.dp),
        textStyle=androidx.compose.ui.text.TextStyle(color=parchment,fontSize=14.sp),
        decorationBox={inner->if(value.isEmpty())Text(hint,color=muted);inner()})
}

@Composable
fun RoomScreen(room:Room,messages:List<Message>,onBack:()->Unit,onSend:(String)->Unit,onClear:()->Unit) {
    var draft by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(Color(0xFF1D1B17)).padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("‹",color=gold,fontSize=30.sp,modifier=Modifier.clickable{onBack()}.padding(end=12.dp))
            Column(Modifier.weight(1f)) {
                Text(room.title,color=parchment,fontSize=17.sp,fontWeight=FontWeight.SemiBold)
                Text(if(room.group) "Общий чат · ${room.members.size} участников" else "Личная совятня",
                    color=gold,fontSize=10.sp)
            }
            Text("Очистить",color=muted,fontSize=10.sp,modifier=Modifier.clickable{onClear()})
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(14.dp)) {
            if(messages.isEmpty()) Text(room.description,color=muted,fontSize=13.sp,modifier=Modifier.padding(8.dp))
            messages.forEach { m ->
                val mine=m.from=="me"
                Row(Modifier.fillMaxWidth(),horizontalArrangement=if(mine)Arrangement.End else Arrangement.Start) {
                    Column(Modifier.widthIn(max=310.dp).padding(vertical=4.dp)
                        .background(if(mine)green else panel,RoundedCornerShape(13.dp)).padding(11.dp)) {
                        Text(if(mine)"ТЫ" else allPeople.find{it.id==m.from}?.name ?: m.from,
                            color=gold,fontSize=9.sp,letterSpacing=1.sp)
                        Text(m.text,color=parchment,fontSize=14.sp)
                        if(m.time.isNotEmpty()) Text(m.time,color=muted,fontSize=9.sp,modifier=Modifier.align(Alignment.End))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF211F1A)).padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
            BasicTextField(draft,{draft=it},Modifier.weight(1f).background(Color(0xFF302D25),RoundedCornerShape(22.dp)).padding(14.dp),
                textStyle=androidx.compose.ui.text.TextStyle(color=parchment,fontSize=14.sp),
                decorationBox={inner->if(draft.isEmpty())Text("Написать письмо…",color=muted);inner()})
            Spacer(Modifier.width(8.dp))
            Button(onClick={val t=draft.trim();if(t.isNotEmpty()){onSend(t);draft=""}},
                colors=ButtonDefaults.buttonColors(containerColor=gold),shape=CircleShape) { Text("➤",color=ink) }
        }
    }
}

object CharacterEngine {
    fun reply(s:Student,input:String,count:Int):String {
        val t=input.lowercase()
        if (s.id in teachers.map { it.id }) return teacherReply(s.id,t)
        return when {
            listOf("привет","здравствуй","доброе утро","добрый вечер").any{t.contains(it)} -> when(s.id) {
                "draco" -> "Наконец-то. Я уж решил, что у тебя закончились достойные темы для разговора."
                "theo" -> "Привет. Как проходит твой день?"
                "hermione" -> "Привет! Надеюсь, у тебя всё хорошо. Что случилось?"
                "ron" -> "О, привет! Есть новости? Только скажи, что не контрольная по зельям."
                "luna" -> "Привет. Сегодня коридоры кажутся особенно задумчивыми."
                else -> "Привет. Рад тебя слышать. Что нового?"
            }
            listOf("помоги","совет","что делать","как поступить").any{t.contains(it)} -> when(s.id) {
                "hermione" -> "Давай разберём ситуацию по пунктам. Сначала отделим факты от предположений, а затем посмотрим, какие есть варианты."
                "draco" -> "Для начала перестань суетиться. Опиши, что именно произошло, и оценим ситуацию без лишних эмоций."
                "theo" -> "Мне нужно чуть больше контекста. Что произошло и какого результата ты хочешь добиться?"
                "ron" -> "Сначала выдохни. Не обязательно решать всё за одну минуту. Расскажи, что стряслось."
                else -> "Расскажи подробнее, что произошло. Тогда я смогу предложить что-то полезное."
            }
            listOf("грущу","грустно","плохо","устала","переживаю").any{t.contains(it)} ->
                if(s.id=="luna") "Иногда даже самые длинные коридоры приводят к тихому месту, где можно передохнуть. Хочешь рассказать, что тревожит?"
                else "Не стану делать вид, будто это пустяк. Хочешь рассказать, что произошло?"
            t.contains("?") -> when(s.id) {
                "hermione" -> "Хороший вопрос. Я бы сначала уточнила обстоятельства, а затем проверила факты. Можешь добавить подробности?"
                "draco" -> "Зависит от обстоятельств. Не люблю делать выводы, не зная всей картины. Что именно ты имеешь в виду?"
                "theo" -> "Вопрос интереснее, чем кажется. Какую сторону ситуации ты считаешь самой важной?"
                "blaise" -> "Ответ зависит от того, чего ты добиваешься. Практичность или эффектность?"
                else -> "Я бы не торопился с выводами. Расскажи чуть больше, и обсудим."
            }
            else -> when(s.id) {
                "draco" -> "Неожиданное замечание. Я бы взглянул на это с другой стороны: что здесь самое важное для тебя?"
                "theo" -> "Я обдумаю это. В твоих словах есть деталь, которую легко упустить. Что ты сама считаешь главным?"
                "blaise" -> "Любопытная история. Обычно люди упускают самое очевидное — мотивы. Как ты оцениваешь происходящее?"
                "hermione" -> "Давай уточним детали, чтобы не строить догадки. Какой результат ты хотела бы получить?"
                "ron" -> "Вот это поворот. И что было дальше?"
                "pansy" -> "О, это уже интереснее. И кто ещё об этом знает?"
                "luna" -> "Возможно, здесь есть нечто, чего мы пока не заметили. Что тебе подсказывает интуиция?"
                "cedric" -> "Спасибо, что поделилась. Как ты сама к этому относишься?"
                else -> "Звучит как начало истории, которая может закончиться либо аплодисментами, либо уборкой кабинета."
            }
        }
    }

    private fun teacherReply(id:String,t:String):String = when {
        listOf("привет","здравствуйте","добрый день").any { t.contains(it) } -> when(id) {
            "dumbledore" -> "Добрый день. Надеюсь, этот день принесёт вам не только новые знания, но и несколько поводов для размышлений. Чем могу помочь?"
            "mcgonagall" -> "Здравствуйте. Надеюсь, вы пришли не для того, чтобы сообщить о новом нарушении школьных правил. Что вам нужно?"
            "snape" -> "Если приветствие закончено, перейдём к сути."
            "hagrid" -> "Здорово! Заходи, чайник как раз закипает. Как дела?"
            else -> "Здравствуйте. Рад вас видеть. Что привело вас ко мне?"
        }
        listOf("совет","помогите","что делать","не получается").any { t.contains(it) } -> when(id) {
            "dumbledore" -> "Попробуйте взглянуть на ситуацию с другой стороны. Иногда правильный вопрос важнее быстрого ответа. Что именно вас беспокоит?"
            "mcgonagall" -> "Начните с того, что вам известно наверняка. Затем составьте план и придерживайтесь его. Не торопитесь."
            "snape" -> "Внимательность избавляет от большинства ошибок. Изложите факты последовательно, если рассчитываете на полезный совет."
            "hagrid" -> "Не переживай раньше времени. Давай разберёмся по порядку — вместе всякая задача кажется поменьше."
            "lupin" -> "Не обязательно справляться в одиночку. Расскажите, что произошло, и мы спокойно рассмотрим возможные решения."
            else -> "Давайте разберём вопрос по шагам. С чего всё началось?"
        }
        t.contains("зель") || t.contains("домашн") -> when(id) {
            "snape" -> "Если бы вы уделяли указаниям столько же внимания, сколько вопросам, результат был бы значительно лучше. Назовите задание."
            "mcgonagall" -> "Прочитайте условие ещё раз и проверьте каждый шаг. Аккуратность — основа хорошей работы."
            else -> "Сначала уточним условие задания. Что именно требуется выполнить?"
        }
        else -> when(id) {
            "dumbledore" -> "Любопытное наблюдение. Не забывайте: выборы определяют не только путь, но и то, кем мы становимся. Что вы думаете об этом?"
            "mcgonagall" -> "Я выслушала вас. Постарайтесь сформулировать, какой результат вы хотите получить."
            "snape" -> "Ваше сообщение не содержит достаточных сведений для содержательного ответа. Будьте конкретнее."
            "hagrid" -> "Вот это да! Расскажи ещё. Только не пугай моих зверушек громкими историями."
            "flitwick" -> "Прекрасный повод для небольшого исследования! Какие детали вы уже успели заметить?"
            "sprout" -> "Всему своё время. Даже самое крепкое растение сначала пускает маленький корень. Расскажите подробнее."
            "lupin" -> "Спасибо, что поделились. Давайте посмотрим на ситуацию спокойно и без поспешных выводов."
            else -> "Звёзды редко дают прямые ответы… Но иногда полезно прислушаться к тому, что происходит вокруг."
        }
    }
}

object WorldEngine {
    fun groupReply(room:Room,userText:String,count:Int):List<Pair<String,String>> {
        val t=userText.lowercase()
        val pool=room.members.mapNotNull{id->allPeople.find{it.id==id}}
        if(pool.isEmpty()) return emptyList()
        // Lightweight local simulation: a few participants react in sequence.
        val first=pool[(count+1)%pool.size]
        val second=pool[(count+3)%pool.size]
        val a=when(first.id) {
            "draco" -> "Надеюсь, это обсуждение окажется содержательнее обычных школьных разговоров."
            "hermione" -> "Прежде чем делать выводы, предлагаю уточнить детали."
            "ron" -> "Я только за, если это не очередная история с домашним заданием."
            "theo" -> "Любопытно. Думаю, здесь есть над чем подумать."
            "fred" -> "У меня уже есть идея. Не уверен, что Филч её одобрит."
            "george" -> "А Филчу необязательно знать все подробности."
            "luna" -> "Иногда хорошие разговоры начинаются с самых неожиданных слов."
            "blaise" -> "Посмотрим, к чему приведёт эта беседа."
            "pansy" -> "Наконец-то хоть что-то интересное."
            else -> "Я согласен. Давайте обсудим."
        }
        val b=when {
            t.contains("?") -> when(second.id) {
                "hermione" -> "Есть несколько вариантов. Давайте рассмотрим их спокойно."
                "draco" -> "Вопрос поставлен неплохо. Ответ, впрочем, не так очевиден."
                "ron" -> "А можно сначала поесть, а потом принимать решения?"
                else -> "Мне кажется, стоит выслушать всех."
            }
            else -> when(second.id) {
                "theo" -> "Я бы обратил внимание на одну деталь: контекст здесь важнее первого впечатления."
                "pansy" -> "И всё же я уверена, что кто-то знает больше, чем говорит."
                "cedric" -> "Главное — не забывать уважать мнение друг друга."
                else -> "Хорошо сказано. Кто-нибудь хочет добавить что-то ещё?"
            }
        }
        return listOf(first.id to a, second.id to b)
    }
}
