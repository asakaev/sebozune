# sebozune

### Dedication

> “Would you tell me, please, which way I ought to go from here?”
>
> “That depends a good deal on where you want to get to,” said the Cat.
>
> “I don’t much care where—” said Alice.
>
> “Then it doesn’t matter which way you go,” said the Cat.
>
> “—so long as I get somewhere,” Alice added as an explanation.
>
> “Oh, you’re sure to do that,” said the Cat, “if you only walk long enough.”
>
> —Chapter 6, Pig and Pepper

### Overview
Web application with basic http auth stored in http only cookies. WebSocket connection allowed with valid cookie.

### Build frontend application
`sbt frontendJS/fastOptJS`

### Run server
`sbt backendJVM/reStart`

### Usage
`http://localhost:9000/`

### Auth
`curl -v -u admin:admin http://localhost:9000/auth`

### Test
`sbt test`

### Known issues & errata
1. Protocol message codecs must be fixed (not derived) and versioned.
2. WebSocket frontend Pipe should handle connection errors.
