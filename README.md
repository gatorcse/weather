# Weather App

This is a basic weather api using the typelevel stack.

## Running
Should run easily in the console with

`sbt run`

## Calling
The weather endpoint is `GET /weather?lat=***&long=***`

Here is an example request using curl:

`curl -X GET --location "http://0.0.0.0:8080/weather?lat=39.7456&long=-97.0892"`

And the expected response:

`{"short":"Sunny","temp":"warm"}`