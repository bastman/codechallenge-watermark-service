# Code Challenge: Watermark Service

## run
    $ ./gradlew bootRun

    ... and then open http://localhost:8080 in your browser
    
## test
    $ ./gradlew clean build test
  
## api endpoints
    - POST /api/watermark-job/submit
    - GET /api/watermark-job/{ticketId}/status
    - GET /api/watermark-job/{ticketId}/download
    
## modules
- rest-service: contains spring-boot application
- common: contains core domain logic (no dependency to spring)
    
## Features:
- spring boot, swagger-ui, undertow, jackson, caffeine, junit5
- gradle-multimodule-project (common, rest-service)
    - the core domain logic resides in common-module
    - you may want to reuse core domain logic within different services/projects (e.g. a kafka-based pipeline) 
- threadsafe immutable data structures 
- sealed class hierarchies
- composition over inheritance

### Testing
- junit5
- JsonSchema-based api testing (https://jsonschema.net)

### Concurrency
- lightweight threading provided by kotlin coroutines, similar to ... 
    - quasar fibers (https://github.com/puniverse/quasar)
    - golang goroutines (https://www.golang-book.com/books/intro/10)
- threadsafe immutable data structures
- immutable message passing via coroutine channels 
    - similar to golang channels (https://gobyexample.com/channels)


### Why Kotlin? Kotlin ...
- is like SWIFT (ios)
- is like Scala, but easy to learn
- is much more type safe than Java - No NullPointerException ;) 
- is fully compatible to Java, but less verbose
- has first class support for closures
- is based on "Effective Java"
- is used in production, by e.g. JetBrains
- coding is pure fun :)


### Kotlin extensions are available for
- spring (https://github.com/sdeleuze/spring-kotlin)
- gradle (https://github.com/gradle/gradle-script-kotlin)
- mockito (https://github.com/nhaarman/mockito-kotlin)
- vert.x (https://github.com/vert-x3/vertx-lang-kotlin)
- quasar (https://github.com/puniverse/quasar/tree/master/quasar-kotlin)
- jackson (https://github.com/FasterXML/jackson-module-kotlin)
- and many more ...