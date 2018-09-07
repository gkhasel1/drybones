(defproject drybones "0.1.0-SNAPSHOT"
  :description "clojure project skeleton by gk"
  :url "https://www.github.com/gkhasel1/drybones"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/core.async "0.2.391"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.cli "0.3.5"]

                 [cheshire "5.6.3"]
                 [clj-time "0.12.0"]
                 [camel-snake-kebab "0.4.0"]
                 [com.rpl/specter "0.13.1"]
                 [com.taoensso/timbre "4.7.4"]
                 [cprop "0.1.7"]
                 [http-kit "2.1.18"]
                 [mount "0.1.10"]
                 [bidi "2.0.12"
                  :exclusions [ring/ring-core]]
                 [ring "1.6.3"]
                 [ring-middleware-format "0.7.0"]]

  :source-paths ["src/"]

  :target-path "target/%s"

  :profiles {:dev     {:dependencies   [[org.clojure/tools.namespace "0.2.11"]]
                       :source-paths   ["env/dev/src"]
                       :resource-paths ["env/dev/resources"]}
             :uberjar {:aot            :all
                       :resource-paths ["env/prod/resources"]}}

  :repl-options {:init-ns dev}

  :main drybones.main)
