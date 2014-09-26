(ns bot.main
  (:use [bot.bot :only (start-bot)])
  (:require [environ.core :as env])
  (:gen-class))

(defn -main
  "Start the bot"
  [& args]
  (System/setProperty "javax.net.ssl.trustStore" (:trust-store env/env))
  (System/setProperty "javax.net.ssl.trustStorePassword" "changeit")
  (start-bot (:servers env/env)))
