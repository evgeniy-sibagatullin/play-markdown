Play Markdown
=====================================

[Play-Markdown on Heroku](https://play-markdown.herokuapp.com/)

The Playframework 2 application with REST service that takes markdown-style text as a POST request body, and returns HTML.

## Stack

[Playframework 2.4](https://www.playframework.com/download)
[Silhouette](http://silhouette.mohiva.com/)
[ReactiveMongo](http://reactivemongo.org/)
[Heroku](https://www.heroku.com/)
[MongoLab](https://www.mongolab.com/)

## Heroku deploy

The present db settings prepared for local usage, but you can upgrade it easily to deploy your app to Heroku. Just update the mongodb entry in ../conf/application.conf and provide your MongoLab db definition(no need to update the uri).

## Further updates

If you perform any dependency update then to launch the app you need to compile project **exactly** by the native Playframework activator and execute "activator clean compile" in root project folder from cmd. Usage of the present IDEA or Eclipse built-in compilers **could** lead to unpredictable compile errors.

## Notes

Anyway after the successful activator compilation the IDEA will complain about missing controllers in routes. Such errors will be present in ../conf/routes, ../app/controllers/_, ../app/views/_ and ../app/utils/Filters.scala It is an issue of IDEA plugin, you can launch the app with no worries about it.

## License

The code is licensed under [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).