(def ^:private url-template "https://%s.wikipedia.org/w/api.php?action=query&prop=info|extracts&inprop=url&format=json&exchars=400&explaintext&redirects&titles=%s")

(def ^:private wikipedia-options [["-l" "--language" :default "en"]])

(defn wikipedia
  "Query Wikipedia for info"
  {:aliases ["w"] :options wikipedia-options}
  [query message]
  (when (not (clojure.string/blank? query))
    (let [opts (cli/parse-opts (clojure.string/split query #"\s") wikipedia-options)
          url (format url-template (get-in opts [:options :language]) (clojure.string/join " " (:arguments opts)))
          raw-json (:body (http/get url))
          json (json/read-str raw-json)
          first-page (first (vals (get-in json ["query" "pages"])))]
      (clojure.string/replace (get first-page "extract") #"\s+" " ")
      )))
