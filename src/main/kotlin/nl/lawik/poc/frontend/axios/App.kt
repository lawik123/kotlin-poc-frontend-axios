package nl.lawik.poc.frontend.axios

import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.js.Promise
import kotlin.js.json

private const val API_URL = "https://jsonplaceholder.typicode.com/todos"

fun main() {
    axiosGET().then { println(it) }
    axiosPOST().then { println(it) }
    axiosPUT().then { println(it) }
    axiosDELETE().then { println("Todo deleted") }
}

fun axiosGET(): Promise<List<Todo>> {
    return axios(Todo.serializer().list) {
        url = API_URL
    }.then { it.data }
}

fun axiosPOST(): Promise<Todo> {
    return axios(Todo.serializer()) {
        url = API_URL
        method = "POST"
        data = Json(encodeDefaults = false).stringify(
            Todo.serializer(),
            Todo(1, "test", false)
        )
        headers = json("Content-Type" to "application/json")
    }.then { it.data }
}

fun axiosPUT(): Promise<Todo> {
    return axios(Todo.serializer()) {
        url = "$API_URL/1"
        method = "PUT"
        data = Json.stringify(
            Todo.serializer(),
            Todo(1, "test2", true, 1)
        )
        headers = json("Content-Type" to "application/json")
    }.then { it.data }
}

fun axiosDELETE(): Promise<Unit> {
    return axios {
        url = "$API_URL/1"
        method = "DELETE"
    }.then { it.data }
}