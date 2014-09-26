(ns bot.commands
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [hickory.select :as s]
            [clojure.data.json :as json]
            [environ.core :as env]
            [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :refer [text xml-> xml1-> attr attr=]]
            [clj-time.local :as local])
  (:use [hickory.core :only (parse as-hickory)]
        [clj-http.util :only (url-encode)]
        [bot.core :only (url-titles)]
        [clj-mmap]
        [iota]))

(defn- flag? [opts flag]
  (and (contains? opts :options) (get-in opts [:options flag])))

(defn- option?
  ([opts option]
   (and (contains? opts :options) (contains? (:options opts) option)))
  ([opts option value]
   (and (contains? opts :options) (= (get-in opts [:options option]) value))))

(load "commands/eval")
(load "commands/help")
(load "commands/weajew")
(load "commands/wikipedia")
(load "commands/translate")
(load "commands/wolfram-alpha")
(load "commands/urban-dictionary")
(load "commands/note")
(load "commands/title")
(load "commands/movie")
(load "commands/quote")
(load "commands/seen")

