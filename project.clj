(defproject bot "0.1.0-SNAPSHOT"
  :description "A simple IRC bot for fun and profit!"
  :url "https://github.com/crobol/cljv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.nrepl "0.2.7"]
                 [org.clojure/data.csv "0.1.2"]
                 [irclj "0.5.0-alpha4"]
                 [clj-http "0.9.2"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.0"]
                 [clj-logging-config "1.9.12"]
                 [hickory "0.5.3" :exclusions [orc.clojure/clojure com.cemerick/clojurescript.test org.clojure/clojurescript]]
                 [environ "0.5.0"]
                 [org.clojure/data.zip "0.1.1"]
                 [clj-mmap "1.1.2"]
                 [iota "1.1.2"]
                 [clj-time "0.8.0"]
                 [commons-lang "2.5"]
                 [factual/clj-leveldb "0.1.1"]]
  :main ^:skip-aot bot.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
