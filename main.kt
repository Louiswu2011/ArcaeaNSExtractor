import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

@Serializable
data class Groups(val Groups: List<Group>)

@Serializable
data class Group(val Name: String, val Offset: Int, val Length: Int, val OrderedEntries: List<Entry>)

@Serializable
data class Entry(val OriginalFilename: String, val Offset: Int, val Length: Int)

fun main(args: Array<String>) { // args: binary file, json file, output dir
    val binBr = File(args[0]).inputStream()
    val jsonBr = BufferedReader(File(args[1]).reader())

    val content = StringBuffer()
    jsonBr.forEachLine { content.append(it) }

    val groups = Json.decodeFromString<Groups>(content.toString())

    groups.Groups.forEach { entries ->
        println("Unpacking section ${entries.Name}")
        entries.OrderedEntries.forEach { entry ->
            println("-Dumping ${entry.OriginalFilename}")
            val out = ByteArray(entry.Length)
            binBr.channel.position(entry.Offset.toLong())
            if (binBr.read(out, 0, entry.Length) == entry.Length) {
                val testFile = File(args[2] + entry.OriginalFilename)
                val path = Paths.get(testFile.toURI()).parent
                if (!path.exists()){
                    path.toFile().mkdirs()
                }
                testFile.createNewFile()
                testFile.writeBytes(out)
                println("Success")
            }
        }
    }
    binBr.close()
}

