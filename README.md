# deck

High performance backend server for building user-interface on top of CloudFoundry platform

## Usage

Fill in the required details in [application.yaml](src/main/resources/application.yaml)

```yaml
app:
  cf:
    api-endpoint:   # CloudFoundry API endpoint
    oauth-endpoint: # CloudFoundry authorization endpoint + /oauth/token
    username:       # CloudFoundry username
    password:       # CloudFoundry password
```

```
$ ./gradlew clean bootRun
```

## Performance

With [cf-cli](https://github.com/cloudfoundry/cli):

```
$ time cf services
...
...
real	0m28.805s
...
```

With deck:

```
$ time curl --request GET \
    --url <app-url>/api/v1/spaces/<space-guid>/service-instances \
    --compressed
...
...
real	0m6.257s
...
```

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more info.
