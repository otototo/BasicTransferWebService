Launching webservice requires sbt in PATH

Tech stack:
------
    Akka-HTTP, Scala, sbt

To run:
------
	> sbt run

Available Http endpoints:
------
    Request:
        POST {baseUrl}/transfer
        {
            "sourceAccount": "123",
            "sourceAccount": "456",
            "amount": 500,
        }
    Response:
    {
        "status": 200
        "message": "success"
    }

    Request:
        GET {baseUrl}/transfer?to={account}
    Response:
    {
        "transfers":[
        {
            "sourceAccount":"123456",
            "destinationAccount":"678912",
            "amount":55}
        ]
    }

    Request:
        GET {baseUrl}/transfer?from={account}
    Response:
    {
        "transfers":[
        {
            "sourceAccount":"123456",
            "destinationAccount":"678912",
            "amount":55}
        ]
    }


To improve upon:
------
    Testing:
        * Test starting up the server
    App:
        * Introduce currency
        * Add logging support
        * Add Account validation
        * Gather the rest of requirements
