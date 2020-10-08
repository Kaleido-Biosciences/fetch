# Fetch

Fetch is a REST API that Atlas uses to serve components and other information needed to run Atlas. 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 
See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You will need to have the [kapture-client](https://github.com/Kaleido-Biosciences/kapture-client) package built and 
available in a maven repository that is available to your build process, typically this is local or some internal artifact repository.

### Building

You will need to have environment variables available to the build process, and runtime. so either export these to your
local environment or make them available to the CI/CD build process.

```
KAPTURE_CLIENT_URL
KAPTURE_CLIENT_USERNAME
KAPTURE_CLIENT_PASSWORD
```
To build the JAR file run the following command from the project root directory
```
./mvnw package -Dspring.profiles.active=prod
```
Then build the docker container. You can either copy the docker build scripts to the root directory using `cp src/main/docker/* .`
Or you can modify the dockerfile. This example is copying the docker build and entrypoint.sh files to the root directory.
This should probably be changed.
```
docker build -t "${CI_REGISTRY_IMAGE}:latest" .
docker tag "${CI_REGISTRY_IMAGE}:latest" "${CI_REGISTRY_IMAGE}:${CI_COMMIT_REF_NAME}"
```

## Configurations
The properties username, password and base assume a localhost server where those credentials would be expected
to work (or would be overridden using standard Spring externalization of configuration methods). By switching the
values of username, password and the base URL you can develop against a local host test server, remote test server
using the same code base that you would then deploy to production and connect to a production server. Each time
you only need to over-ride the relevant variables using a environment statements such as

```
export CABINET_CLIENT_USERNAME=my_user_name 
export CABINET_CLIENT_PASSWORD=my_password
export CABINET_CLIENT_BASE=https://myserver.com/api/
```

## Endpoints

The following endpoints are exposed when the application is running:

GET `/health` - Will return with a response of 200 "Alive"

GET `/components/search/{searchTerm}`

POST `/components/find` - Send in the body a JSON array of tuples and the response will return the relevant object
```json
[
    {"id": 2101,
     "classification": "Supplement"},
    {"id": 1401,
     "classification": "Media"},
    {"id": 1351,
     "classification": "Community"},
    {"id": 1402,
     "classification": "Media"},
    {"id": 2102,
     "classification": "Supplement"},
    {"id": 1251,
     "classification": "Batch"}
]
```

GET `/activities/barcodes/{activityName}` -  Returns list of barcodes under the activityName

GET `/activities/activitySummary/{activityName}` - Returns summaries of activities under the activityName

POST `/activities/save` -  Saves a new activity by sending in the body
```json
{
    "activityName": "G001",
    "data": "zip data"
}
```

POST `/activities/save/draft` -  Updates existing activity and just saved as draft
```json
{
    "id": 1,
    "activityName": "G001",
    "checksum": "12314124",
    "data": "zip data"
}
```

POST `/activities/save/completed` -  Updates existing activity and saved it as completed
```json
{
    "id": 1,
    "activityName": "G001",
    "checksum": "12314124",
    "data": "zip data"
}
```

GET `/activities/cabinet/completedlist/{activityName}` - Retrieves list of all completed platemap under the activityName

GET `/activities/cabinet/draft/{activityName}` - Retrieves draft data under the activityName

GET `/activities/cabinet/completed/{checksum}` - Retrieve the specified completed platemap data based on the checksum

GET `/activities/search/{searchTerm}` - Finds activities based on a search parameter. The activity is the root of everything in atlas

## Built With

* [Spring-Boot](https://spring.io/projects/spring-boot) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/Kaleido-Biosciences/fetch/tags). 

## Authors

* **Mark Schreiber** - *Initial work* - [@markjschreiber](https://github.com/markjschreiber)
* **Pat Kyle** - *Initial work* - [@psk788](https://github.com/psk788)
* **Wes Fowlks** - *Initial work* - [@wfowlks](https://github.com/wfowlks)

See also the list of [contributors](https://github.com/Kaleido-Biosciences/fetch/graphs/contributors) who participated in this project.

## License

This project is licensed under the BSD 3-clause "New" or "Revised" License - see the [LICENSE.md](LICENSE.md) file for details
