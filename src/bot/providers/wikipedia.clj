(ns bot.providers.wikipedia
  (:require [clojure.tools.cli :as cli]
            [clj-http.client :as http]
            [clojure.data.json :as json])
  (:use [clj-http.util :only (url-encode)]
        [bot.query-provider]))

(def ^:private url-template "https://%s.wikipedia.org/w/api.php?action=query&prop=info|extracts&inprop=url&format=json&exchars=400&explaintext&redirects&titles=%s")
(def ^:private wikipedia-options [["-l" "--language LANG" :default "en"]])

(deftype Wikipedia-provider []
  Query-provider
  (provider-name [this] "Wikipedia")
  (options [this] wikipedia-options)
  (run-query [this query]
    (when (not (clojure.string/blank? query))
      (let [opts (cli/parse-opts (clojure.string/split query #"\s") wikipedia-options)
            url (format url-template (get-in opts [:options :language]) (clojure.string/join " " (:arguments opts)))
            raw-json (:body (http/get url))
            json (json/read-str raw-json)
            first-page (first (vals (get-in json ["query" "pages"])))]
        (when (and (some? first-page) (some? (get first-page "extract")))
          (clojure.string/replace (get first-page "extract") #"\s+" " "))
        ))
    ))
