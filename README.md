 # Kotlin Axios PoC
This project is a PoC of using Kotlin (JS) and the Axios library, it showcases the following features:
* Using a JavaScript library (Axios) from npm with Kotlin (including type safety)
* Serializing objects to JSON and de-serializing JSON to objects
* Making GET, POST, PUT, and DELETE requests using the Axios library
* Using Webpack to bundle(and minify)/run the project
* Removing unused code to reduce file size
 
Note: It is assumed that you have basic knowledge of Kotlin, npm, JavaScript, modules, and Webpack
 
## Installation
1. Clone this repository

## Running the project
Run the following command in the root directory of the project: `gradlew -t run`. 
This will download gradle, download the required dependencies and run a webpack server for the project on port 8088 in continuous build mode (the browser will refresh automatically on code changes).

## Bundling for production
Run the following command in the root directory of the project: `gradlew clean bundle -Pprod`. 
This will download gradle, download the required dependencies, compile(transpile) Kotlin to JavaScript, bundle the JavaScript code into a single file, minify it and remove unused code to reduce file size.
The bundle and the HTML file in the `resources` folder will placed in a `web` folder in the root of the project.

## About

### Using a JavaScript library from npm
The kotlin-frontend-plugin allows you to add npm libraries from Gradle, after applying the plugin you can add npm dependencies in the following manner:
```groovy
kotlinFrontend {
    npm {
        dependency "axios"
        // other (dev)dependencies here 
    }
    // ...
}
```
Similarly you can add dev dependencies in the same npm block using a devDependency tag instead of dependency.

To use the library in a type-safe manner, external declarations are written (see `Axios.kt`), these are similar to `*.d.ts` files from TypeScript.

Additionally, in the same file, you can see that the following external function has a `@JsModule` annotation:
```kotlin
@JsModule("axios")
private external fun <T> axios(config: AxiosConfigSettings): Promise<AxiosResponse<T>>
```
This tells the compiler that the external function is a JavaScript module, i.e. it is exported from a JavaScript file somewhere by `module.exports.MODULE_NAME` and imported in Kotlin using `@JsModule("MODULE_NAME")`.

### Serializing
The `kotlinx.serialization` library is used for (de)serialization, 
you must annotate serializable classes with the `@Serializable` annotation (see `Todo.kt`).
This will provide the class with a `serializer()` function which must be passed to the `parse` and `stringify` functions.

### Making GET, POST, PUT, and DELETE requests
A function has been written to ease the use of Axios within Kotlin:
```kotlin
fun <T> axios(
    deserializationStrategy: DeserializationStrategy<T>,
    config: AxiosConfigSettings.() -> Unit
): Promise<AxiosResponse<T>> {
    val axiosConfigSettings = axiosConfigSettings()
    axiosConfigSettings.config()
    axiosConfigSettings.transformResponse = { data, _ -> Json.parse(deserializationStrategy, data as String) }
    return axios(axiosConfigSettings)
}
```
This function takes two parameters, a `DeserializationStrategy` for the expected return type and a `AxiosConfigSettings` function literal with receiver to allow for easy configuration of the request.
The body of the response is de-serialized using the `kotlinx.serialization` library and the provided `DeserializationStrategy`.

This function allows you to make requests in the following manner:
```kotlin
fun axiosPUT(): Promise<Todo> {
    return axios(Todo.serializer()) {
        url = "https://jsonplaceholder.typicode.com/todos/1"
        method = "PUT"
        data = Json.stringify(Todo.serializer(), Todo(1, "test2", true, 1))
        headers = json("Content-Type" to "application/json")
    }.then { it.data }
}
```
As you can see, the request is easily configurable thanks to the function literal with receiver. For all the available configuration settings refer to the `external interface AxiosConfigSettings` in `Axios.kt` or refer to the [Axios Request Config documentation](https://github.com/axios/axios#request-config)

### Using webpack to bundle(and minify)/run the project
The kotlin-frontend-plugin allows you to make use of Webpack, the configuration for this project looks like this:
```groovy
kotlinFrontend {
    // ...
    webpackBundle {
        bundleName = "this-will-be-overwritten" // NOTE: for example purposes this is overwritten in `webpack.config.d/filename.js`.
        contentPath = file('src/main/resources/web')
        if(project.hasProperty('prod')){
            mode = "production"
        }
        // ... 
    }
}
```
It's also possible to customize the Webpack configuration using JavaScript by adding additional scripts in the `webpack.config.d` folder. The scripts will be appended to the end of the generated Webpack config script (you can see the final output by viewing the `webpack.config.js` file in the generated build folder), the scripts are appended alphabetically, use numbers prefix to change the order.
See the `filename.js` script for an example on how a script is used to set the name of the bundle(this is just a simple example, the use of scripts allow for full customization of Webpack).

Using the `gradlew -t run` command runs the webpack dev server in continuous mode.

Using the `gradlew bundle -Pprod` command will build the project for production, the Kotlin files will be compiled(transpiled) to JavaScript, the required JavaScript files will be bundled into a single JavaScript file, the bundle will be minified, and moved to the web folder in the root directory of the project (along with the HTML files in the `src/resources/web) folder.

### Removing unused code
The `kotlin-dce-js` Gradle plugin automatically removes unused code from the compiled(transpiled) JavaScript file, this is especially useful due to the fact that the Kotlin standard library is rather big and has to be included with the app in order for it to run.
Without the plugin, the production bundle (which is minified) is 955KB, the plugin brings this down to 241KB.
