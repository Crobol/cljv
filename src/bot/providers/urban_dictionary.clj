(ns bot.providers.urban-dictionary
  (:require [clojure.tools.cli :as cli]
            [clj-http.client :as http]
            [clojure.data.json :as json])
  (:use [clj-http.util :only (url-encode)]
        [bot.query-provider]))

(def ^:private ud-url-template "http://api.urbandictionary.com/v0/define?term=%s")

(deftype Urban-dictionary-provider []
  Query-provider
  (provider-name [this] "Urban-Dictionary")
  (run-query [this query]
             (let [url (format ud-url-template (url-encode query))
                   raw-json (:body (http/get url))
                   json (json/read-str raw-json)
                   definition (get (first (get json "list")) "definition")]
               (when (some? definition)
                 (clojure.string/replace definition #"\s+" " ")))))
