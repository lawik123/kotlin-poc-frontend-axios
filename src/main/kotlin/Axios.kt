import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

@JsModule("axios")
private external fun <T> axios(config: AxiosConfigSettings): Promise<AxiosResponse<T>>

/**
 * see [Axios Request Config documentation](https://github.com/axios/axios#request-config) for more info
 */
external interface AxiosConfigSettings {
    var url: String
    var method: String
    var baseUrl: String
    var transformRequest: (data: dynamic, headers: dynamic) -> dynamic
    var transformResponse: (data: dynamic, headers: dynamic) -> dynamic
    var headers: dynamic
    var params: dynamic
    var paramsSerializer: (dynamic) -> String
    var data: dynamic
    var timeout: Number
    var withCredentials: Boolean
    var adapter: dynamic
    var auth: dynamic
    var responseType: String
    var xsrfCookieName: String
    var xsrfHeaderName: String
    var onUploadProgress: (dynamic) -> Unit
    var onDownloadProgress: (dynamic) -> Unit
    var maxContentLength: Number
    var validateStatus: (Number) -> Boolean
    var maxRedirects: Number
    var httpAgent: dynamic
    var httpsAgent: dynamic
    var proxy: dynamic
    var cancelToken: dynamic
}

external interface AxiosResponse<T> {
    val data: T
    val status: Number
    val statusText: String
    val headers: dynamic
    val config: AxiosConfigSettings
    val request: dynamic
}

private fun axiosConfigSettings(): AxiosConfigSettings = js("{}")


/**
 * Function used for making HTTP requests with Axios where the expected result is deserializable
 *
 * @param deserializationStrategy deserialization strategy used to deserialize the json body to
 * the object of type [T].
 *
 * @param config function literal with receiver of type [AxiosConfigSettings]
 * see [Axios Request Config documentation](https://github.com/axios/axios#request-config) for more info
 *
 * @return [Promise] of the type [AxiosResponse], the body ([AxiosResponse.data]) is the de-serialized object of type [T]
 */
fun <T> axios(
    deserializationStrategy: DeserializationStrategy<T>,
    config: AxiosConfigSettings.() -> Unit
): Promise<AxiosResponse<T>> {
    val axiosConfigSettings = axiosConfigSettings()
    axiosConfigSettings.config()
    axiosConfigSettings.transformResponse = { data, _ -> Json.parse(deserializationStrategy, data as String) }
    return axios(axiosConfigSettings)
}

/**
 * Function used for making HTTP requests with Axios without deserialization where the response body is empty
 *
 * @param config function literal with receiver of type [AxiosConfigSettings]
 * see [Axios Request Config documentation](https://github.com/axios/axios#request-config) for more info
 *
 * @return [Promise] of the type [AxiosResponse], the body ([AxiosResponse.data]) is empty ([Unit])
 */
fun axios(config: AxiosConfigSettings.() -> Unit): Promise<AxiosResponse<Unit>> {
    val axiosConfigSettings = axiosConfigSettings()
    axiosConfigSettings.config()
    return axios(axiosConfigSettings)
}
