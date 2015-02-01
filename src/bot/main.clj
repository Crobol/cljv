(ns bot.main
  (:use [bot.bot :only (start-bot)]
        [clj-logging-config.log4j])
  (:require [environ.core :as env])
  (:gen-class))

(defn -main
  "Start the bot"
  [& args]
  (System/setProperty "javax.net.ssl.trustStore" (:trust-store env/env))
  (System/setProperty "javax.net.ssl.trustStorePassword" "changeit")
  (set-loggers! ["bot"] {:out (org.apache.log4j.FileAppender.
                               (org.apache.log4j.EnhancedPatternLayout.)
                               "cljv.log")})
  (start-bot (:servers env/env)))
