FROM clojure:lein-2.8.1

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/
RUN lein deps

COPY . /usr/src/app
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["start"]
